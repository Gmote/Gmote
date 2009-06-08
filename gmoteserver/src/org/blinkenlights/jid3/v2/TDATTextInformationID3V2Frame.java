/*
 * TALBTextInformationID3V2Frame.java
 *
 * Created on 8-Jan-2004
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
 * $Id: TDATTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:21 paul Exp $
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
 * Text frame which contains the date of the recording, not including the year.
 */
public class TDATTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iDay;
    private int m_iMonth;
    
    /** Constructor.
     *
     * @param iDay the day on which the recording was made
     * @param iMonth the month in which the recording was made
     * @throws ID3Exception if the given day/month combination is not valid
     */
    public TDATTextInformationID3V2Frame(int iDay, int iMonth)
        throws ID3Exception
    {
        // make sure the values we've been given for day and month are legal
        if ( ! checkDayMonthValidity(iDay, iMonth))
        {
            throw new ID3Exception("Invalid day/month combination " + iDay + "/" + iMonth + ".");
        }
        
        NumberFormat oNF = new DecimalFormat("00");
        m_sInformation = oNF.format(iDay) + oNF.format(iMonth);
        
        m_iDay = iDay;
        m_iMonth = iMonth;
    }

    public TDATTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        // try to parse day and month (we aren't going to require valid dates when reading from a file,
        // but they do have to be numbers)
        try
        {
            String sDay = m_sInformation.substring(0, 2);
            m_iDay = Integer.parseInt(sDay);
            
            String sMonth = m_sInformation.substring(2, 4);
            m_iMonth = Integer.parseInt(sMonth);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TDAT recording date frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTDATTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TDAT".getBytes();
    }
    
    public String toString()
    {
        return "Date (DDMM): [" + m_sInformation + "]";
    }

    /** Set the date on which this recording was made.
     *
     * @param iDay the day on which the recording was made
     * @param iMonth the month in which the recording was made
     * @throws ID3Exception if the given day/month combination is not valid/
     */
    public void setDate(int iDay, int iMonth)
        throws ID3Exception
    {
        // make sure the values we've been given for day and month are legal
        if ( ! checkDayMonthValidity(iDay, iMonth))
        {
            throw new ID3Exception("Invalid day/month combination " + iDay + "/" + iMonth + ".");
        }

        NumberFormat oNF = new DecimalFormat("00");
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = oNF.format(iDay) + oNF.format(iMonth);
        
        m_iDay = iDay;
        m_iMonth = iMonth;
    }

    /** Get the day on which this recording was made.
     *
     * @return the day on which the recording was made
     */
    public int getDay()
    {
        return m_iDay;
    }

    /** Get the month in which the recording was made.
     *
     * @return the month in which the recording was made
     */
    public int getMonth()
        throws ID3Exception
    {
        return m_iMonth;
    }

    /** Internal method which checks a given day/month combination to see if it is valid.
     *
     * @param iDay day value
     * @param iMonth month value
     * @return true if day/month are valid, false otherwise
     */
    private boolean checkDayMonthValidity(int iDay, int iMonth)
    {
        return !
        ( 
          (iDay < 1) ||
          // months with 31 days
          (
            (
              (iMonth == 1) ||
              (iMonth == 3) ||
              (iMonth == 5) ||
              (iMonth == 7) ||
              (iMonth == 8) ||
              (iMonth == 10) ||
              (iMonth == 12)
            ) &&
            (iDay > 31)
          ) ||
          // months with 30 days
          (
            (
              (iMonth == 4) ||
              (iMonth == 6) ||
              (iMonth == 9) ||
              (iMonth == 11)
            ) &&
            (iDay > 30)
          ) ||
          // february (no year specified, so can't check for leap years
          (
            (iMonth == 2) &&
            (iDay > 29) 
          )
        );
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TDATTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TDATTextInformationID3V2Frame oOtherTDAT = (TDATTextInformationID3V2Frame)oOther;
        
        return ((m_iDay == oOtherTDAT.m_iDay) &&
                (m_iMonth == oOtherTDAT.m_iMonth) &&
                m_oTextEncoding.equals(oOtherTDAT.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTDAT.m_sInformation));
    }
}
