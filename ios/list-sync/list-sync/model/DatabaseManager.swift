//
//  DatabaseManager.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
import CouchbaseLiteSwift
import MultipeerConnectivity

// Switch between these various modes to try various auth types
fileprivate enum ListenerTLSTestMode {
        case TLSDisabled
        case TLSWithAnonymousAuth
        case TLSWithBundledCert // Bring your own cert (self-signed or CA)
       // case TLSWithGeneratedCert // REMOVE FOR GA.
}

// Switch between these various modes to try various auth types
fileprivate enum ListenerCertValidationTestMode {
        case TLSSkipValidation
        case TLSEnableValidation // Used for CA cert validation
        case TLSEnableValidationWithCertPinning // Use for self signed cert
}

class DatabaseManager {
    
        public let kUserDBName:String = "userdb"
          
        public let kDocPrefix = "doc::"
    
        fileprivate let kListenerCommonName = "com.example.list-sync-server"
        fileprivate let kListenerCertLabel = "list-sync-server-cert-label"
        fileprivate let kListenerCertKeyP12File = "listener-cert-pkey"
        fileprivate let kListenerPinnedCertFile = "listener-pinned-cert"
        fileprivate let kListenerCertKeyExportPassword = "couchbase"
        fileprivate(set) var currentUserCredentials:(user:String,password:String)?
        
    
        fileprivate var _userDb:Database?
        fileprivate var _replicatorsToPeers:[PeerHost:Replicator] = [PeerHost:Replicator]()
        fileprivate var _replicatorListenersToPeers:[PeerHost:ListenerToken] = [PeerHost:ListenerToken]()
        fileprivate var _userDbChangeListenerToken:ListenerToken?
        
        fileprivate var _websocketListener:URLEndpointListener?
    
        fileprivate var _applicationSupportDirectory = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).last
       
        fileprivate  let kDBPath:String = "cbl"
    
        fileprivate  var _allowlistedUsers:[[String:String]] = []
     
        // Switch between listener auth modes.
        //tag::ListenerTLSTestMode[]
        fileprivate let listenerTLSSupportMode:ListenerTLSTestMode = .TLSWithBundledCert
        //end::ListenerTLSTestMode[]
    
        // Toggle between enabling or disabling server cert auth modes.
        //tag::ListenerValidationTestMode[]
         fileprivate let listenerCertValidationMode:ListenerCertValidationTestMode = .TLSEnableValidationWithCertPinning
        //end::ListenerValidationTestMode[]

        /// Public Access
    
        static let shared:DatabaseManager = {
            let instance = DatabaseManager()
            instance.initialize()
            return instance
        }()
 
        var serviceAdvertiser:ServiceAdvertiser {
            return ServiceAdvertiser.shared
        }

        func initialize() {
            enableCrazyLevelLogging()
            // ONLY FOR TESTING PURPOSES
            removeIdentityFromKeychainWithLabel(kListenerCertLabel)
            
            // load allow-listed user list (used only on listener side)
            _allowlistedUsers = SampleFileLoaderUtils.shared.loadAllowlistUsersFromFile(name: "userallowlist") ?? []

        }
      

        // user database public API
        var userDB:Database? {
            get {
                 return _userDb
            }
        }
    
        // user database public API
        var websocketListener:URLEndpointListener? {
               get {
                    return _websocketListener
               }
        }
    
        // Don't allow instantiation . Enforce singleton
        private init() {
          
        }

        deinit {
            removeIdentityFromKeychainWithLabel(kListenerCertLabel)
  
        }
}


// MARK: Basic Database Ops
extension DatabaseManager {
    
    // Open or create User specific database.
    func openOrCreateDatabaseForUser(_ user:String, password:String, handler:(_ exists:Bool, _ error:Error?)->Void) throws {
  
        let options = DatabaseConfiguration()
        guard let defaultDBPath = _applicationSupportDirectory else {
            fatalError("Could not open Application Support Directory for app!")
        }
        // Create a folder for the logged in user if one does not exist
        let userFolderUrl = defaultDBPath.appendingPathComponent(user, isDirectory: true)
        let userFolderPath = userFolderUrl.path
        let fileManager = FileManager.default
        if !fileManager.fileExists(atPath: userFolderPath) {
            try? fileManager.createDirectory(atPath: userFolderPath,
                                            withIntermediateDirectories: true,
                                            attributes: nil)
            
        }
        // Set the folder path for the CBLite DB
        options.directory = userFolderPath
        
        currentUserCredentials = (user,password)
        print("Will open/create DB  at path \(userFolderPath)")
        // Create a new DB or get handle to existing DB at specified path
        //tag::OpenOrCreateDatabase[]
        var exists = false
        if Database.exists(withName: kUserDBName, inDirectory: userFolderPath) == true {
              _userDb = try? Database(name: kUserDBName, config: options)
            exists = true
 
        }
        else {
              _userDb = try? Database(name: kUserDBName, config: options)
              
        }
        //end::OpenOrCreateDatabase[]
       // register for DB change notifications
        self.registerForUserDatabaseChanges()

        handler(exists,nil)
        
    }
    
