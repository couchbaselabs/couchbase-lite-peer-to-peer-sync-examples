//
//  SampleFileLoaderUtils.swift
//  list-sync
//
//  Created by Priya Rajagopal on 6/4/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
import UIKit
import CouchbaseLiteSwift

class SampleFileLoaderUtils {

    /// Public Access

    static let shared:SampleFileLoaderUtils = {
        let instance = SampleFileLoaderUtils()
         return instance
    }()

    // Don't allow instantiation . Enforce singleton
    private init() {
      
    }
}

extension SampleFileLoaderUtils {


   // Every entry in sample list is data associated with a specific document type. There is only a single document type "list" supported by the app
    // Every document contains 2 elements.
    //   - A “type” field of string
    //   - and an “items” field of array type.
    //       - Every element in the items array is a dictionary of three elements (image, value ,key).
     
    //    Example:
    //    {"type":"list",
    //    "items":[
    //        {
    //        "image":{"length":16608,"digest":"sha1-    LEFKeUfywGIjASSBa0l/cg5rlm8=","content_type":"image/jpeg","@type":"blob"},
    //        "value":46,
    //        "key":"Apples"},
    //       {
    //        "image":{"length":23737,"digest":"sha1-ccilupjPJp+r7hKuTEkLH76FwvA=","content_type":"image/jpeg","@type":"blob"},
    //        "value":12,
    //        "key":"Aragula"
    //       }
    //    ]
    //    }
     
   public func loadSampleJSONDataForUserFromFile(name: String) ->[String:Any]? {

       guard let path = Bundle.main.path(forResource: name, ofType: "json") else {
           fatalError("\(name) sample file not found")
       }
         
       // Every line in this sample file must correspond to a valid JSON object of type "Key:Value"
       guard let content =  try? String(contentsOfFile: path, encoding: String.Encoding.utf8) else {
           fatalError("Invalid file ")
       }
         
       var n = 0
       var jsonEntries:[String:Any] = [:]
       var jsonDict:[String:Any] = [:]
         content.enumerateLines(invoking: { (line: String, stop: inout Bool) in
             n += 1
             let json = line.data(using: String.Encoding.utf8, allowLossyConversion: false)
             if let dict = try? JSONSerialization.jsonObject(with: json!, options: []) as? [String:Any] {
                 
                // locate image from assets folder and embed that as part of each item entry

                 if let items = dict[ListDocumentKeys.items.rawValue] as? Array<AnyObject> {
                     var modifiedItems:[Any] = [Any]()
                     for item in items {
                        
                         guard let key = item[ListItemDocumentKeys.key.rawValue] as? String, let value = item[ListItemDocumentKeys.value.rawValue]  else {
                             print("key or value is nil")
                             continue
                         }
                         let modifiedItem = MutableDictionaryObject()
                        // var modifiedItem:[String:Any] = [:]
                         modifiedItem.setString(key, forKey: ListItemDocumentKeys.key.rawValue)
                         
                         modifiedItem.setValue(value, forKey: ListItemDocumentKeys.value.rawValue)
                         
                         // load images from assets folder and associate with item
                         print(modifiedItem)
                         var imageFile:String = "default_food"
                         
                         let defaultImage = UIImage.init(imageLiteralResourceName: imageFile)
                         if let imageData = defaultImage.jpegData(compressionQuality: 0.75) {
                         
                             modifiedItem.setBlob(Blob.init(contentType: "image/jpeg", data: imageData), forKey: ListItemDocumentKeys.image.rawValue)
                         }
                         if let imageName = item[ListItemDocumentKeys.key.rawValue] as? String {
                             imageFile = imageName.lowercased()
                            // print("locating***\(imageFile) \(UIImage.init(named:  imageFile))***")
                             let customImage = UIImage.init(named:  imageFile)
                                                 
                             if let customImageData = customImage?.jpegData(compressionQuality: 0.75) {
                                  modifiedItem.setBlob(Blob.init(contentType: "image/jpeg", data: customImageData), forKey: ListItemDocumentKeys.image.rawValue)
                             }
                             
                         }
                         modifiedItems.append(modifiedItem)
                     }
                    jsonDict[ListDocumentKeys.items.rawValue] = modifiedItems
                            
                   let docType = dict[ListDocumentKeys.type.rawValue] as! String
                   jsonEntries[docType] = jsonDict
                 }
       
             }
   
         })
  //     print("***** \(jsonEntries) ****")
     return jsonEntries
   }
    
    public func loadWhitelistUsersFromFile(name: String) ->[[String:String]]? {

          guard let path = Bundle.main.path(forResource: name, ofType: "json") else {
              fatalError("\(name) sample file not found")
          }
            
          // Every line in this sample file must correspond to a valid JSON object of type "Key:Value"
          guard let content =  try? String(contentsOfFile: path, encoding: String.Encoding.utf8) else {
              fatalError("Invalid file ")
          }
            
          var n = 0
          var jsonEntries:[[String:String]] = []
             content.enumerateLines(invoking: { (line: String, stop: inout Bool) in
                n += 1
                let json = line.data(using: String.Encoding.utf8, allowLossyConversion: false)
                if let dict = try? JSONSerialization.jsonObject(with: json!, options: []) as? [String:String] {
                    
                   
                    jsonEntries.append(dict)
                    }
      
            })
       //   print("***** \(jsonEntries) ****")
        return jsonEntries
      }
}
