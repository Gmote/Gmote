/*
 * TCOMTextInformationID3V2Frame.java
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
 * $Id: TCOMTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the composer(s) of the track.
 */
public class TCOMTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String[] m_asComposer = null;
    
    /** Constructor.
     *
     * @param sComposer the composer(s) of this track (multiple composers separated by forward-slash)
     */
    public TCOMTextInformationID3V2Frame(String sComposer)
    {
        super(sComposer);
        
        m_asComposer = getComposers(sComposer);
    }
    
    /** Constructor.
     *
     * @param asComposer the composer(s) of this track
     */
    public TCOMTextInformationID3V2Frame(String[] asComposer)
    {
        super("");
        
        // build single string of composers, separated by "/", as described in ID3 spec
        StringBuffer sbComposers = new StringBuffer();
        for (int i=0; i < asComposer.length; i++)
        {
            sbComposers.append(asComposer[i] + "/");
        }
        sbComposers.deleteCharAt(sbComposers.length()-1);   // delete last "/"
        m_sInformation = sbComposers.toString();
        
        m_asComposer = getComposers(m_sInformation);
    }

    public TCOMTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_asComposer = getComposers(m_sInformation);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTCOMTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TCOM".getBytes();
    }
    
    public String toString()
    {
        return "Composer(s): [" + m_sInformation + "]";
    }
    
    /** Set the composer(s) of this track.  Multiple composers can optionally be set with this method
     * by separating them with a slash "/" character.
     *
     * @param sComposer the composer of this track
     */
    public void setComposer(String sComposer)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sComposer;
        m_asComposer = getComposers(sComposer);
    }
    
    /** Set the composer(s) of this track.
     *
     * @param asComposer the composers of this track
     */
    public void setComposers(String[] asComposer)
    {
        // build single string of composers, separated by "/", as described in ID3 spec
        StringBuffer sbComposers = new StringBuffer();
        for (int i=0; i < asComposer.length; i++)
        {
            sbComposers.append(asComposer[i] + "/");
        }
        sbComposers.deleteCharAt(sbComposers.length()-1);   // delete last "/"
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sbComposers.toString();
        
        m_asComposer = getComposers(m_sInformation);
    }
    
    /** Get the composer(s) of this track.
     *
     * @return the composer(s) of this track
     */
    public String[] getComposers()
    {
        return m_asComposer;
    }
    
    /** Split a string containing potentially several distinct values (forward-slash separated) into
     *  an array of Strings, one value per String.
     *
     * @param sValue value to be separated
     * @return an array of values
     */
    private String[] getComposers(String sValue)
    {
        String[] asComposer = sValue.split("/");
        
        return asComposer;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TCOMTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TCOMTextInformationID3V2Frame oOtherTCOM = (TCOMTextInformationID3V2Frame)oOther;
        
        return (m_sInformation.equals(oOtherTCOM.m_sInformation) &&
                m_oTextEncoding.equals(oOtherTCOM.m_oTextEncoding) &&
                Arrays.equals(m_asComposer, oOtherTCOM.m_asComposer));
    }
}
