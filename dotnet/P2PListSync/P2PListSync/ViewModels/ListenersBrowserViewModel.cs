//
// ListenersBrowserViewModel.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using P2PListSync.Models;
using P2PListSync.P2P;
using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net;
using System.Text;
using Xamarin.Forms;

namespace P2PListSync.ViewModels
{
    public class ListenersBrowserViewModel : BaseViewModel
    {
        public ObservableCollection<ReplicatorItem> Items { get; set; }

        //peer discovery
        private UdpListener _discovery;

        public ListenersBrowserViewModel()
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
            var remoteEndpoint = new IPEndPoint(remoteIP, CoreApp.PeerPort);

            AddReplicator(remoteEndpoint);
        }
        #endregion

        public void AddReplicator(IPEndPoint ep)
        {
            var ips = Items.Where(x => x.ListenerEndpoint.Equals(ep)).SingleOrDefault();

            if (ips == null) {
                Device.BeginInvokeOnMainThread(() =>
                {
                    Items.Add(new ReplicatorItem(ep));
                });
            }
        }
    }
}
