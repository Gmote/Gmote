/*
 * TBPMTextInformationID3V2Frame.java
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
 * $Id: TBPMTextInformationID3V2Frame.java,v 1.10 2005/09/21 01:05:29 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the number of beats per minute in the recording.
 *
 */
public class TBPMTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iBeatsPerMinute;
    
    /** Constructor.
     *
     * @param iBeatsPerMinute the number of beats per minute in the recording
     */
    public TBPMTextInformationID3V2Frame(int iBeatsPerMinute)
    {
        super(Integer.toString(iBeatsPerMinute));
        
        m_iBeatsPerMinute = iBeatsPerMinute;
    }

    public TBPMTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            // BPM is supposed to be an integer, but a program named Mixmeister BPM Analyzer writes floating
            // point values.  So we accept floating point values, but cast to an int (ie. we will only write an integer).
            m_iBeatsPerMinute = (int)Double.parseDouble(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered corrupt TBPM frame while reading tag.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTBPMTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TBPM".getBytes();
    }
    
    public String toString()
    {
        return "Beats per minute: [" + m_sInformation + "]";
    }
    
    /** Set the number of beats per minute for this recording.
     *
     * @param iBeatsPerMinute the number of beats per minute
     * @return the previous value set for the beats per minute
     */
    public int setBeatsPerMinute(int iBeatsPerMinute)
    {
        int iOldBeatsPerMinute = m_iBeatsPerMinute;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iBeatsPerMinute);
        m_iBeatsPerMinute = iBeatsPerMinute;
        
        return iOldBeatsPerMinute;
    }
    
    /** Get the number of beats per minute for this recording.
     *
     * @return the number of beats per minute
     */
    public int getBeatsPerMinute()
    {
        return m_iBeatsPerMinute;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TBPMTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TBPMTextInformationID3V2Frame oOtherTBPM = (TBPMTextInformationID3V2Frame)oOther;
        
        return ((m_iBeatsPerMinute == oOtherTBPM.m_iBeatsPerMinute) &&
                m_oTextEncoding.equals(oOtherTBPM.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTBPM.m_sInformation));
    }
}
