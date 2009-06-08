/*
 * TIT3TextInformationID3V2Frame.java
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
 * $Id: TIT3TextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the subtitle, or a description refinement, for the track.
 */
public class TIT3TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sSubtitle = null;
    
    /** Constructor.
     *
     * @param sSubtitle the subtitle, or a description refinement, for the track
     */
    public TIT3TextInformationID3V2Frame(String sSubtitle)
    {
        super(sSubtitle);
        
        m_sSubtitle = sSubtitle;
    }

    public TIT3TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sSubtitle = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTIT3TextInformationID3V2Frame(this);
    }

    /** Set the subtitle, or a description refinement, for the track.
     *
     * @param sSubtitle the subtitle, or a description refinement, for the track
     */
    public void setSubtitle(String sSubtitle)
    {
        m_sSubtitle = sSubtitle;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sSubtitle;
    }

    /** Get the subtitle, or description refinement, for the track.
     *
     * @return the subtitle or description refinement
     */
    public String getSubtitle()
    {
        return m_sSubtitle;
    }

    protected byte[] getFrameId()
    {
        return "TIT3".getBytes();
    }
    
    public String toString()
    {
        return "Subtitle/Description refinement: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TIT3TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TIT3TextInformationID3V2Frame oOtherTIT3 = (TIT3TextInformationID3V2Frame)oOther;
        
        return (m_sSubtitle.equals(oOtherTIT3.m_sSubtitle) &&
                m_oTextEncoding.equals(oOtherTIT3.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTIT3.m_sInformation));
    }
}
