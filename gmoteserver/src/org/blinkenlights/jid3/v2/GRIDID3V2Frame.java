/*
 * GRIDID3V2Frame.java
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
 * $Id: GRIDID3V2Frame.java,v 1.9 2005/02/06 18:11:23 paul Exp $
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
 * Frame containing group identification registration.
 */
public class GRIDID3V2Frame extends ID3V2Frame
{
    private String m_sOwnerIdentifier = null;
    private byte m_byGroupSymbol;
    private byte[] m_abyGroupDependantData = null;
    
    /** Constructor.
     *
     * @param sOwnerIdentifier an URL or email address where information about this grouping can be found
     * @param byGroupSymbol a symbol which will be used to identify this group throughout this tag (values
     *        lower than $80 are reserved)
     * @param abyGroupDependantData any data which is required for the correct interpretation of this grouping
     * @throws ID3Exception if the owner identifier is null or zero-length
     */
    public GRIDID3V2Frame(String sOwnerIdentifier, byte byGroupSymbol, byte[] abyGroupDependantData)
        throws ID3Exception
    {
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("GRID frame requires owner identifier value.");
        }
        m_sOwnerIdentifier = sOwnerIdentifier;
        m_byGroupSymbol = byGroupSymbol;
        m_abyGroupDependantData = abyGroupDependantData;
        if ((m_abyGroupDependantData != null) && (m_abyGroupDependantData.length == 0))
        {
            m_abyGroupDependantData = null;
        }
    }

    public GRIDID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // owner identifier
            m_sOwnerIdentifier = oFrameDataID3DIS.readStringToNull();
            
            // group symbol
            m_byGroupSymbol = (byte)oFrameDataID3DIS.readUnsignedByte();
            
            // optional group dependant data
            if (oFrameDataID3DIS.available() > 0)
            {
                m_abyGroupDependantData = new byte[oFrameDataID3DIS.available()];
                oFrameDataID3DIS.readFully(m_abyGroupDependantData);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitGRIDID3V2Frame(this);
    }

    /** Set group identification registration data.
     *
     * @param sOwnerIdentifier an URL or email address where information about this grouping can be found
     * @param byGroupSymbol a symbol which will be used to identify this group throughout this tag (values
     *        lower than $80 are reserved)
     * @param abyGroupDependantData any data which is required for the correct interpretation of this grouping
     * @throws ID3Exception if the owner identifier is null or zero-length
     * @throws ID3Exception if this frame is in a tag with another GRID frame which would have the same group symbol
     */
    public void setGroupIdentificationRegistration(String sOwnerIdentifier, byte byGroupSymbol, byte[] abyGroupDependantData)
        throws ID3Exception
    {
        String sOrigOwnerIdentifier = m_sOwnerIdentifier;
        byte byOrigGroupSymbol = m_byGroupSymbol;
        byte[] abyOrigGroupDependantData = m_abyGroupDependantData;
        
        if ((sOwnerIdentifier == null) || (sOwnerIdentifier.length() == 0))
        {
            throw new ID3Exception("GRID frame requires owner identifier value.");
        }
        
        m_sOwnerIdentifier = sOwnerIdentifier;
        m_byGroupSymbol = byGroupSymbol;
        m_abyGroupDependantData = abyGroupDependantData;
        if ((m_abyGroupDependantData != null) && (m_abyGroupDependantData.length == 0))
        {
            m_abyGroupDependantData = null;
        }
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sOwnerIdentifier = sOrigOwnerIdentifier;
            m_byGroupSymbol = byOrigGroupSymbol;
            m_abyGroupDependantData = abyOrigGroupDependantData;

            throw e;
        }
    }
    
    /** Get owner identifier information.
     *
     * @return the owner identifier value, which should be either an URL or an email address
     */
    public String getOwnerIdentifier()
    {
        return m_sOwnerIdentifier;
    }
    
    /** Get the group symbol for this grouping.
     *
     * @return the byte value used as a symbol for this group
     */
    public byte getGroupSymbol()
    {
        return m_byGroupSymbol;
    }
    
    /** Get group dependant data.
     *
     * @return any additional group dependant data, or null if none has been set
     */
    public byte[] getGroupDependantData()
    {
        return m_abyGroupDependantData;
    }
    
    protected byte[] getFrameId()
    {
        return "GRID".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbOutput = new StringBuffer();
        sbOutput.append("Group identification registration: Owner identifier=[" + m_sOwnerIdentifier +
                        "], Group symbol=[" + m_byGroupSymbol + "], ");
        if (m_abyGroupDependantData == null)
        {
            sbOutput.append("Group dependant data = none");
        }
        else
        {
            sbOutput.append("Group dependant data length = " + m_abyGroupDependantData.length);
        }
                        
        return sbOutput.toString(); 
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // owner identifier
        oIDOS.write(m_sOwnerIdentifier.getBytes());
        oIDOS.writeUnsignedByte(0);
        // group symbol
        oIDOS.writeUnsignedByte(m_byGroupSymbol);
        // optional group dependant data
        if (m_abyGroupDependantData != null)
        {
            oIDOS.write(m_abyGroupDependantData);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof GRIDID3V2Frame)))
        {
            return false;
        }
        
        GRIDID3V2Frame oOtherGRID = (GRIDID3V2Frame)oOther;
        
        return ( (((m_sOwnerIdentifier == null) && (oOtherGRID.m_sOwnerIdentifier == null)) || m_sOwnerIdentifier.equals(oOtherGRID.m_sOwnerIdentifier)) &&
                 (m_byGroupSymbol == oOtherGRID.m_byGroupSymbol) &&
                 Arrays.equals(m_abyGroupDependantData, oOtherGRID.m_abyGroupDependantData));
    }
}
