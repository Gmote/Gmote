/*
 * WOASUrlLinkID3V2Frame.java
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
 * $Id: WOASUrlLinkID3V2Frame.java,v 1.8 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains a pointer to the official audio source web page pertaining to the track in this file.
 */
public class WOASUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sOfficialAudioSourceUrl URL pointing to the official audio source web page for this track
     * @throws ID3Exception if the URL passed is null
     */
    public WOASUrlLinkID3V2Frame(String sOfficialAudioSourceUrl)
        throws ID3Exception
    {
        super(sOfficialAudioSourceUrl);
    }
    
    /** Constructor.
     *
     * @param oOfficialAudioSourceUrl URL pointing to the official audio source web page for this track
     * @throws ID3Exception if the URL passed is null
     */
    public WOASUrlLinkID3V2Frame(URL oOfficialAudioSourceUrl)
        throws ID3Exception
    {
        super(oOfficialAudioSourceUrl);
    }

    public WOASUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWOASUrlLinkID3V2Frame(this);
    }
    
    /** Set official audio source web page URL for this track.
     *
     * @param sOfficialAudioSourceUrl a string containing an URL
     */
    public void setOfficialAudioSourceWebPage(String sOfficialAudioSourceUrl)
    {
        m_sURL = sOfficialAudioSourceUrl;
    }
    
    /** Set official audio source web page URL for this track.
     *
     * @param oOfficialAudioSourceUrl an URL
     */
    public void setOfficialAudioSourceWebPage(URL oOfficialAudioSourceUrl)
    {
        m_sURL = oOfficialAudioSourceUrl.toExternalForm();
    }

    /** Get the official audio source web page URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getOfficialAudioSourceWebPage()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WOAS".getBytes();
    }
    
    public String toString()
    {
        return "Official audio source webpage URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WOASUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WOASUrlLinkID3V2Frame oOtherWOAS = (WOASUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWOAS.m_sURL);
    }
}
