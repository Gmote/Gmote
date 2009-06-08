/*
 * TOPETextInformationID3V2Frame.java
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
 * $Id: TOPETextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:16 paul Exp $
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
 * Text frame containing the original artist(s) or performer(s) of the original recording in this track.
 */
public class TOPETextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String[] m_asOriginalPerformer = null;
    
    /** Constructor.
     *
     * @param sOriginalPerformer the original artist or performer for this track
     */
    public TOPETextInformationID3V2Frame(String sOriginalPerformer)
    {
        super(sOriginalPerformer);
        
        m_asOriginalPerformer = getPerformers(sOriginalPerformer);
    }

    /** Constructor.
     *
     * @param asOriginalPerformer the original artist(s) or performer(s) for this track
     */
    public TOPETextInformationID3V2Frame(String[] asOriginalPerformer)
    {
        super("");
        
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbPerformers = new StringBuffer();
        for (int i=0; i < asOriginalPerformer.length; i++)
        {
            sbPerformers.append(asOriginalPerformer[i] + "/");
        }
        sbPerformers.deleteCharAt(sbPerformers.length()-1);   // delete last "/"
        m_sInformation = sbPerformers.toString();
        
        m_asOriginalPerformer = getPerformers(m_sInformation);
    }
    
    public TOPETextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);

        m_asOriginalPerformer = getPerformers(m_sInformation);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTOPETextInformationID3V2Frame(this);
    }

    /** Set the original author(s) or performer(s) for the recording in this track.
     *  Multiple performers can optionally be set with this method by separating them
     *  with a slash "/" character.
     *
     * @param sOriginalPerformer the original author or performer for this track
     */
    public void setOriginalPerformer(String sOriginalPerformer)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sOriginalPerformer;
        m_asOriginalPerformer = getPerformers(sOriginalPerformer);
    }
    
    /** Set the original author(s) or performer(s) for the recording in this track.
     *
     * @param asOriginalPerformer the original author(s) or performer(s) for this track
     */
    public void setOriginalPerformers(String[] asOriginalPerformer)
    {
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbPerformers = new StringBuffer();
        for (int i=0; i < asOriginalPerformer.length; i++)
        {
            sbPerformers.append(asOriginalPerformer[i] + "/");
        }
        sbPerformers.deleteCharAt(sbPerformers.length()-1);   // delete last "/"
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sbPerformers.toString();
        
        m_asOriginalPerformer = getPerformers(m_sInformation);
    }
    
    /** Get the original author(s) or performer(s) for this track
     *
     * @return the original author(s) or performer(s) for this track
     */
    public String[] getOriginalPerformers()
    {
        return m_asOriginalPerformer;
    }
    
    protected byte[] getFrameId()
    {
        return "TOPE".getBytes();
    }
    
    public String toString()
    {
        return "Original artist(s)/performer(s): [" + m_sInformation + "]";
    }
    
    /** Split a string containing potentially several distinct values (forward-slash separated) into
     *  an array of Strings, one value per String.
     *
     * @param sValue value to be separated
     * @return an array of values
     */
    private String[] getPerformers(String sValue)
    {
        String[] asPerformer = sValue.split("/");
        
        return asPerformer;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TOPETextInformationID3V2Frame)))
        {
            return false;
        }
        
        TOPETextInformationID3V2Frame oOtherTOPE = (TOPETextInformationID3V2Frame)oOther;
        
        return (m_sInformation.equals(oOtherTOPE.m_sInformation) &&
                m_oTextEncoding.equals(oOtherTOPE.m_oTextEncoding) &&
                Arrays.equals(m_asOriginalPerformer, oOtherTOPE.m_asOriginalPerformer));
    }
}
