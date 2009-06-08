/*
 * TORYTextInformationID3V2Frame.java
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
 * $Id: TORYTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:19 paul Exp $
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
 * Text frame containing the original release year of the recording in this track.
 */
public class TORYTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iOriginalReleaseYear;
    
    /** Constructor.
     *
     * @param iOriginalReleaseYear the year in which the recording in this track was originally released
     * @throws ID3Exception if the year is outside of the range from 0 to 9999
     */
    public TORYTextInformationID3V2Frame(int iOriginalReleaseYear)
        throws ID3Exception
    {
        super(getYearString(iOriginalReleaseYear));

        // it is required that the year value string be four characters long              
        if ((iOriginalReleaseYear < 0) || (iOriginalReleaseYear > 9999))
        {
            throw new ID3Exception("Year in TORY tag must be between 0 and 9999.");
        }
        
        m_iOriginalReleaseYear = iOriginalReleaseYear;
    }

    public TORYTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_iOriginalReleaseYear = Integer.parseInt(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TORY original copyright year frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTORYTextInformationID3V2Frame(this);
    }
    
    /** Set the original release year of the recording in this track.
     *
     * @param iOriginalReleaseYear the year in which the recording in this track was originally released
     * @throws ID3Exception if the year is outside of the range from 0 to 9999
     */
    public void setOriginalReleaseYear(int iOriginalReleaseYear)
        throws ID3Exception
    {
        // it is required that the year value string be four characters long              
        if ((iOriginalReleaseYear < 0) || (iOriginalReleaseYear > 9999))
        {
            throw new ID3Exception("Year in TORY tag must be between 0 and 9999.");
        }
        
        m_iOriginalReleaseYear = iOriginalReleaseYear;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = getYearString(iOriginalReleaseYear);
    }
    
    /** Get the original release year of the recording in this track.
     *
     * @return the original release year of the recording in this track
     */
    public int getOriginalReleaseYear()
    {
        return m_iOriginalReleaseYear;
    }
    
    protected byte[] getFrameId()
    {
        return "TORY".getBytes();
    }
    
    public String toString()
    {
        return "Original release year: [" + m_sInformation + "]";
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
        if ((oOther == null) || (!(oOther instanceof TORYTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TORYTextInformationID3V2Frame oOtherTORY = (TORYTextInformationID3V2Frame)oOther;
        
        return ( (m_iOriginalReleaseYear == oOtherTORY.m_iOriginalReleaseYear) &&
                 m_oTextEncoding.equals(oOtherTORY.m_oTextEncoding) &&
                 m_sInformation.equals(oOtherTORY.m_sInformation) );
    }
}
