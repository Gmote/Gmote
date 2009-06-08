/*
 * USERID3V2Frame.java
 *
 * Created on September 6, 2004, 4:05 PM
 *
 * Copyright (C)2004-2005 Paul Grebenc
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
 * $Id: USERID3V2Frame.java,v 1.9 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing terms of use information.
 *
 * @author  paul
 */
public class USERID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sLanguage = null;
    private String m_sTermsOfUse = null;
    
    /** Creates a new instance of USERID3V2Frame.
     *
     * @param sLanguage a three character code specifying the language of the terms of use
     * @param sTermsOfUse the terms of use for this recording or file
     * @throws ID3Exception if the language is not in a valid format, or if the terms of use are not specified
     */
    public USERID3V2Frame(String sLanguage, String sTermsOfUse)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("Language must be a three character length string in USER frame.");
        }
        m_sLanguage = sLanguage;
        if ((sTermsOfUse == null) || (sTermsOfUse.length() == 0))
        {
            throw new ID3Exception("Terms of use are required in USER frame.");
        }
        m_sTermsOfUse = sTermsOfUse;
    }

    public USERID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            // language
            byte[] abyLanguage = new byte[3];
            oFrameDataID3DIS.readFully(abyLanguage);
            m_sLanguage = new String(abyLanguage);
            // terms of use (to end of frame)
            byte[] abyTermsOfUse = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abyTermsOfUse);
            m_sTermsOfUse = new String(abyTermsOfUse, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitUSERID3V2Frame(this);
    }
    
    /** Set the terms of use.
     *
     * @param sLanguage a three character code specifying the language of the terms of use
     * @param sTermsOfUse the terms of use for this recording or file
     * @throws ID3Exception if the language is not in a valid format, or if the terms of use are not specified
     */
    public void setTermsOfUse(String sLanguage, String sTermsOfUse)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if ((sLanguage == null) || (sLanguage.length() != 3))
        {
            throw new ID3Exception("Language must be a three character length string in USER frame.");
        }
        m_sLanguage = sLanguage;
        if ((sTermsOfUse == null) || (sTermsOfUse.length() == 0))
        {
            throw new ID3Exception("Terms of use are required in USER frame.");
        }
        m_sTermsOfUse = sTermsOfUse;
    }
    
    /** Get the language of the terms of use.
     *
     * @return the three letter language code
     */
    public String getLanguage()
    {
        return m_sLanguage;
    }
    
    /** Get the terms of use for this file.
     *
     * @return the terms of use
     */
    public String getTermsOfUse()
    {
        return m_sTermsOfUse;
    }
    
    /** Set the text encoding to be used for the terms of use in this frame.
     *
     * @param oTextEncoding the text encoding to be used for this frame
     */
    public void setTextEncoding(TextEncoding oTextEncoding)
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Text encoding cannot be null.");
        }
        m_oTextEncoding = oTextEncoding;
    }

    /** Get the text encoding used for the terms of use in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "USER".getBytes();
    }
    
    public String toString()
    {
        return "Terms of use: Language=[" + m_sLanguage +
               "], Terms of use=[" + m_sTermsOfUse + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_oTextEncoding.getEncodingValue()); // text encoding
        oIDOS.write(m_sLanguage.getBytes());   // language
        oIDOS.write(m_sTermsOfUse.getBytes(m_oTextEncoding.getEncodingString()));  // terms of use
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof USERID3V2Frame)))
        {
            return false;
        }
        
        USERID3V2Frame oOtherUSER = (USERID3V2Frame)oOther;
        
        return (m_oTextEncoding.equals(oOtherUSER.m_oTextEncoding) &&
                m_sLanguage.equals(oOtherUSER.m_sLanguage) &&
                m_sTermsOfUse.equals(oOtherUSER.m_sTermsOfUse));
    }
}
