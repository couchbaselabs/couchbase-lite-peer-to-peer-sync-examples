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

        async void Refresh_Clicked(object sender, EventArgs e)
        {
            //await Navigation.PushModalAsync(new NavigationPage(new NewItemPage()));
        }
    }
}