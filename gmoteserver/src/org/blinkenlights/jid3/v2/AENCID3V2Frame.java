/*
 * AENCID3V2Frame.java
 *
 * Created on Jan 17, 2004
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
 * $Id: AENCID3V2Frame.java,v 1.10 2005/02/06 18:11:20 paul Exp $
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
 * Frame containing audio encryption information.
 */
public class AENCID3V2Frame extends ID3V2Frame
{
    private String m_sOwnerIdentifier = null;
    private int m_iPreviewStartFrame;
    private int m_iPreviewLengthFrames;
    private byte[] m_abyEncryptionInfo = null;
    
    /** Constructor.
     *
     * @param sOwnerIdentifier an URL or email address, providing detail regarding the encryption of this file,
     *        or null if not used
     * @param iPreviewStartFrame the number of the first frame of this file which is not encrypted, or zero if 
     *        the entire file is encrypted
     * @param iPreviewLengthFrames the length in frames of the unencrypted preview portion of the file, or zero
     *        if the entire file is encrypted
     * @param abyEncryptionInfo any data which is required for decryption, as defined by the method used, or null
     *        if not required
     * @throws ID3Exception if either iPreviewStartFrame or iPreviewLengthFrames are negative, or greater than 65535
     */
    public AENCID3V2Frame(String sOwnerIdentifier,
                          int iPreviewStartFrame,
                          int iPreviewLengthFrames,
                          byte[] abyEncryptionInfo)
        throws ID3Exception
    {
        // owner identifier (if null replace with zero-length string)
        m_sOwnerIdentifier = sOwnerIdentifier;
        if (m_sOwnerIdentifier == null)
        {
            m_sOwnerIdentifier = "";
        }
        if ((iPreviewStartFrame < 0) || (iPreviewStartFrame > 65535))
        {
            throw new ID3Exception("Preview start frame must be unsigned 16-bit integer values.");
        }
        m_iPreviewStartFrame = iPreviewStartFrame;
        if ((iPreviewLengthFrames < 0) || (iPreviewLengthFrames > 65535))
        {
            throw new ID3Exception("Preview length in frames must be unsinged 16-bit integer values.");
        }
        m_iPreviewLengthFrames = iPreviewLengthFrames;
        // encryption info (if null replace with zero-length byte array)
        m_abyEncryptionInfo = abyEncryptionInfo;
        if (m_abyEncryptionInfo == null)
        {
            m_abyEncryptionInfo = new byte[0];
        }
    }
    
    public AENCID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
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
            
            // start frame of preview
            m_iPreviewStartFrame = oFrameDataID3DIS.readBEUnsigned16();
            
            // length in frames of preview
            m_iPreviewLengthFrames = oFrameDataID3DIS.readBEUnsigned16();
            
            // encryption info
            m_abyEncryptionInfo = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyEncryptionInfo);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitAENCID3V2Frame(this);
    }

    /** Set the owner identifier for the encryption described in this frame.
     *
     * @param sOwnerIdentifier an URL or an email address where the user can find information on decrypting this file
     * @throws ID3Exception if this frame is in a tag with another AENC frame which would have the same owner identifier
     */
    public void setOwnerIdentifier(String sOwnerIdentifier)
        throws ID3Exception
    {
        String sOrigOwnerIdentifier = m_sOwnerIdentifier;
        
        m_sOwnerIdentifier = sOwnerIdentifier;
        if (m_sOwnerIdentifier == null)
        {
            m_sOwnerIdentifier = "";
        }
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sOwnerIdentifier = sOrigOwnerIdentifier;
            
            throw e;
        }
    }

    /** Get the owner identifier for the encryption described in this frame.
     *
     * @return an URL or an email address where the user can find information on decrypting this file
     */
    public String getOwnerIdentifier()
    {
        return m_sOwnerIdentifier;
    }

    /** Set the range of the unencrypted preview section of this file.
     *
     * @param iPreviewStartFrame the number of the first frame of this file which is not encrypted, or zero if 
     *        the entire file is encrypted
     * @param iPreviewLengthFrames the length in frames of the unencrypted preview portion of the file, or zero
     *        if the entire file is encrypted
     * @throws ID3Exception if either iPreviewStartFrame or iPreviewLengthFrames are negative, or greater than 65535
     */
    public void setPreviewRange(int iPreviewStartFrame, int iPreviewLengthFrames)
        throws ID3Exception
    {
        if ((iPreviewStartFrame < 0) || (iPreviewStartFrame > 65535) ||
            (iPreviewLengthFrames < 0) || (iPreviewLengthFrames > 65535))
        {
            throw new ID3Exception("Preview start frame and frames length must be unsigned 16-bit integer values.");
        }
        m_iPreviewStartFrame = iPreviewStartFrame;
        m_iPreviewLengthFrames = iPreviewLengthFrames;
    }

    /** Get the starting frame of the unencrypted preview section of this file.
     *
     * @return the starting frame number, or zero if there is no preview section
     */
    public int getPreviewStartFrame()
    {
        return m_iPreviewStartFrame;
    }

    /** Get the length in frames of the preview section of this file.
     *
     * @return the length in frames, or zero if there is no preview section
     */
    public int getPreviewFramesLength()
    {
        return m_iPreviewLengthFrames;
    }

    /** Set any additional encryption info which will be required for the decryption of this file, based
     *  on the particular encryption method in use.
     *
     * @param abyEncryptionInfo any data which is required for decryption, as defined by the method used, or null
     *        if not required
     */
    public void setEncryptionInfo(byte[] abyEncryptionInfo)
    {
        m_abyEncryptionInfo = abyEncryptionInfo;
        if (m_abyEncryptionInfo == null)
        {
            m_abyEncryptionInfo = new byte[0];
        }
    }
    
    /** Get additional encryption info required for the particular method used.
     *
     * @return any additional encryption info, if required, or null
     */
    public byte[] getEncryptionInfo()
    {
        return m_abyEncryptionInfo;
    }

    protected byte[] getFrameId()
    {
        return "AENC".getBytes();
    }
    
    public String toString()
    {
        return "Audio encryption: Owner identifier=[" + m_sOwnerIdentifier + "], Preview start frame = " +
               m_iPreviewStartFrame + ", Preview length = " + m_iPreviewLengthFrames + ", Encryption info=[" +
               ID3Util.convertBytesToHexString(m_abyEncryptionInfo, true) + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // owner information
        oIDOS.write(m_sOwnerIdentifier.getBytes());
        oIDOS.writeUnsignedByte(0);
        // preview start frame
        oIDOS.writeBEUnsigned16(m_iPreviewStartFrame);
        // preview length in frames
        oIDOS.writeBEUnsigned16(m_iPreviewLengthFrames);
        // encryption info
        oIDOS.write(m_abyEncryptionInfo);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof AENCID3V2Frame)))
        {
            return false;
        }
        
        AENCID3V2Frame oOtherAENC = (AENCID3V2Frame)oOther;
        
        return (m_sOwnerIdentifier.equals(oOtherAENC.m_sOwnerIdentifier) &&
                (m_iPreviewStartFrame == oOtherAENC.m_iPreviewStartFrame) &&
                (m_iPreviewLengthFrames == oOtherAENC.m_iPreviewLengthFrames) &&
                Arrays.equals(m_abyEncryptionInfo, oOtherAENC.m_abyEncryptionInfo));
    }
}
