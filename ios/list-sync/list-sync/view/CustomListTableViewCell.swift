//
//  CustomListTableViewCell.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/4/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
import UIKit


protocol CustomListItemEntryProtocol:AnyObject {
    func onUploadImageForCell(_ cell:CustomListTableViewCell)
    func onUpdateTextForCell(_ cell:CustomListTableViewCell)
}


// optional
extension CustomListItemEntryProtocol {
    func onUploadImageForCell(_ cell:CustomListTableViewCell) {
        print(#function)
    }
    func onUpdateTextForCell(_ cell:CustomListTableViewCell) {
        print(#function)
    }
}

class CustomListTableViewCell: UITableViewCell {
    
    public var name:String? {
        didSet {
            self.updateUI()
        }
    }
    var imageBlob:UIImage? {
        didSet {
            updateUI()
        }
    }
    var uploadButton:UIButton?
    weak var delegate:CustomListItemEntryProtocol?
    @IBOutlet weak var imageEntryView: UIImageView!
    
    @IBOutlet weak var textEntryName: UILabel!
    @IBOutlet weak var textEntryValue: UITextView!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        imageEntryView.contentMode = .scaleAspectFit
        updateUI()
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    private func updateUI(){
        if let _ = textEntryName, let name = name {
            self.textEntryName.text = name
        }
        if let _ = imageEntryView, let imageVal = imageBlob {
            self.imageEntryView.image = imageVal
        }
        self.layoutIfNeeded()
    }
    
}



extension CustomListTableViewCell {
    @IBAction func updateThumbnail(_ sender: UIButton) {
        delegate?.onUploadImageForCell(self)
    }
}

// MARK: UITextViewDelegate
extension CustomListTableViewCell:UITextViewDelegate {
    public func textViewDidChange(_ textView: UITextView) {
        print(#function)
        self.delegate?.onUpdateTextForCell(self)
        
    }

    public func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) ->  Bool {
            if text == "\n" {
                textView.resignFirstResponder()
                
            }
                    
        return true
    }
    
}
