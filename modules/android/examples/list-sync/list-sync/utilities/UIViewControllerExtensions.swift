//
//  UIViewControllerExtensions.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/3/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import UIKit
enum TabViewControllerIndex:Int {
    case PassiveViewController = 0
    case ActiveViewController = 1
}
extension UIViewController {

    @objc func setupNavigationBar(title: String) {
   
        self.tabBarController?.navigationItem.rightBarButtonItems?.removeAll()
        self.tabBarController?.navigationItem.leftBarButtonItems?.removeAll()
        //set titile
        self.tabBarController?.navigationItem.title =  title

  
        //show right button
        let leftButton = UIBarButtonItem(title: "Log Out", style: UIBarButtonItem.Style.plain, target: self, action: #selector(onLogoutTapped))
 
        self.tabBarController?.navigationItem.leftBarButtonItem = leftButton
    }

    @objc func onLogoutTapped(_ sender: UIBarButtonItem) {
        print(#function)
//        let passiveVC = self.tabBarController?.viewControllers?[TabViewControllerIndex.PassiveViewController.rawValue] as? PassiveViewController
//        passiveVC?.stopWebsocketListener()
//        
//        let activeVC = self.tabBarController?.viewControllers?[TabViewControllerIndex.ActiveViewController.rawValue] as? ActiveViewController
//        activeVC?.stopAllReplicators()
        
        NotificationCenter.default.post(Notification.notificationForLogOut())
    }

     
    func showActivitySpinner(_ spinner:SpinnerViewController) {
     
        // add the spinner view controller
        addChild(spinner)
        spinner.view.frame = view.frame
        view.addSubview(spinner.view)
        spinner.didMove(toParent: self)
  
    }
    
    func hideActivitySpinner(_ spinner:SpinnerViewController) {
        spinner.willMove(toParent: nil)
        spinner.view.removeFromSuperview()
        spinner.removeFromParent()
        
    }
    

}


