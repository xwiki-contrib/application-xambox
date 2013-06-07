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
package org.xwiki.contrib.xambox;

import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import org.xwiki.component.annotation.Role;

/**
 * Interface (aka Role) of the Component
 */
@Role
public interface XamboxConnector
{
    public void setOAuthKeys(String key, String secret);
        
    public void setServer(String serverUrl, int serverPort);

    public void setCredentials(String username, String password);

    public String getUserToken();
    
    public boolean login() throws XamboxException;
    
    public List<String> getFolders();
    
    public List getDocuments(String query, int offset, int limit);

    public XamboxDocument getDocument(String pid);

    public org.w3c.dom.Document parseXML(String xml);
    
    public String executeGetRequest(String serviceUrl) throws XamboxException;
    
    public String executeRequest(HttpUriRequest httpRequest) throws XamboxException;

    public Document createFileManagerDocumentFromXambox(String space, XamboxDocument xdoc);
        
    public Document findXamboxDocument(String pid) throws XWikiException;
        
    public String findXamboxDocumentName(String pid);

}

