//
//  CustomErrors.swift
//  simple-p2p-sync
//
//  Created by Priya Rajagopal on 5/1/20.
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//


import Foundation
enum ListDocError: LocalizedError , CustomStringConvertible{
    case DatabaseNotInitialized
    case DatabasePathNotFound
    case UserNotFound
    case RemoteDatabaseNotReachable
    case DataParseError
    case UserCredentialsNotProvided
    case DocumentFetchException
    case ImageProcessingFailure
    case ImageTooBig
    case ReplicatorNotInitialized
    case WebsocketsListenerNotInitialized
    case URLInvalid
    case UnsupportedTLSMode
}

extension ListDocError {
    /// Retrieve the localized description for this error.
    var description: String {
        switch self {
        case .DatabaseNotInitialized :
            return NSLocalizedString("Couchbase Lite Database not initialized", comment: "")
        case .DatabasePathNotFound :
            return NSLocalizedString("Could not open database at specified path", comment: "")
        case .UserNotFound:
            return NSLocalizedString("User not logged in", comment: "")
        case .RemoteDatabaseNotReachable:
            return NSLocalizedString("Could not access remote sync gateway URL", comment: "")
        case .DataParseError:
            return NSLocalizedString("Could not parse response. Appears to be in invalid format ", comment: "")
        case .UserCredentialsNotProvided:
            return NSLocalizedString("Please provide right credentials to sync with Sync Gateway ", comment: "")
        case .DocumentFetchException:
            return NSLocalizedString("Could not create or fetch document from database", comment: "")
        case .ImageProcessingFailure:
            return NSLocalizedString("Failed to process image ", comment: "")
        case .ImageTooBig:
            return NSLocalizedString("Image size too big!", comment: "")
        case .ReplicatorNotInitialized:
            return NSLocalizedString("Could not initialize replicator!", comment: "")
        case .WebsocketsListenerNotInitialized:
            return NSLocalizedString("Websockets listener not initialized", comment: "")
        case .URLInvalid:
            return NSLocalizedString("The listener URL is not valid", comment: "")
        case .UnsupportedTLSMode:
            return NSLocalizedString("This version of app does not support specified TLS mode", comment: "")

        }
        
    }
    
}
extension LocalizedError where Self: CustomStringConvertible {
    var errorDescription: String? {
        return description
    }
    
    
}

