/*
 * ENCRID3V2Frame.java
 *
 * Created on Jan 26, 2004
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
 * $Id: ENCRID3V2Frame.java,v 1.12 2005/10/27 02:10:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Frame containing encryption information.
 */
public class ENCRID3V2Frame extends ID3V2Frame
{
    private String m_sOwnerIdentifier = null;
    private byte m_byMethodSymbol;
    private byte[] m_abyEncryptionData = null;
    
    /** Constructor.
     *
     * @param sOwnerIdentifier an URL or email address where decryption details can be found
     * @param byMethodSymbol a symbol which can be used to identify this encryption method in this file (methods below 0x80 are reserved)
     * @param abyEncryptionData any optional required data for this encryption method
     * @throws ID3Exception if sOwnerIdentifier is null
     */
    public ENCRID3V2Frame(String sOwnerIdentifier, byte byMethodSymbol, byte[] abyEncryptionData)
        throws ID3Exception
    {
        // owner identifier
        if (sOwnerIdentifier == null)
        {
            throw new ID3Exception("ENCR frame requires owner identifier string.");
        }
        m_sOwnerIdentifier = sOwnerIdentifier;
        
        // method symbol
        if ((byMethodSymbol & 0xff) < 0x80)
        {
            throw new ID3Exception("Encryption method symbols below 0x80 are reserved.");
        }
        m_byMethodSymbol = byMethodSymbol;
        
        // encryption data
        m_abyEncryptionData = abyEncryptionData;
        if (m_abyEncryptionData == null)
        {
            m_abyEncryptionData = new byte[0];
        }
    }

    public ENCRID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // owner identifier (read to null)
            ByteArrayOutputStream oOwnerIdentifierBAOS = new ByteArrayOutputStream();
            int iOwnerIdentifierByte;
            do
            {
                iOwnerIdentifierByte = oFrameDataID3DIS.readUnsignedByte();
                if (iOwnerIdentifierByte != 0)
                {
                    oOwnerIdentifierBAOS.write(iOwnerIdentifierByte);
                }
            }
            while (iOwnerIdentifierByte != 0);
            if (oOwnerIdentifierBAOS.size() > 0)
            {
                byte[] abyOwnerIdentifier = oOwnerIdentifierBAOS.toByteArray();
                m_sOwnerIdentifier = new String(abyOwnerIdentifier);
            }
            else
            {
                m_sOwnerIdentifier = "";
            }
            
            // method symbol
            m_byMethodSymbol = (byte)oFrameDataID3DIS.readUnsignedByte();
            
            // encryption data
            m_abyEncryptionData = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyEncryptionData);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitENCRID3V2Frame(this);
    }

    /** Set details for this encryption frame.
     *
     * @param sOwnerIdentifier an URL or email address where decryption details can be found
     * @param byMethodSymbol a symbol which can be used to identify this encryption method in this file
     * @param abyEncryptionData any optional required data for this encryption method
     * @throws ID3Exception if sOwnerIdentifier is null
     * @throws ID3Exception if this frame is in a tag with another ENCR frame which would have the method symbol
     */
    public void setEncryptionDetails(String sOwnerIdentifier, byte byMethodSymbol, byte[] abyEncryptionData)
        throws ID3Exception
    {
        String sOrigOwnerIdentifier = m_sOwnerIdentifier;
        byte byOrigMethodSymbol = m_byMethodSymbol;
        byte[] abyOrigEncryptionData = m_abyEncryptionData;
        
        if (sOwnerIdentifier == null)
        {
            throw new ID3Exception("ENCR frame requires owner identifier string.");
        }
        if ((byMethodSymbol & 0xff) < 0x80)
        {
            throw new ID3Exception("Encryption method symbols below 0x80 are reserved.");
        }

        // owner identifier
        m_sOwnerIdentifier = sOwnerIdentifier;
        
        // method symbol
        m_byMethodSymbol = byMethodSymbol;
        
        // encryption data
        m_abyEncryptionData = abyEncryptionData;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sOwnerIdentifier = sOrigOwnerIdentifier;
            m_byMethodSymbol = byOrigMethodSymbol;
            m_abyEncryptionData = abyOrigEncryptionData;
            
            throw e;
        }
    }
    
    /** Get the owner identifier for this encryption method.
     *
     * @return the owner identifier, which should be an URL or email address
     */
    public String getOwnerIdentifier()
    {
        return m_sOwnerIdentifier;
    }
    
    /** Get the symbol used for this encryption method in this file.
     *
     * @return the unique encryption method symbol
     */
    public byte getEncryptionMethodSymbol()
    {
        return m_byMethodSymbol;
    }
    
    /** Get additional encryption data for this method.
     *
     * @return additional encryption data, or null if none was provided
     */
    public byte[] getEncryptionData()
    {
        return m_abyEncryptionData;
    }

    protected byte[] getFrameId()
    {
        return "ENCR".getBytes();
    }
    
    public String toString()
    {
        return "Encryption Method Registration: Owner identifier=[" + m_sOwnerIdentifier +
               "], Method Symbol=[" + m_byMethodSymbol + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // owner identifier (and terminating null)
        oIDOS.write(m_sOwnerIdentifier.getBytes());
        oIDOS.writeUnsignedByte(0);
        // method symbol
        oIDOS.writeUnsignedByte(m_byMethodSymbol);
        // encryption data
        if (m_abyEncryptionData != null)
        {
            oIDOS.write(m_abyEncryptionData);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof ENCRID3V2Frame)))
        {
            return false;
        }
        
        ENCRID3V2Frame oOtherENCR = (ENCRID3V2Frame)oOther;
        
        return (m_sOwnerIdentifier.equals(oOtherENCR.m_sOwnerIdentifier) &&
                (m_byMethodSymbol == oOtherENCR.m_byMethodSymbol) &&
                Arrays.equals(m_abyEncryptionData, oOtherENCR.m_abyEncryptionData));
    }
}
