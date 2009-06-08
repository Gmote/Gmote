/*
 * TRSNTextInformationID3V2Frame.java
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
 * $Id: TRSNTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the name of the internet radio station from which the content
 * of this track is being or was streamed.
 */
public class TRSNTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sInternetRadioStationName = null;
    
    /** Constructor.
     *
     * @param sInternetRadioStationName the name of the internet radio station from which the
     *        content of this track is being or was streamed
     */
    public TRSNTextInformationID3V2Frame(String sInternetRadioStationName)
    {
        super(sInternetRadioStationName);
        
        m_sInternetRadioStationName = sInternetRadioStationName;
    }

    public TRSNTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sInternetRadioStationName = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTRSNTextInformationID3V2Frame(this);
    }

    /** Set the name of the internet radio station from which the content of this track
     *  is being or was streamed.
     *
     * @param sInternetRadioStationName the name of the internet radio station from which the
     *        content of this track is being or was streamed
     */
    public void setInternetRadioStationName(String sInternetRadioStationName)
    {
        m_sInternetRadioStationName = sInternetRadioStationName;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sInternetRadioStationName;
    }

    /** Get the name of the internet radio station from which the content of this track
     *  is being or was streamed.
     *
     * @return the name of the internet radio station from which the content of this
     *         track is being or was streamed
     */
    public String getInternetRadioStationName()
    {
        return m_sInternetRadioStationName;
    }
    
    protected byte[] getFrameId()
    {
        return "TRSN".getBytes();
    }
    
    public String toString()
    {
        return "Internet radio station name: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TRSNTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TRSNTextInformationID3V2Frame oOtherTRSN = (TRSNTextInformationID3V2Frame)oOther;
        
        return (m_sInternetRadioStationName.equals(oOtherTRSN.m_sInternetRadioStationName) &&
                m_oTextEncoding.equals(oOtherTRSN.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTRSN.m_sInformation));
    }
}
