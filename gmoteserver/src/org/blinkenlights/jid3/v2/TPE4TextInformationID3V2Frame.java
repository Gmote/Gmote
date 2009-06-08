/*
 * TPE4TextInformationID3V2Frame.java
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
 * $Id: TPE4TextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:21 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the interpreter, remixer or modifier of the recording in this track.
 */
public class TPE4TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sModifiedBy = null;

    /** Constructor.
     *
     * @param sModifiedBy the interpreter, remixer or modifier of the recording in this track
     */
    public TPE4TextInformationID3V2Frame(String sModifiedBy)
    {
        super(sModifiedBy);
        
        m_sModifiedBy = sModifiedBy;
    }

    public TPE4TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sModifiedBy = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTPE4TextInformationID3V2Frame(this);
    }

    /** Set the interpreter, remixer or modifier of the recording in this track.
     *
     * @param sModifiedBy the interpreter, remixer or modifier of the recording in this track
     */
    public void setModifiedBy(String sModifiedBy)
    {
        m_sModifiedBy = sModifiedBy;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sModifiedBy;
    }
    
    /** Get the interpreter, remixer or modifier of the recording in this track.
     *
     * @return the interpreter, remixer or modifier of the recording in this track
     */
    public String getModifiedBy()
    {
        return m_sModifiedBy;
    }
    
    protected byte[] getFrameId()
    {
        return "TPE4".getBytes();
    }
    
    public String toString()
    {
        return "Interpreted, remixed, or otherwise modified by: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TPE4TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TPE4TextInformationID3V2Frame oOtherTPE4 = (TPE4TextInformationID3V2Frame)oOther;
        
        return (m_sModifiedBy.equals(oOtherTPE4.m_sModifiedBy) &&
                m_oTextEncoding.equals(oOtherTPE4.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTPE4.m_sInformation));
    }
}
