/*
 * RVADID3V2Frame.java
 *
 * Created on September 4, 2004, 11:33 AM
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
 * $Id: RVADID3V2Frame.java,v 1.8 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing relative volume adjustment details.  There is no parsing support for this frame.
 *
 * @author  paul
 */
public class RVADID3V2Frame extends ID3V2Frame
{
    private byte[] m_abyData = null;
    
    /** Creates a new instance of RVADID3V2Frame.  (No parsing support for this frame.)
     *
     * @param abyData the raw data for this frame
     * @throws ID3Exception if abyData is null, or if you indicate that you do not know what you are doing
     */
    public RVADID3V2Frame(byte[] abyData)
        throws ID3Exception
    {
        if (abyData == null)
        {
            throw new ID3Exception("Data byte array cannot be null in RVAD frame.");
        }
        m_abyData = abyData;
    }
    
    public RVADID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            m_abyData = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyData);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitRVADID3V2Frame(this);
    }
    
    /** Get raw data from this frame.  It is up to you to parse it.
     *
     * @return raw frame data
     */
    public byte[] getFrameData()
    {
        return m_abyData;
    }
    
    protected byte[] getFrameId()
    {
        return "RVAD".getBytes();
    }
    
    public String toString()
    {
        return "Relative volume adjustment: Raw data size=[" + m_abyData.length + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_abyData);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof RVADID3V2Frame)))
        {
            return false;
        }
        
        RVADID3V2Frame oOtherRVAD = (RVADID3V2Frame)oOther;
        
        return (Arrays.equals(m_abyData, oOtherRVAD.m_abyData));
    }
}
