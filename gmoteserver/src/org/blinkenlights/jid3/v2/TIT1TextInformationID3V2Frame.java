/*
 * TIT1TextInformationID3V2Frame.java
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
 * $Id: TIT1TextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:21 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the content group description.
 */
public class TIT1TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sContentGroupDescription = null;

    /** Constructor.
     *
     * @param sContentGroupDescription the content group description
     */
    public TIT1TextInformationID3V2Frame(String sContentGroupDescription)
    {
        super(sContentGroupDescription);
        
        m_sContentGroupDescription = sContentGroupDescription;
    }

    public TIT1TextInformationID3V2Frame(InputStream  oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sContentGroupDescription = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTIT1TextInformationID3V2Frame(this);
    }
    
    /** Set the content group description.
     *
     * @param sContentGroupDescription the content group description
     */
    public void setContentGroupDescription(String sContentGroupDescription)
    {
        m_sContentGroupDescription = sContentGroupDescription;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sContentGroupDescription;
    }

    /** Get the content group description.
     *
     * @return the content group description
     */
    public String getContentGroupDescription()
    {
        return m_sContentGroupDescription;
    }

    protected byte[] getFrameId()
    {
        return "TIT1".getBytes();
    }
    
    public String toString()
    {
        return "Content group description: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TIT1TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TIT1TextInformationID3V2Frame oOtherTIT1 = (TIT1TextInformationID3V2Frame)oOther;
        
        return (m_sContentGroupDescription.equals(oOtherTIT1.m_sContentGroupDescription) &&
                m_oTextEncoding.equals(oOtherTIT1.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTIT1.m_sInformation));
    }
}
