/*
 * WPUBUrlLinkID3V2Frame.java
 *
 * Created on 9-Jan-2004
 *
 * Copyright (C)2004,2005 Paul Grebenc
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * $Id: WPUBUrlLinkID3V2Frame.java,v 1.9 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains the location of the web page of the publisher of the track in this file.
 */
public class WPUBUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sPublishersUrl URL pointing to web page of the publisher of this track
     * @throws ID3Exception if the URL passed is null
     */
    public WPUBUrlLinkID3V2Frame(String sPublishersUrl)
        throws ID3Exception
    {
        super(sPublishersUrl);
    }
    
    /** Constructor.
     *
     * @param oPublishersUrl URL pointing to web page of the publisher of this track
     * @throws ID3Exception if the URL passed is null
     */
    public WPUBUrlLinkID3V2Frame(URL oPublishersUrl)
        throws ID3Exception
    {
        super(oPublishersUrl);
    }

    public WPUBUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWPUBUrlLinkID3V2Frame(this);
    }
    
    /** Set publisher's web page URL for this track.
     *
     * @param sPublishersUrl a string containing an URL
     */
    public void setPublisherWebPage(String sPublishersUrl)
    {
        m_sURL = sPublishersUrl;
    }
    
    /** Set publisher's web page URL for this track.
     *
     * @param oPublishersUrl an URL
     */
    public void setPublisherWebPage(URL oPublishersUrl)
    {
        m_sURL = oPublishersUrl.toExternalForm();
    }

    /** Get the publisher's web page URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getPublisherWebPage()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WPUB".getBytes();
    }
    
    public String toString()
    {
        return "Publishers official webpage URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WPUBUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WPUBUrlLinkID3V2Frame oOtherWPUB = (WPUBUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWPUB.m_sURL);
    }
}
