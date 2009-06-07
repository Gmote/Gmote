/*
 * Created on 1-Jan-2004
 *
 * Copyright (C)2004 Paul Grebenc
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
 * $Id: ID3DataInputStream.java,v 1.3 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.io;

import java.io.*;

import org.blinkenlights.jid3.*;

/**
 * @author paul
 *
 * Custom DataInputStream containing convenience methods for reading ID3 tags.
 *
 */
public class ID3DataInputStream extends DataInputStream
{
    public ID3DataInputStream(InputStream oIS)
    {
        super(oIS);
    }
    
    /** Reads an unsigned big-endian 16-bit value and returns an int.
     * @return the integer value read
     * @throws IOException
     */
    public final int readBEUnsigned16()
        throws IOException
    {
        int iHi = readUnsignedByte();
        int iLo = readUnsignedByte();
        
        int iVal = iLo | (iHi << 8);
        
        return iVal;
    }
    
    /** Read an unsigned big-endian 24-bit value and returns an int.
     *
     * @return the integer value read
     * @throws IOException
     */
    public int readBE24()
        throws IOException
    {
        int iThree = readUnsignedByte();
        int iTwo = readUnsignedByte();
        int iOne = readUnsignedByte();
        
        int iVal = (iOne | (iTwo << 8) | (iThree << 16));
        
        return iVal;
    }
    
    /** Read a signed big-endian 32-bit value and returns an int.
     * 
     * @return the integer value read
     * @throws IOException
     */
    public int readBE32()
        throws IOException
    {
        int iFour = readUnsignedByte();
        int iThree = readUnsignedByte();
        int iTwo = readUnsignedByte();
        int iOne = readUnsignedByte();
        
        int iVal = (iOne | (iTwo << 8) | (iThree << 16) | (iFour << 24));
        
        return iVal;
    }

    /** Read an unsigned big-endian 32-bit value and returns a long.
     * 
     * @return the long value read
     * @throws IOException
     */
    public long readUnsignedBE32()
        throws IOException
    {
        long lFour = readUnsignedByte();
        long lThree = readUnsignedByte();
        long lTwo = readUnsignedByte();
        long lOne = readUnsignedByte();
        
        long lVal = (lOne | (lTwo << 8) | (lThree << 16) | (lFour << 24));
        
        return lVal;
    }
    
    /** Read an encoded four byte value.
     *  The encoding method uses only the lowest seven bits of each byte, to prevent synchronization
     *  errors in the MP3 data stream.
     */
    public int readID3Four()
        throws IOException, ID3Exception
    {
        int iValue = 0;
        byte[] abyValue = new byte[4];
        readFully(abyValue);
        
        if ( ((abyValue[0] & 0x80) != 0) ||
             ((abyValue[1] & 0x80) != 0) ||
             ((abyValue[2] & 0x80) != 0) ||
             ((abyValue[3] & 0x80) != 0) )
        {
            throw new ID3Exception("High bit cannot be set in encoded values.");
        }
        
        iValue |= ((abyValue[0] & 0x7f) << (3 * 7));
        iValue |= ((abyValue[1] & 0x7f) << (2 * 7));
        iValue |= ((abyValue[2] & 0x7f) << (1 * 7));
        iValue |= ((abyValue[3] & 0x7f) << (0 * 7));
        
        return iValue;
    }

    /** Read an ISO-8859-1 encoded string to null.
     * 
     * @return a String, or null if string would be zero length
     * @throws IOException
     */
    public String readStringToNull()
        throws IOException
    {
        return readStringToNull(Integer.MAX_VALUE);
    }

    /** Read an ISO-8859-1 string to null, not exceeding a predefined length.
     * 
     * @param iMaxLength a length beyond which not to read further
     * @return a String, or null if string would be zero length
     * @throws IOException on I/O error, or if string would exceed allowed length
     */
    public String readStringToNull(int iMaxLength)
        throws IOException
    {
        ByteArrayOutputStream oStringBAOS = new ByteArrayOutputStream();
        int iStringByte;
        do
        {
            iStringByte = readUnsignedByte();
            if (iStringByte != 0)
            {
                if (oStringBAOS.size() == iMaxLength)
                {
                    throw new IOException("String length exceeds set " + iMaxLength + " byte limit.");
                }
                oStringBAOS.write(iStringByte);
            }
        }
        while (iStringByte != 0);
        
        // return byte array as a string
        byte[] abyShortDescription = oStringBAOS.toByteArray();
        return new String(abyShortDescription);
    }
    
    /** Read a string in the specified encoding format to null.  Note that Unicode strings must be terminated by
     *  a double null (two zero bytes).
     * 
     * @param oTextEncoding the encoding format of the string to be read
     * @return a String, or null if string would be zero length
     * @throws IOException
     */
    public String readStringToNull(TextEncoding oTextEncoding)
        throws IOException
    {
        return readStringToNull(oTextEncoding, Integer.MAX_VALUE);
    }
    
    /** Read a string in the specified encoding format to null, not exceeding a predefined length.  Note that
     *  Unicode strings must be terminated by a double null (two zero bytes).
     * 
     * @param oTextEncoding the encoding format of the string to be read
     * @param iMaxLength a length beyond which not to read further (in characters, not necessarily bytes)
     * @return a String, or null if string would be zero length
     * @throws IOException on I/O error, or if string would exceed allowed length
     */
    public String readStringToNull(TextEncoding oTextEncoding, int iMaxLength)
        throws IOException
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Text encoding cannot be null.");
        }

        ByteArrayOutputStream oStringBAOS = new ByteArrayOutputStream();
        int iStringByte1;
        int iStringByte2 = 0;
        int iLength = 0;
        do
        {
            iStringByte1 = readUnsignedByte();
            if (oTextEncoding.equals(TextEncoding.UNICODE))
            {
                iStringByte2 = readUnsignedByte();
            }
            if ((iStringByte1 != 0) || (iStringByte2 != 0))
            {
                if (iLength == iMaxLength)
                {
                    throw new IOException("String length exceeds set " + iMaxLength + " byte limit.");
                }
                oStringBAOS.write(iStringByte1);
                if (oTextEncoding.equals(TextEncoding.UNICODE))
                {
                    oStringBAOS.write(iStringByte2);
                }
                iLength++;
            }
        }
        while ((iStringByte1 != 0) || (iStringByte2 != 0)) ;
        
        // return byte array as a string
        byte[] abyShortDescription = oStringBAOS.toByteArray();
        return new String(abyShortDescription, oTextEncoding.getEncodingString());
    }
}
