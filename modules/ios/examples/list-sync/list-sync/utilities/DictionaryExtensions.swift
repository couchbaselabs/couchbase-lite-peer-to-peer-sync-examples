//
//  DictionaryExtensions.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/3/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

import Foundation
extension Dictionary where Value: Equatable {
    func key(forValue value: Value) -> Key? {
        return first { $0.1 == value }?.0
    }
}
