/*
 * WXXXUrlLinkID3V2Frame.java
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
 * $Id: WXXXUrlLinkID3V2Frame.java,v 1.13 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Url frame containing user-defined information.
 */
public class WXXXUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sDescription = null;
    
    /** Constructor.
     *
     * @param sDescription a description of the URL being stored
     * @param sUrl the URL being stored
     * @throws ID3Exception if the URL passed is null, or if the description is null
     */
    public WXXXUrlLinkID3V2Frame(String sDescription, String sUrl)
        throws ID3Exception
    {
        super(sUrl);
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sDescription == null)
        {
            throw new ID3Exception("Description cannot be null in WXXX frame.");
        }
        m_sDescription = sDescription;
        m_sURL = sUrl;
    }

    /** Constructor.
     *
     * @param sDescription a description of the URL being stored
     * @param oURL the URL being stored
     * @throws ID3Exception if the URL passed is null, or if the description is null
     */
    public WXXXUrlLinkID3V2Frame(String sDescription, URL oURL)
        throws ID3Exception
    {
        super(oURL);
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sDescription == null)
        {
            throw new ID3Exception("Description cannot be null in WXXX frame.");
        }
        m_sDescription = sDescription;
        m_sURL = oURL.toExternalForm();
    }
    
    public WXXXUrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);

            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());

            // description (read to null)
            m_sDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // url
            byte[] abyUrl = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyUrl);
            m_sURL = new String(abyUrl);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitWXXXUrlLinkID3V2Frame(this);
    }
    
    /** Set the description of the URL to be stored in this frame, along with the actual URL.
     *
     * @param sDescription a description of the URL being stored
     * @param sUrl the URL being stored
     * @throws ID3Exception if the URL passed is null, or if the description is null
     */
    public void setDescriptionAndUrl(String sDescription, String sUrl)
        throws ID3Exception
    {
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        String sOrigDescription = m_sDescription;
        String sOrigURL = m_sURL;
        
        if (sDescription == null)
        {
            throw new ID3Exception("Description cannot be null in WXXX frame.");
        }
        if (sUrl == null)
        {
            throw new ID3Exception("Url cannot be null in WXXX frame.");
        }

        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sDescription = sDescription;
        m_sURL = sUrl;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_oTextEncoding = oOrigTextEncoding;
            m_sDescription = sOrigDescription;
            m_sURL = sOrigURL;
            
            throw e;
        }
    }

    /** Set the description of the URL to be stored in this frame, along with the actual URL.
     *
     * @param sDescription a description of the URL being stored
     * @param oURL the URL being stored
     * @throws ID3Exception if the URL passed is null, or if the description is null
     */
    public void setDescriptionAndUrl(String sDescription, URL oURL)
        throws ID3Exception
    {
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        String sOrigDescription = m_sDescription;
        String sOrigURL = m_sURL;
        
        if (sDescription == null)
        {
            throw new ID3Exception("Description cannot be null in WXXX frame.");
        }
        if (oURL == null)
        {
            throw new ID3Exception("URL cannot be null in WXXX frame.");
        }

        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sDescription = sDescription;
        m_sURL = oURL.toExternalForm();
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_oTextEncoding = oOrigTextEncoding;
            m_sDescription = sOrigDescription;
            m_sURL = sOrigURL;
            
            throw e;
        }
    }
    
    /** Get the description of the URL stored in this frame.
     *
     * @return the description of the URL stored in this frame
     */
    public String getDescription()
    {
        return m_sDescription;
    }
    
    /** Get the URL stored in this frame.
     *
     * @return the URL stored in this frame (note returned value may not be a valid url)
     */
    public String getUrl()
    {
        return m_sURL;
    }
    
    /** Set the text encoding to be used for the description in this frame.
     *
     * @param oTextEncoding the text encoding to be used for this frame
     */
    public void setTextEncoding(TextEncoding oTextEncoding)
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Text encoding cannot be null.");
        }
        m_oTextEncoding = oTextEncoding;
    }

    /** Get the text encoding used for the description in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "WXXX".getBytes();
    }
    
    public String toString()
    {
        return "User-defined URL: Description=[" + m_sDescription + "], URL=[" + m_sURL + "]";
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        oIDOS.write(m_sDescription.getBytes(m_oTextEncoding.getEncodingString()));
        // null separating content descriptor from lyrics
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // url
        oIDOS.write(m_sURL.getBytes());
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof WXXXUrlLinkID3V2Frame)))
        {
            return false;
        }
        
        WXXXUrlLinkID3V2Frame oOtherWXXX = (WXXXUrlLinkID3V2Frame)oOther;
        
        return (m_sDescription.equals(oOtherWXXX.m_sDescription) &&
                m_oTextEncoding.equals(oOtherWXXX.m_oTextEncoding) &&
                m_sURL.equals(oOtherWXXX.m_sURL));
    }
}
