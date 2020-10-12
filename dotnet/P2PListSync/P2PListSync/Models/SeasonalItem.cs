//
// SeasonalItem.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using P2PListSync.ViewModels;

namespace P2PListSync.Models
{
    public class SeasonalItem : BaseViewModel
    {
        public int Index { get; set; } 

        private string _name;
        public string Name
        {
            get { return _name; }
            set { SetProperty(ref _name, value); }
        }

        private byte[] _imageByteArray;
        public byte[] ImageByteArray
        {
            get { return _imageByteArray; }
            set { SetProperty(ref _imageByteArray, value); }
        }

        private int _quantity;
        public int Quantity
        {
            get { return _quantity; }
            set { SetProperty(ref _quantity, value); }
        }

    }
}