//
//  CBLMessageEndpointListener.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
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
#import "CBLProtocolType.h"
@class CBLDatabase;
@class CBLReplicatorStatus;
@class CBLMessageEndpointListenerChange;
@class CBLMessageEndpointListenerConfiguration;
@protocol CBLListenerToken;
@protocol CBLMessageEndpointConnection;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 The listener for incoming message endpoint connections.
 */
@interface CBLMessageEndpointListener : NSObject

/** The active connections. */
@property (readonly, atomic) NSArray<id<CBLMessageEndpointConnection>>* connections;

/**
 Initializes the CBLMessageEndpointListener object with the given configuration.
 @param config The configuration object.
 @return The CBLMessageEndpointListener object.
 */
- (instancetype) initWithConfig: (CBLMessageEndpointListenerConfiguration*)config;

/**
 Accepts the incoming message endpoint connection.

 @param connection The CBLMessageEndpointConnection object.
 */
- (void) accept: (id<CBLMessageEndpointConnection>)connection;

/**
 Closes the given message endpoint connection.

 @param connection The CBLMessageEndpointConnection object
 */
- (void) close: (id<CBLMessageEndpointConnection>)connection;

/**
 Closes all active connections.
 */
- (void) closeAll;

#pragma mark - Change Listeners

/**
 Adds a change listener. Changes will be posted on the main queue.

 @param listener The listener to post the changes.
 @return An opaque listener token object for removing the listener.
 */
- (id<CBLListenerToken>) addChangeListener: (void (^)(CBLMessageEndpointListenerChange*))listener;

/**
 Adds a change listener with the dispatch queue on which changes
 will be posted. If the dispatch queue is not specified, the changes will be
 posted on the main queue.

 @param queue The dispatch queue.
 @param listener The listener to post changes.
 @return An opaque listener token object for removing the listener.
 */
- (id<CBLListenerToken>) addChangeListenerWithQueue: (nullable dispatch_queue_t)queue
                                           listener: (void (^)(CBLMessageEndpointListenerChange*))listener;

/**
 Removes a change listener with the given listener token.

 @param token The listener token.
 */
- (void) removeChangeListenerWithToken: (id<CBLListenerToken>)token;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end


/**
 ENTERPRISE EDITION ONLY.
 
 The configuration for the CBLMessageEndpointListener.
 */
@interface CBLMessageEndpointListenerConfiguration: NSObject

/**
 The local database.
 */
@property (nonatomic, readonly) CBLDatabase* database;

/**
 The data transmission protocol type.
 */
@property (nonatomic) CBLProtocolType protocolType;

 /**
 Initializes a CBLMessageEndpointListenerConfiguration object.

 @param database The local database.
 @param protocolType The data transmission protocol type.
 @return The CBLMessageEndpointListenerConfiguration object.
 */
- (instancetype) initWithDatabase: (CBLDatabase*)database
                     protocolType: (CBLProtocolType)protocolType;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end


/**
 ENTERPRISE EDITION ONLY.
 
 A change event posted by CBLMessageEndpointListener.
 */
@interface CBLMessageEndpointListenerChange : NSObject

/** The connection whose status changed. */
@property (nonatomic, readonly) id<CBLMessageEndpointConnection> connection;

/** The new replication status of the connection. */
@property (nonatomic, readonly) CBLReplicatorStatus* status;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
