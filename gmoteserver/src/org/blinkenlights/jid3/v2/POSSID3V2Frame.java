/*
 * POSSID3V2Frame.java
 *
 * Created on September 4, 2004, 12:51 AM
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
 * $Id: POSSID3V2Frame.java,v 1.8 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing position synchronization information.
 *
 * @author  paul
 */
public class POSSID3V2Frame extends ID3V2Frame
{
    private TimestampFormat m_oTimestampFormat;
    private int m_iPosition;
    
    /** Creates a new instance of POSSID3V2Frame.
     *
     * @param oTimestampFormat the format for timestamps, whether by millisecond or frame count
     * @param iPosition the position in the full recording/broadcast, of the first frame in this stream
     * @throws ID3Exception if oTimestampFormat is null, or if the position value is negative
     */
    public POSSID3V2Frame(TimestampFormat oTimestampFormat, int iPosition)
        throws ID3Exception
    {
        if (oTimestampFormat == null)
        {
            throw new ID3Exception("Timestamp format required in POSS frame.");
        }
        m_oTimestampFormat = oTimestampFormat;
        if (iPosition < 0)
        {
            throw new ID3Exception("Position cannot be negative in POSS frame.");
        }
        m_iPosition = iPosition;
    }

    public POSSID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and the position value
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // timestamp format
            m_oTimestampFormat = new TimestampFormat((byte)oFrameDataID3DIS.readUnsignedByte());
            
            // position value
            if ((oFrameDataID3DIS.available() >=1) && (oFrameDataID3DIS.available() <= 4))
            {
                byte[] abyPosition = new byte[4];
                int i;
                for (i=0; i < (4 - oFrameDataID3DIS.available()); i++)
                {
                    abyPosition[i] = 0;
                }
                while (oFrameDataID3DIS.available() > 0)
                {
                    abyPosition[i] = (byte)oFrameDataID3DIS.readUnsignedByte();
                    i++;
                }
                ByteArrayInputStream oBAIS = new ByteArrayInputStream(abyPosition);
                ID3DataInputStream oID3DIS = new ID3DataInputStream(oBAIS);
                m_iPosition = oID3DIS.readBE32();
            }
            else
            {
                throw new ID3Exception("Position value of " + oFrameDataID3DIS.available() + " bytes not supported in POSS frame.");
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitPOSSID3V2Frame(this);
    }
    
    /** Set position synchronization values for this frame.
     *
     * @param oTimestampFormat the format for timestamps, whether by millisecond or frame count
     * @param iPosition the position in the full recording/broadcast, of the first frame in this stream
     * @throws ID3Exception if oTimestampFormat is null, or if the position value is negative
     */
    public void setPositionSynchronizationValue(TimestampFormat oTimestampFormat, int iPosition)
        throws ID3Exception
    {
        if (oTimestampFormat == null)
        {
            throw new ID3Exception("Timestamp format required in POSS frame.");
        }
        m_oTimestampFormat = oTimestampFormat;
        if (iPosition < 0)
        {
            throw new ID3Exception("Position cannot be negative in POSS frame.");
        }
        m_iPosition = iPosition;
    }
    
    /** Get the timestamp format.
     *
     * @return the timestamp format
     */
    public TimestampFormat getTimestampFormat()
    {
        return m_oTimestampFormat;
    }

    /** Get the position of the start of this track.
     *
     * @return the position in the specified format, of the start of this track
     */
    public int getPosition()
    {
        return m_iPosition;
    }
    
    protected byte[] getFrameId()
    {
        return "POSS".getBytes();
    }
    
    public String toString()
    {
        return "Position Synchronization: Timestamp format=[" + m_oTimestampFormat.getValue() +
               "], Position=[" + m_iPosition + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // timestamp format
        oIDOS.writeUnsignedByte(m_oTimestampFormat.getValue());
        
        // position (always four bytes)
        oIDOS.writeBE32(m_iPosition);
    }
    
    /** Timestamp format.  The timestamp format is used to specify how offsets from the start of the
     *  the file are measured for events.
     */
    public static class TimestampFormat
    {
        private byte m_byTimestampFormat;
        
        private TimestampFormat(byte byTimestampFormat)
        {
            m_byTimestampFormat = byTimestampFormat;
        }
        
        private byte getValue()
        {
            return m_byTimestampFormat;
        }

        /** Timestamp format indicating that timestamps are measured in MPEG frames from the start of the file. */
        public static final TimestampFormat ABSOLUTE_MPEG_FRAMES = new TimestampFormat((byte)0x01);
        /** Timestamp format indicating that timestamps are measured in milliseconds from the start of the file. */
        public static final TimestampFormat ABSOLUTE_MILLISECONDS = new TimestampFormat((byte)0x02);

        public boolean equals(Object oOther)
        {
            try
            {
                TimestampFormat oOtherTF = (TimestampFormat)oOther;

                return (m_byTimestampFormat == oOtherTF.m_byTimestampFormat);
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof POSSID3V2Frame)))
        {
            return false;
        }
        
        POSSID3V2Frame oOtherPOSS = (POSSID3V2Frame)oOther;
        
        return (m_oTimestampFormat.equals(oOtherPOSS.m_oTimestampFormat) &&
                (m_iPosition == oOtherPOSS.m_iPosition));
    }
}
