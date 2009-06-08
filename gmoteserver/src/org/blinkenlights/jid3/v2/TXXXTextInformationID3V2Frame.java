/*
 * TXXXTextInformationID3V2Frame.java
 *
 * Created on Jan 17, 2004
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
 * $Id: TXXXTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing user-defined infromation.
 */
public class TXXXTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sDescription = null;

    /** Constructor.
     *
     * @param sDescription a description of the information being stored
     * @param sInformation the information being stored
     * @throws ID3Exception if either the description or information are empty
     */
    public TXXXTextInformationID3V2Frame(String sDescription, String sInformation)
        throws ID3Exception
    {
        super(sInformation);
        
        if ((sDescription == null) || (sDescription.length() == 0))
        {
            throw new ID3Exception("Description required for TXXX frame.");
        }
        if ((sInformation == null) || (sInformation.length() == 0))
        {
            throw new ID3Exception("Information required for TXXX frame.");
        }
        
        m_sDescription = sDescription;
    }
    
    public TXXXTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);

            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());

            // description (read to null)
            m_sDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // information
            byte[] abyInformation = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyInformation);
            m_sInformation = new String(abyInformation, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTXXXTextInformationID3V2Frame(this);
    }

    /** Set the description of the information to be stored in this frame, along with the actual information.
     *
     * @param sDescription a description of the information being stored
     * @param sInformation the information being stored
     * @throws ID3Exception if either the description or information are empty, or if this TXXX frame is stored in a tag
     *                      where there is another TXXX frame with the same description
     */
    public void setDescriptionAndInformation(String sDescription, String sInformation)
        throws ID3Exception
    {
        String sOrigDescription = m_sDescription;
        String sOrigInformation = m_sInformation;
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        
        if ((sDescription == null) || (sDescription.length() == 0))
        {
            throw new ID3Exception("Description required for TXXX frame.");
        }
        if ((sInformation == null) || (sInformation.length() == 0))
        {
            throw new ID3Exception("Information required for TXXX frame.");
        }

        m_sDescription = sDescription;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sInformation;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sDescription = sOrigDescription;
            m_sInformation = sOrigInformation;
            m_oTextEncoding = oOrigTextEncoding;
            
            throw e;
        }
    }
    
    /** Get the description of the information stored in this frame.
     *
     * @return the description of the information stored in this frame
     */
    public String getDescription()
    {
        return m_sDescription;
    }
    
    /** Get the information stored in this frame.
     *
     * @return the information stored in this frame
     */
    public String getInformation()
    {
        return m_sInformation;
    }
    
    protected byte[] getFrameId()
    {
        return "TXXX".getBytes();
    }
    
    public String toString()
    {
        return "User-defined text: Description=[" + m_sDescription + "], Information=[" + m_sInformation + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        oIDOS.write(m_sDescription.getBytes(m_oTextEncoding.getEncodingString()));
        // null separating description from information string
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        oIDOS.write(m_sInformation.getBytes(m_oTextEncoding.getEncodingString()));
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TXXXTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TXXXTextInformationID3V2Frame oOtherTXXX = (TXXXTextInformationID3V2Frame)oOther;
        
        return (m_oTextEncoding.equals(oOtherTXXX.m_oTextEncoding) &&
                m_sDescription.equals(oOtherTXXX.m_sDescription) &&
                m_sInformation.equals(oOtherTXXX.m_sInformation));
    }
}
