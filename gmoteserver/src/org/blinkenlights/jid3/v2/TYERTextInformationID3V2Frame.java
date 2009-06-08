/*
 * TYERTextInformationID3V2Frame.java
 *
 * Created on 2-Jan-2004
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
 * $Id: TYERTextInformationID3V2Frame.java,v 1.9 2005/02/06 18:11:21 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.text.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the year of the recording.
 */
public class TYERTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iYear;
    
    /** Constructor.
     *
     * @param iYear the year in which the recording in this track was recorded
     * @throws ID3Exception if the year is outside of the range from 0 to 9999
     */
    public TYERTextInformationID3V2Frame(int iYear)
        throws ID3Exception
    {
        super(Integer.toString(iYear));

        // it is required that the year value string be four characters long              
        if ((iYear < 0) || (iYear > 9999))
        {
            throw new ID3Exception("Year value must be between 0 and 9999.");
        }
        
        m_iYear = iYear;
    }

    public TYERTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_iYear = Integer.parseInt(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TYER year frame.", e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTYERTextInformationID3V2Frame(this);
    }

    /** Set the year of the recording in this track.
     *
     * @param iYear the year in which the recording in this track was recorded
     * @throws ID3Exception if the year is outside of the range from 0 to 9999
     */
    public void setYear(int iYear)
        throws ID3Exception
    {
        // it is required that the year value string be four characters long              
        if ((iYear < 0) || (iYear > 9999))
        {
            throw new ID3Exception("Year in TYER tag must be between 0 and 9999.");
        }
        
        m_iYear = iYear;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = getYearString(iYear);
    }
    
    /** Get the year of the recording in this track.
     *
     * @return the year of the recording in this track
     */
    public int getYear()
    {
        return m_iYear;
    }
    
    protected byte[] getFrameId()
    {
        return "TYER".getBytes();
    }
    
    public String toString()
    {
        return "Year: [" + m_sInformation + "]";
    }

    /** Internal method to return the year value as a four digit string.
     */
    private static String getYearString(int iYear)
    {
        NumberFormat oNF = new DecimalFormat("0000");
        
        return oNF.format(iYear);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TYERTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TYERTextInformationID3V2Frame oOtherTYER = (TYERTextInformationID3V2Frame)oOther;
        
        return ( (m_iYear == oOtherTYER.m_iYear) &&
                 m_oTextEncoding.equals(oOtherTYER.m_oTextEncoding) &&
                 m_sInformation.equals(oOtherTYER.m_sInformation) );
    }
}
