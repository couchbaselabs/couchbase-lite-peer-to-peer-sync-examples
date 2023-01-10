//
// SeasonalItemsPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//
//

using System;

using P2PListSync.ViewModels;
using P2PListSync.Models;
using System.Collections.Generic;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class SeasonalItemsPage : ContentPage
    {
        SeasonalItemsViewModel viewModel;
        HashSet<int> changedIndexes;

        public SeasonalItemsPage()
        {
            InitializeComponent();

            BindingContext = viewModel = new SeasonalItemsViewModel();
            changedIndexes = viewModel.DocsChangeIndexes;
        }

        async void OnImageTapped(object sender, EventArgs args)
        {
            var value = sender as Image;
            var item = ((KeyValuePair<int, SeasonalItem>)value.BindingContext).Value;
            await viewModel.ExecuteImageChangedCommand(item.Index);
        }

        private void OnQuantityChanged(object sender, TextChangedEventArgs e)
        {
            var value = sender as Entry;
            if (value.BindingContext == null) return;
            var item = (SeasonalItem)value.BindingContext;
            var oldValue = e.OldTextValue;
            var newValue = e.NewTextValue;

            if (oldValue == null || newValue == null) {
                return;
            }

            changedIndexes.Add(item.Index);
        }

        private void OnNameChanged(object sender, TextChangedEventArgs e)
        {
            var value = sender as Entry;
            if (value.BindingContext == null) return;
            var item = (SeasonalItem)value.BindingContext;

            var oldValue = e.OldTextValue;
            var newValue = e.NewTextValue;

            if (oldValue == null || newValue == null) {
                return;
            }

            changedIndexes.Add(item.Index);
        }
    }
}