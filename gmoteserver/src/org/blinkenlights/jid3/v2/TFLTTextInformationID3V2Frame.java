/*
 * TFLTTextInformationID3V2Frame.java
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
 * $Id: TFLTTextInformationID3V2Frame.java,v 1.8 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame specifying the file type this tag is describing.
 */
public class TFLTTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    // predefined types (others are allowed)
    public final static String MPEG_AUDIO = "MPG";
    public final static String MPEG_LAYER_1 = "MPG/1";
    public final static String MPEG_LAYER_2 = "MPG/2";
    public final static String MPEG_LAYER_3 = "MPG/3";
    public final static String MPEG_25 = "MPG/2.5";
    public final static String MPEG_AAC = "MPG/AAC";
    public final static String VQF = "VQF";
    public final static String PCM = "PCM";
    
    private String m_sFileType = null;
    
    /** Constructor.
     *
     * @param sFileType the file type of the file to which this tag will apply (predefined or custom values are valid)
     */
    public TFLTTextInformationID3V2Frame(String sFileType)
    {
        super(sFileType);
        
        m_sFileType = sFileType;
    }

    public TFLTTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        m_sFileType = m_sInformation;
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTFLTTextInformationID3V2Frame(this);
    }

    /** Set the file type of the file to which this tag will apply.
     *
     * @param sFileType the file type (predefined or custom values are valid)
     */
    public void setFileType(String sFileType)
    {
        m_sFileType = sFileType;
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = sFileType;
    }

    /** Get the current file type.
     *
     * @return the file type value currently set
     */
    public String getFileType()
    {
        return m_sFileType;
    }
    
    protected byte[] getFrameId()
    {
        return "TFLT".getBytes();
    }
    
    public String toString()
    {
        return "File type: [" + m_sInformation + "]";
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TFLTTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TFLTTextInformationID3V2Frame oOtherTFLT = (TFLTTextInformationID3V2Frame)oOther;
        
        return (m_sFileType.equals(oOtherTFLT.m_sFileType) &&
                m_oTextEncoding.equals(oOtherTFLT.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTFLT.m_sInformation));
    }
}
