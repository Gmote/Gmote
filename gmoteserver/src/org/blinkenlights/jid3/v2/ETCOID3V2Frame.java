/*
 * ETCOID3V2Frame.java
 *
 * Created on Jan 31, 2004
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
 * $Id: ETCOID3V2Frame.java,v 1.10 2005/02/06 18:11:15 paul Exp $
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
 * Frame containing event timing codes.
 */
public class ETCOID3V2Frame extends ID3V2Frame
{
    private TimestampFormat m_oTimestampFormat;
    private SortedMap m_oTimeToEventMap = null;
    
    /** Constructor.
     *
     * @param oTimestampFormat the format for timestamps, whether by millisecond or frame count
     * @throws ID3Exception if oTimestampFormat object is null
     */
    public ETCOID3V2Frame(TimestampFormat oTimestampFormat)
        throws ID3Exception
    {
        if (oTimestampFormat == null)
        {
            throw new ID3Exception("Timestamp format required in ETCO frame.");
        }
        m_oTimestampFormat = oTimestampFormat;
        m_oTimeToEventMap = new TreeMap();
    }
    
    public ETCOID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // timestamp format
            m_oTimestampFormat = new TimestampFormat((byte)oFrameDataID3DIS.readUnsignedByte());
            
            // read events and timestamps to end
            m_oTimeToEventMap = new TreeMap();
            while (oFrameDataID3DIS.available() > 0)
            {
                byte byTypeOfEvent = (byte)oFrameDataID3DIS.readUnsignedByte();
                EventType oEventType = new EventType(byTypeOfEvent);
                int iTimestamp = oFrameDataID3DIS.readBE32();
                
                addEvent(new Event(oEventType, iTimestamp));
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitETCOID3V2Frame(this);
    }

    /** Get the set timestamp format.
     *
     * @return the currently set timestamp format
     */
    public TimestampFormat getTimestampFormat()
    {
        return m_oTimestampFormat;
    }

    /** Add an event to the list.  Note, only one event per exact time can be defined.  An event set at
     *  a time for which another event already is set will overwrite the existing one.
     *
     * @param oEvent the event being set
     */
    public void addEvent(Event oEvent)
    {
        m_oTimeToEventMap.put(new Integer(oEvent.getTimestamp()), oEvent);
    }

    /** Get the event which has been set for a given time.
     *
     * @return the event set for the given time, or null if no event has been set for that time
     * @throws ID3Exception if the timestamp specified is negative
     */
    public Event getEvent(int iTimestamp)
        throws ID3Exception
    {
        if (iTimestamp < 0)
        {
            throw new ID3Exception("Negative timestamps are not valid in ETCO frames.");
        }
        
        return (Event)m_oTimeToEventMap.get(new Integer(iTimestamp));
    }
    
    /** Get all events which have been set.  Events are returned in sorted order by timestamp.
     *
     * @return an array of Events which have been set
     */
    public Event[] getEvents()
    {
        return (Event[])m_oTimeToEventMap.values().toArray(new Event[0]);
    }
    
    /** Remove the event set for a specific time.
     *
     * @return the event which was previously set for the given time, or null if no even was set for that time
     */
    public Event removeEvent(Event oEvent)
    {
        return (Event)m_oTimeToEventMap.remove(new Integer(oEvent.getTimestamp()));
    }
    
