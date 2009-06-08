/*
 * GEOBID3V2Frame.java
 *
 * Created on Feb 1, 2004
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
 * $Id: GEOBID3V2Frame.java,v 1.10 2005/02/06 18:11:15 paul Exp $
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
 * Frame containing a general encapsulated object.
 */
public class GEOBID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sMimeType = null;
    private String m_sFilename = null;
    private String m_sContentDescription = null;
    private byte[] m_abyEncapsulatedObjectData = null;

    /** Constructor.
     *
     * @param sMimeType the mime type of the file being stored (optional)
     * @param sFilename the filename of the file being stored (optional)
     * @param sContentDescription a brief description of the content (required)
     * @param abyEncapsulatedObjectData the data file being stored in this frame
     * @throws ID3Exception if the required content description is null or zero length
     * @throws ID3Exception if the required object data is null or zero length
     */
    public GEOBID3V2Frame(String sMimeType,
                          String sFilename,
                          String sContentDescription,
                          byte[] abyEncapsulatedObjectData)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sMimeType = sMimeType;
        m_sFilename = sFilename;
        if ((sContentDescription == null) || (sContentDescription.length() == 0))
        {
            throw new ID3Exception("Content description is required in GEOB frame.");
        }
        m_sContentDescription = sContentDescription;
        if ((abyEncapsulatedObjectData == null) || (abyEncapsulatedObjectData.length == 0))
        {
            throw new ID3Exception("Encapsulated object data is required in GEOB frame.");
        }
        m_abyEncapsulatedObjectData = abyEncapsulatedObjectData;
    }

    public GEOBID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
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

            // filename (read to null)
            m_sFilename = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);

            // content description (read to null)
            m_sContentDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // encapsulated object data
            m_abyEncapsulatedObjectData = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyEncapsulatedObjectData);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitGEOBID3V2Frame(this);
    }

    /** Set the encapsulated object for this frame.
     *
     * @param sMimeType the mime type of the file being stored (optional)
     * @param sFilename the filename of the file being stored (optional)
     * @param sContentDescription a brief description of the content (required)
     * @param abyEncapsulatedObjectData the data file being stored in this frame
     * @throws ID3Exception if the required content description is null or zero length
     * @throws ID3Exception if the required object data is null or zero length
     * @throws ID3Exception if this frame is in a tag with another GEOB frame which would have the same content description
     */
    public void setEncapsulatedObject(String sMimeType,
                                      String sFilename,
                                      String sContentDescription,
                                      byte[] abyEncapsulatedObjectData)
        throws ID3Exception
    {
        TextEncoding oOrigTextEncoding = m_oTextEncoding;
        String sOrigMimeType = m_sMimeType;
        String sOrigFilename = m_sFilename;
        String sOrigContentDescription = m_sContentDescription;
        byte[] abyOrigEncapsulatedObjectData = m_abyEncapsulatedObjectData;
        
        if ((sContentDescription == null) || (sContentDescription.length() == 0))
        {
            throw new ID3Exception("Content description is required in GEOB frame.");
        }
        if ((abyEncapsulatedObjectData == null) || (abyEncapsulatedObjectData.length == 0))
        {
            throw new ID3Exception("Encapsulated object data is required in GEOB frame.");
        }
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sMimeType = sMimeType;
        m_sFilename = sFilename;
        m_sContentDescription = sContentDescription;
        m_abyEncapsulatedObjectData = abyEncapsulatedObjectData;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_oTextEncoding = oOrigTextEncoding;
            m_sMimeType = sOrigMimeType;
            m_sFilename = sOrigFilename;
            m_sContentDescription = sOrigContentDescription;
            m_abyEncapsulatedObjectData = abyOrigEncapsulatedObjectData;
            
            throw e;
        }
    }
    
    /** Get the mime type of the object in this frame.
     *
     * @return the mime type, or null if one has been set
     */
    public String getMimeType()
    {
        return m_sMimeType;
    }
    
    /** Get the filename of the object in this frame.
     *
     * @return the filename, or null if one has not been set
     */
    public String getFilename()
    {
        return m_sFilename;
    }
    
    /** Get the content description of the object in this frame.
     *
     * @return the content description
     */
    public String getContentDescription()
    {
        return m_sContentDescription;
    }
    
    /** Get the encapsulated object data in this frame.
     *
     * @return the encapsulated object data
     */
    public byte[] getEncapsulatedObjectData()
    {
        return m_abyEncapsulatedObjectData;
    }
    
    /** Set the text encoding to be used for the filename and content description in this frame.
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

    /** Get the text encoding used for the filename and content description in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "GEOB".getBytes();
    }
    
    public String toString()
    {
        return "General Encapsulated Object: Mime-Type=[" + m_sMimeType + "], Filename=["
               + m_sFilename + "], Content description=[" + m_sContentDescription +
               "], Object data length=" + m_abyEncapsulatedObjectData.length;
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // mime type
        if (m_sMimeType != null)
        {
            oIDOS.write(m_sMimeType.getBytes());
        }
        oIDOS.writeUnsignedByte(0);
        // filename
        if (m_sFilename != null)
        {
            oIDOS.write(m_sFilename.getBytes(m_oTextEncoding.getEncodingString()));
        }
        // null after filename
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // content description
        oIDOS.write(m_sContentDescription.getBytes(m_oTextEncoding.getEncodingString()));
        // null after content description
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // actual text of comment
        oIDOS.write(m_abyEncapsulatedObjectData);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof GEOBID3V2Frame)))
        {
            return false;
        }
        
        GEOBID3V2Frame oOtherGEOB = (GEOBID3V2Frame)oOther;
        
        return ( (((m_sMimeType == null) && (oOtherGEOB.m_sMimeType == null)) || m_sMimeType.equals(oOtherGEOB.m_sMimeType)) &&
                 (((m_sFilename == null) && (oOtherGEOB.m_sFilename == null)) || m_sFilename.equals(oOtherGEOB.m_sFilename)) &&
                 m_sContentDescription.equals(oOtherGEOB.m_sContentDescription) &&
                 m_oTextEncoding.equals(oOtherGEOB.m_oTextEncoding) &&
                 Arrays.equals(m_abyEncapsulatedObjectData, oOtherGEOB.m_abyEncapsulatedObjectData));
    }
}
