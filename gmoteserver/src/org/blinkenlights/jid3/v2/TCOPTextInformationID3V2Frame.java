/*
 * TCOPTextInformationID3V2Frame.java
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
 * $Id: TCOPTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

import java.text.*;

/**
 * @author paul
 *
 * Text frame which contains copyright information for the track it is associated with.
 */
public class TCOPTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iYear;
    private String m_sCopyrightMessage = null;
    
    /** Constructor.
     *
     * @param iYear the year in which the track was copyrighted
     * @param sCopyrightMessage the copyright notice message for the track (ie. the holder)
     */
    public TCOPTextInformationID3V2Frame(int iYear, String sCopyrightMessage)
        throws ID3Exception
    {
        super(getYearString(iYear) + " " + ((sCopyrightMessage == null) ? "" : sCopyrightMessage)  );
        
        if ((iYear < 0) || (iYear > 9999))
        {
            throw new ID3Exception("Year must be within the range from 1 to 9999.");
        }
        
        m_iYear = iYear;
        m_sCopyrightMessage = sCopyrightMessage;
    }

    public TCOPTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        // parse copyright message components from frame
        try
        {
            // parse year from copyright notice
            String sYear = m_sInformation.substring(0, 4);
            if (!sYear.matches("(?uis)\\d+"))
            {
                throw new ID3Exception("Missing 4-digit year value.");
            }
            m_iYear = Integer.parseInt(sYear);
            // space between
            if (m_sInformation.charAt(4) != ' ')
            {
                throw new ID3Exception("Missing space after year..");
            }
            // text of copyright notice
            if (m_sInformation.length() > 5)
            {
                m_sCopyrightMessage = m_sInformation.substring(5);
            }
            else
            {
                m_sCopyrightMessage = "";
            }
        }
        catch (Exception e)
        {
            // Tag & Rename It does not enforce the requirement that the first five characters of the copyright
            // notice be the year, so we will just make the whole thing the copyright message if we fail to parse
            // the value correctly, and make the year 0.
            m_iYear = 0;
            m_sCopyrightMessage = m_sInformation;
            
            //throw new ID3Exception("Encountered corrupt TCOP copyright notice frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTCOPTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TCOP".getBytes();
    }
    
    public String toString()
    {
        return "Copyright message: [" + m_sInformation + "]";
    }

    /** Set the copyright information for the track.
     *
     * @param iYear the year of the copyright
     * @param sCopyrightMessage the copyright message itself (ie. the holder)
     */
    public void setCopyright(int iYear, String sCopyrightMessage)
    {
        m_iYear = iYear;
        m_sCopyrightMessage = sCopyrightMessage;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = getYearString(iYear) + " " + ((sCopyrightMessage == null) ? "" : sCopyrightMessage);
    }

    /** Get the copyright year.
     *
     * @return the year of the copyright
     */
    public int getCopyrightYear()
    {
        return m_iYear;
    }

    /** Get the copyright message, not including the year.
     *
     * @return the copyright message
     */
    public String getCopyrightMessage()
    {
        return m_sCopyrightMessage;
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
        if ((oOther == null) || (!(oOther instanceof TCOPTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TCOPTextInformationID3V2Frame oOtherTCOP = (TCOPTextInformationID3V2Frame)oOther;
        
        return ( (m_iYear == oOtherTCOP.m_iYear) &&
                 m_sCopyrightMessage.equals(oOtherTCOP.m_sCopyrightMessage) &&
                 m_oTextEncoding.equals(oOtherTCOP.m_oTextEncoding) &&
                 m_sInformation.equals(oOtherTCOP.m_sInformation) );
    }
}
