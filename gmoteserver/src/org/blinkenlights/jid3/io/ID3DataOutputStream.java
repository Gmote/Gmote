/*
 * Created on 26-Nov-2003
 *
 * Copyright (C)2003,2004 Paul Grebenc
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
 * $Id: ID3DataOutputStream.java,v 1.3 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.io;

import java.io.*;

import org.blinkenlights.jid3.*;

/**
 * @author paul
 *
 * Custom DataOutputStream containing convenience methods for writing ID3 tags.
 *
 */
public class ID3DataOutputStream extends DataOutputStream
{
    public ID3DataOutputStream(OutputStream oOS)
    {
        super(oOS);
    }

    /** Write an unsigned byte value.
     *
     * @param iValue the unsigned byte value to be written
     * @throws IOException
     */
    public void writeUnsignedByte(int iValue)
        throws IOException
    {
        writeByte(iValue);
    }
    
    /** Writes an unsigned big-endian 16-bit value.  (Truncates any higher bits.)
     *
     * @param iValue the 16-bit unsigned integer value to be written
     * @throws IOException
     */
    public void writeBEUnsigned16(int iValue)
        throws IOException
    {
        int iLo = iValue & 0xff;
        int iHi = iValue >> 8;
        
        write(iHi);
        write(iLo);
    }
    
    /** Writes an unsigned big-endian 24-bit value.  (Truncates any higher bits.)
     *
     * @param iValue the 24-bit unsigned integer value to be written
     * @throws IOException
     */
    public void writeBE24(int iValue)
        throws IOException
    {
        int iOne = iValue & 0xff;
        int iTwo = (iValue >> 8) & 0xff;
        int iThree = (iValue >> 16) & 0xff;
        
        write(iThree);
        write(iTwo);
        write(iOne);
    }

    /** Writes a signed big-endian 32-bit value.
     *
     * @param iValue the 32-bit signed integer value to be written
     * @throws IOException
     */
    public void writeBE32(int iValue)
        throws IOException
    {
        int iOne = iValue & 0xff;
        int iTwo = (iValue >> 8) & 0xff;
        int iThree = (iValue >> 16) & 0xff;
        int iFour = (iValue >> 24) & 0xff;
        
        write(iFour);
        write(iThree);
        write(iTwo);
        write(iOne);
    }
    
    /** Writes an unsigned big-endian 32-bit value.
     *
     * @param lValue the 32-bit unsigned integer value to be written
     * @throws IOException
     */
    public void writeUnsignedBE32(long lValue)
        throws IOException
    {
        int iOne = (int)(lValue & 0xff);
        int iTwo = (int)((lValue >> 8) & 0xff);
        int iThree = (int)((lValue >> 16) & 0xff);
        int iFour = (int)((lValue >> 24) & 0xff);
        
        write(iFour);
        write(iThree);
        write(iTwo);
        write(iOne);
    }
    
    /** Write an encoded four byte value.
     *  The encoding method uses only the lowest seven bits of each byte, to prevent synchronization
     *  errors in the MP3 data stream.
     */
    public void writeID3Four(int iValue)
        throws IOException, ID3Exception
    {
        // we're only using the lower seven bits of each byte, so we can't write a value that
        // is greater than 28 bits can hold
        if (iValue >= (1 << 28))
        {
            throw new ID3Exception("Cannot write an encoded value greater than 28-bit unsigned.");
        }
        
        int iOne = ((iValue >> (3*7)) & 0x7f);
        int iTwo = ((iValue >> (2*7)) & 0x7f);
        int iThree = ((iValue >> (1*7)) & 0x7f);
        int iFour = (iValue & 0x7f);
        
        write(iOne);
        write(iTwo);
        write(iThree);
        write(iFour);
    }
}
