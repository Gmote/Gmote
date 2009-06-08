/*
 * WCOPUrlLinkID3V2Frame.java
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
 * $Id: WCOPUrlLinkID3V2Frame.java,v 1.9 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains copyright or legal information for this track.
 */
public class WCOPUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sCopyrightLegalInformationUrl URL pointing to copyright or legal information pertaining to this track
     * @throws ID3Exception if the URL passed is null
     */
    public WCOPUrlLinkID3V2Frame(String sCopyrightLegalInformationUrl)
        throws ID3Exception
    {
        super(sCopyrightLegalInformationUrl);
    }
    
    /** Constructor.
     *
     * @param oCopyrightLegalInformationUrl URL pointing to copyright or legal information pertaining to this track
     * @throws ID3Exception if the URL passed is null
     */
    public WCOPUrlLinkID3V2Frame(URL oCopyrightLegalInformationUrl)
        throws ID3Exception
    {
        super(oCopyrightLegalInformationUrl);
    }

    public WCOPUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWCOPUrlLinkID3V2Frame(this);
    }
    
    /** Set copyright or legal information URL for this track.
     *
     * @param sCopyrightLegalInformationUrl a string containing an URL
     */
    public void setCopyrightLegalInformation(String sCopyrightLegalInformationUrl)
    {
        m_sURL = sCopyrightLegalInformationUrl;
    }
    
    /** Set copyright or legal information URL for this track.
     *
     * @param oCopyrightLegalInformationUrl an URL
     */
    public void setCopyrightLegalInformation(URL oCopyrightLegalInformationUrl)
    {
        m_sURL = oCopyrightLegalInformationUrl.toExternalForm();
    }

    /** Get the copyright or legal information URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getCopyrightLegalInformationUrl()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WCOP".getBytes();
    }
    
    public String toString()
    {
        return "Copyright/legal information URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WCOPUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WCOPUrlLinkID3V2Frame oOtherWCOP = (WCOPUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWCOP.m_sURL);
    }
}
