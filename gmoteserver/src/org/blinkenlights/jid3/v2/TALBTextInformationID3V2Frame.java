/*
 * TALBTextInformationID3V2Frame.java
 *
 * Created on 2-Jan-2004
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
 * $Id: TALBTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the title of the album from which this recording is taken.
 */
public class TALBTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sAlbum = null;
    
    /** Constructor.
     *
     * @param sAlbum the title of the album from which this recording is taken
     */
    public TALBTextInformationID3V2Frame(String sAlbum)
    {
        super(sAlbum);
        
        m_sAlbum = sAlbum;
    }

    public TALBTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sAlbum = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTALBTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TALB".getBytes();
    }
    
    public String toString()
    {
        return "Album/Movie/Show title: [" + m_sInformation + "]";
    }

    /** Set the title of the album from which this recording is taken.
     *
     * @param sAlbum the title of the album
     */
    public void setAlbum(String sAlbum)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sAlbum;
        m_sAlbum = sAlbum;
    }

    /** Get the title of the album from which this recording is taken.
     *
     * @return the title of the album
     */
    public String getAlbum()
    {
        return m_sAlbum;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TALBTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TALBTextInformationID3V2Frame oOtherTALB = (TALBTextInformationID3V2Frame)oOther;
        
        return (m_sAlbum.equals(oOtherTALB.m_sAlbum) &&
                m_oTextEncoding.equals(oOtherTALB.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTALB.m_sInformation));
    }
}
