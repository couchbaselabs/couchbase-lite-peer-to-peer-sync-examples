The complete C#/.Net code samples from which these samples are extracted can be found in the /dotnet directory at the top-level of this repo.
Extracted Date: Fri Feb  4 09:24:13 GMT 2022

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/.NETStandard,Version=v2.0.AssemblyAttributes.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/P2PListSync.AssemblyInfo.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/App.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/ListenerPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/MenuPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/LoginPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/SeasonalItemsPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/ListenersBrowserPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/MainPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.0/Views/SettingsPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/.NETStandard,Version=v2.1.AssemblyAttributes.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/P2PListSync.AssemblyInfo.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/App.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/ListenerPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/MenuPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/LoginPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/SeasonalItemsPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/ListenersBrowserPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/MainPage.xaml.g.cs
//

//
// Tags from P2PListSync/obj/Debug/netstandard2.1/Views/SettingsPage.xaml.g.cs
//

//
// Tags from P2PListSync/ViewModels/ListenerViewModel.cs
//
                    //tag::StartListener[]
                    _urlEndpointListener.Start();
                    //end::StartListener[]
                //tag::StopListener[]
                _urlEndpointListener.Stop();
                _urlEndpointListener.Dispose();
                //end::StopListener[]
            //tag::InitListener[]
            var listenerConfig = new URLEndpointListenerConfiguration(_db); // <1>
            listenerConfig.NetworkInterface = GetLocalIPv4(NetworkInterfaceType.Wireless80211) ?? GetLocalIPv4(NetworkInterfaceType.Ethernet);
            //listenerConfig.Port = 0; // Dynamic port
            listenerConfig.Port = 35262; // Fixed port

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
                //tag::TLSDisabled[]
                case LISTENER_TLS_MODE.DISABLED:
                    listenerConfig.DisableTLS = true;
                    listenerConfig.TlsIdentity = null;
                    //end::TLSDisabled[]
                //tag::TLSWithAnonymousAuth[]
                case LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH:
                    listenerConfig.DisableTLS = false; // Use with anonymous self signed cert if TlsIdentity is null
                    listenerConfig.TlsIdentity = null;
                    //end::TLSWithAnonymousAuth[]
                //tag::TLSWithBundledCert[]
                case LISTENER_TLS_MODE.WITH_BUNDLED_CERT:
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = ImportTLSIdentityFromPkc12(ListenerCertLabel);
                    //end::TLSWithBundledCert[]
                //tag::TLSWithGeneratedSelfSignedCert[]
                case LISTENER_TLS_MODE.WITH_GENERATED_SELF_SIGNED_CERT:
                    listenerConfig.DisableTLS = false;
                    listenerConfig.TlsIdentity = CreateIdentityWithCertLabel(ListenerCertLabel);
                    //end::TLSWithGeneratedSelfSignedCert[]
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

//
// Tags from P2PListSync/ViewModels/ListenersBrowserViewModel.cs
//
        //tag::StartBrowsing[]
        {
            Title = "Browser";
            Items = new ObservableCollection<ReplicatorItem>();

            _discovery = new UdpListener(CoreApp.UdpPort);
            _discovery.UdpPacketReceived += DiscoveryOnUdpPacketReceived;
            _discovery.Start();
        }

        #region discover event
        private void DiscoveryOnUdpPacketReceived(object sender, UdpPacketReceivedEventArgs args)
        {
            var msg = Encoding.ASCII.GetString(args.Data);
            var msgArr = msg.Split(':');
            var remoteId = Guid.Parse(msgArr[0]);
            if (remoteId == CoreApp.Guid) return;
            var remoteIP = IPAddress.Parse(msgArr[1]);
            var remotePort = Int32.Parse(msgArr[2]);
            var remoteEndpoint = new IPEndPoint(remoteIP, remotePort);

            AddReplicator(remoteEndpoint);
        }
        //end::StartBrowsing[]

//
// Tags from P2PListSync/ViewModels/SettingsViewModel.cs
//

//
// Tags from P2PListSync/ViewModels/SeasonalItemsViewModel.cs
//
            //tag::LoadData[]
            var q = QueryBuilder.Select(SelectResult.All())
                .From(DataSource.Database(_db))
                .Where(Meta.ID.EqualTo(Expression.String(CoreApp.DocId)))
                .AddChangeListener((sender, args) =>
                {
                    var allResult = args.Results.AllResults();
                    var result = allResult[0];
                    var dict = result[CoreApp.DB.Name].Dictionary;
                    var arr = dict.GetArray(CoreApp.ArrKey);

                    if (arr.Count < Items.Count)
                        Items = new ObservableConcurrentDictionary<int, SeasonalItem>();

                    Parallel.For(0, arr.Count, i =>
                    {
                        var item = arr[i].Dictionary;
                        var name = item.GetString("key");
                        var cnt = item.GetInt("value");
                        var image = item.GetBlob("image");

                        if (_items.ContainsKey(i)) {
                            _items[i].Name = name;
                            _items[i].Quantity = cnt;
                            _items[i].ImageByteArray = image?.Content;
                        } else {
                            var seasonalItem = new SeasonalItem {
                                Index = i,
                                Name = name,
                                Quantity = cnt,
                                ImageByteArray = image?.Content
                            };

                            _items.Add(i, seasonalItem);
                        }

                    });
                });
            //end::LoadData[]

