/*
 * PRIVID3V2Frame.java
 *
 * Created on August 31, 2004, 11:52 PM
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
 * $Id: PRIVID3V2Frame.java,v 1.9 2005/02/06 18:11:15 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing private information.
 *
 * @author  paul
 */
public class PRIVID3V2Frame extends ID3V2Frame
{
    private String m_sOwnerIdentifier = null;
    private byte[] m_abyPrivateData = null;
    
    /** Creates a new instance of PRIVID3V2Frame 
     *
     * @param sOwnerIdentifier an URL or email address identifier the owner related to the private data
     * @param abyPrivateData the private data to be stored in this frame
     * @throws ID3Exception if the owner identifier is null or zero length, or if the private data is null
     */
    public PRIVID3V2Frame(String sOwnerIdentifier,
                          byte[] abyPrivateData)
        throws ID3Exception
    {
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("Owner identifier required in PRIV frame.");
        }
        m_sOwnerIdentifier = sOwnerIdentifier;
        if (abyPrivateData == null)
        {
            throw new ID3Exception("Private data required in PRIV frame.");
        }
        m_abyPrivateData = abyPrivateData;
    }

    public PRIVID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // owner identifier (read to null)
            m_sOwnerIdentifier = oFrameDataID3DIS.readStringToNull();
            
            // private data (to end)
            m_abyPrivateData = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(m_abyPrivateData);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitPRIVID3V2Frame(this);
    }
    
    /** Set private information.
     *
     * @param sOwnerIdentifier an URL or email address identifier the owner related to the private data
     * @param abyPrivateData the private data to be stored in this frame
     * @throws ID3Exception if the owner identifier is null or zero length, or if the private data is null
     * @throws ID3Exception if this frame is in a tag with another PRIV frame which would have the same contents
     */
    public void setPrivateInformation(String sOwnerIdentifier, byte[] abyPrivateData)
        throws ID3Exception
    {
        String sOrigOwnerIdentifier = m_sOwnerIdentifier;
        byte[] abyOrigPrivateData = m_abyPrivateData;
        
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("Owner identifier required in PRIV frame.");
        }
        if (abyPrivateData == null)
        {
            throw new ID3Exception("Private data required in PRIV frame.");
        }

        m_sOwnerIdentifier = sOwnerIdentifier;
        m_abyPrivateData = abyPrivateData;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sOwnerIdentifier = sOrigOwnerIdentifier;
            m_abyPrivateData = abyOrigPrivateData;
            
            throw e;
        }
    }

    /** Get the owner identifier.
     *
     * @return an identification string for the owner associated with the private data in this frame
     */
    public String getOwnerIdentifier()
    {
        return m_sOwnerIdentifier;
    }

    /** Get private data.
     *
     * @return the private data stored in this frame
     */
    public byte[] getPrivateData()
    {
        return m_abyPrivateData;
    }
    
    protected byte[] getFrameId()
    {
        return "PRIV".getBytes();
    }
    
    public String toString()
    {
        return "Private information: Ownership identifier=[" + m_sOwnerIdentifier +
               "], Private data length=[" + m_abyPrivateData.length + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // ownership identifier string
        oIDOS.write(m_sOwnerIdentifier.getBytes());
        oIDOS.writeUnsignedByte(0);
        // private data
        oIDOS.write(m_abyPrivateData);
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof PRIVID3V2Frame)))
        {
            return false;
        }
        
        PRIVID3V2Frame oOtherPRIV = (PRIVID3V2Frame)oOther;
        
        return (m_sOwnerIdentifier.equals(oOtherPRIV.m_sOwnerIdentifier) &&
                Arrays.equals(m_abyPrivateData, oOtherPRIV.m_abyPrivateData));
    }
}
