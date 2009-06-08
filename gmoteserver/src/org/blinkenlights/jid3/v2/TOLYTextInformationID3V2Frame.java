/*
 * TOLYTextInformationID3V2Frame.java
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
 * $Id: TOLYTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:18 paul Exp $
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
 * Text frame which contains the original lyricist(s) or author(s) of the text in the recording
 * in this track.
 */
public class TOLYTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private String[] m_asOriginalLyricist = null;
    
    /** Constructor.
     *
     * @param sOriginalLyricist the original lyricist or author of the text for this track
     */
    public TOLYTextInformationID3V2Frame(String sOriginalLyricist)
    {
        super(sOriginalLyricist);
        
        m_asOriginalLyricist = getLyricists(sOriginalLyricist);
    }

    /** Constructor.
     *
     * @param asOriginalLyricist the original lyricist(s) or author(s) of the text for this track
     */
    public TOLYTextInformationID3V2Frame(String[] asOriginalLyricist)
    {
        super("");
        
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbLyricists = new StringBuffer();
        for (int i=0; i < asOriginalLyricist.length; i++)
        {
            sbLyricists.append(asOriginalLyricist[i] + "/");
        }
        sbLyricists.deleteCharAt(sbLyricists.length()-1);   // delete last "/"
        m_sInformation = sbLyricists.toString();
        
        m_asOriginalLyricist = getLyricists(m_sInformation);
    }

    public TOLYTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);

        m_asOriginalLyricist = getLyricists(m_sInformation);
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTOLYTextInformationID3V2Frame(this);
    }

    /** Set the original lyricist(s) or author(s) of the text in the recording in this track.
     *  Multiple lyricists can optionally be set with this method by separating them
     *  with a slash "/" character.
     *
     * @param sOriginalLyricist the original lyricist for this track
     */
    public void setOriginalLyricist(String sOriginalLyricist)
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sOriginalLyricist;
        m_asOriginalLyricist = getLyricists(sOriginalLyricist);
    }
    
    /** Set the original lyricist(s) or author(s) of the text in the recording in this track.
     *
     * @param asOriginalLyricist the original lyricists or authors of the text for this track
     */
    public void setOriginalLyricists(String[] asOriginalLyricist)
    {
        // build single string of lyricists, separated by "/", as described in ID3 spec
        StringBuffer sbLyricists = new StringBuffer();
        for (int i=0; i < asOriginalLyricist.length; i++)
        {
            sbLyricists.append(asOriginalLyricist[i] + "/");
        }
        sbLyricists.deleteCharAt(sbLyricists.length()-1);   // delete last "/"
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sbLyricists.toString();
        
        m_asOriginalLyricist = getLyricists(m_sInformation);
    }
    
    /** Get the original lyricist(s) or author(s) of the text for this track
     *
     * @return the original lyricist(s) or author(s) of the text for this track
     */
    public String[] getOriginalLyricists()
    {
        return m_asOriginalLyricist;
    }
    
    protected byte[] getFrameId()
    {
        return "TOLY".getBytes();
    }
    
    public String toString()
    {
        return "Original lyricist(s)/text writer(s): [" + m_sInformation + "]";
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
        if ((oOther == null) || (!(oOther instanceof TOLYTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TOLYTextInformationID3V2Frame oOtherTOLY = (TOLYTextInformationID3V2Frame)oOther;
        
        return (m_sInformation.equals(oOtherTOLY.m_sInformation) &&
                m_oTextEncoding.equals(oOtherTOLY.m_oTextEncoding) &&
                Arrays.equals(m_asOriginalLyricist, oOtherTOLY.m_asOriginalLyricist));
    }
}