    // close database for user
     func closeDatabaseForCurrentUser() {
      
        print(#function)
        // Get handle to DB  specified path
        
        if let userDb = _userDb {
                         
            // Remove listeners for database
            deregisterForUserDatabaseChanges()
            
            // Remove listeners for replicators
            deregisterEventsForAllReplicators()
            
            // Starting 2.8, all replicators must be auto closed
            // all listeners and live queries must be removed
            // Web socket listener must be stopped. It can take upto 5 seconds
            // So run close asynchronously
            
            let group = DispatchGroup()
            group.enter()

            DispatchQueue.init(label: "com.example.list-sync", qos: .userInitiated, attributes: .concurrent, autoreleaseFrequency: .inherit, target: nil) .async
            {
                defer {
                     group.leave()
                    self._userDb = nil
                }
                do {
                    try userDb.close()
                }
                catch {
                    print("Error while attempting to close database \(error)")
                }
                
            }
            // wait
            let result = group.wait(timeout: DispatchTime.now()+8)
            
            if result == .timedOut {
                print("Failed to close database and replicators up succesfully!")
            }
            else {
                for val in _replicatorsToPeers.values {
                    print ("Replicator Status is \(val.status)")
                }
                print("Succesfully closed database, and associated listeners/replicators as appropriate")
            }
            _replicatorsToPeers.removeAll()
            
        }

    }
   
}

// MARK :  Database Events
extension DatabaseManager {
    fileprivate func registerForUserDatabaseChanges() {
        // Add database change listener
        _userDbChangeListenerToken = _userDb?.addChangeListener({ [weak self](change) in
            guard let `self` = self else {
                return
            }
            for docId in change.documentIDs   {
                let doc = self._userDb?.document(withID: docId)
                if doc == nil {
                    print("Document was deleted")
                }
                else {
                    print("Document was added/updated")
                }
                
            }
        })
    }
    
    fileprivate func deregisterForUserDatabaseChanges() {
        // Add database change listener
        print(#function)
        guard let db = _userDb else {
            return
        }
        if let userDbChangeListenerToken = self._userDbChangeListenerToken {
            _userDb?.removeChangeListener(withToken: userDbChangeListenerToken)
                 
        }
        
    }
}


// MARK: Peer-to-peer Passive Listener for user db
extension DatabaseManager {
 
    func initWebsocketsListenerForUserDb()throws {
        guard let db = _userDb else {
            throw ListDocError.DatabaseNotInitialized
        }
    
        if _websocketListener != nil  {
            print("Listener already initialized")
            return
        }
    
        //tag::InitListener[]
        // Include websockets listener initializer code
        let listenerConfig = URLEndpointListenerConfiguration(database: db) // <1>
        
        // Configure the appropriate auth test mode
        switch listenerTLSSupportMode { //<2>
            //tag::TLSDisabled[]
            case .TLSDisabled:
                listenerConfig.disableTLS  = true
                listenerConfig.tlsIdentity = nil
            //end::TLSDisabled[]
            //tag::TLSWithAnonymousAuth[]
            case .TLSWithAnonymousAuth:
                listenerConfig.disableTLS  = false // Use with anonymous self signed cert
                listenerConfig.tlsIdentity = nil
            //end::TLSWithAnonymousAuth[]
            //tag::TLSWithBundledCert[]
             case .TLSWithBundledCert:
                
                if let tlsIdentity = self.importTLSIdentityFromPKCS12DataWithCertLabel(kListenerCertLabel) {
                    listenerConfig.disableTLS  = false
                    listenerConfig.tlsIdentity = tlsIdentity
                }
                else {
                    print("Could not create identity from provided cert")
                    throw ListDocError.WebsocketsListenerNotInitialized
                }
            //end::TLSWithBundledCert[]
                            
            // UNAVAILABLE FOR BETA
            //            case .TLSWithGeneratedCert:
            //                if let tlsIdentity = self.createIdentityWithCertLabel(kListenerCertLabel) {
            //                    listenerConfig.disableTLS  = false
            //                    listenerConfig.tlsIdentity = tlsIdentity
            //                }
            //                else {
            //                    print("Could not create identity from generated self signed cert")
            //                    throw ListDocError.WebsocketsListenerNotInitialized
            //                }
                        

        
        }
        
        listenerConfig.enableDeltaSync = true // <3>
        
        listenerConfig.authenticator = ListenerPasswordAuthenticator.init { // <4>
                   (username, password) -> Bool in
            if (self._allowlistedUsers.contains(["password" : password, "name":username])) {
                return true
            }
            return false
               }
        
        _websocketListener = URLEndpointListener(config: listenerConfig)
        //end::InitListener[]

    }
    
