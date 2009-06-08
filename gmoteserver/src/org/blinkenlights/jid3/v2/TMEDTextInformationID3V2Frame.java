/*
 * TMEDTextInformationID3V2Frame.java
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
 * $Id: TMEDTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:16 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the media type from which the recording in this track was transferred.
 *
 * Refer to the ID3 v2.3.0 specification for a description of legal values.
 */
public class TMEDTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    // These types are a bit of a mess.  Here are some of the predefined values, but
    //  creating providing for all combinations here would be more effort than it's worth.
    public static final String DIGITAL = "(DIG) - Digital";
    public static final String ANALOG = "(ANA) - Analog";
    public static final String CD = "(CD) - CD";
    public static final String LASERDISC = "(LD) - Laserdisc";
    public static final String TURNTABLE_RECORD = "(TT) - Turntable records";
    public static final String MINIDISC = "(MD) - MiniDisc";
    public static final String DAT = "(DAT) - DAT";
    public static final String DIGITAL_CASSETTE = "(DCC) - DCC";
    public static final String DVD = "(DVD) - DVD";
    public static final String TV = "(TV) - Television";
    public static final String VIDEO = "(VID) - Video";
    public static final String RADIO = "(RAD) - Radio";
    public static final String TELEPHONE = "(TEL) - Telephone";
    public static final String PHILIPS_CASSETTE = "(MC) - Normal Cassette";
    public static final String REEL = "(REE) - Reel";
    
    private String m_sMediaType = null;

    /** Constructor.
     *
     * @param sMediaType the media type from which the recording in this track was transferred
     */
    public TMEDTextInformationID3V2Frame(String sMediaType)
    {
        super(sMediaType);
        
        m_sMediaType = sMediaType;
    }

    public TMEDTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sMediaType = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTMEDTextInformationID3V2Frame(this);
    }

    /** Set the media type.
     *
     * @param sMediaType the media type from which the recording in this track was transferred
     */
    public void setMediaType(String sMediaType)
    {
        m_sMediaType = sMediaType;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sMediaType;
    }
    
    /** Get the media type.
     *
     * @return the media type from which the recording in this track was transferred
     */
    public String getMediaType()
    {
        return m_sMediaType;
    }
    
    protected byte[] getFrameId()
    {
        return "TMED".getBytes();
    }
    
    public String toString()
    {
        return "Media type: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TMEDTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TMEDTextInformationID3V2Frame oOtherTMED = (TMEDTextInformationID3V2Frame)oOther;
        
        return (m_sMediaType.equals(oOtherTMED.m_sMediaType) &&
                m_oTextEncoding.equals(oOtherTMED.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTMED.m_sInformation));
    }
}
