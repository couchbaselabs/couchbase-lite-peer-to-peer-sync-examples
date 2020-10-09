//
//  DataPresenter.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/4/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
import CouchbaseLiteSwift

// MARK : typealias

enum ListDocumentKeys:String {
    case type
    case items
}

enum ListItemDocumentKeys:String {
    case image
    case key
    case value
}


// MARK: ListPresenterProtocol
// To be implemented by presenter
protocol ListPresenterProtocol : PresenterProtocol {
    func fetchRecordOfType(_ type:String,liveModeEnabled enabled:Bool )
    func setRecordOfType(_ type:String,record:ListRecord?, handler:@escaping(_ error:Error?)->Void)
}

// MARK: ListPresentingViewProtocol
// To be implemented by the presenting view
protocol ListPresentingViewProtocol:PresentingViewProtocol {
    func updateUIWithListRecord(_ record:ListRecord?,error:Error?)
}

// MARK: ListPresenter
class ListPresenter:ListPresenterProtocol {
    
    fileprivate var dbMgr:DatabaseManager = DatabaseManager.shared
    
    fileprivate var listQueryToken:ListenerToken?
    fileprivate var listQuery:Query?

    weak var associatedView: ListPresentingViewProtocol?
    
    deinit {
        if let listQueryToken = listQueryToken {
            listQuery?.removeChangeListener(withToken: listQueryToken)
        }
        listQuery = nil
    }
}



extension ListPresenter {
    func fetchRecordOfType(_ type:String,liveModeEnabled enabled:Bool = false ){
        
        let docId = "\(dbMgr.kDocPrefix)\(type)"
        print("DocId is \(docId)")
        switch enabled {
        case true :
            // Doing a live query for specific document
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

                self.associatedView?.dataFinishedLoading()
                self.associatedView?.updateUIWithListRecord(listRecord, error: nil)
                
            default:
                self.associatedView?.dataFinishedLoading()
                self.associatedView?.updateUIWithListRecord(nil, error: ListDocError.UserNotFound)
            }
        }
                
   
            
        case false:
            // Case when we are doing a one-time fetch for document
            
            guard let db = dbMgr.userDB else {
                fatalError("db is not initialized at this point!")
            }
            
            var listRecord = ListRecord.init(items: [])
            
            self.associatedView?.dataStartedLoading()
            
            // fetch document corresponding to the doc Id
        
            if let doc = db.document(withID: docId)   {
                if let listItems = doc.array(forKey: ListDocumentKeys.items.rawValue)?.toArray()
                    as? [[String:Any]]{
                    for item in listItems {
                        let key =  item[ListItemDocumentKeys.key.rawValue] as? String
                        let value =  item[ListItemDocumentKeys.value.rawValue]
                        let image = item[ListItemDocumentKeys.image.rawValue] as? Blob
                        listRecord.items.append((image: image?.content, key: key, value: value))
                        
                    }
                }
            }
            
            self.associatedView?.dataFinishedLoading()
            self.associatedView?.updateUIWithListRecord(listRecord, error: nil)
        }
    }
    
    
    func setRecordOfType(_ type:String, record:ListRecord?, handler:@escaping(_ error:Error?)->Void) {
        
        guard let db = dbMgr.userDB else {
            fatalError("db is not initialized at this point!")
            
        }
        
        // This will create a new instance of MutableDocument or will
        // fetch existing one
        // Get mutable version

        let docId = "\(dbMgr.kDocPrefix)\(type)"
        print("DocId is \(docId)")
        guard let mutableDoc = db.document(withID:docId)?.toMutable() else {
            fatalError("document not created from sample at this point!")
            
        }
        
        mutableDoc.setString(record?.type,forKey: ListDocumentKeys.type.rawValue)
        guard let arr = record?.items else {
            print("No elements to add")
            return
        }
        let transformedArr = arr.map { (arg) -> [String:Any] in
            
            let (image, key, value) = arg
            if let image = image, let key = key, let value = value {
                return ["key":key,"value":value,"image":Blob.init(contentType: "image/jpeg", data: image)]
            }
            else {
                return [:]
            }
        }
        
        for item in transformedArr {
            print(item)
        }
 
        mutableDoc.setArray(MutableArrayObject.init(data: transformedArr),  forKey: ListDocumentKeys.items.rawValue)
        do {
            // This will create a document if it does not exist and overrite it if it exists
            // Using default concurrency control policy of "writes always win"
            try db.saveDocument(mutableDoc)
            handler(nil)
        }
        catch {
            handler(error)
        }
    }
    
}


// MARK: PresenterProtocol
extension ListPresenter:PresenterProtocol {
    func attachPresentingView(_ view:PresentingViewProtocol) {
        self.associatedView = view as? ListPresentingViewProtocol
        
    }
    func detachPresentingView(_ view:PresentingViewProtocol) {
        self.associatedView = nil
    }
}
