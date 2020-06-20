//
//  PassiveViewController.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//



import UIKit


class PassiveViewController: UIViewController, PresentingViewProtocol {

    @IBOutlet weak var startlistenerButton: UIButton!
    @IBOutlet weak var listenerStatus: UILabel!
    @IBOutlet weak var connectionsStatusLabel: UILabel!
    @IBOutlet weak var stopListenerButton: UIButton!
    
    let cbMgr = DatabaseManager.shared
    let serviceAdvertiser = ServiceAdvertiser.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
     }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(true)
        setupNavigationBar(title: "Listener")

    }
    
    deinit {
        print("Passive deinit")
        self.stopWebsocketListener()
        serviceAdvertiser.stopListenerServiceForDatabase(cbMgr.kUserDBName)
    }
    
    func stopWebsocketListener() {
        print(#function)
        do {
            try self.cbMgr.stopWebsocketsListenerForUserDb()
        }
        catch {
            print ("Failed to stop websockers listener with error \(error)")
        }
    }
    
    @IBAction func startListener(_ sender: UIButton) {
        print(#function)
        listenerStatus.isHidden = false
        do {
            
            defer {
                startlistenerButton.isEnabled = false
                stopListenerButton.isEnabled = true
            }
            
            // Initialize the websockets listener
            try cbMgr.initWebsocketsListenerForUserDb()

            // Start the websockets listener
            try cbMgr.startWebsocketsListenerForUserDb(handler: {[weak self](urls, error) in
                
                switch error {
                case nil:
                    
                    // get port and dbname from listener
                    
                    guard let urls = urls, urls.capacity != 0 else {
                        self?.listenerStatus.text = NSLocalizedString("Failed to start websockets listener ", comment: "")
                        return
                    }
                      
                    let url = urls[0]
                    let hostName = url.host
                    let portVal:UInt16 = UInt16(url.port!)
                    
                    var urlc = URLComponents.init()
                    urlc.scheme = self?.cbMgr.websocketListener?.config.disableTLS == true ? "ws":"wss"
                    urlc.host = hostName
                    urlc.port = Int(portVal)
                    urlc.path = url.path
                                    
                    
                    // Advertise the service  over Bonjour
                    self?.serviceAdvertiser.startAdvertisingListenerServiceForDatabase((self?.cbMgr.kUserDBName)!,atPort:portVal)
                       
                     // print("***** LISTENER URL IS****** \(urls)")
            
                              
                    print("Started service for database on \(urlc)")
                       
                   guard let updatedUrl = urlc.url else {
                    self?.listenerStatus.text = NSLocalizedString("Failed to start websockets listener ", comment: "")
                       return
                   }
                    self?.listenerStatus.text = NSLocalizedString("Starting websockets listener", comment: "")
                   switch updatedUrl {
                       
                       case nil:
                        self?.listenerStatus.text = NSLocalizedString("Failed to start websockets listener ", comment: "")
                       default:
                        self?.listenerStatus.text = NSLocalizedString("Listening on \(updatedUrl)", comment: "")
                   }
                default:
                    self?.listenerStatus.text = NSLocalizedString("Failed to start websockets listener ", comment: "")
                }
                  
            })
                       
          
         }
        catch {
            print("Exception when starting listener \(error)")
        }
        
    }
    @IBAction func stopListener(_ sender: UIButton) {
        do{
            defer {
                startlistenerButton.isEnabled = true
                stopListenerButton.isEnabled = false
                listenerStatus.text = NSLocalizedString("", comment: "")
            }
        print(#function)
        // Get active and passive
        let numConnections = cbMgr.websocketListener?.status.connectionCount ?? 0
        if numConnections > UInt64(0) {
            // Check if connections are 0 else warn user
            
            let alert = UIAlertController(title: nil, message: NSLocalizedString("There are active connections. Do you wish to stop the listener?", comment: ""), preferredStyle: .alert)
            let cancel = UIAlertAction(title: "Cancel", style: .cancel) { (action:UIAlertAction) in
                print("Listener cancelled")
            }
            let proceed = UIAlertAction(title: "Yes", style: .default) { [weak self](action:UIAlertAction) in
                // Stop websockets listener
                self?.stopWebsocketListener()
                // stop advertising service
                if let dbName = self?.cbMgr.kUserDBName {
                    self?.serviceAdvertiser.stopListenerServiceForDatabase(dbName)
                }
                
            }
            alert.addAction(proceed)
            alert.addAction(cancel)
        
            self.present(alert, animated: true, completion: nil)
        }
        else {
            // stop listener
            stopWebsocketListener()
            // stop advertising service
            serviceAdvertiser.stopListenerServiceForDatabase(cbMgr.kUserDBName)
          }
    
        }
    }
}

//MARK : Navigation Bar Setup
extension PassiveViewController {
    override func setupNavigationBar(title: String) {
     
        super.setupNavigationBar(title: title)

      //show right button
        let rightButton = UIBarButtonItem (barButtonSystemItem: .action, target: self, action: #selector(onConnectionStatusTapped(_:)))
                  
     self.tabBarController?.navigationItem.rightBarButtonItem = rightButton
        
   }
}

// MARK: IBActions
extension PassiveViewController {
    @IBAction func onConnectionStatusTapped(_ sender: UIBarButtonItem) {
        let totalConnections = self.cbMgr.websocketListener?.status.connectionCount ?? 0
        let activeConnections = self.cbMgr.websocketListener?.status.activeConnectionCount ?? 0
        let alert = UIAlertController(title: nil, message: NSLocalizedString("There are \(totalConnections) connected clients of which \(activeConnections) are active.", comment: ""), preferredStyle: .alert)
        let cancel = UIAlertAction(title: "OK", style: .default) { (action:UIAlertAction) in
            

                  }
         alert.addAction(cancel)
              
        self.present(alert, animated: true, completion: nil)
           
    }
    
 
}
