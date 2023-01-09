//
// User.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Newtonsoft.Json;

namespace P2PListSync.Models
{
    public class User
    {
        [JsonProperty(PropertyName = "name")]
        public string Username { get; set; }

        [JsonProperty(PropertyName = "password")]
        public string Password { get; set; }
    }
}
