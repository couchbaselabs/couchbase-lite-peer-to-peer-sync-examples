## Overview
Sample apps that demonstate out-of-box websockets listener based peer-to-peer database sync functionality introduced in Couchbase Lite 2.8.

## Introduction

Couchbase Lite 2.8 release supports out-of-the-box support for secure [Peer-to-Peer Sync](https://docs.couchbase.com/couchbase-lite/2.8/swift/learn/swift-landing-p2psync.html), over websockets directly between Couchbase Lite enabled clients in IP-based networks without the need for a centralized control point (i.e. no Sync Gateway or Couchbase server required)

This feature is Enterprise-only.

## iOS Inventory App Tutorial in Swift
### Prerequisites
This tutorial assumes familiarity with building swift apps with Xcode and with Couchbase Lite.

* If you are unfamiliar with the basics of Couchbase Lite, it is recommended that you follow the link:https://docs.couchbase.com/couchbase-lite/2.7/swift.html[Getting Started] guides


* iOS (Xcode 11.4+)
** Download latest version from the link:https://itunes.apple.com/us/app/xcode/id497799835?mt=12[Mac App Store]

* Wi-Fi network that the peers can communicate over
** You could run your peers in multiple simulators. But if you were running the app on real devices, then you will need to ensure that the devices are on the same WiFi network

### Installation & Code Walkthrough
The complete step by step tutorial is available [here](]https://docs.couchbase.com/tutorials/cbl-p2p-sync-websockets/swift/cbl-p2p-sync-websockets.html)

NOTE: Apps for other platforms coming soon