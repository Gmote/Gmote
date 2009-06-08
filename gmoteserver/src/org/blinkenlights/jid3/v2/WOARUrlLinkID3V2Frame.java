/*
 * WOARUrlLinkID3V2Frame.java
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
 * $Id: WOARUrlLinkID3V2Frame.java,v 1.8 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains a pointer to the official audio file web page pertaining to artist or
 * performer in the track in this file.
 */
public class WOARUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sOfficialArtistUrl URL pointing to the official artist or performer web page for this track
     * @throws ID3Exception if the URL passed is null
     */
    public WOARUrlLinkID3V2Frame(String sOfficialArtistUrl)
        throws ID3Exception
    {
        super(sOfficialArtistUrl);
    }
    
    /** Constructor.
     *
     * @param oOfficialArtistUrl URL pointing to the official artist or performer web page for this track
     * @throws ID3Exception if the URL passed is null
     */
    public WOARUrlLinkID3V2Frame(URL oOfficialArtistUrl)
        throws ID3Exception
    {
        super(oOfficialArtistUrl);
    }

    public WOARUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWOARUrlLinkID3V2Frame(this);
    }
    
    /** Set official artist or performer web page URL for this track.
     *
     * @param sOfficialArtistUrl a string containing an URL
     * @throws ID3Exception if the URL string passed is null, or if this frame is contained in a tag which already contains
     *                      another WOAR frame with the same URL
     */
    public void setOfficialArtistWebPage(String sOfficialArtistUrl)
        throws ID3Exception
    {
        String sOrigURL = m_sURL;
        
        if (sOfficialArtistUrl == null)
        {
            throw new ID3Exception("Official artist URL string cannot be null in WOAR frame.");
        }
        
        m_sURL = sOfficialArtistUrl;
        
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
    
    /** Set official artist or performer web page URL for this track.
     *
     * @param oOfficialArtistUrl an URL
     * @throws ID3Exception if the URL passed is null, or if this frame is contained in a tag which already contains
     *                      another WOAR frame with the same URL
     */
    public void setOfficialArtistWebPage(URL oOfficialArtistUrl)
        throws ID3Exception
    {
        String sOrigURL = m_sURL;
        
        if (oOfficialArtistUrl == null)
        {
            throw new ID3Exception("Official artist URL object cannot be null in WOAR frame.");
        }
        
        m_sURL = oOfficialArtistUrl.toExternalForm();
        
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

    /** Get the official artist or performer web page URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getOfficialArtistWebPage()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WOAR".getBytes();
    }
    
    public String toString()
    {
        return "Official artist/performer webpage URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WOARUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WOARUrlLinkID3V2Frame oOtherWOAR = (WOARUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWOAR.m_sURL);
    }
}
