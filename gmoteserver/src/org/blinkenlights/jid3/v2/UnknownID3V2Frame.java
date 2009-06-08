/*
 * UnknownID3V2Frame.java
 *
 * Created on 2-Jan-2004
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
 * $Id: UnknownID3V2Frame.java,v 1.9 2005/12/10 05:33:39 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * $Id: UnknownID3V2Frame.java,v 1.9 2005/12/10 05:33:39 paul Exp $
 */
public class UnknownID3V2Frame extends ID3V2Frame
{
    private String m_sFrameId = null;
    private byte[] m_abyFrameData = null;
    
    public UnknownID3V2Frame(String sFrameId, byte[] abyFrameData)
    {
        m_sFrameId = sFrameId;
        m_abyFrameData = abyFrameData;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitUnknownID3V2Frame(this);
    }
    
    public String toString()
    {
        return "Unknown ID3V2 frame: " + m_sFrameId;
    }
    
    public byte[] getFrameId()
    {
        return m_sFrameId.getBytes();
    }
    
    protected void writeBody(ID3DataOutputStream oID3DOS)
        throws IOException
    {
        oID3DOS.write(m_abyFrameData);
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof UnknownID3V2Frame)))
        {
            return false;
        }
        
        UnknownID3V2Frame oOtherUnknown = (UnknownID3V2Frame)oOther;
        
        return (m_sFrameId.equals(oOtherUnknown.m_sFrameId) &&
                Arrays.equals(m_abyFrameData, oOtherUnknown.m_abyFrameData));
    }
}
