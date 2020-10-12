//
// ListenerViewModel.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite;
using Couchbase.Lite.P2P;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Windows.Input;
using Xamarin.Forms;

namespace P2PListSync.ViewModels
{
    public class ListenerViewModel : BaseViewModel
    {
        //Listener
        URLEndpointListener _urlEndpointListener;

        //db
        private Database _db = CoreApp.DB;

        private bool _isListening = false;
        public bool IsListening
        {
            set {
                if (SetProperty(ref _isListening, value)) {
                    OnPropertyChanged("ListenerButtonText");
                }
            }
            get { return _isListening; }
        }

        public string ListenerButtonText
        {
            get { return IsListening == true ? "Stop Listener" : "Start Listener"; }
        }

        string _listenerStatus = string.Empty;
        public string ListenerStatus
        {
            get { return _listenerStatus; }
            set { SetProperty(ref _listenerStatus, value); }
        }

        public ICommand StartListenerCommand { get; }

        public ListenerViewModel()
        {
            Title = "Listener";

            StartListenerCommand = new Command(() => ExecuteStartListenerCommand());
        }

        private void ExecuteStartListenerCommand()
        {
            if (!IsListening) {
                var config = new URLEndpointListenerConfiguration(_db);
                config.Port = (ushort)CoreApp.PeerPort;
                config.DisableTLS = true;

                _urlEndpointListener = new URLEndpointListener(config);
                _urlEndpointListener.Start();
                Broadcast();
                IsListening = true;
                ListenerStatus = $"Listening on ws://localhost:{_urlEndpointListener.Port}/{_db.Name}";
            } else {
                _urlEndpointListener.Stop();
                IsListening = false;
                ListenerStatus = "";
            }
        }

        #region Broadcast
        public void Broadcast()
        {
            using (var socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, System.Net.Sockets.ProtocolType.Udp)) {
                socket.EnableBroadcast = true;
                var group = new IPEndPoint(IPAddress.Broadcast, CoreApp.UdpPort);
                var host = Dns.GetHostEntry(Dns.GetHostName());
                foreach (var ip in host.AddressList) {
                    if (ip.AddressFamily == AddressFamily.InterNetwork) {
                        var hi = Encoding.ASCII.GetBytes(CoreApp.Guid + ":" + ip + ":" + CoreApp.PeerPort);
                        socket.SendTo(hi, group);
                    }
                }
                socket.Close();
            }
        }
        #endregion
    }
}
