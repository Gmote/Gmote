/*
 * RVRBID3V2Frame.java
 *
 * Created on September 4, 2004, 12:00 PM
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
 * $Id: RVRBID3V2Frame.java,v 1.8 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing reverb information.
 *
 * @author  paul
 */
public class RVRBID3V2Frame extends ID3V2Frame
{
    private int m_iReverbLeftMS;
    private int m_iReverbRightMS;
    private int m_iReverbBouncesLeft;
    private int m_iReverbBouncesRight;
    private int m_iReverbFeedbackLeftToLeft;
    private int m_iReverbFeedbackLeftToRight;
    private int m_iReverbFeedbackRightToRight;
    private int m_iReverbFeedbackRightToLeft;
    private int m_iPremixLeftToRight;
    private int m_iPremixRightToLeft;
    
    /** Creates a new instance of RVRBID3V2Frame.
     *
     * @param iReverbLeftMS the delay between bounces in milliseconds for the left channel (16-bit unsigned)
     * @param iReverbRightMS the delay between bounces in milliseconds for the right channel (16-bit unsigned)
     * @param iReverbBouncesLeft the number of bounces to make in the left channel (unsigned byte)
     * @param iReverbBouncesRight the number of bounces to make in the right channel (unsigned byte)
     * @param iReverbFeedbackLeftToLeft percentage of feedback from left to left (0-255)
     * @param iReverbFeedbackLeftToRight percentage of feedback from left to right (0-255)
     * @param iReverbFeedbackRightToRight percentage of feedback from right to right (0-255)
     * @param iReverbFeedbackRightToLeft percentage of feedback from right to left (0-255)
     * @param iPremixLeftToRight percentage of left channel mixed to right before reverb (0-255)
     * @param iPremixRightToLeft percentage of right channel mixed to left before reverb (0-255)
     * @throws ID3Exception if any value is out of its valid range
     */
    public RVRBID3V2Frame(int iReverbLeftMS,
                          int iReverbRightMS,
                          int iReverbBouncesLeft,
                          int iReverbBouncesRight,
                          int iReverbFeedbackLeftToLeft,
                          int iReverbFeedbackLeftToRight,
                          int iReverbFeedbackRightToRight,
                          int iReverbFeedbackRightToLeft,
                          int iPremixLeftToRight,
                          int iPremixRightToLeft)
        throws ID3Exception
    {
        if ((iReverbLeftMS < 0) || (iReverbLeftMS > 65535))
        {
            throw new ID3Exception("Reverb left milliseconds must be an unsigned 16-bit value in RVRB frame.");
        }
        m_iReverbLeftMS = iReverbLeftMS;
        if ((iReverbRightMS < 0) || (iReverbRightMS > 65535))
        {
            throw new ID3Exception("Reverb right milliseconds must be an unsigned 16-bit value in RVRB frame.");
        }
        m_iReverbRightMS = iReverbRightMS;
        if ((iReverbBouncesLeft < 0) || (iReverbBouncesLeft > 255))
        {
            throw new ID3Exception("Reverb bounces left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbBouncesLeft = iReverbBouncesLeft;
        if ((iReverbBouncesRight < 0) || (iReverbBouncesRight > 255))
        {
            throw new ID3Exception("Reverb bounces right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbBouncesRight = iReverbBouncesRight;
        if ((iReverbFeedbackLeftToLeft < 0) || (iReverbFeedbackLeftToLeft > 255))
        {
            throw new ID3Exception("Reverb feedback left to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackLeftToLeft = iReverbFeedbackLeftToLeft;
        if ((iReverbFeedbackLeftToRight < 0) || (iReverbFeedbackLeftToRight > 255))
        {
            throw new ID3Exception("Reverb feedback left to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackLeftToRight = iReverbFeedbackLeftToRight;
        if ((iReverbFeedbackRightToLeft < 0) || (iReverbFeedbackRightToLeft > 255))
        {
            throw new ID3Exception("Reverb feedback right to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackRightToLeft = iReverbFeedbackRightToLeft;
        if ((iReverbFeedbackRightToRight < 0) || (iReverbFeedbackRightToRight > 255))
        {
            throw new ID3Exception("Reverb feedback right to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackRightToRight = iReverbFeedbackRightToRight;
        if ((iPremixLeftToRight < 0) || (iPremixLeftToRight > 255))
        {
            throw new ID3Exception("Premix left to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iPremixLeftToRight = iPremixLeftToRight;
        if ((iPremixRightToLeft < 0) || (iPremixRightToLeft > 255))
        {
            throw new ID3Exception("Premix right to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iPremixRightToLeft = iPremixRightToLeft;
    }

    public RVRBID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // reverb left and right (16-bit unsigned)
            m_iReverbLeftMS = oFrameDataID3DIS.readBEUnsigned16();
            m_iReverbRightMS = oFrameDataID3DIS.readBEUnsigned16();
            
            // reverb and premix values (unsigned byte values)
            m_iReverbBouncesLeft = oFrameDataID3DIS.readUnsignedByte();
            m_iReverbBouncesRight = oFrameDataID3DIS.readUnsignedByte();
            m_iReverbFeedbackLeftToLeft = oFrameDataID3DIS.readUnsignedByte();
            m_iReverbFeedbackLeftToRight = oFrameDataID3DIS.readUnsignedByte();
            m_iReverbFeedbackRightToRight = oFrameDataID3DIS.readUnsignedByte();
            m_iReverbFeedbackRightToLeft = oFrameDataID3DIS.readUnsignedByte();
            m_iPremixLeftToRight = oFrameDataID3DIS.readUnsignedByte();
            m_iPremixRightToLeft = oFrameDataID3DIS.readUnsignedByte();
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitRVRBID3V2Frame(this);
    }
    
    /** Set the reverb details for this track.
     *
     * @param iReverbLeftMS the delay between bounces in milliseconds for the left channel (16-bit unsigned)
     * @param iReverbRightMS the delay between bounces in milliseconds for the right channel (16-bit unsigned)
     * @param iReverbBouncesLeft the number of bounces to make in the left channel (unsigned byte)
     * @param iReverbBouncesRight the number of bounces to make in the right channel (unsigned byte)
     * @param iReverbFeedbackLeftToLeft percentage of feedback from left to left (0-255)
     * @param iReverbFeedbackLeftToRight percentage of feedback from left to right (0-255)
     * @param iReverbFeedbackRightToRight percentage of feedback from right to right (0-255)
     * @param iReverbFeedbackRightToLeft percentage of feedback from right to left (0-255)
     * @param iPremixLeftToRight percentage of left channel mixed to right before reverb (0-255)
     * @param iPremixRightToLeft percentage of right channel mixed to left before reverb (0-255)
     * @throws ID3Exception if any value is out of its valid range
     */
    public void setReverbDetails(int iReverbLeftMS,
                                 int iReverbRightMS,
                                 int iReverbBouncesLeft,
                                 int iReverbBouncesRight,
                                 int iReverbFeedbackLeftToLeft,
                                 int iReverbFeedbackLeftToRight,
                                 int iReverbFeedbackRightToRight,
                                 int iReverbFeedbackRightToLeft,
                                 int iPremixLeftToRight,
                                 int iPremixRightToLeft)
        throws ID3Exception
    {
        if ((iReverbLeftMS < 0) || (iReverbLeftMS > 65535))
        {
            throw new ID3Exception("Reverb left milliseconds must be an unsigned 16-bit value in RVRB frame.");
        }
        m_iReverbLeftMS = iReverbLeftMS;
        if ((iReverbRightMS < 0) || (iReverbRightMS > 65535))
        {
            throw new ID3Exception("Reverb right milliseconds must be an unsigned 16-bit value in RVRB frame.");
        }
        m_iReverbRightMS = iReverbRightMS;
        if ((iReverbBouncesLeft < 0) || (iReverbBouncesLeft > 255))
        {
            throw new ID3Exception("Reverb bounces left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbBouncesLeft = iReverbBouncesLeft;
        if ((iReverbBouncesRight < 0) || (iReverbBouncesRight > 255))
        {
            throw new ID3Exception("Reverb bounces right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbBouncesRight = iReverbBouncesRight;
        if ((iReverbFeedbackLeftToLeft < 0) || (iReverbFeedbackLeftToLeft > 255))
        {
            throw new ID3Exception("Reverb feedback left to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackLeftToLeft = iReverbFeedbackLeftToLeft;
        if ((iReverbFeedbackLeftToRight < 0) || (iReverbFeedbackLeftToRight > 255))
        {
            throw new ID3Exception("Reverb feedback left to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackLeftToRight = iReverbFeedbackLeftToRight;
        if ((iReverbFeedbackRightToLeft < 0) || (iReverbFeedbackRightToLeft > 255))
        {
            throw new ID3Exception("Reverb feedback right to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackRightToLeft = iReverbFeedbackRightToLeft;
        if ((iReverbFeedbackRightToRight < 0) || (iReverbFeedbackRightToRight > 255))
        {
            throw new ID3Exception("Reverb feedback right to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iReverbFeedbackRightToRight = iReverbFeedbackRightToRight;
        if ((iPremixLeftToRight < 0) || (iPremixLeftToRight > 255))
        {
            throw new ID3Exception("Premix left to right value must be between 0 and 255 in RVRB frame.");
        }
        m_iPremixLeftToRight = iPremixLeftToRight;
        if ((iPremixRightToLeft < 0) || (iPremixRightToLeft > 255))
        {
            throw new ID3Exception("Premix right to left value must be between 0 and 255 in RVRB frame.");
        }
        m_iPremixRightToLeft = iPremixRightToLeft;
    }
    
    /* Get the delay between bounces in milliseconds for the left channel (16-bit unsigned).
     *
     * @return the delay between bounces in milliseconds for the left channel
     */
    public int getReverbLeftMS()
    {
        return m_iReverbLeftMS;
    }
    
    /* Get the delay between bounces in milliseconds for the right channel (16-bit unsigned).
     *
     * @return the delay between bounces in milliseconds for the right channel
     */
    public int getReverbRightMS()
    {
        return m_iReverbRightMS;
    }
    
    /* Get the number of bounces to make in the left channel (unsigned byte).
     *
     * @return the number of bounces to make in the left channel
     */
    public int getReverbBouncesLeft()
    {
        return m_iReverbBouncesLeft;
    }
    
    /* Get the number of bounces to make in the right channel (unsigned byte).
     *
     * @return the number of bounces to make in the right channel
     */
    public int getReverbBouncesRight()
    {
        return m_iReverbBouncesRight;
    }
    
    /* Get the percentage of feedback from left to left (0-255).
     *
     * @return the percentage of feedback from left to left
     */
    public int getReverbFeedbackLeftToLeft()
    {
        return m_iReverbFeedbackLeftToLeft;
    }
    
    /* Get the percentage of feedback from left to right (0-255).
     *
     * @return the percentage of feedback from left to right
     */
    public int getReverbFeedbackLeftToRight()
    {
        return m_iReverbFeedbackLeftToRight;
    }
    
    /* Get the percentage of feedback from right to right (0-255).
     *
     * @return the percentage of feedback from right to right
     */
    public int getReverbFeedbackRightToRight()
    {
        return m_iReverbFeedbackRightToRight;
    }
    
    /* Get the percentage of feedback from right to left (0-255).
     *
     * return the percentage of feedback from right to left
     */
    public int getReverbFeedbackRightToLeft()
    {
        return m_iReverbFeedbackRightToLeft;
    }
    
    /* Get the percentage of left channel mixed to right before reverb (0-255).
     *
     * @return the percentage of left channel mixed to right before reverb
     */
    public int getPremixLeftToRight()
    {
        return m_iPremixLeftToRight;
    }
    
    /* Get the percentage of right channel mixed to left before reverb (0-255).
     *
     * @return the percentage of right channel mixed to left before reverb
     */
    public int getPremixRightToLeft()
    {
        return m_iPremixRightToLeft;
    }

    protected byte[] getFrameId()
    {
        return "RVRB".getBytes();
    }
    
    public String toString()
    {
        return "Reverb: Reverb Left (ms)=[" + m_iReverbLeftMS + "], Reverb Right (ms)=" + m_iReverbRightMS +
               "], Reverb Bounces Left=[" + m_iReverbBouncesLeft +
               "], Reverb Bounches right=[" + m_iReverbBouncesRight +
               "], Reverb Feedback Left To Left=[" + m_iReverbFeedbackLeftToLeft +
               "], Reverb Feedback Left To Right=[" + m_iReverbFeedbackLeftToRight +
               "], Reverb Feedback Right To Right=[" + m_iReverbFeedbackRightToRight +
               "], Reverb Feedback Right To Left=[" + m_iReverbFeedbackRightToLeft +
               "], Premix Left To Right=[" + m_iPremixLeftToRight +
               "], Premix Right To Left=[" + m_iPremixRightToLeft + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.writeBEUnsigned16(m_iReverbLeftMS);
        oIDOS.writeBEUnsigned16(m_iReverbRightMS);
        oIDOS.writeUnsignedByte(m_iReverbBouncesLeft);
        oIDOS.writeUnsignedByte(m_iReverbBouncesRight);
        oIDOS.writeUnsignedByte(m_iReverbFeedbackLeftToLeft);
        oIDOS.writeUnsignedByte(m_iReverbFeedbackLeftToRight);
        oIDOS.writeUnsignedByte(m_iReverbFeedbackRightToRight);
        oIDOS.writeUnsignedByte(m_iReverbFeedbackRightToLeft);
        oIDOS.writeUnsignedByte(m_iPremixLeftToRight);
        oIDOS.writeUnsignedByte(m_iPremixRightToLeft);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof RVRBID3V2Frame)))
        {
            return false;
        }
        
        RVRBID3V2Frame oOtherRVRB = (RVRBID3V2Frame)oOther;
        
        return ((m_iReverbLeftMS == oOtherRVRB.m_iReverbLeftMS) &&
                (m_iReverbRightMS == oOtherRVRB.m_iReverbRightMS) &&
                (m_iReverbBouncesLeft == oOtherRVRB.m_iReverbBouncesLeft) &&
                (m_iReverbBouncesRight == oOtherRVRB.m_iReverbBouncesRight) &&
                (m_iReverbFeedbackLeftToLeft == oOtherRVRB.m_iReverbFeedbackLeftToLeft) &&
                (m_iReverbFeedbackLeftToRight == oOtherRVRB.m_iReverbFeedbackLeftToRight) &&
                (m_iReverbFeedbackRightToRight == oOtherRVRB.m_iReverbFeedbackRightToRight) &&
                (m_iReverbFeedbackRightToLeft == oOtherRVRB.m_iReverbFeedbackRightToLeft) &&
                (m_iPremixLeftToRight == oOtherRVRB.m_iPremixLeftToRight) &&
                (m_iPremixRightToLeft == oOtherRVRB.m_iPremixRightToLeft));
    }
}
