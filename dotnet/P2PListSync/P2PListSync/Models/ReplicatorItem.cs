//
// ReplicatorViewModel.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite;
using Couchbase.Lite.Sync;
using P2PListSync.ViewModels;
using System;
using System.Net;
using Xamarin.Forms;

namespace P2PListSync.Models
{
    public class ReplicatorItem : BaseViewModel
    {
        private Database _db = CoreApp.DB;
        private ListenerToken listenerToken;
        private Replicator _repl;

        public Command StartReplicatorCommand { get; set; }

        IPEndPoint _listenerEndpoint;
        public IPEndPoint ListenerEndpoint
        {
            get { return _listenerEndpoint; }
            set {
                if (SetProperty(ref _listenerEndpoint, value)) {
                    OnPropertyChanged("ListenerEndpointString");
                }
            }
        }

        public string ListenerEndpointString
        {
            get { return $"ws://{ListenerEndpoint}/{CoreApp.DB.Name}"; }
        }

        private bool _isStarted = false;
        public bool IsStarted
        {
            get { return _isStarted; }
            set { SetProperty(ref _isStarted, value); }
        }

        string _connectionStatus = "DISCONNECTED";
        public string ConnectionStatus
        {
            get { return _connectionStatus; }
            set { SetProperty(ref _connectionStatus, value); }
        }

        Color _connectionStatusColor = Color.Black;
        public Color ConnectionStatusColor
        {
            get { return _connectionStatusColor; }
            set { SetProperty(ref _connectionStatusColor, value); }
        }

        public ReplicatorItem(IPEndPoint listenerEndpoint)
        {
            _listenerEndpoint = listenerEndpoint;
            StartReplicatorCommand = new Command(() => ExecuteStartReplicatorCommand());
            CreateReplicator(ListenerEndpointString);
        }

        public void CreateReplicator(string PeerEndpointString)
        {
            if(_repl != null) {
                return;
            }

            Uri host = new Uri(PeerEndpointString);
            var dbUrl = new Uri(host, _db.Name);
            var replicatorConfig = new ReplicatorConfiguration(_db, new URLEndpoint(dbUrl));
            replicatorConfig.ReplicatorType = ReplicatorType.PushAndPull;
            replicatorConfig.Continuous = true;

            _repl = new Replicator(replicatorConfig);
        }

        public void ExecuteStartReplicatorCommand()
        {
            if (!IsStarted) {
                listenerToken = _repl.AddChangeListener(ReplicationStatusUpdate);

                _repl.Start();
                IsStarted = true;
            } else {
                _repl?.RemoveChangeListener(listenerToken);
                _repl?.Stop();

                IsStarted = false;

                ConnectionStatus = "DISCONNECTED";
                ConnectionStatusColor = Color.Black;
            }
        }

        private void ReplicationStatusUpdate(object sender, ReplicatorStatusChangedEventArgs args)
        {
            //The replication is finished or hit a fatal error.
            if (args.Status.Activity == ReplicatorActivityLevel.Stopped) {
                ConnectionStatus = "DISCONNECTED";
                ConnectionStatusColor = Color.Black;
            }//The replicator is offline as the remote host is unreachable.
            else if (args.Status.Activity == ReplicatorActivityLevel.Offline) {
                ConnectionStatus = "DISCONNECTED";
                ConnectionStatusColor = Color.Black;
            } //The replicator is connecting to the remote host.
            else if (args.Status.Activity == ReplicatorActivityLevel.Connecting) {
                ConnectionStatus = "CONNECTING";
                ConnectionStatusColor = Color.Red;
            } //The replication caught up with all the changes available from the server.
              //The IDLE state is only used in continuous replications.
            else if (args.Status.Activity == ReplicatorActivityLevel.Idle) {
                ConnectionStatus = "CONNECTED";
                ConnectionStatusColor = Color.Green;
            } //The replication is actively transferring data.
            else if (args.Status.Activity == ReplicatorActivityLevel.Busy) {
                ConnectionStatus = "BUSY";
                ConnectionStatusColor = Color.Green;
            }
        }
    }
}
