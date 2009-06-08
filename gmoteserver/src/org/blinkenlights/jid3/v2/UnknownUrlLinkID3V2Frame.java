/*
 * UnknownUrlLinkID3V2Frame.java
 *
 * Created on 8-Jan-2004
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
 * $Id: UnknownUrlLinkID3V2Frame.java,v 1.5 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UnknownUrlLinkID3V2Frame extends UrlLinkID3V2Frame
{
    private String m_sFrameId = null;
    
    public UnknownUrlLinkID3V2Frame(String sFrameId, InputStream oIS)
        throws ID3Exception
    {
        super(oIS);

        // store the frame id so we can return it if requested
        m_sFrameId = sFrameId;
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitUnknownUrlLinkID3V2Frame(this);
    }
    
    public byte[] getFrameId()
    {
        return m_sFrameId.getBytes();
    }
    
    public String toString()
    {
        return "Unknown URL link frame " + m_sFrameId + ": [" + m_sURL + "]";
    }
}
