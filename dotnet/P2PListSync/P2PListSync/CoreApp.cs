//
// CoreApp.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite;
using Newtonsoft.Json;
using P2PListSync.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using Xamarin.Forms;

namespace P2PListSync
{
    public sealed class CoreApp : Application
    {
        public static int PeerPort = 59840;
        public static int UdpPort = 15000;
        public static Guid Guid = Guid.NewGuid();

        public static string DbName = "userdb";
        public static string DocId = "doc::list";
        public static string ArrKey = "items";

        #region Properties
        public static Database DB { get; private set; }
        internal static string DBPath => Path.Combine(Path.GetTempPath().Replace("cache", "files"), "CouchbaseLite");
        
        public static List<User> Users { get; private set; }
        #endregion

        #region Public Methods
        public static void LoadUserAllowList()
        {
            Users = new List<User>();
            var userAllowList = ResourceLoader.GetEmbeddedResourceString(typeof(CoreApp).GetTypeInfo().Assembly, "userallowlist.json");
            Users = JsonConvert.DeserializeObject<List<User>>(userAllowList);
        }

        public static void LoadAndInitDB()
        {
            //Database.Delete(DbName, DBPath); //enable this or uninstall app to reset db
            if (!Database.Exists(DbName, DBPath)) {
                using (var dbZip = new ZipArchive(ResourceLoader.GetEmbeddedResourceStream(typeof(CoreApp).GetTypeInfo().Assembly, $"{DbName}.cblite2.zip"))) {
                    dbZip.ExtractToDirectory(DBPath);
                }
            }

            DB = new Database(DbName, new DatabaseConfiguration() { Directory = DBPath });
        }
        #endregion
    }
}
