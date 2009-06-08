/*
 * UrlLinkID3V2Frame.java
 *
 * Created on 8-Jan-2004
 *
 * Copyright (C)2004 Paul Grebenc
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
 * $Id: UrlLinkID3V2Frame.java,v 1.10 2005/05/05 05:13:35 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.net.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;

/**
 * @author paul
 *
 * The base class for all URL frames.
 */
public abstract class UrlLinkID3V2Frame extends ID3V2Frame
{
    /** The URL content for this frame, represented as a string.  This class will not throw an exception
     *  if an invalid URL is specified, to ensure compatibility with broken implementations, so a string,
     *  rather than an URL, is used to store the value.
     */
    protected String m_sURL = null;
    
    public UrlLinkID3V2Frame()
    {
    }
    
    /** Constructor for user created frames.
     *
     * @param sURL the raw URL text to be stored in this frame when it is written
     * @throws ID3Exception if the URL passed is null
     */
    public UrlLinkID3V2Frame(String sURL)
        throws ID3Exception
    {
        if (sURL == null)
        {
            throw new ID3Exception("URL in an URL link ID3 V2 frame cannot be null.");
        }
        
        m_sURL = sURL;
    }
    
    /** Constructor for user created frames.
     *
     * @param oURL the raw URL from which text is to be stored in this frame when it is written
     * @throws ID3Exception if the URL passed is null
     */
    public UrlLinkID3V2Frame(URL oURL)
        throws ID3Exception
    {
        if (oURL == null)
        {
            throw new ID3Exception("URL in an URL link ID3 V2 frame cannot be null.");
        }
        
        m_sURL = oURL.toExternalForm();
    }
    
    /** Constructor to be used internally when reading frames from a file.
     *
     * @throws ID3Exception if there is any error parsing the URL frame data
     */
    public UrlLinkID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);

            // URL (ignore anything after a null)
            byte[] abyUrl = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyUrl);
            m_sURL = new String(abyUrl);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    /** Write the body of this frame to an output stream.
     *
     * @param oIDOS the ID3 output stream to which the frame body is to be written
     * @throws ID3Exception if there is any error writing the frame body data
     */
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_sURL.getBytes());
    }
}
