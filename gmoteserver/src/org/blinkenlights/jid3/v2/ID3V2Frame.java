/*
 * Created on 26-Nov-2003
 *
 * Copyright (C)2003,2004 Paul Grebenc
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
 * $Id: ID3V2Frame.java,v 1.24 2005/05/11 03:22:19 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.crypt.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * The base class for all ID3 V2 frames.
 */
abstract public class ID3V2Frame implements ID3Subject, ID3Visitable
{
    // observers to changes in this object
    private Set m_oID3ObserverSet = new HashSet();
    
    // flags
    private boolean m_bTagAlterPreservationFlag = false;
    private boolean m_bFileAlterPreservationFlag = false;
    private boolean m_bReadOnlyFlag = false;
    private boolean m_bCompressionFlag = false;
    private boolean m_bEncryptionFlag = false;
    private boolean m_bGroupingIdentityFlag = false;
    
    // encryption support
    private byte m_byEncryptionMethod;
    private ICryptoAgent m_oCryptoAgent = null;
    private byte[] m_abyEncryptionData = null;
    
    public ID3V2Frame()
    {
    }
    
    public void addID3Observer(ID3Observer oID3Observer)
    {
        m_oID3ObserverSet.add(oID3Observer);
    }
    
    public void removeID3Observer(ID3Observer oID3Observer)
    {
        m_oID3ObserverSet.remove(oID3Observer);
    }
    
    public void notifyID3Observers()
        throws ID3Exception
    {
        Iterator oIter = m_oID3ObserverSet.iterator();
        
        while (oIter.hasNext())
        {
            ID3Observer oID3Observer = (ID3Observer)oIter.next();
            
            oID3Observer.update(this);
        }
    }
    
    /** Specify what should happen to this frame, if the tag it is in is modified by another program
     *  which does not recognize it.  If set to true, then this frame should be discarded by a program
     *  which does not recognize it.  If set to false, it should be left as is.
     *
     * @param bTagAlterPreservationFlagValue the state this flag should be set to
     */
    public void setTagAlterPreservationFlag(boolean bTagAlterPreservationFlagValue)
    {
        m_bTagAlterPreservationFlag = bTagAlterPreservationFlagValue;
    }

    /** Specify what should happen to this frame, if this file (other than the tag) is modified by a
     *  program which does not recognize it.  If set to true, then this frame should be discarded.  If
     *  set to false, then the frame should be preserved as is.
     *
     * @param bFileAlterPreservationFlagValue the state this flag should be set to
     */
    public void setFileAlterPreservationFlag(boolean bFileAlterPreservationFlagValue)
    {
        m_bFileAlterPreservationFlag = bFileAlterPreservationFlagValue;
    }
    
    /** Specify whether this frame should be considered read only or not.
     *
     * @param bReadOnlyFlagValue the state this flag should be set to
     */
    public void setReadOnlyFlag(boolean bReadOnlyFlagValue)
    {
        m_bReadOnlyFlag = bReadOnlyFlagValue;
    }

    /** Specify whether this frame is compressed or not.  The compression method used is zlib.
     *
     * @param bCompressionFlagValue the state this flag should be set to
     */
    public void setCompressionFlag(boolean bCompressionFlagValue)
    {
        m_bCompressionFlag = bCompressionFlagValue;
    }

    /** Set encryption method for this frame.  When an encryption method is specified, the
     *  frame will be encrypted before being written to a file.
     *
     * @param byEncryptionMethod the encryption method value to use (this value must match a
     *        method specified in an ENCR frame in this tag)
     */
    public void setEncryption(byte byEncryptionMethod)
        throws ID3Exception
    {
        m_bEncryptionFlag = true;
        m_byEncryptionMethod = byEncryptionMethod;
        
        notifyID3Observers();
    }
    
    /** Check whether this frame is encrypted.
     *
     * @return true if the frame is encrypted, false otherwise
     */
    public boolean isEncrypted()
    {
        return m_bEncryptionFlag;
    }
    
    /** Get the encryption method symbol used to encrypt this frame.
     *
     * @return the encryption method symbol used
     * @throws ID3Exception if this frame is not encrypted
     */
    public byte getEncryptionMethod()
        throws ID3Exception
    {
        if (! isEncrypted())
        {
            throw new ID3Exception("This frame is not encrypted.");
        }
        
        return m_byEncryptionMethod;
    }
    
