//
// CoreApp.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using Couchbase.Lite;
using Couchbase.Lite.Logging;
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
    /// <summary>
    /// Switch between these various modes to try various auth types
    /// </summary>
    public enum LISTENER_TLS_MODE
    {
        DISABLED,
        WITH_ANONYMOUS_AUTH,
        WITH_BUNDLED_CERT, // Bring your own cert (self-signed or CA)
        WITH_GENERATED_SELF_SIGNED_CERT // Use convenience API to generate cert
    }

    /// <summary>
    /// Switch between these various modes to try various auth types
    /// </summary>
    public enum LISTENER_CERT_VALIDATION_MODE
    {
        SKIP_VALIDATION,
        ENABLE_VALIDATION, // Used for CA cert validation
        ENABLE_VALIDATION_WITH_CERT_PINNING // User for self signed cert
    }

    public sealed class CoreApp : Application
    {
        public static int UdpPort = 15000;
        public static Guid Guid = Guid.NewGuid();

        public static string DbName = "userdb";
        public static string DocId = "doc::list";
        public static string ArrKey = "items";

        #region Properties

        /// <summary>
        /// Flag to determine if we need to login to the app and set user password auth for listener 
        /// when LISTENER_TLS_MODE is DISABLED 
        /// </summary>
        public static bool RequiresUserAuth { get; set; }

        public static bool IsDebugging { get; set; }

        private static Database DB { get; set; }

        public static Collection COLL { get; private set; }

        internal static string DBPath => Path.Combine(Path.GetTempPath().Replace("cache", "files"), "CouchbaseLite");
        
        public static List<User> AllowedUsers { get; private set; }

        public static User CurrentUser { get; set; }

        /// <summary>
        /// Switch between listener auth modes.
        /// </summary>
        //tag::ListenerTLSTestMode[]
        public static LISTENER_TLS_MODE ListenerTLSMode = LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH;
        //end::ListenerTLSTestMode[]

        /// <summary>
        /// Skip validation for self signed certs
        /// </summary>
        //tag::ListenerValidationTestMode[]
        public static LISTENER_CERT_VALIDATION_MODE ListenerCertValidationMode = LISTENER_CERT_VALIDATION_MODE.SKIP_VALIDATION;
        //end::ListenerValidationTestMode[]
        #endregion

        #region Public Methods
        internal static List<User> LoadUserAllowList()
        {
            AllowedUsers = new List<User>();
            string userAllowList = ResourceLoader.GetEmbeddedResourceString(typeof(CoreApp).GetTypeInfo().Assembly, "userallowlist.json");
            AllowedUsers = JsonConvert.DeserializeObject<List<User>>(userAllowList);
            return AllowedUsers;
        }

        public static void LoadAndInitDB()
        {
            // Enable this or uninstall app to reset db
            //Database.Delete(DbName, DBPath); 
            //tag::OpenOrCreateDatabase[]
            if (!Database.Exists(DbName, DBPath)) {
                using (ZipArchive dbZip = new ZipArchive(ResourceLoader.GetEmbeddedResourceStream(typeof(CoreApp).GetTypeInfo().Assembly, $"{DbName}.cblite2.zip"))) {
                    dbZip.ExtractToDirectory(DBPath);
                }
            }

            DB = new Database(DbName, new DatabaseConfiguration() { Directory = DBPath });
            COLL = DB.GetDefaultCollection();
            //end::OpenOrCreateDatabase[]
        }
        #endregion
    }
}
