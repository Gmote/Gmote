/*
 * SYTCID3V2Frame.java
 *
 * Created on September 6, 2004, 12:11 AM
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
 * $Id: SYTCID3V2Frame.java,v 1.7 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing synchronized tempo codes.
 *
 * @author  paul
 */
public class SYTCID3V2Frame extends ID3V2Frame
{
    private TimestampFormat m_oTimestampFormat = null;
    private SortedMap m_oTempoChangeMap = null;
    
    /** Creates a new instance of SYTCID3V2Frame.
     *
     * @param oTimestampFormat the timestamp format used in this frame
     * @throws ID3Exception if oTimestampFormat is null
     */
    public SYTCID3V2Frame(TimestampFormat oTimestampFormat)
        throws ID3Exception
    {
        if (oTimestampFormat == null)
        {
            throw new ID3Exception("Timestamp cannot be null in SYTC frame.");
        }
        m_oTimestampFormat = oTimestampFormat;
        m_oTempoChangeMap = new TreeMap();
    }
    
    public SYTCID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // timestamp format
            m_oTimestampFormat = new TimestampFormat((byte)oFrameDataID3DIS.readUnsignedByte());
            
            // tempo changes
            m_oTempoChangeMap = new TreeMap();
            while (oFrameDataID3DIS.available() > 0)
            {
                // one byte if < 255, or sum of two bytes if >= 255
                int iBeatsPerMinute = oFrameDataID3DIS.readUnsignedByte();
                if (iBeatsPerMinute == 255)
                {
                    iBeatsPerMinute += oFrameDataID3DIS.readUnsignedByte();
                }
                int iTimestamp = oFrameDataID3DIS.readBE32();
                
                m_oTempoChangeMap.put(new Integer(iTimestamp), new Integer(iBeatsPerMinute));
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitSYTCID3V2Frame(this);
    }
    
    /** Add a tempo change to the frame.
     *
     * @param oTempoChange the tempo change to be added to this frame
     * @throws ID3Exception if oTempoChange is null, or if a sync entry for this timestamp already exists in this frame
     */
    public void addTempoChange(TempoChange oTempoChange)
        throws ID3Exception
    {
        if (oTempoChange == null)
        {
            throw new ID3Exception("TempoChange cannot be null.");
        }
        if (m_oTempoChangeMap.keySet().contains(new Integer(oTempoChange.getTimestamp())))
        {
            throw new ID3Exception("SYTC frame already contains a tempo change for timestamp " + oTempoChange.getTimestamp() + ".");
        }
        m_oTempoChangeMap.put(new Integer(oTempoChange.getTimestamp()), new Integer(oTempoChange.getBeatsPerMinute()));
    }

    /** Get a tempo change from this frame.
     *
     * @param iTimestamp the timestamp for which the sync entry should be returned
     * @return the tempo change object matching the specified timestamp, or null if no matching tiemstamp exists
     */
    public TempoChange getTempoChange(int iTimestamp)
    {
        if (m_oTempoChangeMap.keySet().contains(new Integer(iTimestamp)))
        {
            try
            {
                return new TempoChange(((Integer)m_oTempoChangeMap.get(new Integer(iTimestamp))).intValue(), iTimestamp);
            }
            catch (Exception e) { return null; }    // we've already created this object, so this can't happen
        }
        else
        {
            return null;
        }
    }

    /** Remove a tempo change from this frame.
     *
     * @param iTimestamp the timestamp for which the tempo change is to be removed
     * @return the previously set sync entry for this timestamp, or null if no tempo change was set for this timestamp
     */
    public TempoChange removeTempoChange(int iTimestamp)
    {
        if (m_oTempoChangeMap.keySet().contains(new Integer(iTimestamp)))
        {
            try
            {
                return new TempoChange(((Integer)m_oTempoChangeMap.remove(new Integer(iTimestamp))).intValue(), iTimestamp);
            }
            catch (Exception e) { return null; }    // we've already created this object, so this can't happen
        }
        else
        {
            return null;
        }
    }
    
    protected byte[] getFrameId()
    {
        return "SYTC".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbText = new StringBuffer();
        sbText.append("Synchronized tempo codes: Timestamp format=[" + m_oTimestampFormat.getValue() + "]");
        Iterator oIter = m_oTempoChangeMap.keySet().iterator();
        while (oIter.hasNext())
        {
            Integer oTimestamp = (Integer)oIter.next();
            Integer oBeatsPerMinute = (Integer)m_oTempoChangeMap.get(oTimestamp);
            sbText.append(" TempoChange(" + oTimestamp.intValue() + ", BPM=" + oBeatsPerMinute.intValue() + ")");
        }
        
        return sbText.toString();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_oTimestampFormat.getValue());
        // tempo changes
        Iterator oIter = m_oTempoChangeMap.keySet().iterator();
        while (oIter.hasNext())
        {
            Integer oTimestamp = (Integer)oIter.next();
            // beats per minute (one byte if < 255, sum of two bytes if >= 255)
            Integer oBeatsPerMinute = (Integer)m_oTempoChangeMap.get(oTimestamp);
            int iBeatsPerMinute = oBeatsPerMinute.intValue();
            if (iBeatsPerMinute >= 255)
            {
                oIDOS.write(255);
                oIDOS.write(iBeatsPerMinute - 255);
            }
            else
            {
                oIDOS.write(iBeatsPerMinute);
            }
            oIDOS.writeBE32(oTimestamp.intValue()); // timestamp
        }
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

    /** TempoChange class.  This container class is used to hold a timestamp and bpm value pair.
     */
    public static class TempoChange
    {
        private int m_iBeatsPerMinute;
        private int m_iTimestamp;

        /** Constructor.
         *
         * @param iBeatsPerMinute the number of beats per minute (0 = beat free, 1 = single beat stroke followed by beat free period)
         * @throws ID3Exception if beats per minute is outside range from 0-510, or if timestamp is negative
         */
        public TempoChange(int iBeatsPerMinute, int iTimestamp)
            throws ID3Exception
        {
            if ((iBeatsPerMinute < 0) || (iBeatsPerMinute > 510))
            {
                throw new ID3Exception("Beats per minute value must be between 0 and 510 in SYTC frame.");
            }
            m_iBeatsPerMinute = iBeatsPerMinute;
            if (iTimestamp < 0)
            {
                throw new ID3Exception("Timestamp cannot be negative in sync entry in SYLT frame.");
            }
            m_iTimestamp = iTimestamp;
        }

        /** Get the number of beats per minute.
         *
         * @return the number of beats per minute (0 = beat free, 1 = single beat stroke followed by beat free period)
         */
        public int getBeatsPerMinute()
        {
            return m_iBeatsPerMinute;
        }
        
        /** Get the timestamp.
         *
         * @return the timestamp value
         */
        public int getTimestamp()
        {
            return m_iTimestamp;
        }
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof SYTCID3V2Frame)))
        {
            return false;
        }
        
        SYTCID3V2Frame oOtherSYTC = (SYTCID3V2Frame)oOther;

        return (m_oTimestampFormat.equals(oOtherSYTC.m_oTimestampFormat) &&
                m_oTempoChangeMap.equals(oOtherSYTC.m_oTempoChangeMap));
    }
}
