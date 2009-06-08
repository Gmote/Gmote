/*
 * TKEYTextInformationID3V2Frame.java
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
 * $Id: TKEYTextInformationID3V2Frame.java,v 1.7 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame containing the initial key in the track.
 *
 * It is represented as a string with a maximum length of three characters.
 * The ground keys are represented with "A","B","C","D","E", "F" and "G" and halfkeys 
 * represented with "b" and "#". Minor is represented as "m". Example "Cbm". Off key 
 * is represented with an "o" only.
 */
public class TKEYTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String m_sInitialKey = null;

    /** Constructor.
     *
     * @param sInitialKey the initial key in the track
     * @throws ID3Exception if the initial key string is invalid
     */
    public TKEYTextInformationID3V2Frame(String sInitialKey)
        throws ID3Exception
    {
        super(sInitialKey);

        // check to make sure initial key format is valid
        if (! sInitialKey.matches("([A-G][#b]?m?|o)"))
        {
            throw new ID3Exception("Invalid initial key string.");
        }
        
        m_sInitialKey = sInitialKey;
    }

    public TKEYTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sInitialKey = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTKEYTextInformationID3V2Frame(this);
    }

    /** Set the initial key of the track.
     *
     * @param sInitialKey the initial key in the track
     * @throws ID3Exception if the initial key string is invalid
     */
    public void setInitialKey(String sInitialKey)
        throws ID3Exception
    {
        // check to make sure initial key format is valid
        if (! sInitialKey.matches("([A-G][#b]?m?|o)"))
        {
            throw new ID3Exception("Invalid initial key string.");
        }
        
        m_sInitialKey = sInitialKey;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sInitialKey;
    }

    /** Get the initial key of the track.  <I>There is no guarantee that values read from a file
     *  will be in a valid format.</I>
     *
     * @return the initial key in the track
     */
    public String getInitialKey()
    {
        return m_sInitialKey;
    }
    
    protected byte[] getFrameId()
    {
        return "TKEY".getBytes();
    }
    
    public String toString()
    {
        return "Initial key: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TKEYTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TKEYTextInformationID3V2Frame oOtherTKEY = (TKEYTextInformationID3V2Frame)oOther;
        
        return (m_sInitialKey.equals(oOtherTKEY.m_sInitialKey) &&
                m_oTextEncoding.equals(oOtherTKEY.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTKEY.m_sInformation));
    }
}
