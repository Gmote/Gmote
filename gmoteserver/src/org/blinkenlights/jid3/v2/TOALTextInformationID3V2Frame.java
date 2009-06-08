/*
 * TOALTextInformationID3V2Frame.java
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
 * $Id: TOALTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the original album, movie, or show title of the track.
 */
public class TOALTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sOriginalAlbumTitle = null;
    
    /** Constructor.
     *
     * @param sOriginalAlbumTitle the original album, movie, or show title of the track
     */
    public TOALTextInformationID3V2Frame(String sOriginalAlbumTitle)
    {
        super(sOriginalAlbumTitle);
        
        m_sOriginalAlbumTitle = sOriginalAlbumTitle;
    }

    public TOALTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sOriginalAlbumTitle = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTOALTextInformationID3V2Frame(this);
    }

    /** Set the original album, movie, or show title of the track.
     *
     * @param sOriginalAlbumTitle the original album, movie, or show title of the track
     */
    public void setOriginalAlbumTitle(String sOriginalAlbumTitle)
    {
        m_sOriginalAlbumTitle = sOriginalAlbumTitle;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sOriginalAlbumTitle;
    }
    
    /** Get the original album, movie, or show title of the track.
     *
     * @return the original album, movie, or show title of the track
     */
    public String getOriginalAlbumTitle()
    {
        return m_sOriginalAlbumTitle;
    }
    
    protected byte[] getFrameId()
    {
        return "TOAL".getBytes();
    }
    
    public String toString()
    {
        return "Original album/movie/show title: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TOALTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TOALTextInformationID3V2Frame oOtherTOAL = (TOALTextInformationID3V2Frame)oOther;
        
        return (m_sOriginalAlbumTitle.equals(oOtherTOAL.m_sOriginalAlbumTitle) &&
                m_oTextEncoding.equals(oOtherTOAL.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTOAL.m_sInformation));
    }
}
