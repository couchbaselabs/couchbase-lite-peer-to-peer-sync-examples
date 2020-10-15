using System;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

using P2PListSync.Models;
using P2PListSync.ViewModels;
using System.Net;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class ListenersBrowserPage : ContentPage
    {
        ListenersBrowserViewModel viewModel;

        public ListenersBrowserPage()
        {
            InitializeComponent();

            BindingContext = viewModel = new ListenersBrowserViewModel();
        }

        void OnItemSelected(object sender, SelectedItemChangedEventArgs args)
        {
            var item = args.SelectedItem as ReplicatorItem;
            if (item == null)
                return;

            item.ExecuteStartReplicatorCommand();

            // Manually deselect item.
            ItemsListView.SelectedItem = null;
        }

        void OnItemClicked(object sender, EventArgs e)
        {
            var value = sender as MenuItem;
            var repl = value.BindingContext as ReplicatorItem;
            viewModel.RemoveReplicator(repl);
        }

        void Add_Clicked(object sender, EventArgs e)
        {
            var ip = newItemName.Text.Split(':');
            var remoteIP = IPAddress.Parse(ip[0]);
            var remotePort = Int32.Parse(ip[1]);
            var remoteEndpoint = new IPEndPoint(remoteIP, remotePort);
            if (ip != null)
                viewModel.AddReplicator(remoteEndpoint);
        }
    }
}