/*
 * COMMID3V2Frame.java
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
 * $Id: COMMID3V2Frame.java,v 1.12 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Frame containing comments.
 */
public class COMMID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sLanguage = null;
    private String m_sShortDescription = null;
    private String m_sActualText = null;
    
    /** Constructor.
     *
     * @param sLanguage three letter language code for this comment
     * @param sShortDescription a short description of this comment (null or zero-length string for no description)
     * @param sActualText the actual text of the comment
     * @throws ID3Exception if the language code is not three characters in length
     * @throws ID3Exception if the actual text is null
     */
    public COMMID3V2Frame(String sLanguage, String sShortDescription, String sActualText)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("Language string in COMM frame must have length of 3.");
        }
        m_sLanguage = sLanguage;
        m_sShortDescription = sShortDescription;
        if (m_sShortDescription == null)
        {
            m_sShortDescription = "";
        }
        if (sActualText == null)
        {
            throw new ID3Exception("Comment text is required in COMM frame.");
        }
        m_sActualText = sActualText;
    }
    
    public COMMID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            byte byTextEncoding = (byte)oFrameDataID3DIS.readUnsignedByte();
            m_oTextEncoding = TextEncoding.getTextEncoding(byTextEncoding);
            
            // language
            byte[] abyLanguage = new byte[3];
            oFrameDataID3DIS.readFully(abyLanguage);
            m_sLanguage = new String(abyLanguage);
            
            // short description (read to null)
            m_sShortDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // actual comment
            byte[] abyActualText = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyActualText);
            m_sActualText = new String(abyActualText, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitCOMMID3V2Frame(this);
    }

    /** Set the language of this comment.
     *
     * @param sLanguage three letter language code for this comment
     * @param sShortDescription a short description of this comment (null or zero-length string for no description)
     * @param sActualText the actual text of the comment
     * @throws ID3Exception if the language code is not three characters in length
     * @throws ID3Exception if the actual text is null
     * @throws ID3Exception if this frame is in a tag with another COMM frame which would have the same language and short description
     */
    public void setComment(String sLanguage, String sShortDescription, String sActualText)
        throws ID3Exception
    {
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        String sOrigLanguage = m_sLanguage;
        String sOrigShortDescription = m_sShortDescription;
        String sOrigActualText = m_sActualText;
        
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("Language string in COMM frame must have length of 3.");
        }
        if (sActualText == null)
        {
            throw new ID3Exception("Comment text is required in COMM frame.");
        }
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sLanguage = sLanguage;
        m_sShortDescription = sShortDescription;
        if (m_sShortDescription == null)
        {
            m_sShortDescription = "";
        }
        m_sActualText = sActualText;
        
        // try this update, and reverse it if it generates an error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sLanguage = sOrigLanguage;
            m_sShortDescription = sShortDescription;
            m_sActualText = sOrigActualText;
            m_oTextEncoding = oOrigTextEncoding;
            
            throw e;
        }
    }

    /** Set the text encoding to be used for the short description and actual text in this frame.
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
    
    /** Get the text encoding used for the short description and actual text in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }

    /** Set the language of this comment.
     *
     * @param sLanguage the language of the comment
     */
    public void setLanguage(String sLanguage)
        throws ID3Exception
    {
        String sOrigLanguage = m_sLanguage;
        
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("Language string in COMM frame must have length of 3.");
        }
        m_sLanguage = sLanguage;

        // try this update, and reverse it if it generates an error
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
    
    /** Get the language of this comment.
     *
     * @return a three letter code defining the language used
     */
    public String getLanguage()
    {
        return m_sLanguage;
    }

    /** Get the short description of this comment.
     *
     * @return the short description of this comment
     */
    public String getShortDescription()
    {
        return m_sShortDescription;
    }
    
    /** Get the actual text of the comment.
     *
     * @return the actual text of the comment
     */
    public String getActualText()
    {
        return m_sActualText;
    }
    
    protected byte[] getFrameId()
    {
        return "COMM".getBytes();
    }
    
    public String toString()
    {
        return "Comment: Language=[" + m_sLanguage + "], Short description=[" +
               m_sShortDescription + "], Actual text=[" + m_sActualText + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // language
        oIDOS.write(m_sLanguage.getBytes());
        // short description
        if (m_sShortDescription != null)
        {
            oIDOS.write(m_sShortDescription.getBytes(m_oTextEncoding.getEncodingString()));
        }
        // null separating description from picture data
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // actual text of comment
        oIDOS.write(m_sActualText.getBytes(m_oTextEncoding.getEncodingString()));
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof COMMID3V2Frame)))
        {
            return false;
        }
        
        COMMID3V2Frame oOtherCOMM = (COMMID3V2Frame)oOther;
        
        return ((m_oTextEncoding.equals(oOtherCOMM.m_oTextEncoding)) &&
                m_sLanguage.equals(oOtherCOMM.m_sLanguage) &&
                m_sShortDescription.equals(oOtherCOMM.m_sShortDescription) &&
                m_sActualText.equals(oOtherCOMM.m_sActualText));
    }
}
