Complete Swift code samples from which these are extracted can be found in the /ios directory at the top-level of this repo.

//
// Tags from list-sync/view/PeerTableViewCell.swift
//

//
// Tags from list-sync/view/CustomListTableViewCell.swift
//

//
// Tags from list-sync/presenter/ListPresenter.swift
//
            //tag::livequerybuilder[]
            guard let db = dbMgr.userDB else {
                fatalError("db is not initialized at this point!")
            }
            
            listQuery = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(db))
                .where(Meta.id.equalTo(Expression.string(docId))) // <1>

                // V1.0. There should be only one document
                
        listQueryToken = listQuery?.addChangeListener { [weak self] (change) in
            guard let `self` = self else {return}
            switch change.error {
            case nil:
                var listRecord = ListRecord.init(items: [])
                
                for (_, row) in (change.results?.enumerated())! {
                    // There should be only one user profile document for a user
                    print(row.toDictionary())
                    if let listVal = row.dictionary(forKey: self.dbMgr.kUserDBName) {
                        if let listItems = listVal.array(forKey: ListDocumentKeys.items.rawValue)?.toArray() as? [[String:Any]]{
                            
                            for item in listItems{
                                let key =  item[ListItemDocumentKeys.key.rawValue] as? String
                                let value =  item[ListItemDocumentKeys.value.rawValue]
                                let image = item[ListItemDocumentKeys.image.rawValue] as? Blob
                                
                                listRecord.items.append((image: image?.content, key: key, value: value))
                                
                                
                            }
                        }
                    }
                }
                //end::livequerybuilder[]

//
// Tags from list-sync/presenter/PresenterProtocol.swift
//

//
// Tags from list-sync/discovery/ServiceBrowser.swift
//
    //tag::StartBrowsing[]
    public func startSearch(withDelegate delegate:ServiceBrowserDelegate? ){
         peerBrowserDelegate = delegate
        self.browser = NetServiceBrowser()
        self.browser?.delegate = self
        self.browser?.searchForServices(ofType: serviceType, inDomain: domain)
    }
    //end::StartBrowsing[]

//
// Tags from list-sync/discovery/ServiceAdvertiser.swift
//
  //tag::ServiceDefinition[]
  /// The Bonjour service name. Setting it to an empty String will be
  /// mapped to the device's name.
  public var serviceName: String = ""
  
  /// The Bonjour service type.
  public var serviceType = "_cblistservicesync._tcp"
  
 /// The Bonjour domain type.
 public var serviceDomain = ""
 //end::ServiceDefinition[]
    //tag::StartAdvertiser[]
    private func doStart(database:String, _ port:UInt16) {
        let service = NetService(domain: serviceDomain, type: serviceType,
                                 name: serviceName, port:Int32(port))

        service.delegate = self
        service.includesPeerToPeer = true
        service.publish()
        services[database] = service

    }
    //end::StartAdvertiser[]

//
// Tags from list-sync/model/DatabaseManager.swift
//
        //tag::ListenerTLSTestMode[]
        fileprivate let listenerTLSSupportMode:ListenerTLSTestMode = .TLSWithBundledCert
        //end::ListenerTLSTestMode[]
        //tag::ListenerValidationTestMode[]
         fileprivate let listenerCertValidationMode:ListenerCertValidationTestMode = .TLSEnableValidationWithCertPinning
        //end::ListenerValidationTestMode[]
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
            //tag::TLSWithGeneratedSelfSignedCert[]
            case .TLSWithGeneratedSelfSignedCert:
                if let tlsIdentity = self.createIdentityWithCertLabel(kListenerCertLabel) {
                    listenerConfig.disableTLS  = false
                    listenerConfig.tlsIdentity = tlsIdentity
                }
                else {
                    print("Could not create identity from generated self signed cert")
                    throw ListDocError.WebsocketsListenerNotInitialized
                }
              //end::TLSWithGeneratedSelfSignedCert[]

        
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
            //tag::TLSWithGeneratedSelfSignedCert[]
            case .TLSWithGeneratedSelfSignedCert:
                if let tlsIdentity = self.createIdentityWithCertLabel(kListenerCertLabel) {
                    listenerConfig.disableTLS  = false
                    listenerConfig.tlsIdentity = tlsIdentity
                }
                else {
                    print("Could not create identity from generated self signed cert")
                    throw ListDocError.WebsocketsListenerNotInitialized
                }
              //end::TLSWithGeneratedSelfSignedCert[]
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
                    // Use acceptOnlySelfSignedServerCertificate set to true to only accept self signed certs.
                    // There is no cert validation
                    config.acceptOnlySelfSignedServerCertificate = true
                                
                
                case .TLSEnableValidationWithCertPinning:
                    // Use acceptOnlySelfSignedServerCertificate set to false to only accept CA signed certs
                    // Self signed certs will fail validation
                   
                    config.acceptOnlySelfSignedServerCertificate = false
                    
                    // Enable cert pinning to only allow certs that match pinned cert
                    
                    if let pinnedCert = self.loadSelfSignedCertForListenerFromBundle() {
                        config.pinnedServerCertificate = pinnedCert
                    }
                    else {
                        print("Failed to load server cert to pin. Will proceed without pinning")
                    }
                
                case .TLSEnableValidation:
                     // Use acceptOnlySelfSignedServerCertificate set to false to only accept CA signed certs
                     // Self signed certs will fail validation. There is no cert pinning
                    config.acceptOnlySelfSignedServerCertificate = false
                
                            
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
        //tag::StopReplication[]
        if let listener = _replicatorListenersToPeers[peer] {
            replicator.removeChangeListener(withToken: listener)
            _replicatorListenersToPeers.removeValue(forKey: peer)
        }
        
        replicator.stop()
        //end::StopReplication[]
        //tag::LoadData[]
        for (key,value) in data {
            let docId = "\(kDocPrefix)\(key)"
            print("DocId is \(docId)")
            let doc = MutableDocument(id:docId, data:value as! Dictionary<String, Any>)
            try db.saveDocument(doc)
        }
        //end::LoadData[]

//
// Tags from list-sync/model/ListRecord.swift
//
//tag::ListRecord[]
let kListRecordDocumentType = "list"
typealias ListRecords = [ListRecord]
typealias ListItem = (image:Data?,key:String?,value:Any?)
struct ListRecord : CustomStringConvertible{
    let type = kListRecordDocumentType
    var items:[ListItem]
    
    var description: String {
        guard  items.count > 0 else  {
            return "No items in list"
        }
        for item in items {
            return "key = \(String(describing: item.key)), value = \(String(describing: item.value))"
        }
        return "No items in list"
    }
}
//end::ListRecord[]

//
// Tags from list-sync/AppDelegate.swift
//

//
// Tags from list-sync/utilities/DictionaryExtensions.swift
//

//
// Tags from list-sync/utilities/SpinnerViewController.swift
//

//
// Tags from list-sync/utilities/UIViewControllerExtensions.swift
//

//
// Tags from list-sync/utilities/CustomErrors.swift
//

//
// Tags from list-sync/utilities/SampleFileLoaderUtils.swift
//

//
// Tags from list-sync/utilities/Notifications.swift
//

//
// Tags from list-sync/view controllers/ActiveViewController.swift
//

//
// Tags from list-sync/view controllers/PassiveViewController.swift
//

//
// Tags from list-sync/view controllers/ListViewController.swift
//

//
// Tags from list-sync/view controllers/LoginViewController.swift
//
