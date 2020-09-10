//
//  CBLURLEndpointListenerConfiguration.h
//  CouchbaseLite
//
//  Copyright (c) 2020 Couchbase, Inc. All rights reserved.
//
//  Licensed under the Couchbase License Agreement (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  https://info.couchbase.com/rs/302-GJY-034/images/2017-10-30_License_Agreement.pdf
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

#import <Foundation/Foundation.h>
#import "CBLListenerAuthenticator.h"
@class CBLDatabase;
@class CBLTLSIdentity;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 The configuration used for configuring and creating a URLEndpointListener.
 */
API_AVAILABLE(macos(10.12), ios(10.0))
@interface CBLURLEndpointListenerConfiguration: NSObject

/** The database object. */
@property (nonatomic, readonly) CBLDatabase* database;

/**
 The port that the listener will listen to. If default value is zero which means that the listener will automatically
 select an available port to listen to when the listener is started.
 */
@property (nonatomic) unsigned short port;

/**
 The network interface in the form of the IP Address or network interface name such as en0 that the listener will
 listen to. The default value is nil which means that the listener will listen to all network interfaces.
 */
@property (nonatomic, nullable) NSString* networkInterface;

/**
 Disable TLS communication. The default value is NO which means that the TLS will be enabled by default.
 */
@property (nonatomic) BOOL disableTLS;

/**
 The TLS Identity used for configuring TLS Communication. The default value is nil which means that
 a generated anonymous self-signed identity will be used unless the disableTLS property is set to YES.
 */
@property (nonatomic, nullable) CBLTLSIdentity* tlsIdentity;

/**
 The authenticator used by the listener to authenticate clients.
 */
@property (nonatomic, nullable) id<CBLListenerAuthenticator> authenticator;

/**
 Allow delta sync when replicating with the listener. The default value is NO.
 */
@property (nonatomic) BOOL enableDeltaSync;

/**
 Allow only pull replication to pull changes from the listener. The default value is NO.
 */
@property (nonatomic) BOOL readOnly;

/** Initializes a listener with the database object. */
- (id) initWithDatabase: (CBLDatabase*)database;

/** Initializes a listener with the configuration object. */
- (instancetype) initWithConfig: (CBLURLEndpointListenerConfiguration*)config;

- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
