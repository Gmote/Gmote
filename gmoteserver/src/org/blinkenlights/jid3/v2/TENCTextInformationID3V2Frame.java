/*
 * TENCTextInformationID3V2Frame.java
 *
 * Created on 8-Jan-2004
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
 * $Id: TENCTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the identity of the encoder.
 */
public class TENCTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sEncodedBy = null;
    
    /** Constructor.
     *
     * @param sEncodedBy the identity of the encoder of this file
     */
    public TENCTextInformationID3V2Frame(String sEncodedBy)
    {
        super(sEncodedBy);
        
        m_sEncodedBy = sEncodedBy;
    }

    public TENCTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sEncodedBy = m_sInformation;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTENCTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TENC".getBytes();
    }
    
    public String toString()
    {
        return "Encoded by: [" + m_sInformation + "]";
    }
    
    /** Set the identity of the encoder of this file.
     *
     * @param sEncodedBy the identity of the encoder of this file
     */
    public void setEncodedBy(String sEncodedBy)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sEncodedBy;
        m_sEncodedBy = sEncodedBy;
    }
    
    /** Get the identity of the encoder of this file.
     *
     * @return the identity of the encoder
     */
    public String getEncodedBy()
    {
        return m_sEncodedBy;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TENCTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TENCTextInformationID3V2Frame oOtherTENC = (TENCTextInformationID3V2Frame)oOther;
        
        return (m_sEncodedBy.equals(oOtherTENC.m_sEncodedBy) &&
                m_oTextEncoding.equals(oOtherTENC.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTENC.m_sInformation));
    }
}
