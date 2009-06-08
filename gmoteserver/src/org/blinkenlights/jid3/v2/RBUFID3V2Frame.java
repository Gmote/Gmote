/*
 * RBUFID3V2Frame.java
 *
 * Created on September 4, 2004, 10:55 AM
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
 * $Id: RBUFID3V2Frame.java,v 1.8 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing recommended buffer size information.
 *
 * @author  paul
 */
public class RBUFID3V2Frame extends ID3V2Frame
{
    private int m_i24BufferSize;
    private boolean m_bEmbeddedInfoFlag;
    private int m_iOffsetToNextTag;
    
    /** Creates a new instance of RBUFID3V2Frame.
     *
     * @param i24BufferSize the recommended buffer size, as a 24-bit unsigned value
     * @param bEmbeddedInfoFlag an indicator whether or not a frame the of the specified maximum size may occur in the audiostream
     * @param iOffsetToNextTag the distance from the end of this tag to the start of the next
     * @throws ID3Exception if i24BufferSize contains an overflow value, or if iOffsetToNextTag is negative
     */
    public RBUFID3V2Frame(int i24BufferSize, boolean bEmbeddedInfoFlag, int iOffsetToNextTag)
        throws ID3Exception
    {
        if ((i24BufferSize < 0) || (i24BufferSize > (1<<24)-1))
        {
            throw new ID3Exception("Buffer size must be an unsigned 24-bit value in RBUF frame.");
        }
        m_i24BufferSize = i24BufferSize;
        m_bEmbeddedInfoFlag = bEmbeddedInfoFlag;
        if (iOffsetToNextTag < 0)
        {
            throw new ID3Exception("Offset to next tag cannot be negative in RBUF frame.");
        }
        m_iOffsetToNextTag = iOffsetToNextTag;
    }
    
    /** Creates a new instance of RBUFID3V2Frame.  (Omitting an offset value.)
     *
     * @param i24BufferSize the recommended buffer size, as a 24-bit unsigned value
     * @param bEmbeddedInfoFlag an indicator whether or not a frame the of the specified maximum size may occur in the audiostream
     * @throws ID3Exception if i24BufferSize contains an overflow value, or if iOffsetToNextTag is negative
     */
    public RBUFID3V2Frame(int i24BufferSize, boolean bEmbeddedInfoFlag)
        throws ID3Exception
    {
        if ((i24BufferSize < 0) || (i24BufferSize > (1<<24)-1))
        {
            throw new ID3Exception("Buffer size must be an unsigned 24-bit value in RBUF frame.");
        }
        m_i24BufferSize = i24BufferSize;
        m_bEmbeddedInfoFlag = bEmbeddedInfoFlag;
        m_iOffsetToNextTag = -1;
    }

    public RBUFID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // buffer size (24-bit value)
            m_i24BufferSize = oFrameDataID3DIS.readBE24();
            
            // embedded info
            int iEmbeddedInfoFlag = oFrameDataID3DIS.readUnsignedByte();
            m_bEmbeddedInfoFlag = ((iEmbeddedInfoFlag & 0x01) == 1) ? true : false;
            
            // offset to next tag (optional)
            if (oFrameDataID3DIS.available() > 0)
            {
                if (oFrameDataID3DIS.available() == 4)
                {
                    m_iOffsetToNextTag = oFrameDataID3DIS.readBE32();
                }
                else
                {
                    // too many bytes left
                    throw new ID3Exception("RBUF frame data longer than expected.");
                }
            }
            else
            {
                // no offset to next tag
                m_iOffsetToNextTag = -1;
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitRBUFID3V2Frame(this);
    }
    
    /** Set the recommended buffer size.
     *
     * @param i24BufferSize the recommended buffer size, as a 24-bit unsigned value
     * @param bEmbeddedInfoFlag an indicator whether or not a frame the of the specified maximum size may occur in the audiostream
     * @param iOffsetToNextTag the distance from the end of this tag to the start of the next
     * @throws ID3Exception if i24BufferSize contains an overflow value, or if iOffsetToNextTag is negative
     */
    public void setRecommendedBufferSize(int i24BufferSize, boolean bEmbeddedInfoFlag, int iOffsetToNextTag)
        throws ID3Exception
    {
        if ((i24BufferSize < 0) || (i24BufferSize > (1<<24)-1))
        {
            throw new ID3Exception("Buffer size must be an unsigned 24-bit value in RBUF frame.");
        }
        m_i24BufferSize = i24BufferSize;
        m_bEmbeddedInfoFlag = bEmbeddedInfoFlag;
        if (iOffsetToNextTag < 0)
        {
            throw new ID3Exception("Offset to next tag cannot be negative in RBUF frame.");
        }
        m_iOffsetToNextTag = iOffsetToNextTag;
    }

    /** Set the recommended buffer size.  (Omitting an offset value.)
     *
     * @param i24BufferSize the recommended buffer size, as a 24-bit unsigned value
     * @param bEmbeddedInfoFlag an indicator whether or not a frame the of the specified maximum size may occur in the audiostream
     * @throws ID3Exception if i24BufferSize contains an overflow value, or if iOffsetToNextTag is negative
     */
    public void setRecommendedBufferSize(int i24BufferSize, boolean bEmbeddedInfoFlag)
        throws ID3Exception
    {
        if ((i24BufferSize < 0) || (i24BufferSize > (1<<24)-1))
        {
            throw new ID3Exception("Buffer size must be an unsigned 24-bit value in RBUF frame.");
        }
        m_i24BufferSize = i24BufferSize;
        m_bEmbeddedInfoFlag = bEmbeddedInfoFlag;
        m_iOffsetToNextTag = -1;
    }

    /** Get recommended buffer size.
     *
     * @return the recommended buffer size
     */
    public int getBufferSize()
    {
        return m_i24BufferSize;
    }
    
    /** Get the embedded info flag.
     *
     * @return the embedded info flag
     */
    public boolean getEmbeddedInfoFlag()
    {
        return m_bEmbeddedInfoFlag;
    }
    
    /** Get the offset to the next tag.
     *
     * @return the offset to the next tag, or -1 if not specified
     */
    public int getOffsetToNextTag()
    {
        return m_iOffsetToNextTag;
    }
    
    protected byte[] getFrameId()
    {
        return "RBUF".getBytes();
    }
    
    public String toString()
    {
        return "Recommended buffer size: Buffer size=[" + m_i24BufferSize + "], Embedded info flag=" + m_bEmbeddedInfoFlag +
               "], Offset to next tag=[" + m_iOffsetToNextTag + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // recommended buffer size
        oIDOS.writeBE24(m_i24BufferSize);
        // embedded info flag
        oIDOS.writeUnsignedByte(m_bEmbeddedInfoFlag ? 1 : 0);
        // offset to next tag
        if (m_iOffsetToNextTag >= 0)
        {
            oIDOS.writeBE32(m_iOffsetToNextTag);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof RBUFID3V2Frame)))
        {
            return false;
        }
        
        RBUFID3V2Frame oOtherRBUF = (RBUFID3V2Frame)oOther;
        
        return ((m_i24BufferSize == oOtherRBUF.m_i24BufferSize) &&
                (m_bEmbeddedInfoFlag == oOtherRBUF.m_bEmbeddedInfoFlag) &&
                (m_iOffsetToNextTag == oOtherRBUF.m_iOffsetToNextTag));
    }
}
