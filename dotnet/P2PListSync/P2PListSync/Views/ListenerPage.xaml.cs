//
// ListenerPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using P2PListSync.ViewModels;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class ListenerPage : ContentPage
    {
        ListenerViewModel viewModel;

        public ListenerPage()
        {
            InitializeComponent();
            BindingContext = viewModel = new ListenerViewModel();
        }
    }
}