//
// CommunicationManager.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite.P2P;
using System.Net;

namespace P2PListSync.P2P
{
    /// <summary>
    /// CommunicationManager is one of the most important classes in the CBP2P class library given it provides 
    /// the main API functionalities, these are the four methods: Connect, Disconnect, Send and Receive.
    /// </summary>
    public class CommunicationManager
    {
        private readonly URLEndpointListener _listener;

        //public readonly ConcurrentDictionary<IPEndPoint, NewConnectionEventArgs> Peers
        //    = new ConcurrentDictionary<IPEndPoint, NewConnectionEventArgs>();

        public delegate void PeerConnectedEvent(object sender, IPEndPoint peerIp);
        public event PeerConnectedEvent PeerConnected;

        internal delegate void PeerDisonnectedEvent(object sender, DisconnectionEventArgs disconnectArgs);
        internal event PeerDisonnectedEvent ConnectionClosed;

        internal delegate void PeerConnectionStatus(object sender, ConnectionEventArgs statusArgs);
        internal event PeerConnectionStatus ConnectionStatus;

        /// <summary>
        /// Initializes a new instance of the <see cref="CommunicationManager"/> class.
        /// </summary>
        internal CommunicationManager(URLEndpointListener listener)
        {
            _listener = listener;
            //_listener.PeerConnected += NewPeerConnected;
        }

        #region public method
        public void DisconnectPeer(IPEndPoint address)
        {
            //var peer = Peers.Where(x => x.Key.Equals(address)).SingleOrDefault();
            //var removePeerSuccess = Peers.TryRemove(address, out NewConnectionEventArgs args);
            //if (removePeerSuccess) {
            //    args.ConnectedPeer.Close();
            //    ConnectionClosed.Invoke(this, new DisconnectionEventArgs(address, true));
            //} else {
            //    ConnectionClosed.Invoke(this, new DisconnectionEventArgs(address, false));
            //}
        }
        #endregion

        //internal void NewPeerConnected(object sender, NewConnectionEventArgs args)
        //{
        //    var ipEndPoint = (IPEndPoint)args.ConnectedPeer.Client.RemoteEndPoint;
        //    var addPeerSuccess = Peers.TryAdd(ipEndPoint, args);
        //    if (addPeerSuccess) {
        //        PeerConnected.Invoke(this, ipEndPoint);
        //        args.TcpConnection.StatusUpdated += PeerStatusUpdate;
        //    }
        //}

        //private void PeerStatusUpdate(object sender, ReplicatorTcpConnection peerConnection)
        //{
        //    var peerEndpoint = peerConnection.PeerEndPoint;
        //    var status = peerConnection.ConnectionStatus;
        //    var statusStr = "";
        //    switch (status) {
        //        case STATUS.CLOSE:
        //            var removePeerSuccess = Peers.TryRemove(peerEndpoint, out NewConnectionEventArgs args);
        //            if (removePeerSuccess) {
        //                ConnectionClosed.Invoke(this, new DisconnectionEventArgs(peerEndpoint, true));
        //            } else {
        //                ConnectionClosed.Invoke(this, new DisconnectionEventArgs(peerEndpoint, false));
        //            }
        //            statusStr = "CLOSE";
        //            break;
        //        case STATUS.CONNECTING:
        //            statusStr = "CONNECTING";
        //            break;
        //        case STATUS.OPEN:
        //            statusStr = "OPEN";
        //            break;
        //        case STATUS.RECEIVE_MESSAGE:
        //            statusStr = "RECEIVE_MESSAGE";
        //            break;
        //        case STATUS.SEND_MESSAGE:
        //            statusStr = "SEND_MESSAGE";
        //            break;
        //        default:
        //            break;
        //    }
        //    ConnectionStatus.Invoke(this, new ConnectionEventArgs(peerEndpoint, statusStr));
        //}
    }

    internal class DisconnectionEventArgs : System.EventArgs
    {
        internal bool IsDisconnecting { get; set; }
        internal IPEndPoint Ip { get; set; }

        public DisconnectionEventArgs(IPEndPoint ip, bool disconnected)
        {
            Ip = ip;
            IsDisconnecting = disconnected;
        }
    }

    internal class ConnectionEventArgs : System.EventArgs
    {
        internal string Status { get; set; }
        internal IPEndPoint Ip { get; set; }

        public ConnectionEventArgs(IPEndPoint ip, string statusStr)
        {
            Ip = ip;
            Status = statusStr;
        }
    }
}
