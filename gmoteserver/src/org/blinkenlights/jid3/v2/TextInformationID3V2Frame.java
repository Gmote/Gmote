/*
 * TextInformationID3V2Frame.java
 *
 * Created on 26-Nov-2003
 *
 * Copyright (C)2003,2004 Paul Grebenc
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
 * $Id: TextInformationID3V2Frame.java,v 1.14 2005/02/06 18:11:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;

/**
 * @author paul
 *
 * The base class for all text frames.
 */
abstract class TextInformationID3V2Frame extends ID3V2Frame
{
    /** The text encoding of the strings in this frame. */
    protected TextEncoding m_oTextEncoding;

    /** The text content of this frame.  Based on the type of frame, there will be different
     * meanings, and potentailly unique restrictions, for this value.
     */
    protected String m_sInformation;
    
    protected TextInformationID3V2Frame()
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
    }
    
    /** Constructor for user created frames.
     *
     * @param oTextEncoding the text encoding
     * @param sInformation the raw text to be stored in this frame when it is written
     */
    protected TextInformationID3V2Frame(TextEncoding oTextEncoding, String sInformation)
    {
        // set the text and its encoding type for this text information frame
        m_oTextEncoding = oTextEncoding;
        m_sInformation = sInformation;
    }

    /** Constructor for user created frames.  Uses the current default text encoding.
     *
     * @param sInformation the raw text to be stored in this frame when it is written
     */
    protected TextInformationID3V2Frame(String sInformation)
    {
        // set the text and its encoding type for this text information frame
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sInformation;
    }
    
    /** Constructor to be used internally when reading frames from a file.
     *
     * @param oIS input stream from which to read the raw data in the frame, to be parsed into a text frame object
     * @throws ID3Exception if there is any error parsing the text frame data
     */
    public TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            byte[] abyInformation = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyInformation);
            m_sInformation = new String(abyInformation, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    /** Set the text encoding to be used for the text information in this frame.
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

    /** Get the text encoding used for the text information in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }

    /** Write the body of this frame to an output stream.
     *
     * @param oIDOS the ID3 output stream to which the frame body is to be written
     * @throws ID3Exception if there is any error writing the frame body data
     */
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        oIDOS.write(m_sInformation.getBytes(m_oTextEncoding.getEncodingString()));
    }
}
