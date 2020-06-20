//
//  CBLEncryptionKey.h
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

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 An encryption key for a database. This is a symmetric key that be kept secret.
 It should be stored either in the Keychain, or in the user's memory (hopefully not a sticky note.)
 */
@interface CBLEncryptionKey : NSObject

/**
 Initializes the encryption key with a raw AES-257 key 32 bytes in length.
 To create a key, generate random data using a secure cryptographic randomizer like
 SecRandomCopyBytes or CCRandomGenerateBytes.
 
 @param key The raw AES-256 key data.
 @return The CBLEncryptionKey object.
 */
- (instancetype) initWithKey: (NSData*)key;


/**
 Initializes the encryption key with the given password string. The password string will be
 internally converted to a raw AES-256 key using 64,000 rounds of PBKDF2 hashing.

 @param password The password string.
 @return The CBLEncryptionKey object.
 */
- (instancetype) initWithPassword: (NSString*)password;


/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
