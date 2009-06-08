/*
 * MLLTID3V2Frame.java
 *
 * Created on August 30, 2004, 1:48 AM
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
 * $Id: MLLTID3V2Frame.java,v 1.10 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing MPEG location lookup table.
 *
 * @author  paul
 */
public class MLLTID3V2Frame extends ID3V2Frame
{
    private byte[] m_abyMPEGLocationLookupTable = null;

    /** Constructor.
     *
     * @param abyMPEGLocationLookupTable MPEG location lookup table data
     * @throws ID3Exception if abyMPEGLocationLookupTable is null or zero-length, or if you do not know what you are doing
     */
    public MLLTID3V2Frame(byte[] abyMPEGLocationLookupTable)
        throws ID3Exception
    {
        if (abyMPEGLocationLookupTable == null)
        {
            throw new ID3Exception("MPEG location lookup table cannot be null in MLLT frame.");
        }
        m_abyMPEGLocationLookupTable = abyMPEGLocationLookupTable;
    }
    
    public MLLTID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // MPEG location lookup table
            m_abyMPEGLocationLookupTable = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyMPEGLocationLookupTable);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitMLLTID3V2Frame(this);
    }
    
    /** Get MPEG lookup table data.
     *
     * @return an array of bytes which contain lookup table information
     */
    public byte[] getMPEGLocationLookupTable()
    {
        return m_abyMPEGLocationLookupTable;
    }

    protected byte[] getFrameId()
    {
        return "MLLT".getBytes();
    }
    
    public String toString()
    {
        return "MPEG location lookup table: length=" + m_abyMPEGLocationLookupTable.length;
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // mpeg location lookup table data
        oIDOS.write(m_abyMPEGLocationLookupTable);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof MLLTID3V2Frame)))
        {
            return false;
        }
        
        MLLTID3V2Frame oOtherMLLT = (MLLTID3V2Frame)oOther;
        
        return (Arrays.equals(m_abyMPEGLocationLookupTable, oOtherMLLT.m_abyMPEGLocationLookupTable));
    }
}
