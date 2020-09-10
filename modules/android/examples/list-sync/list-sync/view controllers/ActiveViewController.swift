//
//  ActiveViewController.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import UIKit

class ActiveViewController: UITableViewController {

    fileprivate let cbMgr = DatabaseManager.shared
    fileprivate let serviceBrowser:ServiceBrowser = ServiceBrowser.shared
    fileprivate var peers:[PeerHost] = [PeerHost]()
    
    fileprivate let lockQueue = DispatchQueue(label: "thread-safe-queue", attributes: .concurrent)

    private var observation: NSKeyValueObservation?
    override func viewDidLoad() {
        super.viewDidLoad()
   
        self.initializeTable()
        self.registerCells()
        self.serviceBrowser.startSearch(withDelegate: self)
   
    }
   
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(true)
        setupNavigationBar(title: "Browser")

    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func removeAllReplicators() {
        defer {
            lockQueue.sync(flags: .barrier) {
                // perform writes on data
                self.peers.removeAll()
            }
           
        }
        lockQueue.sync {
            // perform read and assign value
            for peer in peers {
              do {
                try cbMgr.stopP2PReplicationWithUserDatabaseToRemotePeer(peer, shouldRemove: true) { (status) in
                    print("Status of stopping replicator for \(peer) is \(status)")
                    
                 }
              }
              catch {
                  print("Failed to stop replicator for \(peer) with \(error)")
              }
          }
        }

    }
    
    deinit {
        print("ActiveViewController deinit")
        //TODO: stop all replicators
        self.removeAllReplicators()
        self.serviceBrowser.stopSearch()
    }
    
    private func registerCells() {
        let peerNib = UINib(nibName: "PeerTableViewCell", bundle: Bundle.main)
        self.tableView?.register(peerNib, forCellReuseIdentifier: "PeerCell")
        
    }
    private func initializeTable() {
        //    self.tableView.backgroundColor = UIColor.darkGray
 
        self.tableView.delegate = self
        self.tableView.dataSource = self
        
        self.tableView.rowHeight = 80
        self.tableView.sectionHeaderHeight = 10.0
        self.tableView.sectionFooterHeight = 10.0
        self.tableView.tableHeaderView = UIView(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        
    }
   

}

// MARK: IBActions
extension ActiveViewController {
    @IBAction func onRefreshTapped(_ sender: UIBarButtonItem) {
        self.serviceBrowser.startSearch(withDelegate: self)
    }
    
 
}
extension ActiveViewController {
    // MARK: - Table View
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        var count = 0
        lockQueue.sync(flags: .barrier) {
            count =  self.peers.count
        }
        return count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        guard let cell =  tableView.dequeueReusableCell(withIdentifier: "PeerCell", for: indexPath) as? PeerTableViewCell else {
            return UITableViewCell()
        }
        cell.selectionStyle = .none
        var peerName = ""
        lockQueue.sync {
            peerName = peers[indexPath.row]
        }
        
        cell.peerName = peerName
        return cell
    }
    
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        var remotePeer:PeerHost!
        lockQueue.sync {
            remotePeer = peers[indexPath.row]
        }
    
