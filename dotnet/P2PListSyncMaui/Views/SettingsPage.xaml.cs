//
// SettingsPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//
//

using P2PListSync.ViewModels;
using System;

namespace P2PListSync.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class SettingsPage : ContentPage
    {
        SettingsViewModel viewModel;

        public SettingsPage()
        {
            InitializeComponent();

            BindingContext = viewModel = new SettingsViewModel();
        }

        void OnListenerTLSModePickerSelectedIndexChanged(object sender, EventArgs e)
        {
            var picker = (Picker)sender;
            int selectedIndex = picker.SelectedIndex;

            if (selectedIndex != -1) {
                LISTENER_TLS_MODE listenerTlsMode;
                Enum.TryParse(viewModel.ListenerTLSModes[selectedIndex].Setting, out listenerTlsMode);
                CoreApp.ListenerTLSMode = listenerTlsMode;
            }
        }

        void OnListenerCertValidationModePickerSelectedIndexChanged(object sender, EventArgs e)
        {
            var picker = (Picker)sender;
            int selectedIndex = picker.SelectedIndex;

            if (selectedIndex != -1) {
                LISTENER_CERT_VALIDATION_MODE listenerCertValidationMode;
                Enum.TryParse(viewModel.ListenerCertValidationModes[selectedIndex].Setting, out listenerCertValidationMode);
                CoreApp.ListenerCertValidationMode = listenerCertValidationMode;
            }
        }
    }
}