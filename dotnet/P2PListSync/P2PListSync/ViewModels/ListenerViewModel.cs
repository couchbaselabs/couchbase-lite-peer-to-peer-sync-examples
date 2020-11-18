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
using System.Net.NetworkInformation;
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
        #region Constants
        const string ListenerCommonName = "com.example.list-sync-server";
        const string ListenerCertLabel = "list-sync-server-cert-label";
        const string ListenerCertKeyP12File = "listener-cert-pkey";
        const string ListenerCertKeyExportPassword = "couchbase";
        #endregion

        #region Variables
        //Listener
        private URLEndpointListener _urlEndpointListener;

        private X509Store _store;

        //db
        private Database _db = CoreApp.DB;
        #endregion

        #region Properties
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
        public ICommand BroadcastCommand { get; }
        #endregion

        #region Constructor
        public ListenerViewModel()
        {
            Title = "Listener";

            StartListenerCommand = new Command(() => ExecuteStartListenerCommand());
            BroadcastCommand = new Command(() => Broadcast());

            using (_store = new X509Store(StoreName.My)) {
                TLSIdentity.DeleteIdentity(_store, ListenerCertLabel, null);
            }
        }
        #endregion

        private void ExecuteStartListenerCommand()
        {
            if (!IsListening) {
                try {
                    CreateListener();
                    //tag::StartListener[]
                    _urlEndpointListener.Start();
                    //end::StartListener[]
                    if (CoreApp.IsDebugging) {
                        PrintListener(_urlEndpointListener);
                    }
                } catch (Exception ex) {
                    Debug.WriteLine($"Fail starting listener : {ex}");
                    return;
                }

                IsListening = true;
                Broadcast();
                ListenerStatus = $"Listening on {_urlEndpointListener.Urls[0]}";
            } else {
                //tag::StopListener[]
                _urlEndpointListener.Stop();
                _urlEndpointListener.Dispose();
                //end::StopListener[]
                IsListening = false;
                ListenerStatus = "";
            }
        }

        internal void CreateListener()
        {
            //tag::InitListener[]
            var listenerConfig = new URLEndpointListenerConfiguration(_db); // <1>
            listenerConfig.NetworkInterface = GetLocalIPv4(NetworkInterfaceType.Wireless80211) ?? GetLocalIPv4(NetworkInterfaceType.Ethernet);
            listenerConfig.Port = 0; // Dynamic port

            switch (CoreApp.ListenerTLSMode) { // <2>
                //tag::TLSDisabled[]
                case LISTENER_TLS_MODE.DISABLED:
                    listenerConfig.DisableTLS = true;
                    listenerConfig.TlsIdentity = null;
                    //end::TLSDisabled[]
                    break;
                //tag::TLSWithAnonymousAuth[]
                case LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH:
                    listenerConfig.DisableTLS = false; // Use with anonymous self signed cert if TlsIdentity is null
                    listenerConfig.TlsIdentity = null;
                    //end::TLSWithAnonymousAuth[]
                    break;
                //tag::TLSWithBundledCert[]
                case LISTENER_TLS_MODE.WITH_BUNDLED_CERT:
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = ImportTLSIdentityFromPkc12(ListenerCertLabel);
                    //end::TLSWithBundledCert[]
                    break;
                //tag::TLSWithGeneratedSelfSignedCert[]
                case LISTENER_TLS_MODE.WITH_GENERATED_SELF_SIGNED_CERT:
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = CreateIdentityWithCertLabel(ListenerCertLabel);
                    //end::TLSWithGeneratedSelfSignedCert[]
                    break;
            }

            listenerConfig.EnableDeltaSync = true; // <3>

            if (CoreApp.RequiresUserAuth) { // <4>
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

        internal string GetConnectedPeers()
        {
            if (!IsListening) {
                return null;
            }

            var status = _urlEndpointListener.Status;
            return $"There are {status.ConnectionCount} Connectioned clients of which {status.ActiveConnectionCount} are active.";
        }

        #region Server TLSIdentity
        internal TLSIdentity ImportTLSIdentityFromPkc12(string label)
        {
            using (_store = new X509Store(StoreName.My)) {
                // Check if identity exists, use the id if it is.
                var id = TLSIdentity.GetIdentity(_store, label, null);
                if (id != null) {
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
        }

        internal TLSIdentity CreateIdentityWithCertLabel(string label)
        {
            using (_store = new X509Store(StoreName.My)) {
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
        }
        #endregion

        #region Broadcast
        //tag::StartAdvertiser[]
        public void Broadcast()
        {
            if (!IsListening)
                return;

            using (var socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, System.Net.Sockets.ProtocolType.Udp)) {
                socket.EnableBroadcast = true;
                var group = new IPEndPoint(IPAddress.Broadcast, CoreApp.UdpPort);
                var hi = Encoding.ASCII.GetBytes($"{CoreApp.Guid}:{_urlEndpointListener.Urls[0].Host}:{_urlEndpointListener.Port}");
                socket.SendTo(hi, group);

                socket.Close();
            }
        }
        //end::StartAdvertiser[]
        #endregion

        private string GetLocalIPv4(NetworkInterfaceType type)
        {
            string output = null;
            foreach (NetworkInterface item in NetworkInterface.GetAllNetworkInterfaces())
            {
                if (item.NetworkInterfaceType == type && item.OperationalStatus == OperationalStatus.Up) {
                    IPInterfaceProperties adapterProperties = item.GetIPProperties();
                    if (adapterProperties.GatewayAddresses.FirstOrDefault() != null) {
                        foreach (UnicastIPAddressInformation ip in adapterProperties.UnicastAddresses) {
                            if (ip.Address.AddressFamily == AddressFamily.InterNetwork) {
                                output = ip.Address.ToString();
                                break;
                            }
                        }
                    }
                }

                if (output != null) { break; }
            }

            return output;
        }

        private void PrintListener(URLEndpointListener listener)
        {
            Debug.WriteLine("Listener Config TLSIdentity info: ");
            if (listener?.Config.TlsIdentity == null) {
                Debug.WriteLine("No TLSIdentity to print.");
            } else {
                PrintTLSIdentity(listener.Config.TlsIdentity);
            }

            Debug.WriteLine("Listener TLSIdentity info: ");
            if (listener?.TlsIdentity == null) {
                Debug.WriteLine("No TLSIdentity to print.");
            } else {
                PrintTLSIdentity(listener.TlsIdentity);
            }
        }

        private void PrintTLSIdentity(TLSIdentity id)
        {
            var certs = id.Certs;
            if(certs == null) {
                Debug.WriteLine("No certs to print.");
                return;
            }

            foreach(var x509 in certs) {
                //Print to console information contained in the certificate.
                Debug.WriteLine("{0}Subject: {1}{0}", Environment.NewLine, x509.Subject);
                Debug.WriteLine("{0}Issuer: {1}{0}", Environment.NewLine, x509.Issuer);
                Debug.WriteLine("{0}Version: {1}{0}", Environment.NewLine, x509.Version);
                Debug.WriteLine("{0}Valid Date: {1}{0}", Environment.NewLine, x509.NotBefore);
                Debug.WriteLine("{0}Expiry Date: {1}{0}", Environment.NewLine, x509.NotAfter);
                Debug.WriteLine("{0}Thumbprint: {1}{0}", Environment.NewLine, x509.Thumbprint);
                Debug.WriteLine("{0}Serial Number: {1}{0}", Environment.NewLine, x509.SerialNumber);
                Debug.WriteLine("{0}Friendly Name: {1}{0}", Environment.NewLine, x509.PublicKey.Oid.FriendlyName);
                Debug.WriteLine("{0}Public Key Format: {1}{0}", Environment.NewLine, x509.PublicKey.EncodedKeyValue.Format(true));
                Debug.WriteLine("{0}Raw Data Length: {1}{0}", Environment.NewLine, x509.RawData.Length);
                Debug.WriteLine("{0}Certificate to string: {1}{0}", Environment.NewLine, x509.ToString(true));
                //Debug.WriteLine("{0}Certificate to XML String: {1}{0}", Environment.NewLine, x509.PublicKey.Key.ToXmlString(false));
            }
        }
    }
}
