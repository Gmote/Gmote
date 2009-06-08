/*
 * TSSETextInformationID3V2Frame.java
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
 * $Id: TSSETextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing a description of the software and/or hardware settings and/or encoders used to encode
 * the track which is being tagged.
 */
public class TSSETextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sHardwareSoftwareSettings = null;
    
    /** Constructor.
     *
     * @param sHardwareSoftwareSettings the hardware and/or software settings and/or encoders used to encode
     *        the track which is being tagged
     */
    public TSSETextInformationID3V2Frame(String sHardwareSoftwareSettings)
    {
        super(sHardwareSoftwareSettings);
        
        m_sHardwareSoftwareSettings = sHardwareSoftwareSettings;
    }

    public TSSETextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sHardwareSoftwareSettings = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTSSETextInformationID3V2Frame(this);
    }

    /** Set the hardware and/or software settings and/or encoders used to encode the track
     *  which is being tagged.
     *
     * @param sHardwareSoftwareSettings the hardware and/or software settings and/or encoders used to encode
     *        the track which is being tagged
     */
    public void setHardwareSoftwareSettings(String sHardwareSoftwareSettings)
    {
        m_sHardwareSoftwareSettings = sHardwareSoftwareSettings;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sHardwareSoftwareSettings;
    }

    /** Get the hardware and/or software settings and/or encoders used to encode the track
     *  which was tagged.
     *
     * @return the hardware and/or software settings and/or encoders used to encode this track
     */
    public String getHardwareSoftwareSettings()
    {
        return m_sHardwareSoftwareSettings;
    }
    
    protected byte[] getFrameId()
    {
        return "TSSE".getBytes();
    }
    
    public String toString()
    {
        return "Software/Hardware and settings used for encoding: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TSSETextInformationID3V2Frame)))
        {
            return false;
        }
        
        TSSETextInformationID3V2Frame oOtherTSSE = (TSSETextInformationID3V2Frame)oOther;
        
        return (m_sHardwareSoftwareSettings.equals(oOtherTSSE.m_sHardwareSoftwareSettings) &&
                m_oTextEncoding.equals(oOtherTSSE.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTSSE.m_sInformation));
    }
}
