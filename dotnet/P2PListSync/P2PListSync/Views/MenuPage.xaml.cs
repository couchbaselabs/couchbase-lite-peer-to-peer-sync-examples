//
// MenuPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//
//

using P2PListSync.Models;
using System.Collections.Generic;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class MenuPage : ContentPage
    {
        MainPage RootPage { get => Application.Current.MainPage as MainPage; }
        List<HomeMenuItem> menuItems;
        public MenuPage()
        {
            InitializeComponent();

            menuItems = new List<HomeMenuItem>
            {
                new HomeMenuItem {Id = MenuItemType.SeasonalItemsList, Title="What's in Season?" },
                new HomeMenuItem {Id = MenuItemType.Listener, Title="Listener" },
                new HomeMenuItem {Id = MenuItemType.ListenersBrowser, Title="Browser" },
                new HomeMenuItem {Id = MenuItemType.Settings, Title="Settings" }
            };

            if(CoreApp.RequiresUserAuth) {
                menuItems.Add(new HomeMenuItem { Id = MenuItemType.Logout, Title = "Logout" });
            }

            ListViewMenu.ItemsSource = menuItems;

            ListViewMenu.SelectedItem = menuItems[0];
            ListViewMenu.ItemSelected += async (sender, e) =>
            {
                if (e.SelectedItem == null)
                    return;
                
                var id = (int)((HomeMenuItem)e.SelectedItem).Id;
                if (id == 3) {
                    Application.Current.MainPage = new LoginPage();
                    return;
                }
                
                await RootPage.NavigateFromMenu(id);
            };
        }
    }
}