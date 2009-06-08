/*
 * TEXTTextInformationID3V2Frame.java
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
 * $Id: TEXTTextInformationID3V2Frame.java,v 1.9 2005/02/06 18:11:23 paul Exp $
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
 * Text frame containing the lyricist(s) or author(s) of the text in the recording in this track.
 */
public class TEXTTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String[] m_asLyricist = null;
    
    /** Constructor.
     *
     * @param sLyricist the lyricist or author of the text for this track
     */
    public TEXTTextInformationID3V2Frame(String sLyricist)
    {
        super(sLyricist);
        
        m_asLyricist = getLyricists(sLyricist);
    }

    /** Constructor.
     *
     * @param asLyricist the lyricist(s) or author(s) of the text for this track
     */
    public TEXTTextInformationID3V2Frame(String[] asLyricist)
    {
        super("");
        
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbLyricists = new StringBuffer();
        for (int i=0; i < asLyricist.length; i++)
        {
            sbLyricists.append(asLyricist[i] + "/");
        }
        sbLyricists.deleteCharAt(sbLyricists.length()-1);   // delete last "/"
        m_sInformation = sbLyricists.toString();
        
        m_asLyricist = getLyricists(m_sInformation);
    }

    public TEXTTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);

        m_asLyricist = getLyricists(m_sInformation);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTEXTTextInformationID3V2Frame(this);
    }

    /** Set the lyricist(s) or author(s) of the text in the recording in this track.
     *  Multiple lyricists can optionally be set with this method by separating them
     *  with a slash "/" character.
     *
     * @param sLyricist the lyricist for this track
     */
    public void setLyricist(String sLyricist)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sLyricist;
        m_asLyricist = getLyricists(sLyricist);
    }
    
    /** Set the lyricist(s) or author(s) of the text in the recording in this track.
     *
     * @param asLyricist the lyricists for this track
     */
    public void setLyricists(String[] asLyricist)
    {
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbLyricists = new StringBuffer();
        for (int i=0; i < asLyricist.length; i++)
        {
            sbLyricists.append(asLyricist[i] + "/");
        }
        sbLyricists.deleteCharAt(sbLyricists.length()-1);   // delete last "/"
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sbLyricists.toString();
        
        m_asLyricist = getLyricists(m_sInformation);
    }
    
    /** Get the lyricist(s) or author(s) of the text for this track
     *
     * @return the lyricist(s) or author(s) of the text for this track
     */
    public String[] getLyricists()
    {
        return m_asLyricist;
    }
    
    protected byte[] getFrameId()
    {
        return "TEXT".getBytes();
    }
    
    public String toString()
    {
        return "Lyricist(s)/Text writer(s): [" + m_sInformation + "]";
    }
    
    
    /** Split a string containing potentially several distinct values (forward-slash separated) into
     *  an array of Strings, one value per String.
     *
     * @param sValue value to be separated
     * @return an array of values
     */
    private String[] getLyricists(String sValue)
    {
        String[] asLyricist = sValue.split("/");
        
        return asLyricist;
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TEXTTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TEXTTextInformationID3V2Frame oOtherTEXT = (TEXTTextInformationID3V2Frame)oOther;
        
        return (m_sInformation.equals(oOtherTEXT.m_sInformation) &&
                m_oTextEncoding.equals(oOtherTEXT.m_oTextEncoding) &&
                Arrays.equals(m_asLyricist, oOtherTEXT.m_asLyricist));
    }
}
