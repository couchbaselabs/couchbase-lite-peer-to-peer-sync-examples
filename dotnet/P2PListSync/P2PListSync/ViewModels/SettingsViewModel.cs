// SettingsViewModel.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//


using P2PListSync.Models;
using System.Collections.Generic;

namespace P2PListSync.ViewModels
{
    public class SettingsViewModel : BaseViewModel
    {

        public IList<TLSSetting> ListenerTLSModes { get { return TLSSettingsData.ListenerTLSMode; } }
        public IList<TLSSetting> ListenerCertValidationModes { get { return TLSSettingsData.ListenerCertValidationMode; } }

        TLSSetting _selectedListenerTLSMode;
        public TLSSetting SelectedListenerTLSMode
        {
            get { return _selectedListenerTLSMode; }
            set { SetProperty(ref _selectedListenerTLSMode, value); }
        }

        TLSSetting _selectedListenerCertValidationMode;
        public TLSSetting SelectedListenerCertValidationMode
        {
            get { return _selectedListenerCertValidationMode; }
            set { SetProperty(ref _selectedListenerCertValidationMode, value); }
        }

        public SettingsViewModel()
        {
            SelectedListenerTLSMode = ListenerTLSModes[(int)CoreApp.ListenerTLSMode];
            SelectedListenerCertValidationMode = ListenerCertValidationModes[(int)CoreApp.ListenerCertValidationMode];
        }
    }
}