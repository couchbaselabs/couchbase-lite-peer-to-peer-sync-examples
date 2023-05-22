//
//  ServiceBrowser.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/3/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation

typealias PeerHost = String
protocol ServiceBrowserDelegate : AnyObject {
    func onPeerDiscovered(_ peer:PeerHost?,  error:Any?)
    func onPeerRemoved(_ peer:PeerHost)
 }

// MARK : Browsing of peer supporting service type of _cbmobilesync._tcp
class ServiceBrowser: NSObject{
    weak var peerBrowserDelegate:ServiceBrowserDelegate?
    
    var browser: NetServiceBrowser?
    let domain = ""
    /// The Bonjour service type.
    let serviceType = "_cblistservicesync._tcp"
    
    var peerHosts:[NetService: PeerHost] = [NetService:PeerHost]()
    var services:[NetService] = [NetService]()
    static let shared:ServiceBrowser = {
        let instance = ServiceBrowser()
        return instance
    }()
    
    //tag::StartBrowsing[]
    public func startSearch(withDelegate delegate:ServiceBrowserDelegate? ){
         peerBrowserDelegate = delegate
        self.browser = NetServiceBrowser()
        self.browser?.delegate = self
        self.browser?.searchForServices(ofType: serviceType, inDomain: domain)
    }
    //end::StartBrowsing[]
    public func stopSearch() {
        self.reset()
        self.browser?.stop()
    }

    private func reset() {
        self.peerHosts.removeAll()
        self.services.removeAll()
    }
    
}

extension ServiceBrowser: NetServiceBrowserDelegate {
    func netServiceBrowserWillSearch(_ browser: NetServiceBrowser) {
        print("starting search..")
    }
    
    func netServiceBrowserDidStopSearch(_ browser: NetServiceBrowser) {
        print("Stopped search")
    }
    
    func netServiceBrowser(_ browser: NetServiceBrowser, didNotSearch errorDict: [String : NSNumber]) {
        print("error in search")
        debugPrint(errorDict)
    }
    
    func netServiceBrowser(_ browser: NetServiceBrowser, didFind service: NetService, moreComing: Bool) {
        print("found service at \(service) with \(moreComing)")
        if let index = services.firstIndex(of: service) {
            services.remove(at: index)
        }
        services.append(service)
        service.delegate = self
        service.resolve(withTimeout: 30)
 
    }
    
    func netServiceBrowser(_ browser: NetServiceBrowser, didRemove service: NetService, moreComing: Bool) {
        print(#function)
        
         if let hostName = self.peerHosts[service] {
            self.peerHosts.removeValue(forKey: service)
            peerBrowserDelegate?.onPeerRemoved(hostName)
        
        }
       
     }
}

extension ServiceBrowser : NetServiceDelegate{
    func netService(_ sender: NetService, didNotPublish errorDict: [String : NSNumber]) {
        debugPrint(errorDict)
    }
    
    func netService(_ sender: NetService, didNotResolve errorDict: [String : NSNumber]) {
        print(#function)
        peerBrowserDelegate?.onPeerDiscovered(nil,error:errorDict)
    }
    func netServiceDidResolveAddress(_ sender: NetService) {
        print("did resolve address \(sender)")
        guard let hostname = self.hostnameForService(sender) else {
            print("Failed to get hostname for service \(sender)")
            return
        }
        print("hostname for service \(sender) is \(hostname)")
        peerHosts[sender] = hostname
        peerBrowserDelegate?.onPeerDiscovered(hostname,error:nil)
    }
    
}

// MARK: Utils
extension ServiceBrowser {
    func hostnameForService(_ service:NetService) -> PeerHost? {
        // stackoverview solution
        var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
        guard let data = service.addresses?.first else { return nil}
        data.withUnsafeBytes { ptr in
        guard let sockaddr_ptr = ptr.baseAddress?.assumingMemoryBound(to: sockaddr.self) else {
          // handle error
          return
        }
            let sockaddr = sockaddr_ptr.pointee
        guard getnameinfo(sockaddr_ptr, socklen_t(sockaddr.sa_len), &hostname, socklen_t(hostname.count), nil, 0, NI_NUMERICHOST) == 0 else {
                return
            }
        }
        let port = service.port
        let hostName = "\(String(cString:hostname)):\(port)"
        return hostName

    }
}
