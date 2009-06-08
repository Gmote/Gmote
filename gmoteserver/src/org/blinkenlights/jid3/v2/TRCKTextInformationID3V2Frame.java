/*
 * TRCKTextInformationID3V2Frame.java
 *
 * Created on 3-Jan-2004
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
 * $Id: TRCKTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the track number or position in set of this recording in its original
 * album or collection.  The total number of tracks can also optionally be set.
 */
public class TRCKTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private int m_iTrackNumber;
    private int m_iTotalTracks;
    
    /** Constructor.
     * 
     * @param iTrackNumber the track number or position in set of this recording in its original
     *        album or collection
     * @throws ID3Exception if the track number is negative
     */
    public TRCKTextInformationID3V2Frame(int iTrackNumber)
        throws ID3Exception
    {
        super(Integer.toString(iTrackNumber));
        
        if (iTrackNumber < 0)
        {
            throw new ID3Exception("Track number cannot be negative.");
        }

        m_iTrackNumber = iTrackNumber;
        m_iTotalTracks = -1;
    }
    
    /** Constructor.
     * 
     * @param iTrackNumber the track number or position in set of this recording in its original
     *        album or collection
     * @param iTotalTracks the total number of tracks in the album or collection
     * @throws ID3Exception if the track number is negative, or the total number of tracks is
     *         less than the track number
     */
    public TRCKTextInformationID3V2Frame(int iTrackNumber, int iTotalTracks)
        throws ID3Exception
    {
        super(Integer.toString(iTrackNumber) + "/" + Integer.toString(iTotalTracks));

        if (iTrackNumber < 0)
        {
            throw new ID3Exception("Track number cannot be negative.");
        }
        if (iTotalTracks < iTrackNumber)
        {
            throw new ID3Exception("Total number of tracks must be at least as great as the track number.");
        }
        
        m_iTrackNumber = iTrackNumber;
        m_iTotalTracks = iTotalTracks;
    }

    public TRCKTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);

        try
        {
            if (m_sInformation.indexOf('/') == -1)
            {
                // no slash, just the track number
                m_iTrackNumber = Integer.parseInt(m_sInformation);
                m_iTotalTracks = -1;
            }
            else
            {
                String[] asPart = m_sInformation.split("/", 2);
                m_iTrackNumber = Integer.parseInt(asPart[0]);
                m_iTotalTracks = Integer.parseInt(asPart[1]);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TRCK part number frame.", e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTRCKTextInformationID3V2Frame(this);
    }

    /** Set the track number or position in set of this recording in its original album or collection.
     *
     * @param iTrackNumber the track number or position in set of this recording in its original
     *        album or collection
     * @throws ID3Exception if the track number is negative
     */
    public void setTrackNumber(int iTrackNumber)
        throws ID3Exception
    {
        if (iTrackNumber < 0)
        {
            throw new ID3Exception("Part number cannot be negative.");
        }

        m_iTrackNumber = iTrackNumber;
        m_iTotalTracks = -1;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = Integer.toString(iTrackNumber);
    }

    /** Set the track number or position in set of this recording in its original album or collection,
     * and the total number of tracks in the complete set.
     *
     * @param iTrackNumber the track number or position in set of this recording in its original
     *        album or collection
     * @param iTotalTracks the total number of tracks in the album or collection
     * @throws ID3Exception if the track number is negative, or the total number of tracks is
     *         less than the track number
     */
    public void setTrackNumberAndTotalTracks(int iTrackNumber, int iTotalTracks)
        throws ID3Exception
    {
        if (iTrackNumber < 0)
        {
            throw new ID3Exception("Track number cannot be negative.");
        }
        if (iTotalTracks < iTrackNumber)
        {
            throw new ID3Exception("Total number of tracks must be at least as great as the track number.");
        }

        m_iTrackNumber = iTrackNumber;
        m_iTotalTracks = iTotalTracks;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = iTrackNumber + "/" + iTotalTracks;
    }
    
    /** Get the track number of this recording.
     *
     * @return the track number
     */
    public int getTrackNumber()
    {
        return m_iTrackNumber;
    }
    
    /** Get the total number of tracks in this album or collection.
     *
     * @return the total number of tracks in this album or collection
     * @throws ID3Exception if the total number of tracks has not been set
     */
    public int getTotalTracks()
        throws ID3Exception
    {
        if (m_iTotalTracks != -1)
        {
            return m_iTotalTracks;
        }
        else
        {
            throw new ID3Exception("Total number of tracks not set.");
        }
    }
    
    protected byte[] getFrameId()
    {
        return "TRCK".getBytes();
    }
    
    public String toString()
    {
        return "Track number/Position in set: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TRCKTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TRCKTextInformationID3V2Frame oOtherTRCK = (TRCKTextInformationID3V2Frame)oOther;
        
        return ((m_iTrackNumber == oOtherTRCK.m_iTrackNumber) &&
                (m_iTotalTracks == oOtherTRCK.m_iTotalTracks) &&
                m_oTextEncoding.equals(oOtherTRCK.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTRCK.m_sInformation));
    }
}
