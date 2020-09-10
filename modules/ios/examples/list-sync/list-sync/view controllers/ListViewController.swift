//
//  DataViewController.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import UIKit
import Foundation


class ListViewController:UITableViewController, ListPresentingViewProtocol {
    
    fileprivate var record:ListRecord?
    lazy var listPresenter:ListPresenter = ListPresenter()
    fileprivate var imageUpdated:Bool = false
    fileprivate var indexOfCellUnderEdit:IndexPath?
    override public func viewDidLoad() {
        super.viewDidLoad()
    
        self.initializeTable()
        self.registerCells()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(true)
        self.setupNavigationBar(title: NSLocalizedString(
                "What's in Season?",comment:""))
        self.listPresenter.attachPresentingView(self)
        self.listPresenter.fetchRecordOfType( kListRecordDocumentType, liveModeEnabled: true)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        self.listPresenter.detachPresentingView(self)
    }
    
    
    private func initializeTable() {
        //    self.tableView.backgroundColor = UIColor.darkGray
        self.tableView.delegate = self
        self.tableView.dataSource = self
        
        self.tableView.rowHeight = 80.0
        self.tableView.sectionHeaderHeight = 10.0
        self.tableView.sectionFooterHeight = 10.0
        self.tableView.tableHeaderView = UIView(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        
    }
    
    private func registerCells() {
        let basicInfoNib = UINib(nibName: "CustomListTableViewCell", bundle: Bundle.main)
        self.tableView?.register(basicInfoNib, forCellReuseIdentifier: "ListCell")
        
    }
}

// MARK: IBActions
extension ListViewController {
    @IBAction func onDoneTapped(_ sender: UIBarButtonItem) {
        guard let listProfile = record else {return}
        
        
        self.listPresenter.setRecordOfType(kListRecordDocumentType,record:listProfile, handler: { [weak self](error) in
            guard let `self` = self else {
                return
            }
            if error != nil {
                self.showAlertWithTitle(NSLocalizedString("Error!", comment: ""), message: (error?.localizedDescription) ?? "Failed to update list record")
            }
            else {
                self.showAlertWithTitle("", message: "Succesfully updated List!")
            }
        })
    }
    
 
}

//MARK:UITableViewDataSource
extension ListViewController {
    public override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.record?.items.count ?? 0
    }
    
    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        print(#function)
        guard let cell = tableView.dequeueReusableCell( withIdentifier: "ListCell") as? CustomListTableViewCell else {
            return UITableViewCell()
        }
        
        if let item = self.record?.items[indexPath.row] {
            cell.textEntryName?.text = item.key ?? ""
            cell.textEntryValue?.text = "\(item.value!)"
            if let imageData = item.image{
                cell.imageBlob  = UIImage.init(data: imageData)
            }
            else {
                let imageFile = "default_food"
                if let image = item.image {
                    cell.imageBlob = UIImage.init(data: image)
                }
                else {
                    cell.imageBlob  = UIImage.init(imageLiteralResourceName: imageFile)
                }
            }
            cell.delegate = self
        }
        
        cell.selectionStyle = .none
        return cell
        
        
    }
    override public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat{
        return 80.0
    }
    
    
    public override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
}

// MARK : CustomListItemImageEntryProtocol
extension ListViewController:CustomListItemEntryProtocol {
    
    func onUpdateTextForCell(_ cell: CustomListTableViewCell) {
        indexOfCellUnderEdit = self.tableView.indexPath(for: cell)
        guard let indexOfCellUnderEdit = indexOfCellUnderEdit else {
            return
        }
        
        if var item = self.record?.items[indexOfCellUnderEdit.row]  {
            let value = Int(cell.textEntryValue.text  ?? "")
            item.value = value
 
         //   self.doneButton.isEnabled = true
            self.record?.items[indexOfCellUnderEdit.row] = item
        }
        
        self.indexOfCellUnderEdit = nil
        
    }
    
