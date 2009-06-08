/*
 * PCNTID3V2Frame.java
 *
 * Created on September 1, 2004, 12:44 AM
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
 * $Id: PCNTID3V2Frame.java,v 1.8 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing a play counter.
 * Note: There is no support for counter values greater than a 32-bit signed int.
 *       Will any track ever get played more than 2 billion times?
 *
 * @author  paul
 */
public class PCNTID3V2Frame extends ID3V2Frame
{
    private int m_iPlayCount;
    
    /** Creates a new instance of PCNTID3V2Frame
     *
     * @param iPlayCount the current play count for this track
     * @throws ID3Exception if the play count specified is negative
     */
    public PCNTID3V2Frame(int iPlayCount)
        throws ID3Exception
    {
        if (iPlayCount < 0)
        {
            throw new ID3Exception("Play count cannot be negative in PCNT frame.");
        }
        m_iPlayCount = iPlayCount;
    }

    public PCNTID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);

            // play count
            m_iPlayCount = oFrameDataID3DIS.readBE32();
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitPCNTID3V2Frame(this);
    }
    
    /** Set the play count.
     *
     * @param iPlayCount the current play count for this track
     * @throws ID3Exception if the play count specified is negative
     */
    public void setPlayCount(int iPlayCount)
        throws ID3Exception
    {
        if (iPlayCount < 0)
        {
            throw new ID3Exception("Play count cannot be negative in PCNT frame.");
        }
        m_iPlayCount = iPlayCount;
    }

    /** Get play count.
     *
     * @return the play count
     */
    public int getPlayCount()
    {
        return m_iPlayCount;
    }

    protected byte[] getFrameId()
    {
        return "PCNT".getBytes();
    }
    
    public String toString()
    {
        return "Play counter: Play count=[" + m_iPlayCount + "]";
    }

    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // play count
        oIDOS.writeBE32(m_iPlayCount);
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof PCNTID3V2Frame)))
        {
            return false;
        }
        
        PCNTID3V2Frame oOtherPCNT = (PCNTID3V2Frame)oOther;
        
        return (m_iPlayCount == oOtherPCNT.m_iPlayCount);
    }
}
