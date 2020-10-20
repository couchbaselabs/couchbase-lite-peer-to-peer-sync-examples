//
// App.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Xamarin.Forms;
using Xamarin.Forms.Xaml;
using P2PListSync.Views;

[assembly: XamlCompilation(XamlCompilationOptions.Compile)]
namespace P2PListSync
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();
            CoreApp.RequiresUserAuth = true;

            if (CoreApp.RequiresUserAuth) {
                CoreApp.LoadUserAllowList();
                MainPage = new LoginPage();
            } else {
                CoreApp.LoadAndInitDB();
                MainPage = new MainPage();
            }
        }

        protected override void OnStart()
        {
            // Handle when your app starts
        }

        protected override void OnSleep()
        {
            // Handle when your app sleeps
        }

        protected override void OnResume()
        {
            // Handle when your app resumes
        }
    }
}
