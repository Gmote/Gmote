/*
 * ID3V2Tag.java
 *
 * Created on 24-Nov-2003
 *
 * Copyright (C)2003-2005 Paul Grebenc
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
 * $Id: ID3V2Tag.java,v 1.23 2005/10/27 02:12:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;

/**
 * @author paul
 *
 * Base class representing all ID3 V2 tags.
 */
abstract public class ID3V2Tag extends ID3Tag
{
    /** Flag indicating whether unsynchronization is used in this tag or not. */
    protected boolean m_bUnsynchronizationUsedFlag;
    
    /** Flag indicating whether the extended header is present or not. */
    protected boolean m_bExtendedHeaderFlag;
    
    /** Flag indicating this tag should be considered experimental. */
    protected boolean m_bExperimentalFlag;
    
    /** Flag indicating whether a CRC value exists in the extended header. */
    protected boolean m_bCRCDataFlag;
    
    /** Mapping from frame ID to list containing frames. For frames that can only be used once. */
    protected Map m_oFrameIdToFrameMap = null;
    
    /** Default padding for ID3 v2 frames, if not specified.  16 bytes, because Winamp does not read the last
     *  frame when there isn't at least 6 bytes of padding following it in a tag. */
    private static int s_iDefaultPaddingLength = 16;
    
    /** Value specifying the amount of padding which is appended to the frames in this tag. */
    protected int m_iPaddingLength;

    /** Construct an ID3 V2 tag, specifying flag values.
     *
     * @param bUnsynchronizationUsedFlag specify whether unsynchronization is to be used in this tag or not
     * @param bExtendedHeaderFlag specify whether the extended header will be present or not
     * @param bExperimentalFlag specify whether this tag is to be considered experimental or not
     */
    public ID3V2Tag(boolean bUnsynchronizationUsedFlag,
                    boolean bExtendedHeaderFlag,
                    boolean bExperimentalFlag)
    {
        m_bUnsynchronizationUsedFlag = bUnsynchronizationUsedFlag;
        m_bExtendedHeaderFlag = bExtendedHeaderFlag;
        m_bExperimentalFlag = bExperimentalFlag;
        m_oFrameIdToFrameMap = new HashMap();
        //HACK: Default padding of 16 bytes, because Winamp doesn't seem to see the last frame in a v2 tag
        //      when there is less than 6 bytes of padding.  (???)
        m_iPaddingLength = s_iDefaultPaddingLength;
    }
    
    /** Get all frames set in this tag which can only be stored once in the tag.
     *  This method exists to aid in testing.
     */
    public ID3V2Frame[] getSingleFrames()
    {
        return (ID3V2Frame[])m_oFrameIdToFrameMap.values().toArray(new ID3V2Frame[0]);
    }
    
    /** Check if this tag contains at least one frame.  An ID3V2 tag requires at least one frame to be written.
     *
     * @return true if this tag contains at least one frame, and false otherwise
     */
    abstract public boolean containsAtLeastOneFrame();
    
    abstract public void sanityCheck() throws ID3Exception;
    
    /** Write this tag to an output stream.
     *
     * @param oOS the output stream to which this tag is to be written
     * @throws ID3Exception if an error occurs while writing
     */
    abstract public void write(OutputStream oOS) throws ID3Exception;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sbText = new StringBuffer();
        sbText.append("Unsynchronization: " + m_bUnsynchronizationUsedFlag +
                      "\nExtended header: " + m_bExtendedHeaderFlag +
                      "\nExperimental: " + m_bExperimentalFlag +
                      "\nCRC: " + m_bCRCDataFlag +
                      "\nPadding length: " + + m_iPaddingLength +
                      "\nNum frames: " + m_oFrameIdToFrameMap.size());
        Iterator oIter = m_oFrameIdToFrameMap.keySet().iterator();
        while(oIter.hasNext())
        {
            String sFrameId = (String)oIter.next();
            sbText.append("\n" + ((ID3V2Frame)m_oFrameIdToFrameMap.get(sFrameId)));
        }
        
