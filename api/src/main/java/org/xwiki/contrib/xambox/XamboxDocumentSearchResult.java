package org.xwiki.contrib.xambox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Sample XML output

<GetDocumentSearchResponse>
<searchTotalResultsNumber>1</searchTotalResultsNumber>
<documentSearchList>
<GetDocumentSearch>
<pid>pchFs37GQxRiiKy3E</pid>
<title>toto</title>
<indexTime>2011-08-25 00:00:00.0 CEST</indexTime>
<originalExtention>pdf</originalExtention>
<processingStatus>pre-indexed</processingStatus>
<locationStatus>no-location</locationStatus>
<folders>
<GetDocumentFoldersList>
<name>Mes factures</name>
<code>mes-factures</code>
</GetDocumentFoldersList>
<GetDocumentFoldersList>
<name>Mes contrats</name>
<code>mes-contrats</code>
</GetDocumentFoldersList>
</folders>
</GetDocumentSearch>
</documentSearchList>
</GetDocumentSearchResponse>

 * @author ludovic
 *
 */
public class XamboxDocumentSearchResult
{
    protected String pid = "";
    protected String sharedId = "";
    protected String title = "";
    protected String locationStatus = "";
    protected String processingStatus = "";
    protected Date indexDate;
    protected Date alertDate;
    protected String thumbnailUrl = "";
    protected String previewUrl = "";
    protected String originalExtension = "";
    protected ArrayList<String> folders = new ArrayList<String>();
    

    public XamboxDocumentSearchResult() {
    }
    
    protected String getChildElement(Element element, String fieldName) {
        if (element==null)
            return "";
        
        NodeList list = element.getElementsByTagName(fieldName);
        if (list.getLength()>0)
         return list.item(0).getTextContent();
        else
         return "";
    }
    
    /**
     * Construct a search result document from an XML node result
     * @param node
     */
    public XamboxDocumentSearchResult(Element element) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z");
        
        setPid(getChildElement(element, "pid"));
        setSharedId(getChildElement(element, "sharedId"));
        setTitle(getChildElement(element, "title"));
        setLocationStatus(getChildElement(element, "locationStatus"));
        setProcessingStatus(getChildElement(element, "processingStatus"));
        setOriginalExtension(getChildElement(element, "originalExtention"));
        setThumbnailUrl(getChildElement(element, "thumbnailUrl"));
        setPreviewUrl(getChildElement(element, "previewUrl"));
        String indexTime = getChildElement(element, "indexTime");
        if (!indexTime.equals("")) {
            try {
                setIndexDate(dateFormat.parse(indexTime));
            } catch (ParseException e) {
            }
        }
        String alertTime = getChildElement(element, "alertTime");
        if (!alertTime.equals("")) {
            try {
                setIndexDate(dateFormat.parse(alertTime));
            } catch (ParseException e) {
            }
        }    
        
        NodeList foldersNode = element.getElementsByTagName(("GetDocumentFoldersList"));
        for (int i=0;i<foldersNode.getLength();i++) {
            String folderName = getChildElement(((Element)foldersNode.item(i)), "name");
            folders.add(folderName);
        }
    }
    
    public String getPid()
    {
        return pid;
    }
    public void setPid(String pid)
    {
        this.pid = pid;
    }
    public String getSharedId()
    {
        return sharedId;
    }
    public void setSharedId(String sharedId)
    {
        this.sharedId = sharedId;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getLocationStatus()
    {
        return locationStatus;
    }
    public void setLocationStatus(String locationStatus)
    {
        this.locationStatus = locationStatus;
    }
    public String getProcessingStatus()
    {
        return processingStatus;
    }
    public void setProcessingStatus(String processingStatus)
    {
        this.processingStatus = processingStatus;
    }
    public Date getIndexDate()
    {
        return indexDate;
    }
    public void setIndexDate(Date indexDate)
    {
        this.indexDate = indexDate;
    }
    public Date getAlertDate()
    {
        return alertDate;
    }
    public void setAlertDate(Date alertDate)
    {
        this.alertDate = alertDate;
    }
    public String getThumbnailUrl()
    {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getPreviewUrl()
    {
        return previewUrl;
    }
    public void setPreviewUrl(String previewUrl)
    {
        this.previewUrl = previewUrl;
    }
    public String getOriginalExtension()
    {
        return originalExtension;
    }
    public void setOriginalExtension(String originalExtension)
    {
        this.originalExtension = originalExtension;
    }
    public ArrayList<String> getFolders()
    {
        return folders;
    }
    public void setFolders(ArrayList<String> folders)
    {
        this.folders = folders;
    }

}
