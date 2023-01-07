//
// MainPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//
//

using P2PListSync.Models;
using System.Collections.Generic;
using System.Threading.Tasks;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class MainPage : FlyoutPage
    {
        Dictionary<int, NavigationPage> MenuPages = new Dictionary<int, NavigationPage>();
        public MainPage()
        {
            InitializeComponent();

            FlyoutLayoutBehavior = FlyoutLayoutBehavior.Popover;

            MenuPages.Add((int)MenuItemType.SeasonalItemsList, (NavigationPage)Detail);
        }

        public async Task NavigateFromMenu(int id)
        {
            if (!MenuPages.ContainsKey(id)) {
                switch (id) {
                    case (int)MenuItemType.SeasonalItemsList:
                        MenuPages.Add(id, new NavigationPage(new SeasonalItemsPage()));
                        break;
                    case (int)MenuItemType.Listener:
                        MenuPages.Add(id, new NavigationPage(new ListenerPage()));
                        break;
                    case (int)MenuItemType.ListenersBrowser:
                        MenuPages.Add(id, new NavigationPage(new ListenersBrowserPage()));
                        break;
                    case (int)MenuItemType.Settings:
                        MenuPages.Add(id, new NavigationPage(new SettingsPage()));
                        break;
                    case (int)MenuItemType.Logout:
                        //Do Logout logic
                        break;
                }
            }

            NavigationPage newPage = MenuPages[id];

            if (newPage != null && Detail != newPage) {
                Detail = newPage;

                if (Device.RuntimePlatform == Device.Android)
                    await Task.Delay(100);

                IsPresented = false;
            }
        }
    }
}