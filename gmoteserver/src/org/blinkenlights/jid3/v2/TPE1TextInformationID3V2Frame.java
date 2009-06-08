/*
 * TPE1TextInformationID3V2Frame.java
 *
 * Created on 26-Nov-2003
 *
 * Copyright (C)2003-2005 Paul Grebenc
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
 * $Id: TPE1TextInformationID3V2Frame.java,v 1.12 2005/02/06 18:11:23 paul Exp $
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
 * Text frame containing the lead artist(s), lead performer(s), soloist(s) or performing group in the track.
 */
public class TPE1TextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String[] m_asLeadPerformer = null;
    
    /** Constructor.
     *
     * @param sLeadPerformer the lead artist, performer, soloist or performing group in the track
     */
    public TPE1TextInformationID3V2Frame(String sLeadPerformer)
    {
        super(sLeadPerformer);
        
        m_asLeadPerformer = getPerformers(sLeadPerformer);
    }

    /** Constructor.
     *
     * @param asLeadPerformer the lead artist(s), performer(s), soloist(s) or performing group in this track
     */
    public TPE1TextInformationID3V2Frame(String[] asLeadPerformer)
    {
        super("");
        
        // build single string of composers, separated by "/", as described in ID3 spec
        StringBuffer sbLeadPerformers = new StringBuffer();
        for (int i=0; i < asLeadPerformer.length; i++)
        {
            sbLeadPerformers.append(asLeadPerformer[i] + "/");
        }
        sbLeadPerformers.deleteCharAt(sbLeadPerformers.length()-1);   // delete last "/"
        m_sInformation = sbLeadPerformers.toString();
        
        m_asLeadPerformer = getPerformers(m_sInformation);
    }
    
    public TPE1TextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_asLeadPerformer = getPerformers(m_sInformation);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTPE1TextInformationID3V2Frame(this);
    }

    /** Set the lead artist, performer, soloist or performing group for this track.
     * Multiple values can optionally be set with this method
     * by separating them with a slash "/" character.
     *
     * @param sLeadPerformer the lead artist, performer, soloist or performing group for this track
     */
    public void setLeadPerformer(String sLeadPerformer)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sLeadPerformer;
        m_asLeadPerformer = getPerformers(sLeadPerformer);
    }
    
    /** Set the lead artist(s), performer(s), soloist(s) or performing group for this track.
     *
     * @param asLeadPerformer the lead artist(s), performer(s), soloist(s) or performing group for this track
     */
    public void setLeadPerformers(String[] asLeadPerformer)
    {
        // build single string of performers, separated by "/", as described in ID3 spec
        StringBuffer sbLeadPerformers = new StringBuffer();
        for (int i=0; i < asLeadPerformer.length; i++)
        {
            sbLeadPerformers.append(asLeadPerformer[i] + "/");
        }
        sbLeadPerformers.deleteCharAt(sbLeadPerformers.length()-1);   // delete last "/"
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sbLeadPerformers.toString();
        
        m_asLeadPerformer = getPerformers(m_sInformation);
    }
    
    /** Get the lead artist(s), performer(s), soloist(s) or performing group for this track.
     *
     * @return the lead artist(s), performer(s), soloist(s) or performing group for this track
     */
    public String[] getLeadPerformers()
    {
        return m_asLeadPerformer;
    }
    
    protected byte[] getFrameId()
    {
        return "TPE1".getBytes();
    }
    
    public String toString()
    {
        return "Lead performer(s)/Soloist(s): [" + m_sInformation + "]";
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
        if ((oOther == null) || (!(oOther instanceof TPE1TextInformationID3V2Frame)))
        {
            return false;
        }
        
        TPE1TextInformationID3V2Frame oOtherTPE1 = (TPE1TextInformationID3V2Frame)oOther;
        
        return (m_sInformation.equals(oOtherTPE1.m_sInformation) &&
                m_oTextEncoding.equals(oOtherTPE1.m_oTextEncoding) &&
                Arrays.equals(m_asLeadPerformer, oOtherTPE1.m_asLeadPerformer));
    }
}