        return sbText.toString();
    }

    /** Read a tag from an input stream.
     *
     * @param oIS the input stream from which to read a tag
     * @return the tag read
     * @throws ID3Exception if an error occurs while reading the tag
     */
    public static ID3V2Tag read(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oID3DIS = new ID3DataInputStream(oIS);
            
            // check which version of v2 tags we have
            int iMinorVersion = oID3DIS.readUnsignedByte();
            int iPatchVersion = oID3DIS.readUnsignedByte();
                    
            if (iMinorVersion == 3)
            {
                // there is a tag, we must read it
                ID3V2Tag oID3V2Tag = ID3V2_3_0Tag.internalRead(oID3DIS);
                
                return oID3V2Tag;
            }
            else
            {
                //TODO: If we're going to support >2.3.0 tags, do that here.
                return null;
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error reading tag.", e);
        }
    }
    
    /** Set the unsynchronization status.
     *
     * @param bUnsynchronizationUsed an indication of whether unsynchronization should be used when writing this tag
     */
    public void setUnsynchronization(boolean bUnsynchronizationUsed)
    {
        m_bUnsynchronizationUsedFlag = bUnsynchronizationUsed;
    }

    /** Get the current unsynchronization status for this tag.
     *
     * @return the current status of the unsynchronization flag
     */
    public boolean getUnsynchronization()
    {
        return m_bUnsynchronizationUsedFlag;
    }
    
    /** Set the extended header flag for this tag.
     *
     * @param bExtendedHeaderUsed an indication of whether the extended header should be included in this tag
     */
    public void setExtendedHeader(boolean bExtendedHeaderUsed)
    {
        m_bExtendedHeaderFlag = bExtendedHeaderUsed;
    }

    /** Get the current extended header status for this tag.
     *
     * @return the current status of the extended header flag
     */
    public boolean getExtendedHeader()
    {
        return m_bExtendedHeaderFlag;
    }
    
    /** Set the CRC flag (extended header must be enabled before this flag can be set.
     *
     * @param bCRCUsed an indication of whether a CRC value should be included in the extended header of this tag
     */
    public void setCRC(boolean bCRCUsed)
        throws ID3Exception
    {
        if ( ! m_bExtendedHeaderFlag)
        {
            throw new ID3Exception("The CRC flag cannot be set unless the extended header flag is set first.");
        }
        
        m_bCRCDataFlag = bCRCUsed;
    }

    /** Get the current CRC status for the extended header in this frame.
     *
     * @return true if the extended header is enabled, and the CRC flag is also enabled, or false otherwise
     */
    public boolean getCRC()
    {
        return m_bCRCDataFlag;
    }
    
    /** Set the default padding length to be added at the end of newly created tags.
     *
     * NOTE: When read by Winamp, it seems the last frame in a v2 tag is not seen, unless there are at least six bytes
     *       of padding at the end of the tag.  For this reason, the default padding at the end of v2 tags is set to 16.
     *       This value can be modified if desired, but be aware of this observation regarding Winamp.
     *
     * @param iPaddingLength the padding length to use
     * @throws ID3Exception if the padding length value is negative
     */
    public static void setDefaultPaddingLength(int iPaddingLength)
        throws ID3Exception
    {
        if (iPaddingLength < 0)
        {
            throw new ID3Exception("Padding length in ID3 V2 tag cannot be negative.");
        }
        
        s_iDefaultPaddingLength = iPaddingLength;
    }

    /** Set the padding length to be added at the end of this tag.
     *
     * NOTE: When read by Winamp, it seems the last frame in a v2 tag is not seen, unless there are at least six bytes
     *       of padding at the end of the tag.  For this reason, the default padding at the end of v2 tags is set to 16.
     *       This value can be modified if desired, but be aware of this observation regarding Winamp.
     *
     * @param iPaddingLength the padding length to use
     * @throws ID3Exception if the padding length value is negative
     */
    public void setPaddingLength(int iPaddingLength)
        throws ID3Exception
    {
        if (iPaddingLength < 0)
        {
            throw new ID3Exception("Padding length in ID3 V2 tag cannot be negative.");
        }
        
        m_iPaddingLength = iPaddingLength;
    }
    
    /** Get the default padding length currently set for newly created tags.
     *
     * @return the current padding length
     */
    public static int getDefaultPaddingLength()
    {
        return s_iDefaultPaddingLength;
    }

    /** Get the padding length currently set for this tag.
     *
     * @return the current padding length
     */
    public int getPaddingLength()
    {
        return m_iPaddingLength;
    }
    
    /** Convenience method for setting artist directly from tag.
     *
     * @param sArtist the artist name
     * @throws ID3Exception
     */
    abstract public void setArtist(String sArtist) throws ID3Exception;
    
    /** Convenience method for retrieving artist directly from tag.
     *
     * @return the artist value currently set
     * @throws ID3Exception
     */
    abstract public String getArtist();
    
    /** Convenience method for setting song title directly from tag.
     *
     * @param sTitle the song title
     * @throws ID3Exception
     */
    abstract public void setTitle(String sTitle) throws ID3Exception;
    
    /** Convenience method for retrieving song title directly from tag.
     *
     * @return the song title currently set
     * @throws ID3Exception
     */
    abstract public String getTitle();
    
    /** Convenience method for setting album title directly from tag.
     *
     * @param sAlbum the album title
     * @throws ID3Exception
     */
    abstract public void setAlbum(String sAlbum) throws ID3Exception;
    
    /** Convenience method for retrieving album title directly from tag.
     *
     * @return the album title currently set
     * @throws ID3Exception
     */
    abstract public String getAlbum();

    /** Convenience method for setting year directly from tag.
     *
     * @return the year of the recording
     * @throws ID3Exception
     */
    abstract public void setYear(int iYear) throws ID3Exception;
    
    /** Convenience method for retrieving year directly from tag.
     *
     * @return the year currently set
     * @throws ID3Exception if no year was set
     */
    abstract public int getYear() throws ID3Exception;
    
    /** Convenience method for setting track number directly from tag.
     *
     * @param iTrackNumber the track number
     * @throws ID3Exception
     */
    abstract public void setTrackNumber(int iTrackNumber) throws ID3Exception;
    
    /** Convenience method for setting track number and total number of tracks directly from tag.
     *
     * @param iTrackNumber the track number
     * @param iTotalTracks the total number of tracks
     * @throws ID3Exception
     */
    abstract public void setTrackNumber(int iTrackNumber, int iTotalTracks) throws ID3Exception;

    /** Convenience method for retrieving track number directly from tag.
     *
     * @return the track number currently set
     * @throws ID3Exception if not track number was set
     */
    abstract public int getTrackNumber() throws ID3Exception;
    
    /** Convenience method for retrieving total number of tracks directly from tag.
     *
     * @return the total number of tracks currently set
     * @throws ID3Exception if total number of tracks was not set
     */
    abstract public int getTotalTracks() throws ID3Exception;
    
    /** Convenience method for setting genre directly from tag.
     *
     * @param sGenre the genre (free-form)
     * @throws ID3Exception
     */
    abstract public void setGenre(String sGenre) throws ID3Exception;

    /** Convenience method for retrieving the genre directly from tag.
     *
     * @return the genre currently set
     * @throws ID3Exception
     */
    abstract public String getGenre();
    
    /** Convenience method for setting comment directly from tag.
     *
     * @param sComment the comment
     * @throws ID3Exception
     */
    abstract public void setComment(String sComment) throws ID3Exception;

    /** Convenience method for retrieving the comment directly from tag.
     *
     * @return the comment currently set
     * @throws ID3Exception
     */
    abstract public String getComment();
}
