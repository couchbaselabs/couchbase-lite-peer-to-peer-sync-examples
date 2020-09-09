<div id="header">

# Inventory Sample: Peer-to-Peer Sync

<div id="toc" class="toc2">

<div id="toctitle">Table of Contents</div>

*   [Overview](#overview)
*   [Introduction](#introduction)
*   [iOS Inventory App Tutorial in Swift](#ios-inventory-app-tutorial-in-swift)
*   [Prerequisites](#prerequisites)
*   [App Overview](#app-overview)
*   [App Installation](#app-installation)
    *   [Downloading .zip](#downloading-zip)
    *   [Try it Out](#try-it-out)
*   [Exploring the App Project](#exploring-the-app-project)
*   [Data Model](#data-model)
    *   [The "List" Document](#the-list-document)
    *   [Initializing Local Database](#initializing-local-database)
*   [Passive Peer or Server](#passive-peer-or-server)
    *   [Initializing Websockets Listener](#initializing-websockets-listener)
        *   [Testing Different TLS Modes](#testing-different-tls-modes)
    *   [Start Websockets Listener](#start-websockets-listener)
    *   [Advertising Listener Service](#advertising-listener-service)
    *   [Stop Websockets Listener](#stop-websockets-listener)
    *   [Try it out](#try-it-out-2)
*   [Active Peer or Client](#active-peer-or-client)
    *   [Discovering Listeners](#discovering-listeners)
    *   [Initializing and Starting Replication](#initializing-and-starting-replication)
        *   [Testing Different Server Validation Modes](#testing-different-server-validation-modes)
    *   [Stopping Replication](#stopping-replication)
    *   [Try it out](#try-it-out-3)
*   [Syncing Data](#syncing-data)
    *   [Try it out](#try-it-out-4)
*   [What Next](#what-next)
*   [Learn More](#learn-more)
    *   [Further Reading](#further-reading)

</div>

</div>

<div id="content">

<div class="sect1">

## Overview

<div class="sectionbody">

<div class="paragraph">

Sample apps that demonstate the out-of-box websockets listener based peer-to-peer functionality that is introduced in Couchbase Lite 2.8\. Currently, it includes an iOS swift sample application.

</div>

</div>

</div>

<div class="sect1">

## Introduction

<div class="sectionbody">

<div class="paragraph">

Couchbase Lite 2.8 release supports out-of-the-box support for secure [Peer-to-Peer Sync](https://ibsoln.github.io/stage/h2beta/couchbase-lite/2.8/swift/learn/swift-p2psync-websocket.html), over websockets, between Couchbase Lite enabled clients in IP-based networks without the need for a centralized control point.

</div>

<div class="admonitionblock important">

<table>

<tbody>

<tr>

<td class="icon"></td>

<td class="content">The sample app uses pre-release/beta release version of Couchbase Lite software</td>

</tr>

</tbody>

</table>

</div>

</div>

</div>

<div class="sect1">

## iOS Inventory App Tutorial in Swift

<div class="sectionbody">

<div class="paragraph">

This tutorial will demonstrate how to -

</div>

<div class="ulist">

*   Use [NetService](https://developer.apple.com/documentation/foundation/netservice) for peer discovery (i.e.to advertise and discover services/devices )

*   Configure a websockets listener to listen to incoming requests. We will walk through various TLS modes and client authentication modes.

*   Start a bi-directional replication from active peer.

*   Sync data between connected peers

</div>

<div class="paragraph">

Throughout this tutorial, the terms "passive peer" and "server" will be used interchangeably to refer to the peer on which the websockets listener is started. The "active peer" and "client" will be used interchageably to refer to the peer on which the replicator is initialized.

</div>

<div class="paragraph">

We will be using a simple inventory app in swift as an example to demonstrate the peer-to-peer functionality.

</div>

<div class="exampleblock">

<div class="content">

<div class="paragraph">

You can learn more about Couchbase Lite [here](https://docs.couchbase.com/couchbase-lite/2.7/introduction.html)

</div>

</div>

</div>

</div>

</div>

<div class="sect1">

## Prerequisites

<div class="sectionbody">

<div class="paragraph">

This tutorial assumes familiarity with building swift apps with Xcode and with Couchbase Lite.

</div>

<div class="ulist">

*   If you are unfamiliar with the basics of Couchbase Lite, it is recommended that you follow the [Getting Started](https://docs.couchbase.com/couchbase-lite/2.7/swift.html) guides

*   iOS (Xcode 11.4+)

    <div class="ulist">

    *   Download latest version from the [Mac App Store](https://itunes.apple.com/us/app/xcode/id497799835?mt=12)

    </div>

*   Wi-Fi network that the peers can communicate over

    <div class="ulist">

    *   You could run your peers in multiple simulators. But if you were running the app on real devices, then you will need to ensure that the devices are on the same WiFi network

    </div>

</div>

</div>

</div>

<div class="sect1">

## App Overview

<div class="sectionbody">

<div class="paragraph">

This is a simple inventory app that can be used as a [passive](https://ibsoln.github.io/stage/h2beta/couchbase-lite/2.8/refer-glossary.html#passive-peer) or [active peer](https://ibsoln.github.io/stage/h2beta/couchbase-lite/2.8/refer-glossary.html#active-peer).

</div>

<div class="paragraph">

The app uses a local database that is pre-populated with data. There is no Sync Gateway or Couchbase Server installed.

</div>

<div class="paragraph">

When used as a passive peer :-

</div>

<div class="ulist">

*   Users log in and start websockets listener for the couchbase lite database. A service corresponding to the listener is advertised over Bonjour.

*   View the status of connected clients

*   Directly sync data with connected clients

</div>

<div class="paragraph">

When used as a active peer :-

</div>

<div class="ulist">

*   Users can log in and start browsing for devices

*   Connect to a listener

*   Directly sync data with connected clients

</div>

<div class="imageblock">

<div class="content">![peer to peer sync](content/modules/cbl-p2p-sync-websockets/assets/ios-demo.gif)</div>

</div>

</div>

</div>

<div class="sect1">

## App Installation



<div class="ulist">

* Clone the repo

<div class="content">
git clone https://github.com/rajagp/couchbase-lite-peer-to-peer-sync-websocket-samples
</div>

* The app project does not come bundled with the Couchbase Lite framework. Run the script to pull down the framework

<div class="content">
cd /path/to/cloned/repo/couchbase-lite-peer-to-peer-sync-websocket-samples/ios/list-sync
sh install_11.sh
</div>

*   Download the .zip project from [here](https://drive.google.com/file/d/1wEit_q5oEc5b0UrWzH418S0_-Sia-30p/view?usp=sharing)

</div>

<div class="paragraph">

The app project comes pre-bundled with Couchbase Lite 2.8 beta framework.

</div>


<div class="sect2">

### Try it Out

<div class="ulist">

*   Open the iOS project using Xcode

    <div class="listingblock">

    <div class="content">

        open list-sync.xcodeproj

    </div>

    </div>

*   Build and run the project

*   Verify that you see the login screen

    <div class="imageblock">

    <div class="content">![app login screen](content/modules/cbl-p2p-sync-websockets/assets/swift-login.png)</div>

    </div>

</div>

</div>

</div>

</div>

<div class="sect1">

## Exploring the App Project

<div class="sectionbody">

<div class="ulist">

*   The xcode project comes pre-bundled with some resource files that we will examine here

</div>

<div class="imageblock">

<div class="content">![xcode project explorer](content/modules/cbl-p2p-sync-websockets/assets/swift-project-explorer.png)</div>

</div>

<div class="ulist">

*   `samplelist.json` : JSON data that is loaded into the local Couchbase Lite database. It includes the data for a single document. See [Data Model](#data-model)

*   `userwhitelist.json` : List of valid client users (and passwords) in the system. This list is looked up when the server tries to authenticate credentials associated with incoming connection request.

*   `listener-cert-pkey.p12` : This is [PKCS12](https://en.wikipedia.org/wiki/PKCS_12) file archive that includes a public key cert corresponding to the listener and associated private key. The cert is a sample cert that was generated using [OpenSSL](https://www.openssl.org) tool.

*   `listener-pinned-cert.cer` : This is the public key listener cert (the same cert that is embedded in the `listener-cert-pkey.p12` file) in DER encoded format. This cert is pinned on the client replicator and is used for validating server cert during connection setup

</div>

</div>

</div>

<div class="sect1">

## Data Model

<div class="sectionbody">

<div class="paragraph">

Couchbase Lite is a JSON Document Store. A Document is a logical collection of named fields and values.The values are any valid JSON types. In addition to the standard JSON types, Couchbase Lite supports some special types like `Date` and `Blob`. While it is not required or enforced, it is a recommended practice to include a _"type"_ property that can serve as a namespace for related.

</div>

<div class="sect2">

### The "List" Document

<div class="paragraph">

The app deals with a single Document with a _"type"_ property of _"list"_. This document is loaded from the `samplelist.json` file bundled with the project

</div>

<div class="paragraph">

An example of a document would be

</div>

<div class="listingblock">

<div class="content">

    {
        "type":"list",
        "list":[
          {
             "image":{"length":16608,"digest":"sha1-LEFKeUfywGIjASSBa0l/cg5rlm8=","content_type":"image/jpeg","@type":"blob"},
              "value":10,
              "key":"Apples"
          },
          {
            "image":{"length":16608,"digest":"sha1-LEFKeUsswGIjASssSBa0l/cg5rlm8=","content_type":"image/jpeg","@type":"blob"},
            "value":110,
            "key":"oranges"
           }
        ]

    }

</div>

</div>

<div class="paragraph">

The document is encoded as a `ListRecord` struct defined in the `ListRecord.swift` file

</div>

</div>

<div class="sect2">

### Initializing Local Database

<div class="paragraph">

The app loads the data from the JSON document named `samplelist.json` the first time the database is created. This is done regardless of whether the app is ;launched in passive or active mode.

</div>

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `openOrCreateDatabaseForUser()` method. This method creates an instance of Couchbase Lite database for the user if one does not exist and loads the empty database with data ready from bundled sample JSON file

</div>

<div class="listingblock">

<div class="content">

            var exists = false
            if Database.exists(withName: kUserDBName, inDirectory: userFolderPath) == true {
                  _userDb = try? Database(name: kUserDBName, config: options)
                exists = true

            }
            else {
                  _userDb = try? Database(name: kUserDBName, config: options)

            }

</div>

</div>

<div class="ulist">

*   Open the **SampleFileLoaderUtils.swift** file and locate the `loadSampleJSONDataForUserFromFile()` method. This function parses the document in JSON and udpates it to embed the "image" property into every object in the "list" array. The "image" property holds a blob entry to an image asset. The image for the blob is available in the "Assets.xcassets" folder

*   Open the **DatabaseManager.swift** file and locate the `createUserDocumentWithData()` method. This is where the document is saved into the database. Again, this is only done if there is no preexisting database for the user

</div>

<div class="listingblock">

<div class="content">

            for (key,value) in data {
                let docId = "\(kDocPrefix)\(key)"
                print("DocId is \(docId)")
                let doc = MutableDocument(id:docId, data:value as! Dictionary<String, Any>)
                try db.saveDocument(doc)
            }

</div>

</div>

</div>

</div>

</div>

<div class="sect1">

## Passive Peer or Server

<div class="sectionbody">

<div class="paragraph">

First, we will walk through the steps of using the app in passive peer mode

</div>

<div class="sect2">

### Initializing Websockets Listener

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `initWebsocketsListenerForUserDb()` function. This is where the websockets listsner for peer-to-peer sync is initialized

</div>

<div class="listingblock">

<div class="content">

            // Include websockets listener initializer code
            let listenerConfig = URLEndpointListenerConfiguration(database: db) (1)

            // Configure the appropriate auth test mode
            switch listenerTLSSupportMode { (2)
                case .TLSDisabled:
                    listenerConfig.disableTLS  = true
                    listenerConfig.tlsIdentity = nil
                case .TLSWithAnonymousAuth:
                    listenerConfig.disableTLS  = false // Use with anonymous self signed cert
                    listenerConfig.tlsIdentity = nil
                 case .TLSWithBundledCert:

                    if let tlsIdentity = self.getTLSIdentityFromPKCS12DataWithCertLabel(kListenerCertLabel) {
                        listenerConfig.disableTLS  = false // Use with anonymous self signed cert
                        listenerConfig.tlsIdentity = tlsIdentity
                    }
                    else {
                        print("Could not create identity from provided cert")
                        throw ListDocError.WebsocketsListenerNotInitialized
                    }

                // UNAVAILABLE FOR BETA
                //            case .TLSWithGeneratedCert:
                //                if let tlsIdentity = self.createIdentityWithCertLabel(kListenerCertLabel) {
                //                    listenerConfig.disableTLS  = false
                //                    listenerConfig.tlsIdentity = tlsIdentity
                //                }
                //                else {
                //                    print("Could not create identity from generated self signed cert")
                //                    throw ListDocError.WebsocketsListenerNotInitialized
                //                }

            }

            listenerConfig.enableDeltaSync = true (3)

            listenerConfig.authenticator = ListenerPasswordAuthenticator.init { (4)
                       (username, password) -> Bool in
                if (self._whitelistedUsers.contains(["password" : password, "name":username])) {
                    return true
                }
                return false
                   }

            _websocketListener = URLEndpointListener(config: listenerConfig)

</div>

</div>

<div class="colist arabic">

<table>

<tbody>

<tr>

<td>**1**</td>

<td>Initialize the `URLEndpointListenerConfiguration` for the specified database. There is a listener for a given database. You can specify a port to be associated with the listener. In our app, we let Couchbase Lite choose the port</td>

</tr>

<tr>

<td>**2**</td>

<td>This is where we configure the TLS mode. In the app, we have a flag named `listenerTLSSupportMode` that allows the app to switch between the various modes. You can change the mode by changing the value of the variable. See [Testing Different TLS Modes](#testing-different-tls-modes)</td>

</tr>

<tr>

<td>**3**</td>

<td>Enable delta sync. It is disabled by default</td>

</tr>

<tr>

<td>**4**</td>

<td>Configure the password authenticator callback function that authenticates the username/password received from the client during replication setup. The list of valid users are configured in `userwhitelist.json` file bundled with the app</td>

</tr>

</tbody>

</table>

</div>

<div class="sect3">

#### Testing Different TLS Modes

<div class="paragraph">

The app can be configured to test different TLS modes as follows by setting the `listenerTLSSupportMode` property in the `DatabaseManager.swift` file

</div>

<div class="listingblock">

<div class="content">

             fileprivate let listenerCertValidationMode:ListenerCertValidationTestMode = .TLSEnableValidationWithCertPinning

</div>

</div>

<table class="tableblock frame-all grid-all stretch"><caption class="title">Table 1\. TLS Modes on Listener</caption> <colgroup><col style="width: 50%;"> <col style="width: 50%;"></colgroup> 

<thead>

<tr>

<th class="tableblock halign-left valign-top">listenerTLSSupportMode Value</th>

<th class="tableblock halign-left valign-top">Behavior</th>

</tr>

</thead>

<tbody>

<tr>

<td class="tableblock halign-left valign-top">

TLSDisabled

</td>

<td class="tableblock halign-left valign-top">

There is no TLS. ALl communication is plaintext (insecure mode)

</td>

</tr>

<tr>

<td class="tableblock halign-left valign-top">

TLSWithAnonymousAuth

</td>

<td class="tableblock halign-left valign-top">

The app uses Couchbase Lite APIs to auto-generate an anonymous server cert and to use that as `TLSIdentity` of the server. Communication is encrypted

</td>

</tr>

<tr>

<td class="tableblock halign-left valign-top">

TLSWithBundledCert

</td>

<td class="tableblock halign-left valign-top">

The app generates `TLSIdentity` of the server from public key cert and private key bundled in the`listener-cert-pkey.p12` archive. Communication is encrypted

</td>

</tr>

</tbody>

</table>

</div>

</div>

<div class="sect2">

### Start Websockets Listener

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `startWebsocketsListenerForUserDb()` method.

</div>

<div class="listingblock">

<div class="content">

            DispatchQueue.global().sync {
                do {
                    try websocketListener.start()
                    handler(websocketListener.urls,nil)
                }
                catch {
                    handler(nil,error)
                }

            }

</div>

</div>

</div>

<div class="sect2">

### Advertising Listener Service

<div class="paragraph">

In the app, we use [NetService](https://developer.apple.com/documentation/foundation/netservice) to advertise the websockets listener service listening at the specified listener port. This aspect of the app has nothing to do with Couchbase Lite. In your production app, you can use any suitable mechanism including using a well known URL to advertise your service that active clients can be preconfigured to connect to.

</div>

<div class="ulist">

*   Open the **ServiceAdvertiser.swift** file and look for `ServiceAdvertiser` class. Here, we advertise a [Bonjour](https://developer.apple.com/bonjour/) service with service type of _`_cblistservicesync._tcp`_

</div>

<div class="listingblock">

<div class="content">

      /// The Bonjour service name. Setting it to an empty String will be
      /// mapped to the device's name.
      public var serviceName: String = ""

      /// The Bonjour service type.
      public var serviceType = "_cblistservicesync._tcp"

     /// The Bonjour domain type.
     public var serviceDomain = ""

</div>

</div>

<div class="ulist">

*   The service is published as implemented in the `doStart()` method.

</div>

<div class="listingblock">

<div class="content">

        private func doStart(database:String, _ port:UInt16) {
            let service = NetService(domain: serviceDomain, type: serviceType,
                                     name: serviceName, port:Int32(port))

            service.delegate = self
            service.includesPeerToPeer = true
            service.publish()
            services[database] = service

        }

</div>

</div>

<div class="paragraph">

Explore the content in the `ServiceAdvertiser.swift`. It includes implementation of the `NetServiceDelegate` delegate callback methods to accept incoming connections.

</div>

</div>

<div class="sect2">

### Stop Websockets Listener

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `stopWebsocketsListenerForUserDb()` method. You can stop the listener at any point. If there are connected clients, it will warn you that there are active connections. If you choose to stop listener, all connected clients will be disonnected

</div>

<div class="listingblock">

<div class="content">

        func stopWebsocketsListenerForUserDb() throws{
            print(#function)
            guard let websocketListener = _websocketListener else {
                throw ListDocError.WebsocketsListenerNotInitialized
            }
            websocketListener.stop()
            _websocketListener = nil
        }

</div>

</div>

</div>

<div class="sect2">

### Try it out

<div class="ulist">

*   Run the app on a simulator or a real device. If its the latter, make sure you sign your app with the appropriate developer certificate

*   On login screen, sign in as any one of the users configured in the `userwhitelist.json` file such as "bob" and "password"

*   From the "listener" tab, start the listener by clicking on "Start Listener" button

*   Click on the "action" button to see number of connected clients. It should be 0 if there are no connected clients

*   From the "listener" tab, stop the listener by clicking on "Stop Listener" button

</div>

<div class="imageblock">

<div class="content">![server websockets listener login screen](content/modules/cbl-p2p-sync-websockets/assets/ios-passive-start-listener.gif)</div>

</div>

</div>

</div>

</div>

<div class="sect1">

## Active Peer or Client

<div class="sectionbody">

<div class="paragraph">

We will walk through the steps of using the app in active peer mode

</div>

<div class="sect2">

### Discovering Listeners

<div class="paragraph">

In the app, we use [NetService](https://developer.apple.com/documentation/foundation/netservice) to browse for devices that are advertising services with name _`_cblistservicesync._tcp`_. This aspect of the app has nothing to do with Couchbase Lite. In your production app, you could launch your listener at well known URL well and preconfigure your active peer to connect to the URL.

</div>

<div class="ulist">

*   Open the **ServiceBrowser.swift** file and look for `ServiceBrowser` class. Here, we browse for a service with service type of _`_cblistservicesync._tcp`_ using [Bonjour](https://developer.apple.com/bonjour/)

</div>

<div class="listingblock">

<div class="content">

        public func startSearch(withDelegate delegate:ServiceBrowserDelegate? ){
             peerBrowserDelegate = delegate
            self.browser = NetServiceBrowser()
            self.browser?.delegate = self
            self.browser?.searchForServices(ofType: serviceType, inDomain: domain)
        }

</div>

</div>

<div class="paragraph">

Explore the content in the `ServiceBrowser.swift`. It includes implementation of the `NetServiceDelegate` delegate callback methods to resolve the service to its IP Address and port that will be used by the client to connnect to the listener.

</div>

</div>

<div class="sect2">

### Initializing and Starting Replication

<div class="paragraph">

Initialilzing a replicator for peer-to-peer sync is fundamentally not different than if your Couchbase Lite client were to [syncing](https://docs.couchbase.com/couchbase-lite/2.7/swift.html#replication) with a remote Sync Gateway.

</div>

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `startP2PReplicationWithUserDatabaseToRemotePeer()` method. If you have been using Couchbase Lite to sync data with Sync Gateway, this code should seem very familiar. In this function, we initialize a bi-directional replication to the listener peer in continuous mode. We also register a Replication Listener to be notified of status to the replication status.

</div>

<div class="listingblock">

<div class="content">

            if replicatorForUserDb == nil {
                // Start replicator to connect to the URLListenerEndpoint
                guard let targetUrl = URL(string: "wss://\(peer)/\(kUserDBName)") else {
                    throw ListDocError.URLInvalid
                }

                let config = ReplicatorConfiguration.init(database: userDb, target: URLEndpoint.init(url:targetUrl)) (1)

                config.replicatorType = .pushAndPull
                config.continuous =  true

                // Explicitly allows self signed certificates. By default, only
                // CA signed cert is allowed
                switch listenerCertValidationMode { (2)

                    case .TLSSkipValidation :
                        // Use serverCertificateVerificationMode set to .selfSignedCert to disable cert validation
                        config.serverCertificateVerificationMode = .selfSignedCert

                    case .TLSEnableValidationWithCertPinning:
                        // Use serverCertificateVerificationMode set to .caCert to enable cert validation

                        config.serverCertificateVerificationMode = .caCert // will likely change post beta
                        if let pinnedCert = self.loadSelfSignedCertForListenerFromBundle() {
                            config.pinnedServerCertificate = pinnedCert
                        }
                        else {
                            print("Failed to load server cert to pin. Will proceed without pinning")
                        }

                    case .TLSEnableValidation:
                        config.serverCertificateVerificationMode = .caCert

                }

                let authenticator = BasicAuthenticator(username: user, password: password)(3)
                config.authenticator = authenticator

                replicatorForUserDb = Replicator.init(config: config) (4)
                _replicatorsToPeers[peer] = replicatorForUserDb

              }
            if let pushPullReplListenerForUserDb = registerForEventsForReplicator(replicatorForUserDb,handler:handler) {
                _replicatorListenersToPeers[peer] = pushPullReplListenerForUserDb

            }

            replicatorForUserDb?.start() (5)
            handler(PeerConnectionStatus.Connecting)

</div>

</div>

<div class="colist arabic">

<table>

<tbody>

<tr>

<td>**1**</td>

<td>Initialize a Repicator Configuration for the specified local database and remote listener URL endpoint</td>

</tr>

<tr>

<td>**2**</td>

<td>This is where we configure the TLS server cert validation mode - whether we enable cert validation or skip validation. This would only apply if you had enabled TLS support on listener as discussed in [[TLS Modes on Listener]](#TLS Modes on Listener). If you skip server cert validation, you still get encrypted communication, but you are communicating with a untrusted listener. In the app, we have a flag named `listenerCertValidationMode` that allows you to try the various modes. You can change the mode by changing the value of the variable. See [Testing Different Server Validation Modes](#testing-different-server-validation-modes)</td>

</tr>

<tr>

<td>**3**</td>

<td>The app uses basic client authentication to authenticate with the server</td>

</tr>

<tr>

<td>**4**</td>

<td>Initialize the Replicator</td>

</tr>

<tr>

<td>**5**</td>

<td>Start replication. The app uses the events on the Replicator Listener to listen to monitor the replication.</td>

</tr>

</tbody>

</table>

</div>

<div class="sect3">

#### Testing Different Server Validation Modes

<div class="paragraph">

The app can be configured to test different TLS modes as follows by setting the `listenerCertValidationMode` property in the `DatabaseManager.swift` file

</div>

<div class="listingblock">

<div class="content">

             fileprivate let listenerCertValidationMode:ListenerCertValidationTestMode = .TLSEnableValidationWithCertPinning

</div>

</div>

<table class="tableblock frame-all grid-all stretch"><caption class="title">Table 2\. TLS Listener Cert Validation</caption> <colgroup><col style="width: 50%;"> <col style="width: 50%;"></colgroup> 

<thead>

<tr>

<th class="tableblock halign-left valign-top">listenerCertValidationMode Value</th>

<th class="tableblock halign-left valign-top">Behavior</th>

</tr>

</thead>

<tbody>

<tr>

<td class="tableblock halign-left valign-top">

TLSSkipValidation

</td>

<td class="tableblock halign-left valign-top">

There is no validation of server cert. This is typically in used in dev/test environments with self signed certs. Skipping server cert authentication is discouraged in production environments. Communication is encrypted

</td>

</tr>

<tr>

<td class="tableblock halign-left valign-top">

TLSEnableValidation

</td>

<td class="tableblock halign-left valign-top">

If the listener cert is a well known CA then you use this mode. Of course, in our sample app, the listener cert as specified in `listener-cert-pkey` is a self signed cert - so you probably will not use this mode to test. But if you have a CA signed cert, you can configure your listener with that cert and use this mode to test. Communication is encrypted

</td>

</tr>

<tr>

<td class="tableblock halign-left valign-top">

TLSEnableValidationWithCertPinning

</td>

<td class="tableblock halign-left valign-top">

In this mode, the app uses the pinned cert,`listener-pinned-cert.cer` that is bundled in the app to validate the listener identity. Communication is encrypted

</td>

</tr>

</tbody>

</table>

</div>

</div>

<div class="sect2">

### Stopping Replication

<div class="ulist">

*   Open the **DatabaseManager.swift** file and locate the `stopP2PReplicationWithUserDatabaseToRemotePeer()` method. If you have been using Couchbase Lite to sync data with Sync Gateway, this code should seem very familiar. In this function, we remove any listeners attached to the replicator and stop it. You can restart the replicator again in `startP2PReplicationWithUserDatabaseToRemotePeer()` method

</div>

<div class="listingblock">

<div class="content">

            if let listener = _replicatorListenersToPeers[peer] {
                replicator.removeChangeListener(withToken: listener)
                _replicatorListenersToPeers.removeValue(forKey: peer)
            }

            replicator.stop()

</div>

</div>

</div>

<div class="sect2">

### Try it out

<div class="ulist">

*   Follow instructions in "Try It Out" section of [Passive Peer or Server](#passive-peer-or-server) to start app in passive mode on a simulator instance or real device.

*   Run the app on a separate simulator instance or a real device. If its the latter, make sure you sign your app with the appropriate developer certificate

*   On login screen, sign in as any one of the users configured in the `userwhitelist.json` file such as "bob" and "password". An an exercise, try with an invalid user and ensure it fails

*   Tap on the "browser" tab. The app automatically browses for listener and lists it here.

*   Tap on the row corresponding to listener. This will start replication with the listener and it shoud transition to Connected state

*   Verify the connection count on listener

*   Swipe left on the the row. You should see option to remove listener and Disconect. Try Disconnect and then reconnect again

</div>

<div class="imageblock">

<div class="content">![p2p sync](content/modules/cbl-p2p-sync-websockets/assets/ios-active-start-replicator.gif)</div>

</div>

</div>

</div>

</div>

<div class="sect1">

## Syncing Data

<div class="sectionbody">

<div class="paragraph">

Once the connection is established between the peers, you can start syncing. Couchbase Lite takes care of it.

</div>

<div class="sect2">

### Try it out

<div class="ulist">

*   Run the app on two or more simulators or real devices. If its the latter, make sure you sign your app with the appropriate developer certificate

*   Start the listener on one of the app instances. You could also have multiple listeners.

*   Connect the other instances of the app to the listener

*   Tap on the "List" tab

*   Edit the quanity or image on any one of the instances

*   Watch it sync automatically to other connected clients

</div>

<div class="imageblock">

<div class="content">![server websockets listener login screen](content/modules/cbl-p2p-sync-websockets/assets/ios-sync.gif)</div>

</div>

</div>

</div>

</div>

<div class="sect1">

## What Next

<div class="sectionbody">

<div class="paragraph">

As an exercise, switch between the various TLS modes and server cert validation modes and see how the app behaves. You can also try with different topologies to connect the peers.

</div>

</div>

</div>

<div class="sect1">

## Learn More

<div class="sectionbody">

<div class="paragraph">

Congratulations on completing this tutorial!

</div>

<div class="paragraph">

This tutorial walked you through an example of how to directly synchronize data between Couchbase Lite clients. While the tutorial is for iOS, the concepts apply equally to other Couchbase Lite platforms.

</div>

<div class="sect2">

### Further Reading

<div class="paragraph">

TBD

</div>

</div>

</div>

</div>

</div>

<div id="footer">

<div id="footer-text">Last updated 2020-06-20 16:33:14 -0400</div>

</div>
