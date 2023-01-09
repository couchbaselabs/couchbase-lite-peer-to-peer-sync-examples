//
// ConnectedPeer.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using System.Net;

namespace P2PListSync.Models
{
    public class ConnectedPeer
    {
        public IPEndPoint PeerEndPoint { get; set; }
        public string PeerEndpointString { get; set; }
        public string Status { get; set; }
    }
}
