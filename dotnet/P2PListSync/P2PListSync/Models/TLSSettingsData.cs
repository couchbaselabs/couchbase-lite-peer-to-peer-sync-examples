﻿//
// TLSSettingsData.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using System.Collections.Generic;

namespace P2PListSync.Models
{
    public class TLSSetting
    {
        public int Index { get; set; }

        public string Setting { get; set; }

        public string Description { get; set; }
    }

    public static class TLSSettingsData
    {
        public static IList<TLSSetting> ListenerTLSMode { get; private set; }
        public static IList<TLSSetting> ListenerCertValidationMode { get; private set; }

        static TLSSettingsData()
        {
            ListenerTLSMode = new List<TLSSetting>() {
                new TLSSetting(){Index = 0, Setting = LISTENER_TLS_MODE.DISABLED.ToString(), Description="There is no TLS. All communication is plaintext (insecure mode and not recommended in production)"},
                new TLSSetting(){Index = 1, Setting = LISTENER_TLS_MODE.WITH_ANONYMOUS_AUTH.ToString(), Description="The app uses self-signed cert that is auto-generated by Couchbase Lite as TLSIdentity of the server. While server authentication is skipped, all encryption is still encrypted. This is the default mode of Couchbase Lite."},
                new TLSSetting(){Index = 2, Setting = LISTENER_TLS_MODE.WITH_BUNDLED_CERT.ToString(), Description="The app generates TLSIdentity of the server from public key cert and private key bundled in the listener-cert-pkey.p12 archive. Communication is encrypted"},
                new TLSSetting(){Index = 3, Setting = LISTENER_TLS_MODE.WITH_GENERATED_SELF_SIGNED_CERT.ToString(), Description="The app uses Couchbase Lite CreateIdentity convenience API to generate the TLSIdentity of the server. Communication is encrypted"}
            };

            ListenerCertValidationMode = new List<TLSSetting>() {
                new TLSSetting(){Index = 0, Setting = LISTENER_CERT_VALIDATION_MODE.ENABLE_VALIDATION.ToString(), Description="There is no authentication of server cert. The server cert is a self-signed cert. This is typically in used in dev or test environments. Skipping server cert authentication is discouraged in production environments. Communication is encrypted."},
                new TLSSetting(){Index = 1, Setting = LISTENER_CERT_VALIDATION_MODE.ENABLE_VALIDATION_WITH_CERT_PINNING.ToString(), Description="If the listener cert is from well known CA then you will use this mode. Of course, in our sample app, the listener cert as specified in listener-cert-pkey is a self signed cert - so you probably will not use this mode to test. But if you have a CA signed cert, you can configure your listener with the CA signed cert and use this mode to test. Communication is encrypted"},
                new TLSSetting(){Index = 2, Setting = LISTENER_CERT_VALIDATION_MODE.SKIP_VALIDATION.ToString(), Description="In this mode, the app uses the pinned cert,listener-pinned-cert.cer that is bundled in the app to validate the listener identity. Only the server cert that exactly matches the pinned cert will be authenticated. Communication is encrypted"}
            };
        }
    }
}