     func startWebsocketsListenerForUserDb(handler:@escaping(_ urls:[URL]?, _ error:Error?)->Void) throws{
        print(#function)
        guard let websocketListener = _websocketListener else {
            throw ListDocError.WebsocketsListenerNotInitialized
            }
        //tag::StartListener[]
        DispatchQueue.global().sync {
            do {
                try websocketListener.start()
                handler(websocketListener.urls,nil)
            }
            catch {
                handler(nil,error)
            }
    
        }
        //end::StartListener[]
        
    }
    //tag::StopListener[]
    func stopWebsocketsListenerForUserDb() throws{
        print(#function)
        guard let websocketListener = _websocketListener else {
            throw ListDocError.WebsocketsListenerNotInitialized
        }
        websocketListener.stop()
        _websocketListener = nil
    }
    //end::StopListener[]
}

// MARK: Peer-to-peer Active Replicator
extension DatabaseManager {
       
    func startP2PReplicationWithUserDatabaseToRemotePeer(_ peer:PeerHost, handler:@escaping(_ status:PeerConnectionStatus)->Void) throws{
        print("\(#function) with wss://\(peer)/\(kUserDBName)")
        guard let userDb = _userDb else {
             throw ListDocError.DatabaseNotInitialized
            
        }
        guard let user = self.currentUserCredentials?.user, let password = self.currentUserCredentials?.password else {
                    throw ListDocError.UserCredentialsNotProvided
                   
        }
        
      
        var replicatorForUserDb = _replicatorsToPeers[peer]
        //tag::StartReplication[]
        if replicatorForUserDb == nil {
            // Start replicator to connect to the URLListenerEndpoint
            guard let targetUrl = URL(string: "wss://\(peer)/\(kUserDBName)") else {
                throw ListDocError.URLInvalid
            }

            
            let config = ReplicatorConfiguration.init(database: userDb, target: URLEndpoint.init(url:targetUrl)) //<1>

            config.replicatorType = .pushAndPull
            config.continuous =  true
            
            // Explicitly allows self signed certificates. By default, only
            // CA signed cert is allowed
            switch listenerCertValidationMode { //<2>
                  
                case .TLSSkipValidation :
                    // Use serverCertificateVerificationMode set to .selfSignedCert to disable cert validation
                    config.serverCertificateVerificationMode = .selfSignedCert
                                
                
                case .TLSEnableValidationWithCertPinning:
                    // Use serverCertificateVerificationMode set to .caCert to enable cert validation
                   
                    config.serverCertificateVerificationMode = .caCert // will likely change post beta
                    if let pinnedCert = self.loadSelfSignedCertForListenerFromBundle() {
                        config.pinnedServerCertificate = pinnedCert
                    }
                    else {
                        print("Failed to load server cert to pin. Will proceed without pinning")
                    }
                
                case .TLSEnableValidation:
                    config.serverCertificateVerificationMode = .caCert
                
                            
            }
             
            
            let authenticator = BasicAuthenticator(username: user, password: password)//<3>
            config.authenticator = authenticator

            replicatorForUserDb = Replicator.init(config: config) //<4>
            _replicatorsToPeers[peer] = replicatorForUserDb

          }
        if let pushPullReplListenerForUserDb = registerForEventsForReplicator(replicatorForUserDb,handler:handler) {
            _replicatorListenersToPeers[peer] = pushPullReplListenerForUserDb
        
        }

            
        replicatorForUserDb?.start() //<5>
        handler(PeerConnectionStatus.Connecting)
         //end::StartReplication[]
      }
    
    func stopP2PReplicationWithUserDatabaseToRemotePeer(_ peer:PeerHost, shouldRemove:Bool, handler:@escaping(_ status:PeerConnectionStatus)->Void) throws{
        guard let replicator = _replicatorsToPeers[peer] else {
            print("Replicator does not exist!! ")
            handler(.Error)
            return
        }
        //tag::StopReplication[]
        if let listener = _replicatorListenersToPeers[peer] {
            replicator.removeChangeListener(withToken: listener)
            _replicatorListenersToPeers.removeValue(forKey: peer)
        }
        
        replicator.stop()
        //end::StopReplication[]
        if shouldRemove {
            _replicatorsToPeers.removeValue(forKey: peer)
            handler(.Disconnected)
        }
        else {
            handler(.Disconnected)
        }
    }
   
}

// MARK :  Replicator Status Events
extension DatabaseManager {
    fileprivate func registerForEventsForReplicator(_ replicator:Replicator?, handler:@escaping(_ status:PeerConnectionStatus)->Void )->ListenerToken? {
        let pushPullReplListenerForUserDb = replicator?.addChangeListener({ (change) in
                  
                   let s = change.status
                   if s.error != nil {
                       handler(PeerConnectionStatus.Error)
                       return
                   }
                   
                   switch s.activity {
                   case .connecting:
                       print("Replicator Connecting to Peer")
                        handler(PeerConnectionStatus.Connecting)
                   case .idle:
                        print("Replicator in Idle state")
                        handler(PeerConnectionStatus.Connected)
                   case .busy:
                       print("Replicator in busy state")
                       handler(PeerConnectionStatus.Busy)
                   case .offline:
                       print("Replicator in offline state")
                   case .stopped:
                       print("Completed syncing documents")
                        handler(PeerConnectionStatus.Error)
            
                   }
                   
                   if s.progress.completed == s.progress.total {
                       print("All documents synced")
                   }
                   else {
                       print("Documents \(s.progress.total - s.progress.completed) still pending sync")
                   }
               })
        return pushPullReplListenerForUserDb
               
    }
    
    fileprivate func deregisterEventsForAllReplicators() {
        print(#function)
        for (peer,replicator) in _replicatorsToPeers {
            if let listener = _replicatorListenersToPeers[peer] {
                replicator.removeChangeListener(withToken: listener)
                _replicatorListenersToPeers.removeValue(forKey: peer)
            }
            
        }
    }
}

// Certs
extension DatabaseManager {
    func createIdentityWithCertLabel(_ label:String)->TLSIdentity? {
        do {
            var identity = try TLSIdentity.identity(withLabel: label)
            if identity != nil {
                return identity
            }
            let attrs = [certAttrCommonName: kListenerCommonName]
            identity = try TLSIdentity.createIdentity(forServer: true,
                                                        attributes: attrs,
                                                        expiration: nil, // Use default expiration of 365 days
                                                        label: label)
            return identity
        } catch {
            print("Error while creating self signed cert : \(error)")
            return nil
        }
    }
    
    func loadSelfSignedCertForListenerFromBundle()->SecCertificate? {
        do {
            if let pathToCert = Bundle.main.path(forResource: kListenerPinnedCertFile, ofType: "cer") {
 
                if let localCertificate:NSData = NSData(contentsOfFile: pathToCert) {

                    let certificate = SecCertificateCreateWithData(nil, localCertificate)
                    return certificate
            
                }
            }
        } catch {
            print("Error while loading self signed cert : \(error)")
            return nil
        }
        return nil
    }
    
    
    // This API extracts cert and pkey from the bundled .p12 file and stores it in keychain.
    // It then creates TLSIdentity from the cert/pkey stored in keychain
    // This is useful if you want to use predefined credentials stored in keychain
    func getTLSIdentityFromPKCS12DataLoadedIntoKeychainWithCertLabel(_ label:String)->TLSIdentity? {
           do {
            
                // Check if identity exists in keychain. If so use that
                if let identity = try TLSIdentity.identity(withLabel: label) {
                    print("An identity with label : \(label) already exists in keychain")
                    return identity
                }
                
                var result : CFArray?
                var kcStatus = errSecSuccess
                guard let pathToCert = Bundle.main.path(forResource: kListenerCertKeyP12File, ofType: "p12") else {
                    return nil
                }
            
      
                guard let data = NSData(contentsOfFile: pathToCert) else {
                    return nil
                }
            
            
                let options = [String(kSecImportExportPassphrase): kListenerCertKeyExportPassword] // This passphrase should correspond to what was specified when .p12 file was created
                kcStatus = SecPKCS12Import(data as CFData, options as CFDictionary, &result)
                if kcStatus != errSecSuccess {
                    switch kcStatus {
                        case errSecDecode :
                            print("Failed to decode. Blob can't be read or malformed :\(kcStatus)")
                             return nil
                        case errSecAuthFailed :
                            print("Password was incorrect :\(kcStatus)")
                             return nil
                        default:
                        print("failed to import data from provided with error :\(kcStatus) ")
                        return nil
                    }
                }
              
                      
                let importedItems = result! as NSArray

                let item = importedItems[0] as! [String: Any]
                let secIdentity = item[String(kSecImportItemIdentity)] as! SecIdentity
                  
                // Extract Private Key:
                var privateKey : SecKey?
                kcStatus = SecIdentityCopyPrivateKey(secIdentity, &privateKey)
                if kcStatus != errSecSuccess {
                    print("failed to import private key from provided with error :\(kcStatus) ")
                    return nil
                }
                // Extract Certs
                let certs = item[String(kSecImportItemCertChain)] as? [SecCertificate]
            
                guard let pKey = privateKey, let pubCerts = certs else {
                    return nil
                }
            
                // Save key and private key in keychain
                kcStatus = storeInKeyChain(privateKey: pKey, certs: pubCerts, label: label)
            
                print ("Key chain storage status : \(kcStatus)")
                if kcStatus == errSecSuccess {
                    // Now create the TLSIdentity with the provided sec identity (that is stored in keychain)
                    return try TLSIdentity.identity(withIdentity: secIdentity, certs: [pubCerts[0]])
                }
                else{
                    return nil
                }
                
                       
           } catch {
               print("Error while loading self signed cert : \(error)")
               return nil
           }

    }
    
    // This API imports the TLSIdentity from bundled  .p12 file
    func importTLSIdentityFromPKCS12DataWithCertLabel(_ label:String)->TLSIdentity? {
         do {

                // Check if identity exists in keychain. If so use that
                if let identity = try TLSIdentity.identity(withLabel: label) {
                  print("An identity with label : \(label) already exists in keychain")
                  return identity
                }
                guard let pathToCert = Bundle.main.path(forResource: kListenerCertKeyP12File, ofType: "p12") else {
                  return nil
                }

    
                let data = try NSData(contentsOfFile: pathToCert) as Data
          
                // Now import the TLSIdentity with the provided cert identity
                return try TLSIdentity.importIdentity(withData: data, password: String(kListenerCertKeyExportPassword), label: label)
         } catch {
             print("Error while loading self signed cert : \(error)")
             return nil
         }

     }
}

// Keychain
extension DatabaseManager {
    
    func storeInKeyChain(privateKey: SecKey, certs: [SecCertificate], label: String)->OSStatus {
         // Private Key:
        let status = storeInKeyChain(privateKey: privateKey)
        if status != errSecSuccess {
            return status
        }
        
        // Certs:
        var i = 0;
        for cert in certs {
            let status = storeInKeyChain(cert: cert, label: (i == 0 ? label : nil))
             if status != errSecSuccess {
                return status
            }
            i = i + 1
        }
        return errSecSuccess
    }
    
    func storeInKeyChain(privateKey: SecKey)->OSStatus {
        let params: [String : Any] = [
            String(kSecClass):          kSecClassKey,
            String(kSecAttrKeyType):    kSecAttrKeyTypeRSA,
            String(kSecAttrKeyClass):   kSecAttrKeyClassPrivate,
            String(kSecValueRef):       privateKey
        ]
        return SecItemAdd(params as CFDictionary, nil)
    }
    
    func storeInKeyChain(cert: SecCertificate, label: String?) -> OSStatus{
        var params: [String : Any] = [
            String(kSecClass):          kSecClassCertificate,
            String(kSecValueRef):       cert
        ]
        if let l = label {
            params[String(kSecAttrLabel)] = l
        }
        return SecItemAdd(params as CFDictionary, nil)
    }
    
    
    func removeIdentityFromKeychainWithLabel(_ label:String) {
        do {
        try TLSIdentity.deleteIdentity(withLabel: label)
        }
        catch {
            print("Error in trying to remove identity with label \(label) :\(error)")
        }
    }
}

// MARK: Utils
extension DatabaseManager {
    
    fileprivate func enableCrazyLevelLogging() {
        Database.log.console.level = .debug
        Database.log.console.domains = .all
    }
    
    public func createUserDocumentWithData(_ data:[String:Any])throws {

        guard let db = userDB else {
            throw ListDocError.DatabaseNotInitialized
        }
        //tag::LoadData[]
        for (key,value) in data {
            let docId = "\(kDocPrefix)\(key)"
            print("DocId is \(docId)")
            let doc = MutableDocument(id:docId, data:value as! Dictionary<String, Any>)
            try db.saveDocument(doc)
        }
        //end::LoadData[]
    }
    
   
}
