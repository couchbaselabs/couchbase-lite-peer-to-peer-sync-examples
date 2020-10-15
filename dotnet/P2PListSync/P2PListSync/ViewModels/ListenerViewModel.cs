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
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Windows.Input;
using Xamarin.Forms;

namespace P2PListSync.ViewModels
{
    public class ListenerViewModel : BaseViewModel
    {
        const string ListenerCommonName = "com.example.list-sync-server";
        const string ListenerCertLabel = "list-sync-server-cert-label";
        const string ListenerCertKeyP12File = "listener-cert-pkey";
        const string ListenerCertKeyExportPassword = "couchbase";

        //Listener
        private URLEndpointListener _urlEndpointListener;

        private X509Store _store;

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

            _store = new X509Store(StoreName.My);

            StartListenerCommand = new Command(() => ExecuteStartListenerCommand());

            TLSIdentity.DeleteIdentity(_store, ListenerCertLabel, null);
        }

        private void ExecuteStartListenerCommand()
        {
            if (!IsListening) {
                try {
                    CreateListener();
                    //tag::StartListener[]
                    _urlEndpointListener.Start();
                    //end::StartListener[]
                } catch (Exception ex) {
                    Debug.WriteLine($"Fail starting listener : {ex}");
                    return;
                }

                Broadcast();
                IsListening = true;
                ListenerStatus = $"Listening on {_urlEndpointListener.Urls[0]}";
            } else {
                //tag::StopListener[]
                _urlEndpointListener.Stop();
                //end::StopListener[]
                IsListening = false;
                ListenerStatus = "";
            }
        }

        internal void CreateListener()
        {
            //tag::InitListener[]
            var listenerConfig = new URLEndpointListenerConfiguration(_db);
            listenerConfig.Port = 0; // Dynamic port
            TLSIdentity tlsId = null;

            switch (CoreApp.ListenerTLSMode) {
                //tag::TLSDisabled[]
                case LISTENER_TLS_MODE.DISABLED:
                    listenerConfig.DisableTLS = true;
                    listenerConfig.TlsIdentity = tlsId;
                    //end::TLSDisabled[]
                    break;
                //tag::TLSWithAnonymousAuth[]
                case LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH:
                    listenerConfig.DisableTLS = false; // Use with anonymous self signed cert if TlsIdentity is null
                    listenerConfig.TlsIdentity = tlsId;
                    //end::TLSWithAnonymousAuth[]
                    break;
                //tag::TLSWithBundledCert[]
                case LISTENER_TLS_MODE.WITH_BUNDLED_CERT:
                    tlsId = ImportTLSIdentityFromPkc12(ListenerCertLabel);
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = tlsId;
                    //end::TLSWithBundledCert[]
                    break;
                //tag::TLSWithGeneratedSelfSignedCert[]
                case LISTENER_TLS_MODE.WITH_GENERATED_SELF_SIGNED_CERT:
                    tlsId = CreateIdentityWithCertLabel(ListenerCertLabel);
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = tlsId;
                    //end::TLSWithGeneratedSelfSignedCert[]
                    break;
            }

            listenerConfig.EnableDeltaSync = true;

            if (CoreApp.RequiresUserAuth) {
                listenerConfig.Authenticator = new ListenerPasswordAuthenticator((sender, username, password) =>
                {
                    // ** This is only a sample app to use an existing users credential shared cross platforms.
                    //    Developers should use SecureString password properly.
                    var found = CoreApp.AllowedUsers.Where(u => username == u.Username && new NetworkCredential(string.Empty, password).Password == u.Password).SingleOrDefault();
                    return found != null;
                });
            }

            _urlEndpointListener = new URLEndpointListener(listenerConfig);
            //end::InitListener[]
        }

        #region Server TLSIdentity
        internal TLSIdentity ImportTLSIdentityFromPkc12(string label)
        {
            // Check if identity exists, use the id if it is.
            var id = TLSIdentity.GetIdentity(_store, label, null);
            if(id != null) {
                return id;
            }

            try {
                byte[] data = null;
                using (var stream = ResourceLoader.GetEmbeddedResourceStream(typeof(ListenerViewModel).GetTypeInfo().Assembly, $"{ListenerCertKeyP12File}.p12")) {
                    using (var reader = new BinaryReader(stream)) {
                        data = reader.ReadBytes((int)stream.Length);
                    }
                }

                id = TLSIdentity.ImportIdentity(_store, data, ListenerCertKeyExportPassword, label, null);
            } catch (Exception ex) {
                Debug.WriteLine($"Error while loading self signed cert : {ex}");
            }

            return id;
        }

        internal TLSIdentity CreateIdentityWithCertLabel(string label)
        {
            // Check if identity exists, use the id if it is.
            var id = TLSIdentity.GetIdentity(_store, label, null);
            if (id != null) {
                return id;
            }

            try {
                id = TLSIdentity.CreateIdentity(true,
                new Dictionary<string, string>() { { Certificate.CommonNameAttribute, ListenerCommonName } },
                null,
                _store,
                label,
                null);
            } catch (Exception ex) {
                Debug.WriteLine($"Error while creating self signed cert : {ex}");
            }

            return id;
        }
        #endregion

        #region Broadcast
        //tag::StartAdvertiser
        public void Broadcast()
        {
            using (var socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, System.Net.Sockets.ProtocolType.Udp)) {
                socket.EnableBroadcast = true;
                var group = new IPEndPoint(IPAddress.Broadcast, CoreApp.UdpPort);
                var hi = Encoding.ASCII.GetBytes($"{CoreApp.Guid}:{_urlEndpointListener.Urls[0].Host}:{_urlEndpointListener.Port}");
                socket.SendTo(hi, group);

                socket.Close();
            }
        }
        //end::StartAdvertiser
        #endregion
    }
}
