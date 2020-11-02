//
// HomeMenuItem.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//


namespace P2PListSync.Models
{
    public enum MenuItemType
    {
        ListenersBrowser,
        Listener,
        SeasonalItemsList,
        Logout,
        Settings
    }

    public class HomeMenuItem
    {
        public MenuItemType Id { get; set; }

        public string Title { get; set; }
    }
}
