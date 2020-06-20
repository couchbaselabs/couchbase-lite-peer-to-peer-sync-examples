//
//  ListRecord.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation


import Foundation
import UIKit
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
