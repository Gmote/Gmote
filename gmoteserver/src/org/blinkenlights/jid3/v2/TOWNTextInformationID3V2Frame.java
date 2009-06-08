/*
 * TOWNTextInformationID3V2Frame.java
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
 * $Id: TOWNTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the file owner or licensee of the content of this track.
 */
public class TOWNTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sFileOwner = null;

    /** Constructor.
     *
     * @param sFileOwner the owner or licensee of the content of this track
     */
    public TOWNTextInformationID3V2Frame(String sFileOwner)
    {
        super(sFileOwner);
        
        m_sFileOwner = sFileOwner;
    }

    public TOWNTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sFileOwner = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTOWNTextInformationID3V2Frame(this);
    }

    /** Set the owner or licensee of the content of this track.
     *
     * @param sFileOwner the owner or licensee of the content of this track
     */
    public void setFileOwner(String sFileOwner)
    {
        m_sFileOwner = sFileOwner;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sFileOwner;
    }
    
    /** Get the owner or licensee of the content of this track.
     *
     * @return the owner or licensee of the content of this track
     */
    public String getFileOwner()
    {
        return m_sFileOwner;
    }
    
    protected byte[] getFrameId()
    {
        return "TOWN".getBytes();
    }
    
    public String toString()
    {
        return "File owner/licensee: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TOWNTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TOWNTextInformationID3V2Frame oOtherTOWN = (TOWNTextInformationID3V2Frame)oOther;
        
        return (m_sFileOwner.equals(oOtherTOWN.m_sFileOwner) &&
                m_oTextEncoding.equals(oOtherTOWN.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTOWN.m_sInformation));
    }
}
