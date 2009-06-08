/*
 * EncryptedID3V2Frame.java
 *
 * Created on November 25, 2004, 12:00 PM
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
 * $Id: EncryptedID3V2Frame.java,v 1.3 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 *
 * @author  paul
 */
public class EncryptedID3V2Frame extends ID3V2Frame
{
    private String m_sFrameId = null;
    private byte[] m_abyEncryptedFrameData = null;
    
    /** Creates a new instance of EncryptedID3V2Frame.
     *
     * @param sFrameId the original frame id of the encrypted frame
     * @param abyEncryptedFrameData a byte array containing the entire encrypted frame
     */
    public EncryptedID3V2Frame(String sFrameId, byte[] abyEncryptedFrameData)
    {
        m_sFrameId = sFrameId;
        m_abyEncryptedFrameData = abyEncryptedFrameData;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitEncryptedID3V2Frame(this);
    }
    
    /** Get the frame id of the encrypted frame which is stored in this object.
     *
     * @return the frame id of the encrypted frame stroed in this object.
     */
    public byte[] getEncryptedFrameId()
    {
        return m_sFrameId.getBytes();
    }

    /** Get the raw frame data for the encrypted frame stored in this object.
     *
     * @return the raw frame data for the encrypted frame stored in this object.
     */
    public byte[] getEncryptedData()
    {
        return m_abyEncryptedFrameData;
    }
    
    public String toString()
    {
        return "Encrypted ID3V2 frame: " + m_sFrameId;
    }

    protected byte[] getFrameId()
    {
        return m_sFrameId.getBytes();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        oIDOS.write(m_abyEncryptedFrameData);
    }
}
