//
// - UdpListener.cs
// 
// Author:
//     Lucas Ontivero <lucasontivero@gmail.com>
// 
// Copyright 2013 Lucas E. Ontivero
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//  http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 

using P2PListSync.Utils;
using System;
using System.Net;
using System.Net.Sockets;

namespace P2PListSync.P2P
{
    public enum ListenerStatus
    {
        Listening,
        Stopped
    }

    // Please note UDP broadcast only works on actual android device, not on emulator. See https://stackoverflow.com/questions/60420560/unable-to-receive-udp-broadcast-in-xamarin-forms-app for detail.
    public class UdpListener
    {
        /// <summary>
        /// Occurs when discovered node.
        /// </summary>
        public event EventHandler<UdpPacketReceivedEventArgs> UdpPacketReceived;

        private static readonly FuncConcurrentQueue<SocketAsyncEventArgs> ConnectSaeaPool =
            new FuncConcurrentQueue<SocketAsyncEventArgs>(() =>
            {
                var e = new SocketAsyncEventArgs();
                return e;
            });

        protected IPEndPoint EndPoint { get; set; }
        protected Socket Listener { get; set; }
        private int Port { get; }
        private ListenerStatus Status { get; set; }

        /// <summary>
        /// Initializes a new instance of the <see cref="UdpListener"/> class.
        /// </summary>
        /// <param name='port'>
        /// Port.
        /// </param>
        public UdpListener(int port)
        {
            Port = port;
            EndPoint = new IPEndPoint(IPAddress.Any, port);
            Status = ListenerStatus.Stopped;
        }

        public void Start()
        {
            try {
                Listener = CreateSocket();
                Status = ListenerStatus.Listening;

                Listen();
            } catch (SocketException) {
                if (Listener == null) return;
                Stop();
                throw;
            }
        }

        public void Stop()
        {
            Status = ListenerStatus.Stopped;
            if (Listener != null) {
                Listener.Close();
                Listener = null;
            }
        }

        protected Socket CreateSocket()
        {
            var socket = new Socket(EndPoint.AddressFamily, SocketType.Dgram, ProtocolType.Udp);
            socket.Bind(EndPoint);
            return socket;
        }

        protected bool ListenAsync(SocketAsyncEventArgs saea)
        {
            var bufferSize = (Guid.Empty + ":127.127.127.127:0000").Length;
            saea.SetBuffer(new byte[bufferSize], 0, bufferSize);
            saea.RemoteEndPoint = new IPEndPoint(IPAddress.Any, Port);
            return Listener.ReceiveFromAsync(saea);
        }

        protected void Notify(SocketAsyncEventArgs saea)
        {
            var endPoint = saea.RemoteEndPoint as IPEndPoint;
            Raise(UdpPacketReceived, this, new UdpPacketReceivedEventArgs(endPoint, saea.Buffer));
        }

        private void Raise<T>(EventHandler<T> handler, object sender, T args) where T : System.EventArgs
        {
            handler?.Invoke(sender, args);
        }

        private void Listen()
        {
            var saea = ConnectSaeaPool.Take();
            saea.AcceptSocket = null;
            saea.Completed += IOCompleted;
            if (Status == ListenerStatus.Stopped) return;

            var async = ListenAsync(saea);

            if (!async) {
                IOCompleted(null, saea);
            }
        }

        private void IOCompleted(object sender, SocketAsyncEventArgs saea)
        {
            try {
                if (saea.SocketError == SocketError.Success) {
                    Notify(saea);
                }
            } finally {
                saea.Completed -= IOCompleted;
                ConnectSaeaPool.Add(saea);
                if (Listener != null) Listen();
            }
        }
    }

    public class UdpPacketReceivedEventArgs : System.EventArgs
    {
        private readonly IPEndPoint _endpoint;
        private readonly byte[] _data;

        public UdpPacketReceivedEventArgs(IPEndPoint endpoint, byte[] data)
        {
            _endpoint = endpoint;
            _data = data;
        }

        public IPEndPoint EndPoint
        {
            get { return _endpoint; }
        }

        public byte[] Data
        {
            get { return _data; }
        }
    }
}
