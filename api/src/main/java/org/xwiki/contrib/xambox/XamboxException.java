package org.xwiki.contrib.xambox;

public class XamboxException extends RuntimeException
{
 public Throwable wrappedException; 
    
 public XamboxException(Throwable e) {
     this.wrappedException = e;
 }

}
