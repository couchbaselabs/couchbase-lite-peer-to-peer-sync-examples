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
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Reflection;
using System.Security.Cryptography.X509Certificates;
using Xamarin.Forms;

namespace P2PListSync.Models
{
    public class ReplicatorItem : BaseViewModel
    {
        #region Constants
        const string ListenerPinnedCertFile = "listener-pinned-cert";
        #endregion

        #region Variables
        private Database _db = CoreApp.DB;
        private ListenerToken listenerToken;
        private Replicator _repl;
        #endregion

        #region Properties
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
        #endregion

        #region Constructor
        //tag::StartReplication[]
        public ReplicatorItem(IPEndPoint listenerEndpoint)
        {
            _listenerEndpoint = listenerEndpoint;
            StartReplicatorCommand = new Command(() => ExecuteStartReplicatorCommand());
            CreateReplicator(ListenerEndpointString);
        }
        #endregion

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

            if (CoreApp.ListenerTLSMode > 0) {

                // Explicitly allows self signed certificates. By default, only
                // CA signed cert is allowed
                switch (CoreApp.ListenerCertValidationMode) {
                    case LISTENER_CERT_VALIDATION_MODE.SKIP_VALIDATION:
                        // Use acceptOnlySelfSignedServerCertificate set to true to only accept self signed certs.
                        // There is no cert validation
                        replicatorConfig.AcceptOnlySelfSignedServerCertificate = true;
                        break;

                    case LISTENER_CERT_VALIDATION_MODE.ENABLE_VALIDATION_WITH_CERT_PINNING:
                        // Use acceptOnlySelfSignedServerCertificate set to false to only accept CA signed certs
                        // Self signed certs will fail validation

                        replicatorConfig.AcceptOnlySelfSignedServerCertificate = false;

                        // Enable cert pinning to only allow certs that match pinned cert

                        try {
                            var pinnedCert = LoadSelfSignedCertForListenerFromBundle();
                            replicatorConfig.PinnedServerCertificate = pinnedCert;
                        } catch (Exception ex) {
                            Debug.WriteLine($"Failed to load server cert to pin. Will proceed without pinning. {ex}");
                        }

                        break;

                    case LISTENER_CERT_VALIDATION_MODE.ENABLE_VALIDATION:
                        // Use acceptOnlySelfSignedServerCertificate set to false to only accept CA signed certs
                        // Self signed certs will fail validation. There is no cert pinning
                        replicatorConfig.AcceptOnlySelfSignedServerCertificate = false;
                        break;
                }
            }

            if (CoreApp.RequiresUserAuth) {
                var user = CoreApp.CurrentUser;
                replicatorConfig.Authenticator = new BasicAuthenticator(user.Username, user.Password);
            }

            _repl = new Replicator(replicatorConfig);
        }

        public void ExecuteStartReplicatorCommand()
        {
            if (!IsStarted) {
                listenerToken = _repl.AddChangeListener(ReplicationStatusUpdate);

                _repl.Start();
                //end::StartReplication[]
                IsStarted = true;
            } else {
                StopReplicator();
                ConnectionStatus = "DISCONNECTED";
                ConnectionStatusColor = Color.Black;
            }
        }

        public void RemoveReplicator()
        {
            if (IsStarted) {
                StopReplicator();
            }
            
            _repl.Dispose();
        }

        private void StopReplicator()
        {
            //tag::StopReplication[]
            _repl?.RemoveChangeListener(listenerToken);
            _repl?.Stop();
            //end::StopReplication[]

            IsStarted = false;
        }

        private X509Certificate2 LoadSelfSignedCertForListenerFromBundle()
        {
            using (var cert = ResourceLoader.GetEmbeddedResourceStream(typeof(ListenerViewModel).GetTypeInfo().Assembly, $"{ListenerPinnedCertFile}.cer")) {
                using (var ms = new MemoryStream()) {
                    cert.CopyTo(ms);
                    return new X509Certificate2(ms.ToArray());
                }
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
