/*
 * UFIDID3V2Frame.java
 *
 * Created on September 6, 2004, 1:19 AM
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
 * $Id: UFIDID3V2Frame.java,v 1.10 2005/02/06 18:11:22 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing a unique file identifier.
 *
 * @author  paul
 */
public class UFIDID3V2Frame extends ID3V2Frame
{
    private String m_sOwnerIdentifier = null;
    private byte[] m_abyIdentifier = null;
    
    /** Creates a new instance of UFIDID3V2Frame.
     *
     * @param sOwnerIdentifier an URL or email address identifying the owner of this file
     * @param abyIdentifier up to 64 bytes of data which uniquely identify this file
     * @throws ID3Exception if sOwnerIdentifier is null or zero length, or if abyIdentifier is null or of length
     *                      outside the range from 0-64
     */
    public UFIDID3V2Frame(String sOwnerIdentifier, byte[] abyIdentifier)
        throws ID3Exception
    {
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("The owner identifier cannot be null or zero length in UFID frame.");
        }
        m_sOwnerIdentifier = sOwnerIdentifier;
        if ((abyIdentifier == null) || (abyIdentifier.length > 64))
        {
            throw new ID3Exception("The identifier be a non-null byte array of length 0-64 bytes in UFID frame.");
        }
        m_abyIdentifier = abyIdentifier;
    }

    public UFIDID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // owner identifier
            m_sOwnerIdentifier = oFrameDataID3DIS.readStringToNull();
            // identifier
            m_abyIdentifier = new byte[oFrameDataID3DIS.available()];
            if (oFrameDataID3DIS.available() > 0)
            {
                oFrameDataID3DIS.readFully(m_abyIdentifier);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitUFIDID3V2Frame(this);
    }
    
    /** Set unique file identifier.
     *
     * @param sOwnerIdentifier an URL or email address identifying the owner of this file
     * @param abyIdentifier up to 64 bytes of data which uniquely identify this file
     * @throws ID3Exception if sOwnerIdentifier is null or zero length, or if abyIdentifier is null or of length
     *                      outside the range from 0-64, or if this frame is in a tag with another UFID frame which
     *                      has the same owner identifier
     */
    public void setUniqueIdentifier(String sOwnerIdentifier, byte[] abyIdentifier)
        throws ID3Exception
    {
        String sOrigOwnerIdentifier = m_sOwnerIdentifier;
        byte[] abyOrigIdentifier = m_abyIdentifier;
        
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("The owner identifier cannot be null or zero length in UFID frame.");
        }
        if ((abyIdentifier == null) || (abyIdentifier.length > 64))
        {
            throw new ID3Exception("The identifier must be a non-null byte array of length 0-64 bytes in UFID frame.");
        }

        m_sOwnerIdentifier = sOwnerIdentifier;
        m_abyIdentifier = abyIdentifier;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sOwnerIdentifier = sOrigOwnerIdentifier;
            m_abyIdentifier = abyOrigIdentifier;
            
            throw e;
        }
    }
    
    /** Get the owner identifier for this frame.
     *
     * @return the owner identifier
     */
    public String getOwnerIdentifier()
    {
        return m_sOwnerIdentifier;
    }
    
    /** Get the unique file identifier from this frame
     *
     * @return the unique file identifier
     */
    public byte[] getIdentifier()
    {
        return m_abyIdentifier;
    }
    
    protected byte[] getFrameId()
    {
        return "UFID".getBytes();
    }
    
    public String toString()
    {
        return "Unique file identifier: Owner identifier=[" + m_sOwnerIdentifier +
               "], Identifier length=[" + m_abyIdentifier.length + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_sOwnerIdentifier.getBytes());
        oIDOS.write(0);
        oIDOS.write(m_abyIdentifier);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof UFIDID3V2Frame)))
        {
            return false;
        }
        
        UFIDID3V2Frame oOtherUFID = (UFIDID3V2Frame)oOther;
        
        return (m_sOwnerIdentifier.equals(oOtherUFID.m_sOwnerIdentifier) &&
                Arrays.equals(m_abyIdentifier, oOtherUFID.m_abyIdentifier));
    }
}