    func onUploadImageForCell(_ cell: CustomListTableViewCell) {
        indexOfCellUnderEdit = self.tableView.indexPath(for: cell)
        
        let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        alert.modalPresentationStyle = .popover
        let albumAction = UIAlertAction(title: NSLocalizedString("Select From Photo Album", comment: ""), style: .default) { action in
            
            let imagePickerController = UIImagePickerController()
            imagePickerController.delegate = self
            imagePickerController.allowsEditing = false
            imagePickerController.sourceType = UIImagePickerController.SourceType.photoLibrary;
            
            imagePickerController.modalPresentationStyle = .overCurrentContext
            
            self.present(imagePickerController, animated: true, completion: nil)
            
        }
        /// The camera stuff does not work
        
//        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerController.SourceType.camera) {
//            let cameraAction = UIAlertAction(title: NSLocalizedString("Take Photo", comment: ""), style: .default) { [unowned self] action in
//
//                let imagePickerController = UIImagePickerController()
//                imagePickerController.delegate = self
//                imagePickerController.allowsEditing = false
//                imagePickerController.sourceType = UIImagePickerController.SourceType.camera;
//                imagePickerController.cameraDevice = UIImagePickerController.CameraDevice.front;
//
//                imagePickerController.modalPresentationStyle = .overCurrentContext
//
//                self.present(imagePickerController, animated: true, completion: nil)
//
//            }
//            alert.addAction(cameraAction)
//
//        }
        alert.addAction(albumAction)
        
        if let presenter = alert.popoverPresentationController {
            presenter.sourceView = self.view
            presenter.sourceRect = self.view.bounds
        }
        present(alert, animated: true, completion: nil)
        
    }
}


extension ListViewController : UIImagePickerControllerDelegate , UINavigationControllerDelegate{
    
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        if (info[UIImagePickerController.InfoKey(rawValue: UIImagePickerController.InfoKey.originalImage.rawValue)] as? UIImage) != nil {
            self.imageUpdated = true
       //     self.doneButton.isEnabled = true
            guard let indexOfCellUnderEdit = indexOfCellUnderEdit else {
                return
            }
            
            if let image = info[UIImagePickerController.InfoKey(rawValue: UIImagePickerController.InfoKey.originalImage.rawValue)] as? UIImage {
                if var item = self.record?.items[indexOfCellUnderEdit.row]  {
                    item.image = image.jpegData(compressionQuality: 0.75)
                    
                    self.imageUpdated = true
          //          self.doneButton.isEnabled = true
                    self.record?.items[indexOfCellUnderEdit.row] = item
                }
                
            }
            tableView.beginUpdates()
            tableView.endUpdates()
            tableView.reloadRows(at: [IndexPath.init(row: indexOfCellUnderEdit.row, section: 0)], with: .automatic)
            
            self.indexOfCellUnderEdit = nil
            picker.presentingViewController?.dismiss(animated: true, completion: nil)
        }
    }
    
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.presentingViewController?.dismiss(animated: true, completion: nil)
        indexOfCellUnderEdit = nil
        
    }
    
}
// MARK : ListPresentingViewProtocol
extension ListViewController {
    func updateUIWithListRecord(_ record: ListRecord?, error: Error?) {
        switch error {
        case nil:
            self.record = record
            self.tableView.reloadData()
        default:
            self.showAlertWithTitle(NSLocalizedString("Error!", comment: ""), message: (error?.localizedDescription) ?? "Failed to fetch date list record")
        }
    }
}


//MARK : Navigation Bar Setup
extension ListViewController {
    override func setupNavigationBar(title: String) {
     
        super.setupNavigationBar(title: title)

       //show right button
       let rightButton = UIBarButtonItem(title: "Done", style: UIBarButtonItem.Style.plain, target: self, action: #selector(onDoneTapped(_:)))

       self.tabBarController?.navigationItem.rightBarButtonItem = rightButton
   
   }
}