        self.updateConnectionStatusForPeer(remotePeer,status: PeerConnectionStatus.Connecting)
        do {
            try cbMgr.startP2PReplicationWithUserDatabaseToRemotePeer(remotePeer, handler: { [weak self] (status) in
                    self?.updateConnectionStatusForPeer(remotePeer,status: status)
            })
         }
        catch {
            self.updateConnectionStatusForPeer(remotePeer,status: PeerConnectionStatus.Error)
        }
    }
    
    override public func tableView(_ tableView: UITableView, editActionsForRowAt indexPath: IndexPath) -> [UITableViewRowAction]? {
        guard let _ = tableView.cellForRow(at: indexPath) as? PeerTableViewCell  else {
            return nil
        }
       
       
        let stopReplicationAction = UITableViewRowAction(style: .normal, title: NSLocalizedString("Disconnect", comment: ""), handler: { [weak self] (action, indexPath) in
            
            var remotePeer:PeerHost?
            self?.lockQueue.sync {
                remotePeer = self?.peers[indexPath.row]
            }
            guard let peerName = remotePeer else {
                print("could not locate peer at index path")
                return
            }
            do {
                try self?.cbMgr.stopP2PReplicationWithUserDatabaseToRemotePeer(peerName, shouldRemove: false, handler: { [weak self] (status) in
                        self?.updateConnectionStatusForPeer(peerName,status: status)
                })
             }
            catch {
                self?.updateConnectionStatusForPeer(peerName,status: PeerConnectionStatus.Error)
            }
            
        })
        
        
        let removeAction = UITableViewRowAction(style: .destructive, title: NSLocalizedString("Remove", comment: ""), handler: { [weak self] (action, indexPath) in
            
            var remotePeer:PeerHost?
            self?.lockQueue.sync {
                remotePeer = self?.peers[indexPath.row]
            }
            guard let peerName = remotePeer else {
                print("could not locate peer at index path")
                return
            }
            do {
                try self?.cbMgr.stopP2PReplicationWithUserDatabaseToRemotePeer(peerName, shouldRemove: true, handler: { [weak self] (status) in
                    self?.updateTableWithPeerRemovedAtIndex(indexPath.row)
                 })
        
             }
            catch {
                self?.updateTableWithPeerRemovedAtIndex(indexPath.row)

            }
            
        })
        return [stopReplicationAction,removeAction]
        
    }

    
}


extension ActiveViewController:ServiceBrowserDelegate {
   
    func onPeerDiscovered(_ peer: PeerHost?, error: Any?) {
       
        if error == nil  {
            // Check if peer already in list. If so, ignore
  
            if let peer = peer {
                var peerCount = 0
                  var index:Int?
                  lockQueue.sync(flags: .barrier){
                    index = peers.firstIndex(of: peer)
                    peerCount = peers.count
                  }
 
            if let _ = index {
                return
            }
            lockQueue.sync(flags: .barrier) {
                self.peers.append(peer)
            }
            self.updateTableWithPeerAddedAtIndex(peerCount)
    
            }
        }
        else {
            print("\(#function). Error: \(String(describing: error))")
        }
    }
   
    
    func onPeerRemoved(_ peer: PeerHost) {
        var index:Int?
        lockQueue.sync(flags: .barrier){
            index = peers.firstIndex(of: peer)
        }
        if  index != nil{
            self.updateTableWithPeerRemovedAtIndex(index!)
        }
    }
    
}

// MARK: Table Update Helpers
extension ActiveViewController {
    private func updateConnectionStatusForPeer(_ peer:PeerHost, status:PeerConnectionStatus) {
        var index:Int?
        lockQueue.sync(flags: .barrier){
            index = peers.firstIndex(of: peer)
        }
        if index != nil {
            let indexPathToUpdate = IndexPath(row: index!, section: 0)
            DispatchQueue.main.async {
                if let cell = self.tableView.cellForRow(at: indexPathToUpdate) as? PeerTableViewCell {
                    cell.status = status
                }
            }
           
        }
    }
    
    private func updateTableWithPeerAddedAtIndex(_ index:Int) {
        print(#function)
        let indexPathToUpdate = IndexPath(row: index, section: 0)
        self.tableView.beginUpdates()
        self.tableView.insertRows(at: [indexPathToUpdate],  with: .automatic)
        self.tableView.endUpdates()
             
       }
    
    private func updateTableWithPeerRemovedAtIndex(_ index:Int) {
       print(#function)
        let indexPathToUpdate = IndexPath(row: index, section: 0)

        lockQueue.sync(flags: .barrier){
            peers.remove(at: index)
        }
        self.tableView.beginUpdates()
        self.tableView.deleteRows(at: [indexPathToUpdate],  with: .automatic)
        self.tableView.endUpdates()
         
    }
}


//MARK : Navigation Bar Setup
extension ActiveViewController {
    override func setupNavigationBar(title: String) {
     
        super.setupNavigationBar(title: title)

       //show right button
        let rightButton = UIBarButtonItem (barButtonSystemItem: .refresh, target: self, action: #selector(onRefreshTapped(_:)))
            
       self.tabBarController?.navigationItem.rightBarButtonItem = rightButton
   
   }

}


