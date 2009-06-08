/*
 * TPUBTextInformationID3V2Frame.java
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
 * $Id: TPUBTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the publisher of the recording in this track.
 */
public class TPUBTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sPublisher = null;
    
    /** Constructor.
     *
     * @param sPublisher the publisher of the recording in this track
     */
    public TPUBTextInformationID3V2Frame(String sPublisher)
    {
        super(sPublisher);
        
        m_sPublisher = sPublisher;
    }

    public TPUBTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sPublisher = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTPUBTextInformationID3V2Frame(this);
    }

    /** Set the publisher of the recording in this track.
     *
     * @param sPublisher the publisher of the recording in this track
     */
    public void setPublisher(String sPublisher)
    {
        m_sPublisher = sPublisher;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sPublisher;
    }

    /** Get the publisher of the recording in this track.
     *
     * @return the publisher of the recording in this track
     */
    public String getPublisher()
    {
        return m_sPublisher;
    }
    
    protected byte[] getFrameId()
    {
        return "TPUB".getBytes();
    }
    
    public String toString()
    {
        return "Publisher: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TPUBTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TPUBTextInformationID3V2Frame oOtherTPUB = (TPUBTextInformationID3V2Frame)oOther;
        
        return (m_sPublisher.equals(oOtherTPUB.m_sPublisher) &&
                m_oTextEncoding.equals(oOtherTPUB.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTPUB.m_sInformation));
    }
}
