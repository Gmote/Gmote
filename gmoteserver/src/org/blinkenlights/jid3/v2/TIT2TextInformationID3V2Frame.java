/*
 * TIT2TextInformationID3V2Frame.java
 *
 * Created on 3-Jan-2004
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
 * $Id: TIT2TextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the title, song name, or content description of the track.
 */
public class TIT2TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sTitle = null;

    /** Constructor.
     *
     * @param sTitle the title, song name, or content description of the track
     */
    public TIT2TextInformationID3V2Frame(String sTitle)
    {
        super(sTitle);
        
        m_sTitle = sTitle;
    }

    public TIT2TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sTitle = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTIT2TextInformationID3V2Frame(this);
    }

    /** Set the title, song name, or content description of the track.
     *
     * @param sTitle the title, song name, or content description of the track
     */
    public void setTitle(String sTitle)
    {
        m_sTitle = sTitle;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sTitle;
    }

    /** Get the title, song name, or content description of the track.
     *
     * @return the title, song name, or content description of the track
     */
    public String getTitle()
    {
        return m_sTitle;
    }

    protected byte[] getFrameId()
    {
        return "TIT2".getBytes();
    }
    
    public String toString()
    {
        return "Title/Songname/Content description: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TIT2TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TIT2TextInformationID3V2Frame oOtherTIT2 = (TIT2TextInformationID3V2Frame)oOther;
        
        return (m_sTitle.equals(oOtherTIT2.m_sTitle) &&
                m_oTextEncoding.equals(oOtherTIT2.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTIT2.m_sInformation));
    }
}
