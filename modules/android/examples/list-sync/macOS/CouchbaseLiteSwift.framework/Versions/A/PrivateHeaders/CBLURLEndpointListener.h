//
//  CBLURLEndpointListener.h
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
@class CBLURLEndpointListenerConfiguration;
@class CBLTLSIdentity;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 CBLConnectionStatus provides the information about the connections from the
 replicators to the listener.
 */
typedef struct {
    uint64_t connectionCount;       ///< The total number of connections.
    uint64_t activeConnectionCount; ///< The number of the connections that are in active or busy state.
} CBLConnectionStatus;

/**
 ENTERPRISE EDITION ONLY.
 
A listener to provide websocket based endpoint for peer-to-peer replication.
Once the listener is started, peer replicators can connect to the listener by using
CBLURLEndpoint.
*/
API_AVAILABLE(macos(10.12), ios(10.0))
@interface CBLURLEndpointListener : NSObject

/** The read-only configuration. */
@property (nonatomic, readonly) CBLURLEndpointListenerConfiguration* config;

/** The port that the listener is listening to. If the listener is not started, the port property will be zero. */
@property (nonatomic, readonly) unsigned short port;

/** The TLS identity used by the listener to provide TLS communication. If the listener is not started or
    the TLS is disabled, the property value will be nil.  */
@property (nonatomic, readonly, nullable) CBLTLSIdentity* tlsIdentity;

/** The possible URLs of the listener. If the listener is not started, the urls property will be nil. */
@property (nonatomic, readonly, nullable) NSArray<NSURL*>* urls;

/** The current connection status of the listener.  */
@property (nonatomic, readonly) CBLConnectionStatus status;

/** Initializes a listener with the given configuration object. */
- (instancetype) initWithConfig: (CBLURLEndpointListenerConfiguration*)config;

/** Starts the listener. */
- (BOOL) startWithError: (NSError**)error;

/** Stop the listener. */
- (void) stop;

- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
