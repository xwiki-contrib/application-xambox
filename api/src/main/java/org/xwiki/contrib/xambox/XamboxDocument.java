package org.xwiki.contrib.xambox;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * @author ludovic
 *
 *
<pid>foXMW33oBKdxJEprj</pid>
<title>toto</title>
<indexTime>2011-08-25 00:00:00.0 CEST</indexTime>
<originalExtention>pdf</originalExtention>
<processingStatus>pre-indexed</processingStatus>
<locationStatus>no-location</locationStatus>
<sharedId>2An8I34f51gnuKIW2</sharedId>
<sheetsCount>8</sheetsCount>
<nonBlankPagesCount>17</nonBlankPagesCount>
<locLowDivider>1</locLowDivider>
<contentFileExtention>pdf</contentFileExtention>
<contentFormat>pdf</contentFormat>
<contentFileSize>156</contentFileSize>
<contentValueType>local-file</contentValueType>
<GETDocumentFoldersList>
<GETDocumentFolders>
<name>Mes factures</name>
<code>mes-factures</code>
</GETDocumentFolders>
<GETDocumentFolders>
<name>Mes contrats</name>
<code>mes-contrats</code>
</GETDocumentFolders>
</GETDocumentFoldersList>
*/
public class XamboxDocument extends XamboxDocumentSearchResult
{
    protected String note = "";
       
    protected String contentFileExtension = "";
    protected String contentFormat = "";
    protected int contentFileSize = 0;
    protected String contentValueType = "";
    protected String contentUrl = "";
    protected ArrayList<String> pagesUrl = new ArrayList<String>();

    protected int sheetsCount = 0;
    protected int nonBlankPagesCount = 0;
    protected String seriesName = "";
    protected int boxNumber = 0;
    protected int locLowDivider = 0;

    public XamboxDocument() {
        super();
    }
      
    protected int getIntChildElement(Element el, String fieldName) {
        String value = getChildElement(el, fieldName);
        try {
            if (value.equals(""))
                return 0;
            else
                return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
     
    /**
     * Construct a search result document from an XML node result
     * @param node
     */
    public XamboxDocument(Element element) {
        super(element);
        setNote(getChildElement(element, "note"));
        setContentFileExtension(getChildElement(element, "contentFileExtention"));
        
        setContentFormat(getChildElement(element, "contentFormat"));
        setContentValueType(getChildElement(element, "contentValueType"));
        setContentUrl(getChildElement(element, "contentUrl"));
        setContentFileSize(getIntChildElement(element, "contentFileSize"));
        setSheetsCount(getIntChildElement(element, "sheetsCount"));
        setNonBlankPagesCount(getIntChildElement(element, "nonBlankPagesCount"));
        setSeriesName(getChildElement(element, "seriesName"));
        setBoxNumber(getIntChildElement(element, "boxNumber"));
        setLocLowDivider(getIntChildElement(element, "locLowDivider"));
        
        NodeList pagesUrlNode = element.getElementsByTagName(("pagesUrl"));
        for (int i=0;i<pagesUrlNode.getLength();i++) {
            String pageUrl = getChildElement(((Element)pagesUrlNode.item(i)), "string");
            pagesUrl.add(pageUrl);
        }       
    }
    
    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public int getSheetsCount()
    {
        return sheetsCount;
    }

    public void setSheetsCount(int sheetsCount)
    {
        this.sheetsCount = sheetsCount;
    }

    public int getNonBlankPagesCount()
    {
        return nonBlankPagesCount;
    }

    public void setNonBlankPagesCount(int nonBlankPagesCount)
    {
        this.nonBlankPagesCount = nonBlankPagesCount;
    }

    public String getSeriesName()
    {
        return seriesName;
    }

    public void setSeriesName(String seriesName)
    {
        this.seriesName = seriesName;
    }

    public int getBoxNumber()
    {
        return boxNumber;
    }

    public void setBoxNumber(int boxNumber)
    {
        this.boxNumber = boxNumber;
    }

    public int getLocLowDivider()
    {
        return locLowDivider;
    }

    public void setLocLowDivider(int locLowDivider)
    {
        this.locLowDivider = locLowDivider;
    }

    public String getContentFileExtension()
    {
        return contentFileExtension;
    }

    public void setContentFileExtension(String contentFileExtension)
    {
        this.contentFileExtension = contentFileExtension;
    }

    public String getContentFormat()
    {
        return contentFormat;
    }

    public void setContentFormat(String contentFormat)
    {
        this.contentFormat = contentFormat;
    }

    public int getContentFileSize()
    {
        return contentFileSize;
    }

    public void setContentFileSize(int contentFileSize)
    {
        this.contentFileSize = contentFileSize;
    }

    public String getContentValueType()
    {
        return contentValueType;
    }

    public void setContentValueType(String contentValueType)
    {
        this.contentValueType = contentValueType;
    }

    public String getContentUrl()
    {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
    }

    public ArrayList<String> getPagesUrl()
    {
        return pagesUrl;
    }

    public void setPagesUrl(ArrayList<String> pagesUrl)
    {
        this.pagesUrl = pagesUrl;
    }
      
 
   
}