    /** Set the crypto agent to be used in this frame.
     *
     * @param oCryptoAgent the crypto agent to be used for encrypting this frame
     * @param abyEncryptionData the encryption data to be used when encrypting/decrypting with this agent
     */
    void setCryptoAgent(ICryptoAgent oCryptoAgent, byte[] abyEncryptionData)
    {
        m_oCryptoAgent = oCryptoAgent;
        m_abyEncryptionData = abyEncryptionData;
    }

    /** Specify whether this frame belongs to a group of other frames or not.  If set, the group
     *  identifier must be specified.
     *
     * @param bGroupingIdentityFlagValue the state this flag should be set to
     */
    public void setGroupingIdentityFlag(boolean bGroupingIdentityFlagValue)
    {
        m_bGroupingIdentityFlag = bGroupingIdentityFlagValue;
    }
    
    /** Get the four bytes which uniquely specify of which type this frame is. */
    abstract protected byte[] getFrameId();

    /** Return number of bytes required to store the body of this frame.
     *
     * @return the number of bytes
     */
    private int getLength()
        throws IOException
    {
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
        ID3DataOutputStream oLengthIDOS = new ID3DataOutputStream(oBAOS);
        writeBody(oLengthIDOS);
        
        return oBAOS.size();
    }
    
    /** Represent the contents of this frame as a string.  For debugging purposes.
     *
     * @return a string representing this frame
     */
    public abstract String toString();
    
    /** Read an ID3 v2 frame from an ID3DataInputStream.
     * 
     * @param oID3DIS input stream from which a frame can directly be read
     * @return an ID3V2Frame which was read from the input stream
     * @throws ID3Exception if an error while reading occurs
     */
    static ID3V2Frame read(ID3DataInputStream oID3DIS)
        throws ID3Exception
    {
        return read(oID3DIS, new ENCRID3V2Frame[0]);
    }

