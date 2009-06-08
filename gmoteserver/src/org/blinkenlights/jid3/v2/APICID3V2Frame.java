/*
 * APICID3V2Frame.java
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
 * $Id: APICID3V2Frame.java,v 1.14 2005/02/06 18:11:17 paul Exp $
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
 * Frame containing an attached picture.
 */
public class APICID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sMimeType = null;
    private PictureType m_oPictureType;
    private String m_sDescription = null;
    private byte[] m_abyPictureData = null;

    /** Constructor.
     *
     * Note: It is valid to set the MIME type to "-->", and set the picture data to an URL pointing to the image,
     *       although this is discouraged.
     *
     * @param sMimeType the valid MIME type (ie. image/png) describing the format of the contained image (the default
     *                  if null is specified is "image/")
     * @param oPictureType the classification of the picture attached
     * @param sDescription an optional description of the image, or null if no description required
     * @param abyPictureData the data content of the image
     *
     * @throws ID3Exception if the description is longer than 64 characters
     * @throws ID3Exception if the picture data is null, or zero length
     */
    public APICID3V2Frame(String sMimeType,
                          PictureType oPictureType,
                          String sDescription,
                          byte[] abyPictureData)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sMimeType = sMimeType;
        if (m_sMimeType == null)
        {
            m_sMimeType = "image/";
        }
        m_oPictureType = oPictureType;
        if (sDescription.length() > 64)
        {
            // I have no idea why...
            throw new ID3Exception("Description in APIC frame cannot exceed 64 characters.");
        }
        m_sDescription = sDescription;
        if ((abyPictureData == null) || (abyPictureData.length == 0))
        {
            throw new ID3Exception("APIC frame requires picture data.");
        }
        m_abyPictureData = abyPictureData;
    }

    public APICID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            
            // mime type (read to null)
            ByteArrayOutputStream oMimeTypeBAOS = new ByteArrayOutputStream();
            int iMimeTypeByte;
            do
            {
                iMimeTypeByte = oFrameDataID3DIS.readUnsignedByte();
                if (iMimeTypeByte != 0)
                {
                    oMimeTypeBAOS.write(iMimeTypeByte);
                }
            }
            while (iMimeTypeByte != 0);
            if (oMimeTypeBAOS.size() > 0)
            {
                byte[] abyMimeType = oMimeTypeBAOS.toByteArray();
                m_sMimeType = new String(abyMimeType);
            }
            
            // picture type
            m_oPictureType = new APICID3V2Frame.PictureType((byte)oFrameDataID3DIS.readUnsignedByte());
            
            // description (read to null)
            m_sDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // picture data
            m_abyPictureData = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyPictureData);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitAPICID3V2Frame(this);
    }

    /** Set the MIME type for the image contained in this frame.
     *
     * Note: It is valid to set the MIME type to "-->", and set the picture data to an URL pointing to the image,
     *       although this is discouraged.
     *
     * @param sMimeType the valid MIME type (ie. image/png) describing the format of the contained image (the default
     *                  if null is specified is "image/")
     */
    public void setMimeType(String sMimeType)
    {
        m_sMimeType = sMimeType;
        if (m_sMimeType == null)
        {
            m_sMimeType = "image/";
        }
    }
    
    /** Get the MIME type of the image contained in this frame.  A MIME type of "-->" implies that the
     *  image data contains an URL reference to the actual image data.
     *
     * @return the specified MIME type
     */
    public String getMimeType()
    {
        return m_sMimeType;
    }
    
    /** Set the classification of the picture in this frame.
     *
     * @param oPictureType the type of the picture in this frame.
     */
    public void setPictureType(PictureType oPictureType)
    {
        m_oPictureType = oPictureType;
    }

    /** Get the classification of the picture in this frame.
     *
     * @return the type of the picture in this frame
     */
    public PictureType getPictureType()
    {
        return m_oPictureType;
    }

    /** Set the description for the picture in this frame.
     *
     * @param sDescription an optional description of the image, or null if no description required
     * @throws ID3Exception if the description is longer than 64 characters
     * @throws ID3Exception if this frame is in a tag with another APIC frame which would have the same description
     */
    public void setDescription(String sDescription)
        throws ID3Exception
    {
        String sOrigDescription = m_sDescription;
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        
        if (sDescription.length() > 64)
        {
            // I have no idea why...
            throw new ID3Exception("Description in APIC frame cannot exceed 64 characters.");
        }
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sDescription = sDescription;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sDescription = sOrigDescription;
            m_oTextEncoding = oOrigTextEncoding;
            
            throw e;
        }
    }
    
    /** Get the description for the picture in this frame.
     *
     * @return the set description, or null if no description has been defined
     */
    public String getDescription()
    {
        return m_sDescription;
    }

    /** Set the picture data for the image in this frame.
     *
     * @param abyPictureData the data content of the image
     *
     * @throws ID3Exception if the picture data is null, or zero length
     */
    public void setPictureData(byte[] abyPictureData)
        throws ID3Exception
    {
        if ((abyPictureData == null) || (abyPictureData.length == 0))
        {
            throw new ID3Exception("APIC frame requires picture data.");
        }
        m_abyPictureData = abyPictureData;
    }
    
    /** Get the picture data for the image in this frame.
     *
     * @return the picture data for the image
     */
    public byte[] getPictureData()
    {
        return m_abyPictureData;
    }
    
    /** Set the text encoding to be used for the description in this frame.
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

    /** Get the text encoding used for the description in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "APIC".getBytes();
    }
    
    public String toString()
    {
        return "Attached picure: Mime type=[" + m_sMimeType + "], Picture type = " +
               m_oPictureType.getValue() + ", Description=[" + m_sDescription + "], Picture data length = " +
               m_abyPictureData.length;
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // mime type (and trailing null)
        oIDOS.write(m_sMimeType.getBytes());
        oIDOS.writeUnsignedByte(0);
        // picture type
        oIDOS.writeUnsignedByte(m_oPictureType.getValue());
        // description
        if (m_sDescription != null)
        {
            oIDOS.write(m_sDescription.getBytes(m_oTextEncoding.getEncodingString()));
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
        // actual picture data
        oIDOS.write(m_abyPictureData);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof APICID3V2Frame)))
        {
            return false;
        }
        
        APICID3V2Frame oOtherAPIC = (APICID3V2Frame)oOther;
        
        return (m_oTextEncoding.equals(oOtherAPIC.m_oTextEncoding) &&
                m_sMimeType.equals(oOtherAPIC.m_sMimeType) &&
                m_oPictureType.equals(oOtherAPIC.m_oPictureType) &&
                m_sDescription.equals(oOtherAPIC.m_sDescription) &&
                Arrays.equals(m_abyPictureData, oOtherAPIC.m_abyPictureData));
    }
    
    
    public static class PictureType
    {
        private byte m_byValue;

        /** Private constructor.  Picture types are predefined. */
        private PictureType(byte byValue)
        {
            m_byValue = byValue;
        }
        
        private byte getValue()
        {
            return m_byValue;
        }
        
        /** Equality test returns if two objects represent the same picture type. */
        public boolean equals(PictureType oPictureType)
        {
            if ( (oPictureType == null) || ( ! (oPictureType instanceof PictureType)) )
            {
                return false;
            }
            
            return (oPictureType.m_byValue == this.m_byValue);
        }

        /** Predefined picture type. */
        public static final PictureType Other = new PictureType((byte)0);
        /** Predefined picture type.  Note, file icon images should be resctricted to 32 by 32 pixel
         *  images in PNG format.
         */
        public static final PictureType FileIcon = new PictureType((byte)1);
        /** Predefined picture type. */
        public static final PictureType OtherFileIcon = new PictureType((byte)2);
        /** Predefined picture type. */
        public static final PictureType FrontCover = new PictureType((byte)3);
        /** Predefined picture type. */
        public static final PictureType BackCover = new PictureType((byte)4);
        /** Predefined picture type. */
        public static final PictureType LeafletPage = new PictureType((byte)5);
        /** Predefined picture type. */
        public static final PictureType Media = new PictureType((byte)6);
        /** Predefined picture type. */
        public static final PictureType LeadArtist = new PictureType((byte)7);
        /** Predefined picture type. */
        public static final PictureType Artist = new PictureType((byte)8);
        /** Predefined picture type. */
        public static final PictureType Conductor = new PictureType((byte)9);
        /** Predefined picture type. */
        public static final PictureType Band = new PictureType((byte)10);
        /** Predefined picture type. */
        public static final PictureType Composer = new PictureType((byte)11);
        /** Predefined picture type. */
        public static final PictureType Lyricist = new PictureType((byte)12);
        /** Predefined picture type. */
        public static final PictureType Location = new PictureType((byte)13);
        /** Predefined picture type. */
        public static final PictureType DuringRecording = new PictureType((byte)14);
        /** Predefined picture type. */
        public static final PictureType DuringPerformance = new PictureType((byte)15);
        /** Predefined picture type. */
        public static final PictureType FrameCapture = new PictureType((byte)16);
        /** Predefined picture type.  (?!?) */
        public static final PictureType BrightColouredFish = new PictureType((byte)17);
        /** Predefined picture type. */
        public static final PictureType Illustration = new PictureType((byte)18);
        /** Predefined picture type. */
        public static final PictureType ArtistLogo = new PictureType((byte)19);
        /** Predefined picture type. */
        public static final PictureType PublisherLogo = new PictureType((byte)20);
    }
}
