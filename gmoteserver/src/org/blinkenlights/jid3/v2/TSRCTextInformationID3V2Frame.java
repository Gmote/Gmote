/*
 * TSRCTextInformationID3V2Frame.java
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
 * $Id: TSRCTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the International Standard Recording Code (ISRC) for this track.
 * ISRC codes are 12 characters in length.
 */
public class TSRCTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sISRC = null;
    
    /** Constructor.
     *
     * @param sISRC the ISRC code for this track
     * @throws ID3Exception if the ISRC code is not 12 characters in length
     */
    public TSRCTextInformationID3V2Frame(String sISRC)
        throws ID3Exception
    {
        super(sISRC);
              
        if (sISRC.length() != 12)
        {
            throw new ID3Exception("ISRC code must be 12 characters long.");
        }
        
        m_sISRC = sISRC;
    }

    public TSRCTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        // we're not going to enforce the 12-character limit when reading tracks
        
        m_sISRC = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTSRCTextInformationID3V2Frame(this);
    }

    /** Set the ISRC code for this track.
     *
     * @param sISRC the ISRC code for this track
     * @throws ID3Exception if the ISRC code is not 12 characters in length
     */
    public void setISRC(String sISRC)
        throws ID3Exception
    {
        if (sISRC.length() != 12)
        {
            throw new ID3Exception("ISRC code must be 12 characters long.");
        }
        
        m_sISRC = sISRC;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sISRC;
    }

    /** Get the ISRC code for this track.
     *
     * @return the ISRC code for this track
     */
    public String getISRC()
    {
        return m_sISRC;
    }
    
    protected byte[] getFrameId()
    {
        return "TSRC".getBytes();
    }
    
    public String toString()
    {
        return "International Standard Recording Code (ISRC): [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TSRCTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TSRCTextInformationID3V2Frame oOtherTSRC = (TSRCTextInformationID3V2Frame)oOther;
        
        return (m_sISRC.equals(oOtherTSRC.m_sISRC) &&
                m_oTextEncoding.equals(oOtherTSRC.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTSRC.m_sInformation));
    }
}