    /** Read an ID3 v2 frame from an ID3DataInputStream, providing the possibility for decryption.
     * 
     * @param oID3DIS input stream from which a frame can directly be read
     * @param aoENCRID3V2Frame the array of ENCR frames which were read in, which describe encryption details
     * @return an ID3V2Frame which was read from the input stream
     * @throws ID3Exception if an error while reading occurs
     */
    static ID3V2Frame read(ID3DataInputStream oID3DIS, ENCRID3V2Frame[] aoENCRID3V2Frame)
        throws ID3Exception
    {
        String sFrameId = null;
        try
        {
            // read frame id
            byte[] abyFrameId = new byte[4];
            oID3DIS.readFully(abyFrameId);
            if (abyFrameId[0] == 0) // we're reading into the padding past the frames
            {
                return null;
            }
            sFrameId = new String(abyFrameId);
            
            //HACK: This is a work-around for a bug in the MP3ext Windows explorer extension.  It repeatedly
            //      writes the string "MP3ext V3.3.18(unicode)" into the padding area after the frames in a v2 tag.
            //      This is invalid, and the resulting frames are corrupt, according to the ID3 specification, which
            //      requires that padding contain only nulls.
            if (sFrameId.equals("MP3e"))
            {
                return null;
            }
            
            if (( ! sFrameId.matches("[A-Z0-9]+")) && ID3V2Tag.usingStrict())
            {
                throw new InvalidFrameID3Exception("Invalid frame id [" + sFrameId + "].");
            }
            
            // read size
            int iFrameSize = oID3DIS.readBE32();
            
            // read first flags byte
            int iFirstFlags = oID3DIS.readUnsignedByte();
            boolean bTagAlterPreservationFlag = ((iFirstFlags & 0x80) != 0);
            boolean bFileAlterPreservationFlag = ((iFirstFlags & 0x40) != 0);
            boolean bReadOnlyFlag = ((iFirstFlags & 0x20) != 0);
            boolean bUnknownFirstByteFlags = ((iFirstFlags & 0x1f) != 0);
            
            // read second flags byte
            int iSecondFlags = oID3DIS.readUnsignedByte();
            boolean bCompressionFlag = ((iSecondFlags & 0x80) != 0);
            boolean bEncryptionFlag = ((iSecondFlags & 0x40) != 0);
            boolean bGroupingIdentityFlag = ((iSecondFlags & 0x20) != 0);
            boolean bUnknownSecondByteFlags = ((iSecondFlags & 0x1f) != 0);
            
            // get length of uncompressed frame if compression set
            int iUncompressedSize = iFrameSize;
            if (bCompressionFlag)
            {
                iUncompressedSize = oID3DIS.readBE32();
                iFrameSize -= 4;    // FIX: four bytes read for frame size
            }
            
            // read encryption method byte, if used
            int iEncryptionMethodSymbol = 0;
            ICryptoAgent oCryptoAgent = null;
            byte[] abyEncryptionData = null;
            if (bEncryptionFlag)
            {
                iEncryptionMethodSymbol = oID3DIS.readUnsignedByte();
                iFrameSize -= 1;    // FIX: one byte for encryption method
                
                // this frame is encrypted.. do we have a means of decrypting it?
                for (int i=0; i < aoENCRID3V2Frame.length; i++)
                {
                    if ((aoENCRID3V2Frame[i].getEncryptionMethodSymbol() & 0xff) == iEncryptionMethodSymbol)
                    {
                        // we can decrypt this frame now
                        oCryptoAgent = ID3Encryption.getInstance().lookupCryptoAgent(aoENCRID3V2Frame[i].getOwnerIdentifier());
                        abyEncryptionData = aoENCRID3V2Frame[i].getEncryptionData();
                        break;
                    }
                }
                
                if (oCryptoAgent == null)
                {
                    ByteArrayOutputStream oEncryptedBAOS = new ByteArrayOutputStream();
                    ID3DataOutputStream oEncryptedIDOS = new ID3DataOutputStream(oEncryptedBAOS);
                    oEncryptedIDOS.write(abyFrameId);
                    oEncryptedIDOS.writeBE32(iFrameSize + (bEncryptionFlag ? 1 : 0));
                    oEncryptedIDOS.writeUnsignedByte(iFirstFlags);
                    oEncryptedIDOS.writeUnsignedByte(iSecondFlags);
                    if (bCompressionFlag)
                    {
                        oEncryptedIDOS.writeID3Four(iUncompressedSize);
                    }
                    oEncryptedIDOS.writeUnsignedByte(iEncryptionMethodSymbol);
                    
                    // determine the length of the compressed/encrypted data to be read in (minus the after header bytes we've already read)
                    int iFrameDataLength = iFrameSize;  // initial length of frame data before data we have already read

                    // read compressed/encrypted data
                    byte[] abyEncryptedFrameData = new byte[iFrameDataLength];
                    oID3DIS.readFully(abyEncryptedFrameData);
                    oEncryptedIDOS.write(abyEncryptedFrameData);
                    
                    // we cannot decrypt this frame at this time, so return it, as we read it, as a special encrypted frame object
                    EncryptedID3V2Frame oEncryptedFrame = new EncryptedID3V2Frame(sFrameId, oEncryptedBAOS.toByteArray());
                    
                    return oEncryptedFrame;
                }
            }
            
            // read frame data
            byte[] abyFrameData = null;
            if (bCompressionFlag)
            {
                // read compressed data
                byte[] abyCompressedFrameData = new byte[iFrameSize];
                oID3DIS.readFully(abyCompressedFrameData);
                
                // decrypt compressed data first, if encrypted
                if (bEncryptionFlag)
                {
                    abyCompressedFrameData = oCryptoAgent.decrypt(abyCompressedFrameData, abyEncryptionData);
                }
                
                // deflate data
                ByteArrayInputStream oBAIS = new ByteArrayInputStream(abyCompressedFrameData);
                InflaterInputStream oInflaterIS = new InflaterInputStream(oBAIS);
                ID3DataInputStream oInflaterID3DIS = new ID3DataInputStream(oInflaterIS);
                abyFrameData = new byte[iUncompressedSize];
                oInflaterID3DIS.readFully(abyFrameData);
            }
            else
            {
                abyFrameData = new byte[iFrameSize];
                oID3DIS.readFully(abyFrameData);
                
                // decrypt data, if encrypted
                if (bEncryptionFlag)
                {
                    abyFrameData = oCryptoAgent.decrypt(abyFrameData, abyEncryptionData);
                }
            }
            
            // create a frame object here based on what we've read
            ID3V2Frame oID3V2Frame;
            if (sFrameId.startsWith("T"))
            {
                // text information frame
                String sClassName = "org.blinkenlights.jid3.v2." + sFrameId + "TextInformationID3V2Frame";
                
                // if this class exists, then create such an object
                try
                {
                    Class oID3V2FrameClass = Class.forName(sClassName);
                    Class[] aoArgClassTypes = { InputStream.class };
                    Constructor oConstructor = oID3V2FrameClass.getConstructor(aoArgClassTypes);
                    Object[] aoConstructorArgs = { new ByteArrayInputStream(abyFrameData) };
                    oID3V2Frame = (ID3V2Frame)oConstructor.newInstance(aoConstructorArgs);
                }
                catch (ClassNotFoundException e)
                {
                    // unknown frame type
                    oID3V2Frame = new UnknownTextInformationID3V2Frame(sFrameId, new ByteArrayInputStream(abyFrameData));
                }
                catch (NoSuchMethodException e)
                {
                    // unknown frame type
                    oID3V2Frame = new UnknownTextInformationID3V2Frame(sFrameId, new ByteArrayInputStream(abyFrameData));
                }
                catch (InvocationTargetException e)
                {
                    // constructor threw an exception
                    if (e.getCause() instanceof Exception)
                    {
                        throw (Exception)e.getCause();
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
            else if (sFrameId.startsWith("W"))
            {
                // URL link frame
                String sClassName = "org.blinkenlights.jid3.v2." + sFrameId + "UrlLinkID3V2Frame";
                
                // if this class exists, then create such an object
                try
                {
                    Class oID3V2FrameClass = Class.forName(sClassName);
                    Class[] aoArgClassTypes = { InputStream.class };
                    Constructor oConstructor = oID3V2FrameClass.getConstructor(aoArgClassTypes);
                    Object[] aoConstructorArgs = { new ByteArrayInputStream(abyFrameData) };
                    oID3V2Frame = (ID3V2Frame)oConstructor.newInstance(aoConstructorArgs);
                }
                catch (ClassNotFoundException e)
                {
                    // unknown frame type
                    oID3V2Frame = new UnknownUrlLinkID3V2Frame(sFrameId, new ByteArrayInputStream(abyFrameData));
                }
                catch (NoSuchMethodException e)
                {
                    // unknown frame type
                    oID3V2Frame = new UnknownUrlLinkID3V2Frame(sFrameId, new ByteArrayInputStream(abyFrameData));
                }
                catch (InvocationTargetException e)
                {
                    // constructor threw an exception
                    if (e.getCause() instanceof Exception)
                    {
                        throw (Exception)e.getCause();
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
            else
            {
                // unique frame
                String sClassName = "org.blinkenlights.jid3.v2." + sFrameId + "ID3V2Frame";
                
                // if this class exists, then create such an object
                try
                {
                    Class oID3V2FrameClass = Class.forName(sClassName);
                    Class[] aoArgClassTypes = { InputStream.class };
                    Constructor oConstructor = oID3V2FrameClass.getConstructor(aoArgClassTypes);
                    Object[] aoConstructorArgs = { new ByteArrayInputStream(abyFrameData) };
                    oID3V2Frame = (ID3V2Frame)oConstructor.newInstance(aoConstructorArgs);
                }
                catch (ClassNotFoundException e)
                {
                    // unknown frame
                    oID3V2Frame = new UnknownID3V2Frame(sFrameId, abyFrameData);
                }
                catch (NoSuchMethodException e)
                {
                    // unknown frame type
                    oID3V2Frame = new UnknownID3V2Frame(sFrameId, abyFrameData);
                }
            }

            // set flags applicable to all v2 frames
            oID3V2Frame.setTagAlterPreservationFlag(bTagAlterPreservationFlag);
            oID3V2Frame.setFileAlterPreservationFlag(bFileAlterPreservationFlag);
            oID3V2Frame.setReadOnlyFlag(bReadOnlyFlag);
            oID3V2Frame.setCompressionFlag(bCompressionFlag);
            if (bEncryptionFlag)
            {
                oID3V2Frame.setEncryption((byte)iEncryptionMethodSymbol);
            }
            oID3V2Frame.setGroupingIdentityFlag(bGroupingIdentityFlag);
            
            return oID3V2Frame;
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            if (sFrameId == null)
            {
                throw new ID3Exception("Error reading v2 frame.", e);
            }
            else
            {
                throw new ID3Exception("Error reading " + sFrameId + " v2 frame.", e);
            }
        }
    }

    /** Write the header of this frame to an output stream.
     *
     * @param oOS the output stream to write to
     * @throws ID3Exception if an error occurs while writing
     */
    protected void writeHeader(OutputStream oOS)
        throws ID3Exception
    {
        try
        {
            ID3DataOutputStream oIDOS = new ID3DataOutputStream(oOS);
            
            // frame id
            oIDOS.write(getFrameId());
            // size
            int iActualLength = getActualLength();
            oIDOS.writeBE32(iActualLength);
            //oIDOS.writeBE32(getLength());
            // first flags
            int iFirstFlags = 0;
            if (m_bTagAlterPreservationFlag)
            {
                iFirstFlags |= (1 << 7);
            }
            if (m_bFileAlterPreservationFlag)
            {
                iFirstFlags |= (1 << 6);
            }
            if (m_bReadOnlyFlag)
            {
                iFirstFlags |= (1 << 5);
            }
            oIDOS.writeUnsignedByte(iFirstFlags);
            // second flags
            int iSecondFlags = 0;
            if (m_bCompressionFlag)
            {
                iSecondFlags |= (1 << 7);
            }
            if (m_bEncryptionFlag)
            {
                iSecondFlags |= (1 << 6);
            }
            if (m_bGroupingIdentityFlag)
            {
                iSecondFlags |= (1 << 5);
            }
            oIDOS.writeUnsignedByte(iSecondFlags);
            
            // write uncompressed length of the body, if it is compressed
            if (m_bCompressionFlag)
            {
                oIDOS.writeBE32(getLength());
            }
            // write encrypted method, if used
            if (m_bEncryptionFlag)
            {
                oIDOS.writeUnsignedByte(m_byEncryptionMethod & 0xff);
            }
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error writing frame: " + e.getMessage(), e);
        }
    }
    
    /** Returns the length in bytes that the body of the frame will require when actually written to
     *  a file.  This may be shorter than the default length, if the frame is compressed.
     *
     * @return the length of the frame when written
     * @throws IOException if an error occurs while determining the compressed length
     */
    private int getActualLength()
        throws Exception
    {
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
        ID3DataOutputStream oIDOS = new ID3DataOutputStream(oBAOS);
        writeBody(oIDOS);
        byte[] abyBody = oBAOS.toByteArray();

        if (m_bCompressionFlag)
        {
            ByteArrayOutputStream oCompressedBAOS = new ByteArrayOutputStream();
            DeflaterOutputStream oDeflaterOS = new DeflaterOutputStream(oCompressedBAOS);
            oDeflaterOS.write(abyBody);
            oDeflaterOS.finish();
            abyBody = oCompressedBAOS.toByteArray();
        }
        
        if (m_bEncryptionFlag)
        {
            if (m_oCryptoAgent == null)
            {
                throw new ID3Exception("Crypto agent for method " + m_byEncryptionMethod + " not registered.  Cannot write frame.");
            }
            
            abyBody = m_oCryptoAgent.encrypt(abyBody, m_abyEncryptionData);
        }
        
        return abyBody.length + (m_bCompressionFlag ? 4 : 0) + (m_bEncryptionFlag ? 1 : 0);
    }
    
    /** Write the body of the frame to an ID3 data output stream.
     *
     * @param oIDOS the output stream to write to
     * @throws ID3Exception if an error occurs while writing
     */
    protected abstract void writeBody(ID3DataOutputStream oIDOS) throws IOException;

    /** Write this frame to an output stream.
     *
     * @param oOS the output stream to write to
     * @throws ID3Exception if an error occurs while writing the frame
     * @throws IOException if an error occurs while writing the frame
     */
    public void write(OutputStream oOS)
        throws IOException, ID3Exception
    {
        ID3DataOutputStream oIDOS = new ID3DataOutputStream(oOS);
        
        // write header
        writeHeader(oIDOS);
        
        // write body
        byte[] abyBody = null;
        
        // put original body bytes in abyBody
        ByteArrayOutputStream oBodyBAOS = new ByteArrayOutputStream();
        ID3DataOutputStream oBodyIDOS = new ID3DataOutputStream(oBodyBAOS);
        writeBody(oBodyIDOS);
        abyBody = oBodyBAOS.toByteArray();
        
        // if compression used, compress body byte array
        if (m_bCompressionFlag)
        {
            ByteArrayOutputStream oCompressedBAOS = new ByteArrayOutputStream();
            DeflaterOutputStream oDeflaterOS = new DeflaterOutputStream(oCompressedBAOS);
            oDeflaterOS.write(abyBody);
            oDeflaterOS.finish();
            abyBody = oCompressedBAOS.toByteArray();
        }
        
        // if encryption used, encrypt body byte array
        if (m_bEncryptionFlag)
        {
            if (m_oCryptoAgent == null)
            {
                throw new ID3Exception("Crypto agent for method " + m_byEncryptionMethod + " not registered.  Cannot write frame.");
            }
            
            abyBody = m_oCryptoAgent.encrypt(abyBody, m_abyEncryptionData);
        }
        
        oIDOS.write(abyBody);
    }
}
