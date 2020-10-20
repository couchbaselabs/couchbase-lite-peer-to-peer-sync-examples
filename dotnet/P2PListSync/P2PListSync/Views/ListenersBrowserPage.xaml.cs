using System;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

using P2PListSync.Models;
using P2PListSync.ViewModels;

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
            if (!viewModel.ManuallyAddReplicator())
                DisplayAlert("Invalid IP Endpoint", "IP Endpoint should look something like ex. 192.168.0.14:59840", "OK");
        }
    }
}