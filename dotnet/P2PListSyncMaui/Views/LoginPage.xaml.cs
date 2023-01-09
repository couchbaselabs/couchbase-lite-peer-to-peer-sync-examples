//
// LoginPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using P2PListSync.Models;
using System;
using System.Linq;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class LoginPage : ContentPage
    {
        public LoginPage()
        {
            InitializeComponent();
        }

        async void OnLoginButtonClicked(object sender, EventArgs e)
        {
            var user = new User {
                Username = usernameEntry.Text,
                Password = passwordEntry.Text
            };
            
            var isValid = AreCredentialsCorrect(user);
            if (!isValid) {
                messageLabel.Text = "You are not an autherized user.";
                passwordEntry.Text = string.Empty;
                var enter = await DisplayAlert("Login Warning", "You are not an autherized user. You want to verify the fail case of ClientListenerAuthenticator functionality?", "Yes", "No");
                if (!enter) {
                    return;
                }
            }
            
            CoreApp.CurrentUser = user; // User may not be autherized
            Application.Current.MainPage = new MainPage();
        }

        bool AreCredentialsCorrect(User user)
        {
            var found = CoreApp.AllowedUsers?.Where(u => u.Username == user.Username && u.Password == user.Password).SingleOrDefault();
            return found != null;
        }
    }
}