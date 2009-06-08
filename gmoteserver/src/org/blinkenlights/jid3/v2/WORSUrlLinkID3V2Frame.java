/*
 * WORSUrlLinkID3V2Frame.java
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
 * $Id: WORSUrlLinkID3V2Frame.java,v 1.8 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains a pointer to the official internet radio station web page from which this track originated.
 */
public class WORSUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sOfficialInternetRadioStationUrl URL pointing to the official internet radio station from which this
     *        track originated
     * @throws ID3Exception if the URL passed is null
     */
    public WORSUrlLinkID3V2Frame(String sOfficialInternetRadioStationUrl)
        throws ID3Exception
    {
        super(sOfficialInternetRadioStationUrl);
    }
    
    /** Constructor.
     *
     * @param oOfficialInternetRadioStationUrl URL pointing to the official internet radio station from which this
     *        track originated
     * @throws ID3Exception if the URL passed is null
     */
    public WORSUrlLinkID3V2Frame(URL oOfficialInternetRadioStationUrl)
        throws ID3Exception
    {
        super(oOfficialInternetRadioStationUrl);
    }

    public WORSUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWORSUrlLinkID3V2Frame(this);
    }
    
    /** Set official internet radio station web page URL from which this track originated.
     *
     * @param sOfficialInternetRadioStationUrl a string containing an URL
     */
    public void setOfficialInternetRadioStationWebPage(String sOfficialInternetRadioStationUrl)
    {
        m_sURL = sOfficialInternetRadioStationUrl;
    }
    
    /** Set official internet radio station web page URL from which this track originated.
     *
     * @param oOfficialInternetRadioStationUrl an URL
     */
    public void setOfficialInternetRadioStationWebPage(URL oOfficialInternetRadioStationUrl)
    {
        m_sURL = oOfficialInternetRadioStationUrl.toExternalForm();
    }

    /** Get the official internet radio station web page URL from which this track originated.
     *  Note, there is no guarantee that this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getOfficialInternetRadioStationWebPage()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WORS".getBytes();
    }
    
    public String toString()
    {
        return "Official internet radio station homepage URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WORSUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WORSUrlLinkID3V2Frame oOtherWORS = (WORSUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWORS.m_sURL);
    }
}
