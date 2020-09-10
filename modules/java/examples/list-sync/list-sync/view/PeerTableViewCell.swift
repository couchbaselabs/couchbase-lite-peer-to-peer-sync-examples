//
//  PeerTableViewCell.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/3/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import UIKit

enum PeerConnectionStatus {
    case Connected
    case Connecting
    case Disconnected
    case Busy
    case Error
}
class PeerTableViewCell: UITableViewCell {
    
    public var peerName:String? {
        didSet {
            status = .Disconnected
            self.updateUI()
        }
    }
 
    public var status:PeerConnectionStatus = .Disconnected {
        didSet {
            self.updateUI()
        }
    }
    public var location:String? {
        didSet {
            self.updateUI()
        }
    }
    @IBOutlet weak var peerLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var locationLabel: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        updateUI()
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    private func updateUI(){
        if let name = peerName {
            self.peerLabel.text = name
        }
       
        if let locationStr = location {
            self.locationLabel.text = locationStr
        }
        switch status {
        case .Connected:
            self.statusLabel.text = "Connected".uppercased()
            self.statusLabel.textColor = #colorLiteral(red: 0.3411764801, green: 0.6235294342, blue: 0.1686274558, alpha: 1)
        case .Connecting:
            self.statusLabel.text = "Connecting".uppercased()
            self.statusLabel.textColor = #colorLiteral(red: 0.9372549057, green: 0.3490196168, blue: 0.1921568662, alpha: 1)
        case .Disconnected:
            self.statusLabel.text = "Disconnected".uppercased()
            self.statusLabel.textColor = #colorLiteral(red: 0.2549019754, green: 0.2745098174, blue: 0.3019607961, alpha: 1)
        case .Error:
            self.statusLabel.text = "Error".uppercased()
            self.statusLabel.textColor = UIColor.red
        case .Busy:
            self.statusLabel.text = "Busy".uppercased()
            self.statusLabel.textColor = #colorLiteral(red: 0.1411764771, green: 0.3960784376, blue: 0.5647059083, alpha: 1)
        }
        self.layoutIfNeeded()
    }
    
}


