//
// CoreApp.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite;
using System;
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

        public static string DocId = "doc::list";
        public static string ArrKey = "items";

        #region Properties
        public static Database DB { get; private set; }
        internal static string DBPath => Path.Combine(Path.GetTempPath().Replace("cache", "files"), "CouchbaseLite");
        #endregion

        #region Public Methods
        public static void LoadAndInitDB()
        {
            //Database.Delete("userdb", DBPath); //enable this to reset db
            if (!Database.Exists("userdb", DBPath)) {
                using (var dbZip = new ZipArchive(ResourceLoader.GetEmbeddedResourceStream(typeof(CoreApp).GetTypeInfo().Assembly, "userdb.cblite2.zip"))) {
                    dbZip.ExtractToDirectory(DBPath);
                }
            }

            DB = new Database("userdb", new DatabaseConfiguration() { Directory = DBPath });
        }
        #endregion
    }
}
