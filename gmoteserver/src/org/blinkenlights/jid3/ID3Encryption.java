/*
 * ID3Encryption.java
 *
 * Created on November 25, 2004, 11:22 AM
 *
 * Copyright (C)2004 Paul Grebenc
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
 * $Id: ID3Encryption.java,v 1.3 2005/02/06 18:11:26 paul Exp $
 */

package org.blinkenlights.jid3.crypt;

import java.util.*;

/**
 *
 * @author  paul
 */
public class ID3Encryption
{
    private static ID3Encryption s_oInstance;
    
    private Map m_oOwnerIdentifierToCryptoAgentMap;
    
    /** Creates a new instance of ID3Encryption */
    private ID3Encryption()
    {
        m_oOwnerIdentifierToCryptoAgentMap = new HashMap();
    }
    
    /** Get the single instance of the ID3Encryption registrar.
     *
     * @return the ID3Encryption instance
     */
    public static ID3Encryption getInstance()
    {
        synchronized(ID3Encryption.class)
        {
            if (s_oInstance == null)
            {
                s_oInstance = new ID3Encryption();
            }
        }
        
        return s_oInstance;
    }
    
    /** Register a crypto agent.  This is the means by which support for encryption algorithms are added.
     *  An encryption method must be registered before it can be used to encrypt or decrypt an encrypted ID3V2 frame.
     *
     * @param oCryptoAgent the crypto agent to be registered
     */
    public void registerCryptoAgent(ICryptoAgent oCryptoAgent)
    {
        m_oOwnerIdentifierToCryptoAgentMap.put(oCryptoAgent.getOwnerIdentifier(), oCryptoAgent);
    }
    
    /** Deregister a crypto agent.
     *
     * @param sOwnerIdentifier the owner identifier string uniquely identifying the crypto agent to be deregistered
     * @return the previously registered agent if there is a match, or null otherwise
     */
    public ICryptoAgent deregisterCryptoAgent(String sOwnerIdentifier)
    {
        return (ICryptoAgent)m_oOwnerIdentifierToCryptoAgentMap.remove(sOwnerIdentifier);
    }

    /** Look up a registered crypto agent.
     *
     * @param sOwnerIdentifier the owner identifier string uniquely identifying the crypto agent to be returned
     * @return the registered agent if there is a match, or null otherwise
     */
    public ICryptoAgent lookupCryptoAgent(String sOwnerIdentifier)
    {
        return (ICryptoAgent)m_oOwnerIdentifierToCryptoAgentMap.get(sOwnerIdentifier);
    }
}
