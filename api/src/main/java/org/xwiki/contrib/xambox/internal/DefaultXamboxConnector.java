/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.xambox.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.api.Document;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.contrib.xambox.XamboxConnector;
import org.xwiki.contrib.xambox.XamboxDocument;
import org.xwiki.contrib.xambox.XamboxDocumentSearchResult;
import org.xwiki.contrib.xambox.XamboxException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.xml.XMLUtils;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.*;
import oauth.signpost.commonshttp.*;
import oauth.signpost.signature.*;
import org.apache.http.conn.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.conn.ssl.*;

import org.slf4j.Logger;

/**
 * Implementation of a <tt>HelloWorld</tt> component.
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXamboxConnector implements XamboxConnector, Initializable
{
    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * <p>
     * The XWiki component manager that is used to lookup XWiki components and context.
     * </p>
     */
    @Inject
    protected ComponentManager componentManager;

    /**
     * <p>
     * The query manager to be used to perform low-level queries for retrieving information about wiki content.
     * </p>
     */
    @Inject
    protected QueryManager queryManager;
    
    @Inject
    protected ConfigurationSource configuration;
    
    public final static String CONFIG_PAGE = "FileManager.XamboxSyncConfig";
    public final static String CONFIG_CLASS = "FileManager.XamboxSyncConfigClass";
    public final static String CONFIG_PREFIX = "xambox.";
    public final static String CONFIG_SERVER_URL = "serverurl";
    public final static String CONFIG_OAUTH_KEY = "oauthkey";
    public final static String CONFIG_OAUTH_SECRET = "oauthsecret";
    public final static String CONFIG_USERNAME = "username";
    public final static String CONFIG_PASSWORD = "password";

       
    public final static String DEFAULT_SERVER_URL = "https://aws.xambox.com/ws-rest/api/";
    public final static int DEFAULT_SERVER_PORT = 443;
    public final static String LOGIN_URI = "authentications?userLogin={userLogin}&userPassword={userPassword}";
    public final static String FOLDERS_URI = "folders?userToken={userToken}";
    public final static String DOCUMENTS_URI = "documents/search?userToken={userToken}&query={query}&offset={offset}&limit={limit}";
    public final static String DOCUMENT_URI = "documents?userToken={userToken}&documentPid={pid}";
       
    
    private String serverUrl = DEFAULT_SERVER_URL;
    private int serverPort = DEFAULT_SERVER_PORT;
    private String username = "";
    private String password = "";
    private String oauthKey = "";
    private String oauthSecret = "";
    private String oauthToken = "";
    
    private XWikiDocument configDoc;

    /** Helper object for manipulating DOM Level 3 Load and Save APIs. */
    private DOMImplementationLS lsImpl;


    public DefaultXamboxConnector() 
    {
        try {
            this.lsImpl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");
        } catch (Exception ex) {
        }
        
  }
    
    @Override
    public void initialize() throws InitializationException
    {
        try {
            XWikiContext context = getXWikiContext();
            setServer(getDefaultServerUrl(context), DEFAULT_SERVER_PORT);
            setOAuthKeys(getDefaultOAuthKey(context), getDefaultOAuthSecret(context));
            setCredentials(getDefaultUsername(context), getDefaultPassword(context));
          } catch (Exception ex) {
              ex.printStackTrace();
          }
    }

    
    protected XWikiDocument getConfigDocument(XWikiContext context) {
        if (configDoc==null)
            try {
                configDoc = context.getWiki().getDocument(CONFIG_PAGE, context);
            } catch (XWikiException e) {
                return null;
            }
        return configDoc;
    }

    protected String getConfigProperty(String name, String defaultValue, XWikiContext context) {
        try {
        XWikiDocument configDoc = getConfigDocument(context);
        BaseObject configObj = configDoc.getObject(CONFIG_CLASS);
        String prop = configObj.getStringValue(name);
        logger.debug("Config from object " + name + " is " + prop);
        if (prop==null || prop.equals("")) {
            prop = configuration.getProperty(CONFIG_PREFIX + name);
            logger.debug("Config from props " + name + " is " + prop);
        }
        if (prop==null || prop.equals("")) {
            prop = defaultValue;    
            logger.debug("Config from default " + name + " is " + prop);
        }
        return prop;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
       
    protected String getDefaultServerUrl(XWikiContext context) {
       return getConfigProperty(CONFIG_SERVER_URL, DEFAULT_SERVER_URL, context);
    }

    protected String getDefaultOAuthKey(XWikiContext context) {
        return getConfigProperty(CONFIG_OAUTH_KEY, "", context);
    }

    protected String getDefaultOAuthSecret(XWikiContext context) {
        return getConfigProperty(CONFIG_OAUTH_SECRET, "", context);
    }
    
    protected String getDefaultUsername(XWikiContext context) {
        return getConfigProperty(CONFIG_USERNAME, "", context);
    }

    protected String getDefaultPassword(XWikiContext context) {
        return getConfigProperty(CONFIG_PASSWORD, "", context);
    }

    @Override
    public void setServer(String serverUrl, int serverPort)
    {
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
    }

    @Override
    public void setOAuthKeys(String key, String secret)
    {
        this.oauthKey = key;
        this.oauthSecret = secret;
    }

    @Override
    public void setCredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public boolean login() throws XamboxException {
        String loginURI = LOGIN_URI;
        loginURI = loginURI.replaceAll("\\{userLogin\\}", this.username).replaceAll("\\{userPassword\\}", this.password);
        String xml = executeGetRequest(loginURI);
        org.w3c.dom.Document loginDoc = parseXML(xml);
        this.oauthToken = loginDoc.getFirstChild().getFirstChild().getTextContent();
        if (oauthToken.equals(""))
            return false;
        else
            return true;
    }

    @Override
    public String getUserToken()
    {
        return oauthToken;  
    }
    
    protected String getURI(String serviceUri) {
       return serviceUri.replaceAll("\\{userToken\\}", this.oauthToken);
    }

    @Override
    public List<String> getFolders()
    {
        String foldersURI = getURI(FOLDERS_URI);
        org.w3c.dom.Document xdoc = parseXML(executeGetRequest(foldersURI));
        ArrayList<String> list = new ArrayList<String>();
        NodeList nodeList = xdoc.getElementsByTagName("label");
        
        for (int i=0;i<nodeList.getLength();i++) {
             list.add(nodeList.item(i).getTextContent());
        }
        return list;
    }

    @Override
    public List<XamboxDocumentSearchResult> getDocuments(String query, int offset, int limit)
    {
        String documentsUri = getURI(DOCUMENTS_URI).replaceAll("\\{query\\}", URLEncoder.encode(query)).replaceAll("\\{offset\\}", "" + offset).replaceAll("\\{limit\\}", "" + limit);
        String result = executeGetRequest(documentsUri);

        org.w3c.dom.Document xdoc = parseXML(result);
        NodeList nodeList = xdoc.getElementsByTagName("GetDocumentSearch");
        ArrayList<XamboxDocumentSearchResult> list = new ArrayList<XamboxDocumentSearchResult>();
        for (int i = 0;i<nodeList.getLength();i++) {
            list.add(new XamboxDocumentSearchResult((Element) nodeList.item(i)));
        }
        return list;
    }

    @Override
    public XamboxDocument getDocument(String pid)
    {
        String documentUri = getURI(DOCUMENT_URI).replaceAll("\\{pid\\}", URLEncoder.encode(pid));
        String result = executeGetRequest(documentUri);

        org.w3c.dom.Document xdoc = parseXML(result);

        if (xdoc==null)
             return null;

        return new XamboxDocument((Element) xdoc.getFirstChild());
    }
    
    public org.w3c.dom.Document parseXML(String xml) {
        logger.debug("XML to parse: " + xml);
        LSInput input = this.lsImpl.createLSInput();
        input.setCharacterStream(new StringReader(xml)); 
        return XMLUtils.parse(input);
    }


    public String executeGetRequest(String serviceUrl) throws XamboxException {
        HttpUriRequest httpRequest = new HttpGet(serverUrl + serviceUrl);
        return executeRequest(httpRequest);
    }


    public String executeRequest(HttpUriRequest httpRequest) throws XamboxException {
        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            TrustStrategy ts = new XamboxConnectorTrustStrategy();

            SSLSocketFactory sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", serverPort, sf));
            ClientConnectionManager cm = new ThreadSafeClientConnManager(schemeRegistry);
            DefaultHttpClient httpClient = new DefaultHttpClient(cm);


            String responseFormat = ".xml";

            CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(oauthKey, oauthSecret);
            consumer.setMessageSigner(new HmacSha1MessageSigner());
            consumer.setTokenWithSecret(null, "");
            consumer.sign(httpRequest);
            // WARNING: These two settings should be set after the 'signing' call.
            consumer.setSendEmptyTokens(true);
            consumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
            HttpResponse httpResponse = httpClient.execute(httpRequest);

            StringBuffer textView = new StringBuffer();
            // Get the response
            BufferedReader rd = new BufferedReader
                (new InputStreamReader(httpResponse.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                textView.append(line);
            } 
            return textView.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new XamboxException(e);
        }
    }
    
    protected XWikiContext getXWikiContext() {
        Execution execution;
        XWikiContext xwikiContext;
        try {
            execution = componentManager.getInstance(Execution.class);
            xwikiContext = (XWikiContext) execution.getContext().getProperty("xwikicontext");
            return xwikiContext;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get XWiki context", e);
        }
    }
    
    public Document createFileManagerDocumentFromXambox(String space, XamboxDocument xdoc) {
        if (xdoc==null)
            return null;
        
        XWikiContext context = getXWikiContext();
        XWiki xwiki = context.getWiki();
        String pageName = xwiki.getUniquePageName(space, xdoc.getTitle(), context);
        XWikiDocument doc = new XWikiDocument(space, pageName);
        Document mydoc = new com.xpn.xwiki.api.Document(doc, context);
               
        try {
            // let's read the bytes of the Xambox file
            String contentUrl = xdoc.getContentUrl();
            byte[] content = xwiki.getURLContentAsBytes(contentUrl, context);
            
            mydoc.setParent(space + ".WebHome");
            mydoc.setTitle(xdoc.getTitle());
            mydoc.use(mydoc.newObject("FileManager.FileManagerClass"));
            mydoc.set("description", xdoc.getNote());
            
            // set the xambox specific data
            mydoc.use(mydoc.newObject("FileManager.XamboxDocumentClass"));
            mydoc.set("pid", xdoc.getPid());
            mydoc.set("indexDate", xdoc.getIndexDate());
            mydoc.set("locationStatus", xdoc.getLocationStatus());
            mydoc.set("processingStatus", xdoc.getProcessingStatus());
            mydoc.set("sheetsCount", xdoc.getSheetsCount());
            mydoc.set("nonBlankPagesCount", xdoc.getNonBlankPagesCount());
            mydoc.set("seriesName", xdoc.getSeriesName());
            mydoc.set("boxNumber", xdoc.getBoxNumber());
            mydoc.set("locLowDivider", xdoc.getLocLowDivider());
                       
            // transform folders into tags
            // TODO: fix this as we need to get the nice folder names, for now commenting it
            // mydoc.use(mydoc.newObject("XWiki.TagClass"));
            // mydoc.set("tags", xdoc.getFolders());
 
            mydoc.use(mydoc.newObject("XWiki.DocumentSheetBinding"));
            mydoc.set("sheet", "FileManager.XamboxDocumentSheet");
            
            String fileName = cleanFilename(xdoc.getTitle(), context) + "." + xdoc.getOriginalExtension(); 
            
            mydoc.addAttachment(fileName, content);
            // now let's save
            mydoc.save("Imported from Xambox");

            return mydoc;
            // now we need to get the file
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String cleanFilename(String fileName, XWikiContext context)
    {
        return context.getWiki().clearName(fileName, true, false, context);
    }


    public Document findXamboxDocument(String pid) throws XWikiException {
        String fullName = findXamboxDocumentName(pid);
        if (fullName==null)
            return null;
        XWikiContext context = getXWikiContext();
        XWikiDocument doc = context.getWiki().getDocument(fullName, context);
        return new Document(doc, context);
    }
    
    public String findXamboxDocumentName(String pid) {
        try {
            Query query;
            query = queryManager.createQuery("from doc.object(FileManager.XamboxDocumentClass) as file where file.pid=:pid",Query.XWQL);
            query.bindValue("pid", pid);
            List list = query.execute();
            if (list.size()==0)
                return null;
            else 
                return (String) list.get(0);
        } catch (QueryException e) {
            e.printStackTrace();
            return null;
        }
    }   
}

