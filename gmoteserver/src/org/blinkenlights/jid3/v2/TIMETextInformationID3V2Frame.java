/*
 * TIMETextInformationID3V2Frame.java
 *
 * Created on 9-Jan-2004
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
 * $Id: TIMETextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:18 paul Exp $
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
 * Text frame containing time information.
 */
public class TIMETextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iHours;
    private int m_iMinutes;
    
    /** Constructor.
     *
     * @param iHours the hour value
     * @param iMinutes the minute value
     * @throws ID3Exception if an invalid hour or minute value is specified
     */
    public TIMETextInformationID3V2Frame(int iHours, int iMinutes)
        throws ID3Exception
    {
        // make sure the values we've been given for hours and minutes are legal
        if ((iHours < 0) || (iHours > 24) || (iMinutes < 0) || (iMinutes > 59))
        {
            throw new ID3Exception("Hours and minutes must each be two digits or less.");
        }
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        NumberFormat oNF = new DecimalFormat("00");
        m_sInformation = oNF.format(iHours) + oNF.format(iMinutes);
        
        m_iHours = iHours;
        m_iMinutes = iMinutes;
    }

    public TIMETextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        // convert information string to ISO-8859-1 encoding before parsing (we only accept ISO-8559-1 chars anyway)
        byte[] abyInformation = null;
        try
        {
            abyInformation = m_sInformation.getBytes("ISO-8859-1");
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a TIME frame in which the time digits cannot be parsed.", e);
        }

        // time must be four characters
        if (abyInformation.length != 4)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TIME frame with time string length not equal to four.");
        }
        
        // try to parse hours and minutes (we aren't going to require valid times when reading from a file,
        // but they do have to be numbers)
        try
        {
            byte[] abyHours = { abyInformation[0], abyInformation[1] };
            byte[] abyMinutes = { abyInformation[2], abyInformation[3] };

            m_iHours = Integer.parseInt(new String(abyHours));
            m_iMinutes = Integer.parseInt(new String(abyMinutes));
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TIME frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTIMETextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TIME".getBytes();
    }
    
    public String toString()
    {
        return "Time (HHMM): [" + m_sInformation + "]";
    }
    
    /** Set the time value.
     *
     * @param iHours the hour value
     * @param iMinutes the minute value
     * @throws ID3Exception if an invalid hour or minute value is specified
     */
    public void setTime(int iHours, int iMinutes)
        throws ID3Exception
    {
        // make sure the values we've been given for hours and minutes are legal
        if ((iHours < 0) || (iHours > 24) || (iMinutes < 0) || (iMinutes > 59))
        {
            throw new ID3Exception("Hours and minutes must each be two digits or less.");
        }
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        NumberFormat oNF = new DecimalFormat("00");
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = oNF.format(iHours) + oNF.format(iMinutes);
        
        m_iHours = iHours;
        m_iMinutes = iMinutes;
    }

    /** Get the hour value.
     *
     * @return the hour value
     */
    public int getHours()
    {
        return m_iHours;
    }

    /** Get the minute value.
     *
     * @return the minute value
     */
    public int getMinutes()
    {
        return m_iMinutes;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TIMETextInformationID3V2Frame)))
        {
            return false;
        }
        
        TIMETextInformationID3V2Frame oOtherTIME = (TIMETextInformationID3V2Frame)oOther;
        
        return ((m_iHours == oOtherTIME.m_iHours) &&
                (m_iMinutes == oOtherTIME.m_iMinutes) &&
                m_oTextEncoding.equals(oOtherTIME.m_oTextEncoding) && 
                m_sInformation.equals(oOtherTIME.m_sInformation));
    }
}
