//
//  SpinnerViewController.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/8/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import  UIKit
// Taken from https://www.hackingwithswift.com/
class SpinnerViewController: UIViewController {
    var spinner = UIActivityIndicatorView(style: .whiteLarge)

    override func loadView() {
        view = UIView()
        view.backgroundColor = UIColor(white: 0, alpha: 0.7)

        spinner.translatesAutoresizingMaskIntoConstraints = false
        spinner.startAnimating()
        view.addSubview(spinner)

        spinner.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        spinner.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
    }
}

