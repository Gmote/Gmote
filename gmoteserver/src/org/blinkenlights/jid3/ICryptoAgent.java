/*
 * ICryptoAgent.java
 *
 * Created on May 13, 2004, 12:27 AM
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
 * $Id: ICryptoAgent.java,v 1.2 2005/02/06 18:11:26 paul Exp $
 */

package org.blinkenlights.jid3.crypt;

/**
 *
 * @author  paul
 */
public interface ICryptoAgent
{
    /** Get the owner identifier for this encryption method.
     *  Each encrypted frame in a tag has its encryption method represented by a unique owner identifier.
     *  This value much match the owner identifier of a crypto agent to be used for decryption.
     *
     * @return a string containing the owner identifier
     */
    public String getOwnerIdentifier();
    
    /** Encrypt an array of bytes.
     *
     * @param abyRawData an array of bytes which are to be encrypted
     * @param abyEncryptionData an array of bytes which are used to encrypt the raw data
     * @return an array of encrypted bytes
     */
    public byte[] encrypt(byte[] abyRawData, byte[] abyEncryptionData) throws ID3CryptException;
    
    /** Decrypt an array of bytes.
     *
     * @param abyEncryptedData an array of encrypted bytes
     * @param abyEncryptionData an array of bytes which are used to decrypt the encrypted data
     * @return an array of unencrypted bytes
     */
    public byte[] decrypt(byte[] abyEncryptedData, byte[] abyEncryptionData) throws ID3CryptException;
}
