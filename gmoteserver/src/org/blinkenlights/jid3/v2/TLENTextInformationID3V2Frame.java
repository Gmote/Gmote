/*
 * TLENTextInformationID3V2Frame.java
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
 * $Id: TLENTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the length of the track in milliseconds.
 */
public class TLENTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iLengthInMilliseconds;
    
    /** Constructor.
     *
     * @param iLengthInMilliseconds the length of the track to be tagged, in milliseconds
     */
    public TLENTextInformationID3V2Frame(int iLengthInMilliseconds)
    {
        super(Integer.toString(iLengthInMilliseconds));
        
        m_iLengthInMilliseconds = iLengthInMilliseconds;
    }

    public TLENTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_iLengthInMilliseconds = Integer.parseInt(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered corrupt TLEN track length frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTLENTextInformationID3V2Frame(this);
    }

    /** Set the length of this track.
     *
     * @param iLengthInMilliseconds the length of the track to be tagged, in milliseconds
     */
    public void setTrackLength(int iLengthInMilliseconds)
    {
        m_iLengthInMilliseconds = iLengthInMilliseconds;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iLengthInMilliseconds);
    }

    /** Get the length of this track.
     *
     * @return the length of the track, in milliseconds
     */
    public int getTrackLength()
    {
        return m_iLengthInMilliseconds;
    }
    
    protected byte[] getFrameId()
    {
        return "TLEN".getBytes();
    }
    
    public String toString()
    {
        return "Length (milliseconds): [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TLENTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TLENTextInformationID3V2Frame oOtherTLEN = (TLENTextInformationID3V2Frame)oOther;
        
        return ((m_iLengthInMilliseconds == oOtherTLEN.m_iLengthInMilliseconds) &&
                m_oTextEncoding.equals(oOtherTLEN.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTLEN.m_sInformation));
    }
}
