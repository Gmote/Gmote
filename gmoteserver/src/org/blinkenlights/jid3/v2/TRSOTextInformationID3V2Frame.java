/*
 * TRSOTextInformationID3V2Frame.java
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
 * $Id: TRSOTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the name of the owner of the internet radio station from which the
 * content of this track is being or was streamed.
 *
 */
public class TRSOTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sInternetRadioStationOwner = null;
    
    /** Constructor.
     *
     * @param sInternetRadioStationOwner the name of the owner of the internet radio station from which the
     *        content of this track is being or was streamed
     */
    public TRSOTextInformationID3V2Frame(String sInternetRadioStationOwner)
    {
        super(sInternetRadioStationOwner);
        
        m_sInternetRadioStationOwner = sInternetRadioStationOwner;
    }

    public TRSOTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sInternetRadioStationOwner = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTRSOTextInformationID3V2Frame(this);
    }

    /** Set the name of the owner of the internet radio station from which the content of this track
     *  is being or was streamed.
     *
     * @param sInternetRadioStationOwner the name of the owner of the internet radio station from which the
     *        content of this track is being or was streamed
     */
    public void setInternetRadioStationOwner(String sInternetRadioStationOwner)
    {
        m_sInternetRadioStationOwner = sInternetRadioStationOwner;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sInternetRadioStationOwner;
    }
    
    /** Get the name of the owner of the internet radio station from which the content of this track
     *  is being or was streamed.
     *
     * @return the name of the owner of the internet radio station from which the content of this
     *         track is being or was streamed
     */
    public String getInternetRadioStationOwner()
    {
        return m_sInternetRadioStationOwner;
    }
    
    protected byte[] getFrameId()
    {
        return "TRSO".getBytes();
    }
    
    public String toString()
    {
        return "Internet radio station owner: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TRSOTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TRSOTextInformationID3V2Frame oOtherTRSO = (TRSOTextInformationID3V2Frame)oOther;
        
        return (m_sInternetRadioStationOwner.equals(oOtherTRSO.m_sInternetRadioStationOwner) &&
                m_oTextEncoding.equals(oOtherTRSO.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTRSO.m_sInformation));
    }
}