    protected byte[] getFrameId()
    {
        return "ETCO".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbOutput = new StringBuffer();
        sbOutput.append("Event Timing Codes: Timestamp format = " + m_oTimestampFormat.getValue());
        sbOutput.append(", Events = ");
        Iterator oIter = m_oTimeToEventMap.values().iterator();
        while (oIter.hasNext())
        {
            Event oEvent = (Event)oIter.next();
            sbOutput.append(oEvent.getEventType().getValue() + ":" + oEvent.getTimestamp() + " ");
        }

        return sbOutput.toString();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // timestamp format
        oIDOS.writeUnsignedByte(m_oTimestampFormat.getValue());

        // events
        Iterator oIter = m_oTimeToEventMap.values().iterator();
        while (oIter.hasNext())
        {
            Event oEvent = (Event)oIter.next();
            oIDOS.writeUnsignedByte(oEvent.getEventType().getValue());
            oIDOS.writeBE32(oEvent.getTimestamp());
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

    /** Event types. */
    public static class EventType
    {
        private byte m_byTypeOfEvent;
        
        private EventType(byte byTypeOfEvent)
        {
            m_byTypeOfEvent = byTypeOfEvent;
        }
        
        private byte getValue()
        {
            return m_byTypeOfEvent;
        }

        /** Pre-defined event types. */    
        public static final EventType PADDING = new EventType((byte)0x00);
        public static final EventType END_OF_INITIAL_SILENCE = new EventType((byte)0x01);
        public static final EventType INTRO_START = new EventType((byte)0x02);
        public static final EventType MAINPART_START = new EventType((byte)0x03);
        public static final EventType OUTRO_START = new EventType((byte)0x04);
        public static final EventType OUTRO_END = new EventType((byte)0x05);
        public static final EventType VERSE_START = new EventType((byte)0x06);
        public static final EventType REFRAIN_START = new EventType((byte)0x07);
        public static final EventType INTERLUDE_START = new EventType((byte)0x08);
        public static final EventType THEME_START = new EventType((byte)0x09);
        public static final EventType VARIATION_START = new EventType((byte)0x0a);
        public static final EventType KEY_CHANGE = new EventType((byte)0x0b);
        public static final EventType TIME_CHANGE = new EventType((byte)0x0c);
        public static final EventType MOMENTARY_UNWANTED_NOISE = new EventType((byte)0x0d);
        public static final EventType SUSTAINED_NOISE = new EventType((byte)0x0e);
        public static final EventType SUSTAINED_NOISE_END = new EventType((byte)0x0f);
        public static final EventType INTRO_END = new EventType((byte)0x10);
        public static final EventType MAINPART_END = new EventType((byte)0x11);
        public static final EventType VERSE_END = new EventType((byte)0x12);
        public static final EventType REFRAIN_END = new EventType((byte)0x13);
        public static final EventType THEME_END = new EventType((byte)0x14);
        public static final EventType USER_DEFINED_01 = new EventType((byte)0xe0);
        public static final EventType USER_DEFINED_02 = new EventType((byte)0xe1);
        public static final EventType USER_DEFINED_03 = new EventType((byte)0xe2);
        public static final EventType USER_DEFINED_04 = new EventType((byte)0xe3);
        public static final EventType USER_DEFINED_05 = new EventType((byte)0xe4);
        public static final EventType USER_DEFINED_06 = new EventType((byte)0xe5);
        public static final EventType USER_DEFINED_07 = new EventType((byte)0xe6);
        public static final EventType USER_DEFINED_08 = new EventType((byte)0xe7);
        public static final EventType USER_DEFINED_09 = new EventType((byte)0xe8);
        public static final EventType USER_DEFINED_10 = new EventType((byte)0xe9);
        public static final EventType USER_DEFINED_11 = new EventType((byte)0xea);
        public static final EventType USER_DEFINED_12 = new EventType((byte)0xeb);
        public static final EventType USER_DEFINED_13 = new EventType((byte)0xec);
        public static final EventType USER_DEFINED_14 = new EventType((byte)0xed);
        public static final EventType USER_DEFINED_15 = new EventType((byte)0xee);
        public static final EventType USER_DEFINED_16 = new EventType((byte)0xef);
        public static final EventType AUDIO_END = new EventType((byte)0xfd);
        public static final EventType AUDIO_FILE_ENDS = new EventType((byte)0xfe);
        //NOTE:  $FF seems to be defined in the spec, but I do not understand the definition...

        public boolean equals(Object oOther)
        {
            try
            {
                EventType oOtherET = (EventType)oOther;

                return (m_byTypeOfEvent == oOtherET.m_byTypeOfEvent);
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
    
    /** Event.  Events are comprised of a timestamp, and a type of event indicated for that time. */
    public static class Event
    {
        private EventType m_oEventType;
        private int m_iTimestamp;
        
        /** Constructor.
         *
         * @param oEventType the type of event to be indicated
         * @param iTimestamp the time of the event, specified in the chosen format
         * @throws ID3Exception if the timestamp value is negative
         */
        public Event(EventType oEventType, int iTimestamp)
            throws ID3Exception
        {
            if (iTimestamp < 0)
            {
                throw new ID3Exception("Negative timestamps are not valid in ETCO frames.");
            }
            
            m_oEventType = oEventType;
            m_iTimestamp = iTimestamp;
        }

        /** Get the type of event specified.
         *
         * @return the type of event specified
         */
        public EventType getEventType()
        {
            return m_oEventType;
        }

        /** Get the timestamp for this event.
         *
         * @return the timestamp for this event, to be interpreted in the current chosen format
         */
        public int getTimestamp()
        {
            return m_iTimestamp;
        }

        public boolean equals(Object oOther)
        {
            try
            {
                Event oOtherEvent = (Event)oOther;

                return ((m_iTimestamp == oOtherEvent.m_iTimestamp) &&
                        m_oEventType.equals(oOtherEvent.m_oEventType));
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof ETCOID3V2Frame)))
        {
            return false;
        }
        
        ETCOID3V2Frame oOtherETCO = (ETCOID3V2Frame)oOther;
        
        return (m_oTimestampFormat.equals(oOtherETCO.m_oTimestampFormat) &&
                m_oTimeToEventMap.equals(oOtherETCO.m_oTimeToEventMap));
    }
}
