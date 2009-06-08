/*
 * WPAYUrlLinkID3V2Frame.java
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
 * $Id: WPAYUrlLinkID3V2Frame.java,v 1.9 2005/02/06 18:11:21 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame which contains the location at which payment for this recording can be made.
 */
public class WPAYUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    /** Constructor.
     *
     * @param sPaymentUrl URL pointing to the location where payment for this recording can be made
     * @throws ID3Exception if the URL passed is null
     */
    public WPAYUrlLinkID3V2Frame(String sPaymentUrl)
        throws ID3Exception
    {
        super(sPaymentUrl);
    }
    
    /** Constructor.
     *
     * @param oPaymentUrl URL pointing to the location where payment for this recording can be made
     * @throws ID3Exception if the URL passed is null
     */
    public WPAYUrlLinkID3V2Frame(URL oPaymentUrl)
        throws ID3Exception
    {
        super(oPaymentUrl);
    }

    public WPAYUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWPAYUrlLinkID3V2Frame(this);
    }
    
    /** Set the payment location URL for this track.
     *
     * @param sPaymentUrl a string containing an URL
     */
    public void setPaymentLocation(String sPaymentUrl)
    {
        m_sURL = sPaymentUrl;
    }
    
    /** Set the payment location URL for this track.
     *
     * @param oPaymentUrl an URL
     */
    public void setPaymentLocation(URL oPaymentUrl)
    {
        m_sURL = oPaymentUrl.toExternalForm();
    }

    /** Get the payment location URL for this track.  Note, there is no guarantee that
     * this value will in fact be a valid URL.
     *
     * @return a string containing the set URL
     */
    public String getPaymentLocationUrl()
    {
        return m_sURL;
    }

    protected byte[] getFrameId()
    {
        return "WPAY".getBytes();
    }
    
    public String toString()
    {
        return "Payment URL: [" + m_sURL + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WPAYUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WPAYUrlLinkID3V2Frame oOtherWPAY = (WPAYUrlLinkID3V2Frame)oOther;
        
        return m_sURL.equals(oOtherWPAY.m_sURL);
    }
}