//
// Tags from P2PListSync/ViewModels/BaseViewModel.cs
//

//
// Tags from P2PListSync/App.xaml.cs
//

//
// Tags from P2PListSync/CoreApp.cs
//
        //tag::ListenerTLSTestMode[]
        public static LISTENER_TLS_MODE ListenerTLSMode = LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH;
        //end::ListenerTLSTestMode[]
        //tag::ListenerValidationTestMode[]
        public static LISTENER_CERT_VALIDATION_MODE ListenerCertValidationMode = LISTENER_CERT_VALIDATION_MODE.SKIP_VALIDATION;
        //end::ListenerValidationTestMode[]
            //tag::OpenOrCreateDatabase[]
            if (!Database.Exists(DbName, DBPath)) {
                using (var dbZip = new ZipArchive(ResourceLoader.GetEmbeddedResourceStream(typeof(CoreApp).GetTypeInfo().Assembly, $"{DbName}.cblite2.zip"))) {
                    dbZip.ExtractToDirectory(DBPath);
                }
            }

            DB = new Database(DbName, new DatabaseConfiguration() { Directory = DBPath });
            //end::OpenOrCreateDatabase[]

//
// Tags from P2PListSync/Utils/ByteArrayToImageConverter.cs
//

//
// Tags from P2PListSync/Utils/ResourceLoader.cs
//

//
// Tags from P2PListSync/Utils/EmbeddedImageResourceExtension.cs
//

//
// Tags from P2PListSync/Utils/FuncConcurrentQueue.cs
//

//
// Tags from P2PListSync/Utils/ObservableConcurrentDictionary.cs
//

//
// Tags from P2PListSync/Models/HomeMenuItem.cs
//

//
// Tags from P2PListSync/Models/User.cs
//

//
// Tags from P2PListSync/Models/TLSSettingsData.cs
//

//
// Tags from P2PListSync/Models/ConnectedPeer.cs
//

//
// Tags from P2PListSync/Models/SeasonalItem.cs
//

//
// Tags from P2PListSync/Models/ReplicatorItem.cs
//
        //tag::StartReplication[]
        public ReplicatorItem(IPEndPoint listenerEndpoint)
        {
            _listenerEndpoint = listenerEndpoint;
            StartReplicatorCommand = new Command(() => ExecuteStartReplicatorCommand());
            CreateReplicator(ListenerEndpointString);
        }

        ~ReplicatorItem()
        {
            Dispose(disposing: false);
        }
        #endregion

        public void CreateReplicator(string PeerEndpointString)
        {
            if(_repl != null) {
                return;
            }

            Uri host = new Uri(PeerEndpointString);
            var dbUrl = new Uri(host, _db.Name);
            var replicatorConfig = new ReplicatorConfiguration(_db, new URLEndpoint(dbUrl)); // <1>
            replicatorConfig.ReplicatorType = ReplicatorType.PushAndPull;
            replicatorConfig.Continuous = true;

            if (CoreApp.ListenerTLSMode > 0) {

                // Explicitly allows self signed certificates. By default, only
                // CA signed cert is allowed
                switch (CoreApp.ListenerCertValidationMode) { // <2>
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
                replicatorConfig.Authenticator = new BasicAuthenticator(user.Username, user.Password); // <3>
            }

            _repl = new Replicator(replicatorConfig); // <4>
            _listenerToken = _repl.AddChangeListener(ReplicationStatusUpdate);
        }

        public void ExecuteStartReplicatorCommand()
        {
            if (!IsStarted) {
                _repl.Start(); // <5>
                //end::StartReplication[]
            //tag::StopReplication[]
            _repl?.Stop();
            //end::StopReplication[]

//
// Tags from P2PListSync/P2P/UdpListener.cs
//

//
// Tags from P2PListSync/Views/MenuPage.xaml.cs
//

//
// Tags from P2PListSync/Views/SeasonalItemsPage.xaml.cs
//

//
// Tags from P2PListSync/Views/SettingsPage.xaml.cs
//

//
// Tags from P2PListSync/Views/ListenersBrowserPage.xaml.cs
//

//
// Tags from P2PListSync/Views/ListenerPage.xaml.cs
//

//
// Tags from P2PListSync/Views/MainPage.xaml.cs
//

//
// Tags from P2PListSync/Views/LoginPage.xaml.cs
//

//
// Tags from P2PListSync/Services/IPhotoPickerService.cs
//
