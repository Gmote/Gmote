/*
 * TPE3TextInformationID3V2Frame.java
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
 * $Id: TPE3TextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the conductor of the recording in this track.
 */
public class TPE3TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sConductor = null;
    
    /** Constructor.
     *
     * @param sConductor the conductor of the recording in this track
     */
    public TPE3TextInformationID3V2Frame(String sConductor)
    {
        super(sConductor);
        
        m_sConductor = sConductor;
    }

    public TPE3TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sConductor = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTPE3TextInformationID3V2Frame(this);
    }

    /** Set the conductor of the recording in this track.
     *
     * @param sConductor the conductor of the recording in this track
     */
    public void setConductor(String sConductor)
    {
        m_sConductor = sConductor;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sConductor;
    }

    /** Get the conductor of the recording in this track.
     *
     * @return the conductor of the recording in this track
     */
    public String getConductor()
    {
        return m_sConductor;
    }
    
    protected byte[] getFrameId()
    {
        return "TPE3".getBytes();
    }
    
    public String toString()
    {
        return "Conductor: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TPE3TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TPE3TextInformationID3V2Frame oOtherTPE3 = (TPE3TextInformationID3V2Frame)oOther;
        
        return (m_sConductor.equals(oOtherTPE3.m_sConductor) &&
                m_oTextEncoding.equals(oOtherTPE3.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTPE3.m_sInformation));
    }
}
