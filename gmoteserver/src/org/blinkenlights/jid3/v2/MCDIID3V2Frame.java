/*
 * MCDIID3V2Frame.java
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
 * $Id: MCDIID3V2Frame.java,v 1.9 2005/02/06 18:11:23 paul Exp $
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
 * Frame containing CD identification data.
 */
public class MCDIID3V2Frame extends ID3V2Frame
{
    private byte[] m_abyCDTOC = null;

    /** Constructor.
     *
     * @param abyCDTOC CD identification data, by which the CD this track came from can be identified
     * @throws ID3Exception if abyCDTOC is null, zero-length, or greater than 804 bytes in length
     */
    public MCDIID3V2Frame(byte[] abyCDTOC)
        throws ID3Exception
    {
        m_abyCDTOC = abyCDTOC;
        
        // cd toc
        if ((abyCDTOC == null) || (abyCDTOC.length == 0))
        {
            throw new ID3Exception("MCDI frame requires CD TOC data.");
        }
        if (abyCDTOC.length > 804)
        {
            throw new ID3Exception("MCDI frame CD TOC data cannot exceed 804 bytes.");
        }
        m_abyCDTOC = abyCDTOC;
    }
    
    public MCDIID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // cd toc
            m_abyCDTOC = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyCDTOC);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitMCDIID3V2Frame(this);
    }
    
    /** Get CD identification data.
     *
     * @return an array of bytes which can identify the CD which this track comes from
     */
    public byte[] getCDTOCData()
    {
        return m_abyCDTOC;
    }

    protected byte[] getFrameId()
    {
        return "MCDI".getBytes();
    }
    
    public String toString()
    {
        return "Music CD Identifier: CD TOC length=" + m_abyCDTOC.length;
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // cd toc data
        oIDOS.write(m_abyCDTOC);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof MCDIID3V2Frame)))
        {
            return false;
        }
        
        MCDIID3V2Frame oOtherMCDI = (MCDIID3V2Frame)oOther;
        
        return (Arrays.equals(m_abyCDTOC, oOtherMCDI.m_abyCDTOC));
    }
}
