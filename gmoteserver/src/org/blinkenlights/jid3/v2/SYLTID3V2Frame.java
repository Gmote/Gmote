/*
 * SYLTID3V2Frame.java
 *
 * Created on September 4, 2004, 6:58 PM
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
 * $Id: SYLTID3V2Frame.java,v 1.10 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing synchronized lyrics/text.
 *
 * @author  paul
 */
public class SYLTID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sLanguage = null;
    private TimestampFormat m_oTimestampFormat = null;
    private ContentType m_oContentType = null;
    private String m_sContentDescriptor = null;
    private SortedMap m_oSyncEntryMap = null;
    
    /** Creates a new instance of SYLTID3V2Frame.
     *
     * @param sLanguage three letter language code for the content descriptor
     * @param oTimestampFormat the timestamp format used in this frame
     * @param oContentType the content for which synchronization is set
     * @param sContentDescriptor a unique text description for the synchronization details
     * @throws ID3Exception if language is not three characters,  or if oTimestampFormat, oContentType or sContentDescriptor are null
     */
    public SYLTID3V2Frame(String sLanguage, TimestampFormat oTimestampFormat, ContentType oContentType, String sContentDescriptor)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("A three character length language code is required in SYLT frame.");
        }
        m_sLanguage = sLanguage;
        if (oTimestampFormat == null)
        {
            throw new ID3Exception("Timestamp is required for SYLT frame.");
        }
        m_oTimestampFormat = oTimestampFormat;
        if (oContentType == null)
        {
            throw new ID3Exception("Content type is required for SYLT frame.");
        }
        m_oContentType = oContentType;
        if (sContentDescriptor == null)
        {
            throw new ID3Exception("Content descriptor is required for SYLT frame.");
        }
        m_sContentDescriptor = sContentDescriptor;
        // this is where sync entries go
        m_oSyncEntryMap = new TreeMap();
    }

    public SYLTID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);

            // header
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            byte[] abyLanguage = new byte[3];
            oFrameDataID3DIS.readFully(abyLanguage);
            m_sLanguage = new String(abyLanguage);
            m_oTimestampFormat = new SYLTID3V2Frame.TimestampFormat((byte)oFrameDataID3DIS.readUnsignedByte());
            m_oContentType = new SYLTID3V2Frame.ContentType((byte)oFrameDataID3DIS.readUnsignedByte());
            m_sContentDescriptor = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // sync entries
            m_oSyncEntryMap = new TreeMap();
            while (oFrameDataID3DIS.available() > 0)
            {
                String sText = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
                int iTimestamp = oFrameDataID3DIS.readBE32();
                
                m_oSyncEntryMap.put(new Integer(iTimestamp), sText);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitSYLTID3V2Frame(this);
    }
    
    /** Set the text encoding to be used for the content descriptor and text in this frame.
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

    /** Get the text encoding used for the content descriptor and text in this frame.
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
     *         contains another SYLT frame with the same language and content descriptor
     * @throws ID3Exception if this frame is in a tag with another SYLT frame which would have the same language and content descriptor
     */
    public void setLanguage(String sLanguage)
        throws ID3Exception
    {
        String sOrigLanguage = m_sLanguage;
        
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("A three character length language code is required in SYLT frame.");
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

    /** Set the timestamp format used.
     *
     * @param oTimestampFormat the timestamp format used in this frame
     */
    public void setTimestampFormat(TimestampFormat oTimestampFormat)
    {
        m_oTimestampFormat = oTimestampFormat;
    }
    
    /** Get the timestamp format used.
     *
     * @return the timestamp format
     */
    public TimestampFormat getTimestampFormat()
    {
        return m_oTimestampFormat;
    }

    /** Set the content type.
     *
     * @param oContentType the content type for this frame
     */
    public void setContentType(ContentType oContentType)
    {
        m_oContentType = oContentType;
    }
    
    /** Get the content type.
     *
     * @return the content type
     */
    public ContentType getContentType()
    {
        return m_oContentType;
    }

    /** Set the content descriptor for this frame.
     * 
     * @param sContentDescriptor the content descriptor for this frame
     * @throws ID3Exception if the content descriptor is null
     * @throws ID3Exception if this frame is in a tag with another SYLT frame which would have the same language and content descriptor
     */
    public void setContentDescriptor(String sContentDescriptor)
        throws ID3Exception
    {
        String sOrigContentDescriptor = m_sContentDescriptor;
        
        if (sContentDescriptor == null)
        {
            throw new ID3Exception("Content descriptor is required for SYLT frame.");
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
    
    /** Add a sync entry to the frame.
     *
     * @param oSyncEntry the sync entry to be added to this frame
     * @throws ID3Exception if oSyncEntry is null, or if a sync entry for this timestamp already exists in this frame
     */
    public void addSyncEntry(SyncEntry oSyncEntry)
        throws ID3Exception
    {
        if (oSyncEntry == null)
        {
            throw new ID3Exception("SyncEntry cannot be null.");
        }
        if (m_oSyncEntryMap.keySet().contains(new Integer(oSyncEntry.getTimestamp())))
        {
            throw new ID3Exception("SYLT frame already contains a sync entry for timestamp " + oSyncEntry.getTimestamp() + ".");
        }
        m_oSyncEntryMap.put(new Integer(oSyncEntry.getTimestamp()), oSyncEntry.getText());
    }

    /** Get a sync entry from this frame.
     *
     * @param iTimestamp the timestamp for which the sync entry should be returned
     * @return the sync entry object matching the specified timestamp, or null if no matching tiemstamp exists
     */
    public SyncEntry getSyncEntry(int iTimestamp)
    {
        if (m_oSyncEntryMap.keySet().contains(new Integer(iTimestamp)))
        {
            try
            {
                return new SyncEntry((String)m_oSyncEntryMap.get(new Integer(iTimestamp)), iTimestamp);
            }
            catch (Exception e) { return null; }    // we've already created this object, so this can't happen
        }
        else
        {
            return null;
        }
    }

    /** Remove a sync entry from this frame.
     *
     * @param iTimestamp the timestamp for which the sync entry is to be removed
     * @return the previously set sync entry for this timestamp, or null if no sync entry was set for this timestamp
     */
    public SyncEntry removeSyncEntry(int iTimestamp)
    {
        if (m_oSyncEntryMap.keySet().contains(new Integer(iTimestamp)))
        {
            try
            {
                return new SyncEntry((String)m_oSyncEntryMap.remove(new Integer(iTimestamp)), iTimestamp);
            }
            catch (Exception e) { return null; }    // we've already created this object, so this can't happen
        }
        else
        {
            return null;
        }
    }

    /** Get all timestamps for which entries have been set in this frame.
     *
     * @return an array of ints, containing all of the timestamps
     */
    public int[] getTimestamps()
    {
        int[] aiTimestamp = new int[m_oSyncEntryMap.keySet().size()];
        
        int i=0;
        Iterator oIter = m_oSyncEntryMap.keySet().iterator();
        while (oIter.hasNext())
        {
            aiTimestamp[i] = ((Integer)oIter.next()).intValue();
            i++;
        }
        
        return aiTimestamp;
    }
    
    protected byte[] getFrameId()
    {
        return "SYLT".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbText = new StringBuffer();
        sbText.append("Synchronized lyrics/text: Language=[" + m_sLanguage + "], Timestamp format=[" + m_oTimestampFormat.getValue() +
               "], Content type=[" + m_oContentType.getValue() + "], Content descriptor=[" + m_sContentDescriptor + "]");
        Iterator oIter = m_oSyncEntryMap.keySet().iterator();
        while (oIter.hasNext())
        {
            Integer oTimestamp = (Integer)oIter.next();
            String sText = (String)m_oSyncEntryMap.get(oTimestamp);
            sbText.append(" SyncEntry(" + oTimestamp.intValue() + ", " + sText + ")");
        }
        
        return sbText.toString();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // header
        oIDOS.write(m_oTextEncoding.getEncodingValue()); // text encoding
        oIDOS.write(m_sLanguage.getBytes());    // language
        oIDOS.write(m_oTimestampFormat.getValue()); // timestamp format
        oIDOS.write(m_oContentType.getValue()); // content type
        oIDOS.write(m_sContentDescriptor.getBytes(m_oTextEncoding.getEncodingString()));   // content descriptor
        // null after content descriptor
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // sync entries
        Iterator oIter = m_oSyncEntryMap.keySet().iterator();
        while (oIter.hasNext())
        {
            Integer oTimestamp = (Integer)oIter.next();
            String sText = (String)m_oSyncEntryMap.get(oTimestamp);
            oIDOS.write(sText.getBytes(m_oTextEncoding.getEncodingString()));  // sync text
            // null after sync text
            if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
            {
                oIDOS.writeUnsignedByte(0);
            }
            else
            {
                oIDOS.writeUnsignedByte(0);
                oIDOS.writeUnsignedByte(0);
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
    
    /** Content type.  This class is used to set the content type for which synchronization events are set in this frame.
     */
    public static class ContentType
    {
        private byte m_byContentType;
        
        private ContentType(byte byContentType)
        {
            m_byContentType = byContentType;
        }
        
        private byte getValue()
        {
            return m_byContentType;
        }
        
        public static final ContentType OTHER = new ContentType((byte)0x00);
        public static final ContentType LYRICS = new ContentType((byte)0x01);
        public static final ContentType TEXT_TRANSCRIPTION = new ContentType((byte)0x02);
        public static final ContentType MOVEMENT = new ContentType((byte)0x03);
        public static final ContentType EVENTS = new ContentType((byte)0x04);
        public static final ContentType CHORD = new ContentType((byte)0x05);
        public static final ContentType TRIVIA = new ContentType((byte)0x06);
        
        public boolean equals(Object oOther)
        {
            try
            {
                ContentType oOtherCT = (ContentType)oOther;
                
                return (m_byContentType == oOtherCT.m_byContentType);
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    /** SyncEntry class.  This container class is used to hold a timestamp and message pair.
     */
    public static class SyncEntry
    {
        private String m_sText = null;
        private int m_iTimestamp;
        
        public SyncEntry(String sText, int iTimestamp)
            throws ID3Exception
        {
            if (sText == null)
            {
                throw new ID3Exception("Text cannot be null in sync entry in SYLT frame.");
            }
            m_sText = sText;
            if (iTimestamp < 0)
            {
                throw new ID3Exception("Timestamp cannot be negative in sync entry in SYLT frame.");
            }
            m_iTimestamp = iTimestamp;
        }
        
        public String getText()
        {
            return m_sText;
        }
        
        public int getTimestamp()
        {
            return m_iTimestamp;
        }
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof SYLTID3V2Frame)))
        {
            return false;
        }
        
        SYLTID3V2Frame oOtherSYLT = (SYLTID3V2Frame)oOther;

        return (m_oTextEncoding.equals(oOtherSYLT.m_oTextEncoding) &&
                m_sLanguage.equals(oOtherSYLT.m_sLanguage) &&
                m_oTimestampFormat.equals(oOtherSYLT.m_oTimestampFormat) &&
                m_oContentType.equals(oOtherSYLT.m_oContentType) &&
                m_sContentDescriptor.equals(oOtherSYLT.m_sContentDescriptor) &&
                m_oSyncEntryMap.equals(oOtherSYLT.m_oSyncEntryMap));
    }
}
