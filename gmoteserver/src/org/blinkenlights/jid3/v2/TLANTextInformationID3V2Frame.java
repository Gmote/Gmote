/*
 * TLANTextInformationID3V2Frame.java
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
 * $Id: TLANTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the language(s) used in this track.  Although this requirement is
 * not enforced, languages should be specified using ISO-639-2 three letter language codes.
 */
public class TLANTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sLanguages = null;

    /** Constructor.
     *
     * @param sLanguages the language(s) used in this track
     */
    public TLANTextInformationID3V2Frame(String sLanguages)
    {
        // We could try to check to see if the language codes are valid... but in Sun's
        // documentation of the getLanguage method of the Locale class in jdk1.4, it is
        // claimed that ISO639 is not a stable standard.
        super(sLanguages);
        
        m_sLanguages = sLanguages;
    }

    public TLANTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sLanguages = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTLANTextInformationID3V2Frame(this);
    }

    /** Set the language(s) used in this track.
     *
     * @param sLanguages the language(s) used in this track
     */
    public void setLanguages(String sLanguages)
    {
        m_sLanguages = sLanguages;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sLanguages;
    }

    /** Get the language(s) used in this track.
     *
     * @return the language(s) used in this track
     */
    public String getLanguages()
    {
        return m_sLanguages;
    }
    
    protected byte[] getFrameId()
    {
        return "TLAN".getBytes();
    }
    
    public String toString()
    {
        return "Language(s): [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TLANTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TLANTextInformationID3V2Frame oOtherTLAN = (TLANTextInformationID3V2Frame)oOther;
        
        return (m_sLanguages.equals(oOtherTLAN.m_sLanguages) &&
                m_oTextEncoding.equals(oOtherTLAN.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTLAN.m_sInformation));
    }
}
