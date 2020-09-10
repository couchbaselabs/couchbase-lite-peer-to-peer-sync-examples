//
//  AppDelegate.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import UIKit

//TODO:: Clean up when loggig out.  listener 

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    fileprivate var loginViewController:LoginViewController?
    fileprivate var cbMgr = DatabaseManager.shared
    fileprivate var isObservingForLoginEvents:Bool = false
    fileprivate var tabBarController:UITabBarController?
    fileprivate var navController:UINavigationController?
       
    internal func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        self.setAppearance()
        self.loadLoginViewController()
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

// MARK : Notification Observers
extension AppDelegate {
    func loadLoginViewController() {
         
        if let loginVC = loginViewController {
            window?.rootViewController = loginVC
            
        }
        else {
            let storyboard = UIStoryboard.init(name: "Main", bundle: Bundle.main)
            loginViewController = storyboard.instantiateViewController(withIdentifier: "LoginViewController") as? LoginViewController
            window?.rootViewController = loginViewController
            
        }
        self.registerNotificationObservers()
        
    }
    
    
    func loadPostLoginViewController() {
        if let mainNVC = navController {
            window?.rootViewController = mainNVC
            
        }
        else {
            let storyboard = UIStoryboard.init(name: "Main", bundle: Bundle.main)
            navController = storyboard.instantiateViewController(withIdentifier: "mainNVC") as? UINavigationController
            window?.rootViewController = navController
        }
        
//        if let mainTBC = tabBarController {
//                   window?.rootViewController = mainTBC
//
//               }
//               else {
//                   let storyboard = UIStoryboard.init(name: "Main", bundle: Bundle.main)
//                   tabBarController = storyboard.instantiateViewController(withIdentifier: "mainTBC") as? UITabBarController
//                   window?.rootViewController = tabBarController
//               }
        
    }
   
    
    func login() {
          
        loadPostLoginViewController()
    }
    
    func logout() {
        print(#function)
        let spinner = SpinnerViewController()
        
        self.deregisterNotificationObservers()
        DispatchQueue.main.async {
            self.showSpinner(spinner: spinner)
 
            defer {
                self.hideSpinner(spinner)
                self.navController = nil
                           
            }
            self.cbMgr.closeDatabaseForCurrentUser()
       
        }
  
        loadLoginViewController()
    }
    
    
    func isUserLoggedIn() -> Bool{
        return self.window?.rootViewController == navController
    }
}

// MARK: Observers
extension AppDelegate {
    
    func registerNotificationObservers() {
        if isObservingForLoginEvents == false {
            NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: AppNotifications.loginInSuccess.name.rawValue), object: nil, queue: nil) { [weak self] (notification) in
                guard let `self` = self else { return }
                print("Log in success")
                self.login()
                
            }
            
            NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: AppNotifications.loginInFailure.name.rawValue), object: nil, queue: nil) { (notification) in
                print("failed to log in")
//                if let userInfo = (notification as NSNotification).userInfo as? Dictionary<String,String> {
//                    if let _ = userInfo[AppNotifications.loginInSuccess.userInfoKeys.user.rawValue]{
//                        self.logout()
//                    }
//                }
            }
            
            NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: AppNotifications.logout.name.rawValue), object: nil, queue: nil) { [unowned self] (notification) in
                self.logout()
            }
            
            isObservingForLoginEvents = true
        }
    }
    
    
    func deregisterNotificationObservers() {
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: AppNotifications.loginInSuccess.name.rawValue), object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: AppNotifications.loginInFailure.name.rawValue), object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: AppNotifications.logout.name.rawValue), object: nil)
        isObservingForLoginEvents = false
        
    }
    
}

extension AppDelegate {
    func showSpinner(spinner:SpinnerViewController){
        
        // add the spinner view controller
        tabBarController?.showActivitySpinner(spinner)
      
     
    }
       
    func hideSpinner(_ spinner:SpinnerViewController) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 10) {
            self.tabBarController?.hideActivitySpinner(spinner)
        }
    }
}

extension AppDelegate {
    func setAppearance() {
        UINavigationBar.appearance().barTintColor = UIColor.init(displayP3Red: 247/255, green:  242/255, blue:  174/255, alpha: 1.0)
        UITabBar.appearance().tintColor = UIColor.init(displayP3Red: 247/255, green:  242/255, blue:  174/255, alpha: 1.0)
 
        
        UIBarButtonItem.appearance(whenContainedInInstancesOf: [UINavigationBar.self]).tintColor = .black
        UIBarButtonItem.appearance(whenContainedInInstancesOf: [UITabBar.self]).tintColor = .black
        UIBarButtonItem.appearance(whenContainedInInstancesOf: [UIToolbar.self]).tintColor = .black
       
    }
}
