//
//  CBLMessageEndpoint.h
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


#import "CBLEndpoint.h"
#import "CBLProtocolType.h"
@protocol CBLMessageEndpointConnection;
@protocol CBLMessageEndpointDelegate;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 Message endpoint.
 */
@interface CBLMessageEndpoint : NSObject <CBLEndpoint>

/**
 The unique identifier of the endpoint.
 */
@property (nonatomic, readonly, copy) NSString* uid;

/**
 The target used for storing any arbitary value representing the endpoint.
 */
@property (nonatomic, readonly, nullable) id target;

/**
 The data transportation protocol.
 */
@property (nonatomic, readonly) CBLProtocolType protocolType;

/**
 The delegate for creating CBLMessageEndpointConnection object.
 */
@property (nonatomic, weak, readonly) id<CBLMessageEndpointDelegate> delegate;

/**
 Initializes a CBLMessageEndpoint object.

 @param uid The unique identifier of the endpoint.
 @param target An optional arbitrary object that represents the endpoint.
 @param protocolType The data transportation protocol.
 @param delegate The delegate for creating CBLMessageEndpointConnection objects.
 @return The CBLMessageEndpoint object.
 */
- (instancetype) initWithUID: (NSString*)uid
                      target: (nullable id)target
                protocolType: (CBLProtocolType)protocolType
                    delegate: (id<CBLMessageEndpointDelegate>)delegate;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

/**
 ENTERPRISE EDITION ONLY.
 
 A delegate used by the replicator to create CBLMessageEndpointConnection objects.
 */
@protocol CBLMessageEndpointDelegate

/**
 Creates an object of type CBLMessageEndpointConnection protocol.
 An application implements the CBLMessageEndpointConnection protocol using a
 custom transportation method such as using the MultipeerConnectivity framework
 to exchange replication data with the endpoint.
 
 @param endpoint The endpoint object.
 @return The CBLMessageEndpointConnection object.
 */
- (id <CBLMessageEndpointConnection>) createConnectionForEndpoint: (CBLMessageEndpoint*)endpoint;

@end

NS_ASSUME_NONNULL_END
