/*
 * TPOSTextInformationID3V2Frame.java
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
 * $Id: TPOSTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing a reference to the part number of a set to which this recording belongs.
 */
public class TPOSTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iPartNumber;
    private int m_iTotalParts;
    
    /** Constructor.
     *
     * @param iPartNumber the part number in the set to which this recording belongs
     * @throws ID3Exception if the part number is negative
     */
    public TPOSTextInformationID3V2Frame(int iPartNumber)
        throws ID3Exception
    {
        super(Integer.toString(iPartNumber));
        
        if (iPartNumber < 0)
        {
            throw new ID3Exception("Part number cannot be negative.");
        }
        
        m_iPartNumber = iPartNumber;
        m_iTotalParts = -1;
    }
    
    /** Constructor.
     *
     * @param iPartNumber the part number in the set to which this recording belongs
     * @param iTotalParts the total number of parts in the complete set
     * @throws ID3Exception if the part number is negative, or the total number of parts is
     *         less than the part number
     */
    public TPOSTextInformationID3V2Frame(int iPartNumber, int iTotalParts)
        throws ID3Exception
    {
        super(Integer.toString(iPartNumber) + "/" + Integer.toString(iTotalParts));
        
        if (iPartNumber < 0)
        {
            throw new ID3Exception("Part number cannot be negative.");
        }
        if (iTotalParts < iPartNumber)
        {
            throw new ID3Exception("Total number of parts must be at least as great as the part number.");
        }
        
        m_iPartNumber = iPartNumber;
        m_iTotalParts = iTotalParts;
    }
    
    public TPOSTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            if (m_sInformation.indexOf('/') == -1)
            {
                // no slash, just the part number
                m_iPartNumber = Integer.parseInt(m_sInformation);
                m_iTotalParts = -1;
            }
            else
            {
                String[] asPart = m_sInformation.split("/", 2);
                m_iPartNumber = Integer.parseInt(asPart[0]);
                m_iTotalParts = Integer.parseInt(asPart[1]);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TPOS part number frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTPOSTextInformationID3V2Frame(this);
    }

    /** Set the part number in the set to which this recording belongs.
     *
     * @param iPartNumber the part number in the set to which this recording belongs
     * @throws ID3Exception if the part number is negative
     */
    public void setPartNumber(int iPartNumber)
        throws ID3Exception
    {
        if (iPartNumber < 0)
        {
            throw new ID3Exception("Part number cannot be negative.");
        }

        m_iPartNumber = iPartNumber;
        m_iTotalParts = -1;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iPartNumber);
    }

    /** Set the part number in the set to which this recording belongs, and the total number
     * of parts in the complete set.
     *
     * @param iPartNumber the part number in the set to which this recording belongs
     * @param iTotalParts the total number of parts in a complete set
     * @throws ID3Exception if the part number is negative, or the total number of parts is
     *         less than the part number
     */
    public void setPartNumberAndTotalParts(int iPartNumber, int iTotalParts)
        throws ID3Exception
    {
        if (iPartNumber < 0)
        {
            throw new ID3Exception("Part number cannot be negative.");
        }
        if (iTotalParts < iPartNumber)
        {
            throw new ID3Exception("Total number of parts must be at least as great as the part number.");
        }

        m_iPartNumber = iPartNumber;
        m_iTotalParts = iTotalParts;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = iPartNumber + "/" + iTotalParts;
    }
    
    /** Get the part number in the set to which this recording belongs.
     *
     * @return the part number in the set to which this recording belongs
     */
    public int getPartNumber()
    {
        return m_iPartNumber;
    }
    
    /** Get the total number of parts in the complete set to which this recording belongs.
     *
     * @return the total number of parts in the complete set to which this recording belongs,
     *         or -1 if the total number of parts has not been specified
     */
    public int getTotalParts()
    {
        return m_iTotalParts;
    }
    
    protected byte[] getFrameId()
    {
        return "TPOS".getBytes();
    }
    
    public String toString()
    {
        return "Part of a set: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TPOSTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TPOSTextInformationID3V2Frame oOtherTPOS = (TPOSTextInformationID3V2Frame)oOther;
        
        return ((m_iPartNumber == oOtherTPOS.m_iPartNumber) &&
                (m_iTotalParts == oOtherTPOS.m_iTotalParts) &&
                m_oTextEncoding.equals(oOtherTPOS.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTPOS.m_sInformation));
    }
}
