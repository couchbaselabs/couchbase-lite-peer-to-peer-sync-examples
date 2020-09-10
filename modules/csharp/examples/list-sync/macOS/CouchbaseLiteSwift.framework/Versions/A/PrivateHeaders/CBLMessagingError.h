//
//  CBLMessagingError.h
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

/**
 ENTERPRISE EDITION ONLY.
 
 The messaging error.
 */
@interface CBLMessagingError : NSObject

/**
 The error object.
 */
@property (nonatomic, readonly) NSError* error;

/**
 The flag identifying whether the error is recoverable or not.
 */
@property (nonatomic, readonly) BOOL isRecoverable;

/**
 Initializes a CBLMessagingError object with the given error and the flag
 identifying if the error is recoverable or not. The replicator uses isRecoverable
 flag to determine whether the replication should be retried as the error is
 recoverable or stopped as the error is non-recoverable.

 @param error The error object.
 @param isRecoverable The flag identifying if the error is recoverable or not.
 @return The CBLMessagingError object.
 */
- (instancetype) initWithError: (NSError*)error isRecoverable: (BOOL)isRecoverable;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end
