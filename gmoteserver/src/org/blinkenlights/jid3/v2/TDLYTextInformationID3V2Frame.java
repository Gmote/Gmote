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
 * $Id: TDLYTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the playlist delay value.
 */
public class TDLYTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iPlaylistDelayMillis;
    
    /** Constructor.
     *
     * @param iPlaylistDelayMillis the playlist delay in milliseconds
     * @throws ID3Exception if the playlist delay is negative
     */
    public TDLYTextInformationID3V2Frame(int iPlaylistDelayMillis)
        throws ID3Exception
    {
        super(Integer.toString(iPlaylistDelayMillis));
        
        if (iPlaylistDelayMillis < 0)
        {
            throw new ID3Exception("Playlist delay cannot be negative.");
        }
        
        m_iPlaylistDelayMillis = iPlaylistDelayMillis;
    }

    public TDLYTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_iPlaylistDelayMillis = Integer.parseInt(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered corrupt TDLY playlist delay frame.", e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTDLYTextInformationID3V2Frame(this);
    }

    /** Set the playlist delay value.
     *
     * @param iPlaylistDelayMillis the playlist delay in milliseconds
     * @throws ID3Exception if the playlist delay is negative
     */
    public void setPlaylistDelay(int iPlaylistDelayMillis)
        throws ID3Exception
    {
        if (iPlaylistDelayMillis < 0)
        {
            throw new ID3Exception("Playlist delay cannot be negative.");
        }

        m_iPlaylistDelayMillis = iPlaylistDelayMillis;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iPlaylistDelayMillis);
    }

    /** Get the playlist delay value.
     * @return the playlist delay value
     */
    public int getPlaylistDelay()
    {
        return m_iPlaylistDelayMillis;
    }
    
    protected byte[] getFrameId()
    {
        return "TDLY".getBytes();
    }
    
    public String toString()
    {
        return "Playlist delay: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TDLYTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TDLYTextInformationID3V2Frame oOtherTDLY = (TDLYTextInformationID3V2Frame)oOther;
        
        return ((m_iPlaylistDelayMillis == oOtherTDLY.m_iPlaylistDelayMillis) &&
                m_oTextEncoding.equals(oOtherTDLY.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTDLY.m_sInformation));
    }
}
