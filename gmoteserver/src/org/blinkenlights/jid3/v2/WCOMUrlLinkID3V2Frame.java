/*
 * WCOMUrlLinkID3V2Frame.java
 *
 * Created on 8-Jan-2004
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
 * $Id: WCOMUrlLinkID3V2Frame.java,v 1.10 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains commercial information pertaining to the track in this file.
 */
public class WCOMUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sCommercialInformationUrl URL pointing to commercial information pertaining to this track
     * @throws ID3Exception if the URL passed is null
     */
    public WCOMUrlLinkID3V2Frame(String sCommercialInformationUrl)
        throws ID3Exception
    {
        super(sCommercialInformationUrl);
    }
    
    /** Constructor.
     *
     * @param oCommercialInformationUrl URL pointing to commercial information pertaining to this track
     * @throws ID3Exception if the URL passed is null
     */
    public WCOMUrlLinkID3V2Frame(URL oCommercialInformationUrl)
        throws ID3Exception
    {
        super(oCommercialInformationUrl);
    }

    public WCOMUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWCOMUrlLinkID3V2Frame(this);
    }
    
    /** Set commercial information URL for this track.
     *
     * @param sCommercialInformationUrl a string containing an URL
     * @throws ID3Exception if the URL string passed is null, or if this frame is contained in a tag which already contains
     *                      another WCOM frame with the same URL
     */
    public void setCommercialInformation(String sCommercialInformationUrl)
        throws ID3Exception
    {
        String sOrigURL = m_sURL;
        
        if (sCommercialInformationUrl == null)
        {
            throw new ID3Exception("Commercial information URL string cannot be null in WCOM frame.");
        }
        
        m_sURL = sCommercialInformationUrl;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sURL = sOrigURL;
            
            throw e;
        }
    }
    
    /** Set commercial information URL for this track.
     *
     * @param oCommercialInformationUrl an URL
     * @throws ID3Exception if the URL passed is null, or if this frame is contained in a tag which already contains
     *                      another WCOM frame with the same URL
     */
    public void setCommercialInformation(URL oCommercialInformationUrl)
        throws ID3Exception
    {
        String sOrigURL = m_sURL;
        
        if (oCommercialInformationUrl == null)
        {
            throw new ID3Exception("Commerical information URL object cannot be null in WCOM frame.");
        }
        
        m_sURL = oCommercialInformationUrl.toExternalForm();
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sURL = sOrigURL;
            
            throw e;
        }
    }

    /** Get the commercial information URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getCommercialInformationUrl()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WCOM".getBytes();
    }
    
    public String toString()
    {
        return "Commercial information URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WCOMUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WCOMUrlLinkID3V2Frame oOtherWCOM = (WCOMUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWCOM.m_sURL);
    }
}
