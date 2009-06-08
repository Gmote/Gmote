/*
 * USLTID3V2Frame.java
 *
 * Created on 8-Jan-2004
 *
 * Copyright (C)2004-2005 Paul Grebenc
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
 * $Id: USLTID3V2Frame.java,v 1.10 2005/02/06 18:11:17 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing unsynchronized lyrics/text transcription.
 *
 * @author paul
 */
public class USLTID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sLanguage = null;
    private String m_sContentDescriptor = null;
    private String m_sLyrics = null;
    
    public USLTID3V2Frame(String sLanguage, String sContentDescriptor, String sLyrics)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sLanguage.length() != 3)
        {
            throw new ID3Exception("Language string length must be 3.");
        }
        m_sLanguage = sLanguage;
        m_sContentDescriptor = sContentDescriptor;
        m_sLyrics = sLyrics;
        if (m_sLyrics == null)
        {
            m_sLyrics = "";
        }
    }

    public USLTID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            
            // language
            byte[] abyLanguage = new byte[3];
            oFrameDataID3DIS.readFully(abyLanguage);
            m_sLanguage = new String(abyLanguage);
            
            // content descriptor (read to null)
            m_sContentDescriptor = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // lyrics
            byte[] abyLyrics = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyLyrics);
            m_sLyrics = new String(abyLyrics, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitUSLTID3V2Frame(this);
    }
    
    /** Set the text encoding to be used for the content descriptor and lyrics in this frame.
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

    /** Get the text encoding used for the content descriptor and lyrics in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }

    /** Set the language used in this frame.
     *
     * @param sLanguage a three letter language code
     * @throws ID3Exception if the language is null or not three characters in length or if this frame is in a tag which
     *         contains another USLT frame with the same language and content descriptor
     */
    public void setLanguage(String sLanguage)
        throws ID3Exception
    {
        String sOrigLanguage = m_sLanguage;
        
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("A three character length language code is required in USLT frame.");
        }

        m_sLanguage = sLanguage;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sLanguage = sOrigLanguage;
            
            throw e;
        }
    }

    /** Get the language used in this frame.
     *
     * @return a three letter language code
     */
    public String getLanguage()
    {
        return m_sLanguage;
    }

    /** Set the content descriptor for this frame.
     * 
     * @param sContentDescriptor the content descriptor for this frame
     * @throws ID3Exception if the content descriptor is null, or if this frame is in a tag which contains another
     *         USLT frame with the same language and content descriptor
     */
    public void setContentDescriptor(String sContentDescriptor)
        throws ID3Exception
    {
        String sOrigContentDescriptor = m_sContentDescriptor;
        
        if (sContentDescriptor == null)
        {
            throw new ID3Exception("Content descriptor is required for USLT frame.");
        }

        m_sContentDescriptor = sContentDescriptor;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sContentDescriptor = sOrigContentDescriptor;
            
            throw e;
        }
    }
    
    /** Get the content descriptor for this frame.
     *
     * @return the content descriptor
     */
    public String getContentDescriptor()
    {
        return m_sContentDescriptor;
    }

    /** Set the lyrics for this frame.
     *
     * @param sLyrics the lyrics for this frame
     */
    public void setLyrics(String sLyrics)
    {
        if (sLyrics == null)
        {
            m_sLyrics = "";
        }
        m_sLyrics = sLyrics;
    }
    
    /** Get the lyrics for this frame.
     *
     * @return the lyrics
     */
    public String getLyrics()
    {
        return m_sLyrics;
    }
    
    protected byte[] getFrameId()
    {
        return "USLT".getBytes();
    }
    
    public String toString()
    {
        return "Unsychronized lyrics: Language=[" + m_sLanguage + "], Content descriptor=[" +
               m_sContentDescriptor + "], Lyrics=[" + m_sLyrics + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // language
        oIDOS.write(m_sLanguage.getBytes());
        // content descriptor
        if (m_sContentDescriptor != null)
        {
            oIDOS.write(m_sContentDescriptor.getBytes(m_oTextEncoding.getEncodingString()));
        }
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
        // lyrics
        oIDOS.write(m_sLyrics.getBytes(m_oTextEncoding.getEncodingString()));
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof USLTID3V2Frame)))
        {
            return false;
        }
        
        USLTID3V2Frame oOtherUSLT = (USLTID3V2Frame)oOther;

        return (m_oTextEncoding.equals(oOtherUSLT.m_oTextEncoding) &&
                m_sLanguage.equals(oOtherUSLT.m_sLanguage) &&
                m_sContentDescriptor.equals(oOtherUSLT.m_sContentDescriptor) &&
                m_sLyrics.equals(oOtherUSLT.m_sLyrics));
    }
}
