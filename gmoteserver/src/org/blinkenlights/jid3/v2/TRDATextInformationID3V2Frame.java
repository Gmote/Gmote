/*
 * TRDATextInformationID3V2Frame.java
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
 * $Id: TRDATextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing a free-form description of the recording date(s) of this track.
 */
public class TRDATextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sRecordingDates = null;
    
    /** Constructor.
     *
     * @param sRecordingDates the recording date(s) of this track
     */
    public TRDATextInformationID3V2Frame(String sRecordingDates)
    {
        super(sRecordingDates);
        
        m_sRecordingDates = sRecordingDates;
    }

    public TRDATextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sRecordingDates = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTRDATextInformationID3V2Frame(this);
    }

    /** Set the recording date(s) of this track.
     *
     * @param sRecordingDates the recording date(s) of this track
     */
    public void setRecordingDates(String sRecordingDates)
    {
        m_sRecordingDates = sRecordingDates;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sRecordingDates;
    }

    /** Get the recording date(s) of this track.
     *
     * @return the recording date(s) of this track
     */
    public String getRecordingDates()
    {
        return m_sRecordingDates;
    }
    
    protected byte[] getFrameId()
    {
        return "TRDA".getBytes();
    }
    
    public String toString()
    {
        return "Recording dates: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TRDATextInformationID3V2Frame)))
        {
            return false;
        }
        
        TRDATextInformationID3V2Frame oOtherTRDA = (TRDATextInformationID3V2Frame)oOther;
        
        return (m_sRecordingDates.equals(oOtherTRDA.m_sRecordingDates) &&
                m_oTextEncoding.equals(oOtherTRDA.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTRDA.m_sInformation));
    }
}
