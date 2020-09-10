//
//  CBLMessageEndpointConnection.h
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
@class CBLMessage;
@class CBLMessageEndpoint;
@class CBLMessagingError;
@protocol CBLReplicatorConnection;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 The protocol implemented by the application using a custom transporation
 method to exchange replication data between peers.
 */
@protocol CBLMessageEndpointConnection <NSObject>

/**
 Called to open a remote connection to the other peer when the replicator
 starts or when the CBLMessageEndpointListener accepts the connection.
 When the remote connection is established, call the completion block to
 acknowledge the completion.

 @param connection The replicator connection.
 @param completion The completion block.
 */
- (void) open: (id<CBLReplicatorConnection>)connection
   completion: (void (^)(BOOL success, CBLMessagingError* _Nullable))completion;

/**
 Called to close the remote connection with the other peer when the
 replicator stops or when the CBLMessageEndpointListener closes the connection.
 When the remote connection is closed, call the completion block to acknowledge
 the completion.

 @param error The error if available.
 @param completion The completion block.
 */
- (void) close: (nullable NSError*)error completion: (void (^)(void))completion;

/**
 Called to send the replication data to the other peer. When the replication
 data is sent, call the completion block to acknowledge the completion.

 @param message The message.
 @param completion The completion block.
 */
- (void) send: (CBLMessage*)message
   completion: (void (^)(BOOL success, CBLMessagingError* _Nullable))completion;

@end

/**
 ENTERPRISE EDITION ONLY.
 
 The replicator connection used by the application to tell the replicator to
 consume the data received from the other peer or to close the connection.
 */
@protocol CBLReplicatorConnection <NSObject>

/**
 Tells the replicator to close the current replicator connection. In return,
 the replicator will call the CBLMessageEndpointConnection's -close:completion:
 to acknowledge the closed connection.

 @param error The error if available.
 */
- (void) close: (nullable CBLMessagingError*)error;

/**
 Tells the replicator to consume the message received from the other peer.

 @param message The message.
 */
- (void) receive: (CBLMessage*)message;

@end

NS_ASSUME_NONNULL_END
