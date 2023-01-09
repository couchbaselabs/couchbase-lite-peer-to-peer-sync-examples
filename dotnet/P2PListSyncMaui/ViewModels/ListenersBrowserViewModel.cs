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
using System.Globalization;
using System.Linq;
using System.Net;
using System.Text;

namespace P2PListSync.ViewModels
{
    public class ListenersBrowserViewModel : BaseViewModel
    {
        public ObservableCollection<ReplicatorItem> Items { get; set; }

        private string _iPEndpointInput;
        public string IPEndpointInput
        {
            get { return _iPEndpointInput; }
            set { SetProperty(ref _iPEndpointInput, value); }
        }

        //peer discovery
        private UdpListener _discovery;

        public ListenersBrowserViewModel()
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
        #endregion

        public void AddReplicator(IPEndPoint ep)
        {
            Device.BeginInvokeOnMainThread(() =>
            {
                var ips = Items.Where(x => x.ListenerEndpoint.Equals(ep)).SingleOrDefault();
                if (ips == null) {
                    Items.Add(new ReplicatorItem(ep));
                }
            });
        }

        public void RemoveReplicator(ReplicatorItem repl)
        {
            Device.BeginInvokeOnMainThread(() =>
            {
                Items.Remove(repl);
                repl.Dispose();
            });
        }

        internal bool ManuallyAddReplicator()
        {
            if(IPEndpointInput == null) {
                return false;
            }

            var remoteEndpoint = CreateIPEndPoint(IPEndpointInput);
            if (remoteEndpoint == null) {
                return false;
            }

            AddReplicator(remoteEndpoint);
            return true;
        }

        private IPEndPoint CreateIPEndPoint(string endPoint)
        {
            string[] ep = endPoint.Split(':');
            if (ep.Length != 2) {
                return null;
            }

            IPAddress ip;
            if (!IPAddress.TryParse(ep[0], out ip) || ip == null) {
                return null;
            }

            int port;
            if (!int.TryParse(ep[ep.Length - 1], NumberStyles.None, NumberFormatInfo.CurrentInfo, out port)) {
                return null;
            }

            IPEndPoint ipEp;
            try {
                ipEp = new IPEndPoint(ip, port);
            } catch {
                return null;
            }

            return ipEp;
        }
    }
}
