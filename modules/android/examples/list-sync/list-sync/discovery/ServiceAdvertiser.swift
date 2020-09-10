//
//  ServiceAdvertiser.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/2/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
import CouchbaseLiteSwift

// Advertises bonjour service
private enum NSServiceState {
    case stopped
    case starting
    case ready
    case stopping
}


// A separate service is published for each websockets listener. 
// A websocket listener is associated with a single database
public final class ServiceAdvertiser: NSObject {
    
  //tag::ServiceDefinition[]
  /// The Bonjour service name. Setting it to an empty String will be
  /// mapped to the device's name.
  public var serviceName: String = ""
  
  /// The Bonjour service type.
  public var serviceType = "_cblistservicesync._tcp"
  
 /// The Bonjour domain type.
 public var serviceDomain = ""
 //end::ServiceDefinition[]
    
  /// An error if occurred.
  public fileprivate(set) var error: Error?
  
  private var services:[String:NetService] = [:]
  private var serviceState:[String:NSServiceState] = [:]
       
    static let shared:ServiceAdvertiser = {
        let instance = ServiceAdvertiser()
        return instance
    }()

    func closeAllServices() {
        for (_,service) in services {
            service.stop()
        }
    }
   
   // Don't allow instantiation . Enforce singleton
    private override init() {
       
   }
   
    
    @available(iOS 10.0, *)
    /// Starts the advertiser.
    public func startAdvertisingListenerServiceForDatabase(_ database:String, atPort listenerPort:UInt16) {
        if serviceState[database] == .starting || serviceState[database] == .ready {
            return
        }
      
        serviceState[database] = .starting
        self.doStart(database: database,listenerPort)
            
    }
    
    /// Stops the advertiser.
    public func stopListenerServiceForDatabase(_ database:String) {
        if serviceState[database] == nil {
            return
        }
        
        if serviceState[database]  != .stopped && serviceState[database]  != .stopping {
            serviceState[database]  = .stopping
            doStop(database: database)

        }
    }
    
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
    
    @objc fileprivate func doStop(database:String) {
        guard let service = services[database] else {
            serviceState[database]  = .stopped
            services.removeValue(forKey: database)
            return
        }
        
        service.stop()
        services.removeValue(forKey: database)
        
        serviceState[database]  = .stopped
    }
    
    /// The URL at which the listener can be reached from another device.
    /// This URL will only work for _local_ clients, i.e. over the same WiFi LAN
    /// or over Bluetooth.
    public var hostName: String {
      var baseHostName = [CChar](repeating: 0, count: Int(NI_MAXHOST))
      gethostname(&baseHostName, Int(NI_MAXHOST))
      var hostname = String(cString: baseHostName)
      #if targetEnvironment(simulator)
      if !hostname.hasSuffix(".\(serviceDomain)") {
          hostname.append(".\(serviceDomain)")
      }
      #endif
        
      return hostname

    }
    
}
extension ServiceAdvertiser: NetServiceDelegate {
    public func netServiceDidPublish(_ sender: NetService) {
       if let databaseForService = services.key(forValue: sender) {
            serviceState[databaseForService] = .ready
        }
     }
    
    public func netService(_ sender: NetService, didNotPublish errorDict: [String : NSNumber]) {
        print (#function)
        if let code = errorDict[NetService.errorCode]?.intValue {
            self.error = NSError.init(domain: "NetService", code: code, userInfo: nil)
        }
        if let databaseForService = services.key(forValue: sender) {
             doStop(database: databaseForService)
        }
    }
    
    public func netService(_ sender: NetService, didAcceptConnectionWith inputStream: InputStream, outputStream: OutputStream) {
        print (#function)
    }
    
    public func netServiceWillResolve(_: NetService) {
        print (#function)
    }
    
    public func netServiceDidStop(_: NetService) {
        print (#function)
    }
}
