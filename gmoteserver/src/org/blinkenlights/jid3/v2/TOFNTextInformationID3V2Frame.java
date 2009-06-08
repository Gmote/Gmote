/*
 * TOFNTextInformationID3V2Frame.java
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
 * $Id: TOFNTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the original, or preferred, filename for the tagged file.
 */
public class TOFNTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sOriginalFilename = null;
    
    /** Constructor.
     *
     * @param sOriginalFilename the original, or preferred, filename for the tagged file
     */
    public TOFNTextInformationID3V2Frame(String sOriginalFilename)
    {
        super(sOriginalFilename);
        
        m_sOriginalFilename = sOriginalFilename;
    }

    public TOFNTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sOriginalFilename = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTOFNTextInformationID3V2Frame(this);
    }

    /** Set the original, or preferred, filename for the tagged file.
     *
     * @param sOriginalFilename the original, or preferred, filename for the tagged file
     */
    public void setOriginalFilename(String sOriginalFilename)
    {
        m_sOriginalFilename = sOriginalFilename;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sOriginalFilename;
    }
    
    /** Get the original, or preferred, filename for this tagged file.
     *
     * @return the original, or preferred, filename for the tagged file
     */
    public String getOriginalFilename()
    {
        return m_sOriginalFilename;
    }
    
    protected byte[] getFrameId()
    {
        return "TOFN".getBytes();
    }
    
    public String toString()
    {
        return "Original filename: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TOFNTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TOFNTextInformationID3V2Frame oOtherTOFN = (TOFNTextInformationID3V2Frame)oOther;
        
        return (m_sOriginalFilename.equals(oOtherTOFN.m_sOriginalFilename) &&
                m_oTextEncoding.equals(oOtherTOFN.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTOFN.m_sInformation));
    }
}
