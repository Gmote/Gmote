/*
 * TSIZTextInformationID3V2Frame.java
 *
 * Created on 9-Jan-2004
 *
 * Copyright (C)2004-2005 Paul Grebenc
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
 * $Id: TSIZTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the size of the tagged audio file in bytes, not counting the tag length.
 */
public class TSIZTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iSizeInBytes;
    
    /** Constructor.
     *
     * @param iSizeInBytes the size in bytes, of the tagged audio file, not counting the tag length
     * @throws ID3Exception if the size is negative
     */
    public TSIZTextInformationID3V2Frame(int iSizeInBytes)
        throws ID3Exception
    {
        super(Integer.toString(iSizeInBytes));
        
        if (iSizeInBytes < 0)
        {
            throw new ID3Exception("Size value cannot be negative.");
        }
        
        m_iSizeInBytes = iSizeInBytes;
    }

    public TSIZTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_iSizeInBytes = Integer.parseInt(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TSIZ file size frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTSIZTextInformationID3V2Frame(this);
    }

    /** Set the size in bytes, of the tagged audio file, not counting the tag length.
     *
     * @param iSizeInBytes the size in bytes, of the tagged audio file, not counting the tag length
     * @throws ID3Exception if the size is negative
     */
    public void setSizeInBytes(int iSizeInBytes)
        throws ID3Exception
    {
        if (iSizeInBytes < 0)
        {
            throw new ID3Exception("Size value cannot be negative.");
        }
        
        m_iSizeInBytes = iSizeInBytes;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iSizeInBytes);
    }
    
    /** Get the size in bytes, of the tagged file, not counting the tag length.
     *
     * @return the specified size in bytes, of the tagged file, not counting the tag length
     */
    public int getSizeInBytes()
    {
        return m_iSizeInBytes;
    }
    
    protected byte[] getFrameId()
    {
        return "TSIZ".getBytes();
    }
    
    public String toString()
    {
        return "Size (in bytes excluding tags): [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TSIZTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TSIZTextInformationID3V2Frame oOtherTSIZ = (TSIZTextInformationID3V2Frame)oOther;
        
        return ((m_iSizeInBytes == oOtherTSIZ.m_iSizeInBytes) &&
                m_oTextEncoding.equals(oOtherTSIZ.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTSIZ.m_sInformation));
    }
}
