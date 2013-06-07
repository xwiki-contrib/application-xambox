package org.xwiki.contrib.xambox.internal;

import org.apache.http.conn.ssl.TrustStrategy;


public class XamboxConnectorTrustStrategy implements TrustStrategy
{
    @Override
    public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1) {
        return true;
     }
}
