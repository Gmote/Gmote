/*
 * LINKID3V2Frame.java
 *
 * Created on Feb 1, 2004
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
 * $Id: LINKID3V2Frame.java,v 1.10 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing linked frame information.
 *
 * @author paul
 */
public class LINKID3V2Frame extends ID3V2Frame
{
    //NOTE: Shouldn't the frame identifier be _four_ bytes, not three, as described
    //      in the spec?
    private byte[] m_abyFrameIdentifier = null;
    private String m_sURL = null;
    private String m_sAdditionalData = null;
    
    /** Constructor
     *
     * @param abyFrameIdentifier the frame identifier of the frame linked to
     * @param sURL a reference to the location of the linked frame
     * @param sAdditionalData any additional data which may be needed to retrieve the linked frame
     * @throws ID3Exception if a frame identifier of four bytes is not specified, or if the URL is not specified
     */
    public LINKID3V2Frame(byte[] abyFrameIdentifier, String sURL, String sAdditionalData)
        throws ID3Exception
    {
        if (abyFrameIdentifier == null)
        {
            throw new ID3Exception("LINK frame requires frame identifier.");
        }
        if (abyFrameIdentifier.length != 4)
        {
            throw new ID3Exception("Frame identifiers must be four bytes in length in LINK frame.");
        }
        m_abyFrameIdentifier = abyFrameIdentifier;
        if ((sURL == null) || (sURL.length() == 0))
        {
            throw new ID3Exception("LINK frame requires an URL.");
        }
        m_sURL = sURL;
        m_sAdditionalData = sAdditionalData;
    }

    public LINKID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // frame identifier (this is probably wrong... it should probably be four
            // bytes.. if anyone ever uses this (?) it might break
            m_abyFrameIdentifier = new byte[4];
            oFrameDataID3DIS.readFully(m_abyFrameIdentifier);
            
            // url
            m_sURL = oFrameDataID3DIS.readStringToNull();
            
            // id and additional data
            if (oFrameDataID3DIS.available() > 0)
            {
                byte[] abyAdditionalData = new byte[oFrameDataID3DIS.available()];
                oFrameDataID3DIS.readFully(abyAdditionalData);
                m_sAdditionalData = new String(abyAdditionalData);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitLINKID3V2Frame(this);
    }
    
    /** Set the contents of this frame.
     *
     * @param abyFrameIdentifier the frame identifier of the frame linked to
     * @param sURL a reference to the location of the linked frame
     * @param sAdditionalData any additional data which may be needed to retrieve the linked frame
     * @throws ID3Exception if a frame identifier of four bytes is not specified, or if the URL is not specified
     * @throws ID3Exception if this frame is in a tag with another LINK frame which would have the same contents
     */
    public void setContents(byte[] abyFrameIdentifier, String sURL, String sAdditionalData)
        throws ID3Exception
    {
        byte[] abyOrigFrameIdentifier = m_abyFrameIdentifier;
        String sOrigURL = m_sURL;
        String sOrigAdditionalData = m_sAdditionalData;
        
        if (abyFrameIdentifier == null)
        {
            throw new ID3Exception("LINK frame requires frame identifier.");
        }
        if (abyFrameIdentifier.length != 4)
        {
            throw new ID3Exception("Frame identifiers must be four bytes in length in LINK frame.");
        }
        if ((sURL == null) || (sURL.length() == 0))
        {
            throw new ID3Exception("LINK frame requires an URL.");
        }

        m_abyFrameIdentifier = abyFrameIdentifier;
        m_sURL = sURL;
        m_sAdditionalData = sAdditionalData;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_abyFrameIdentifier = abyOrigFrameIdentifier;
            m_sURL = sOrigURL;
            m_sAdditionalData = sOrigAdditionalData;
            
            throw e;
        }
    }
    
    /** Get the frame identifier of the linked frame.
     *
     * @return the frame identifier bytes (which should be of length four)
     */
    public byte[] getFrameIdentifier()
    {
        return m_abyFrameIdentifier;
    }
    
    /** Get the URL pointing to the location of the file containing the linked frame.
     *
     * @return the location url
     */
    public String getLinkUrl()
    {
        return m_sURL;
    }
    
    /** Get any additional link-related data in this frame.
     * 
     * @return any additional link-related data
     */
    public String getAdditionalData()
    {
        return m_sAdditionalData;
    }

    protected byte[] getFrameId()
    {
        return "LINK".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbOutput = new StringBuffer();
        sbOutput.append("Link: Frame identifier=[" + ID3Util.convertBytesToHexString(m_abyFrameIdentifier, true) +
                        "], URL=[" + m_sURL + "], Additional data=[" + m_sAdditionalData + "]");
        
        return sbOutput.toString();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // frame identifier
        oIDOS.write(m_abyFrameIdentifier);
        
        // url
        oIDOS.write(m_sURL.getBytes());
        oIDOS.writeUnsignedByte(0);
        
        // additional data
        oIDOS.write(m_sAdditionalData.getBytes());
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof LINKID3V2Frame)))
        {
            return false;
        }
        
        LINKID3V2Frame oOtherLINK = (LINKID3V2Frame)oOther;
        
        return ( Arrays.equals(m_abyFrameIdentifier, oOtherLINK.m_abyFrameIdentifier) &&
                 (((m_sURL == null) && (oOtherLINK.m_sURL == null)) || m_sURL.equals(oOtherLINK.m_sURL)) &&
                 m_sAdditionalData.equals(oOtherLINK.m_sAdditionalData) );
    }
}
