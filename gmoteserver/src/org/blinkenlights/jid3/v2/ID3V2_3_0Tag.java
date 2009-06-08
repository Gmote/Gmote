/*
 * ID3V2_3_0Tag.java
 *
 * Created on 24-Nov-2003
 *
 * Copyright (C)2003-2005 Paul Grebenc
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
 * $Id: ID3V2_3_0Tag.java,v 1.31 2005/12/10 05:36:40 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.security.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.crypt.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Object representing a v2.3.0 tag, containing frames, which can be written to a file.
 */
public class ID3V2_3_0Tag extends ID3V2Tag implements ID3Observer, ID3Visitable
{
    /** Containers for frames for which there can be more than one in a tag. */
    protected SortedMap m_oAENCOwnerIdentifierToFrameMap = null;
    protected SortedMap m_oAPICDescriptionToFrameMap = null;
    protected SortedMap m_oCOMMLanguageAndContentDescriptorToFrameMap = null;
    protected SortedMap m_oENCRMethodToFrameMap = null;
    protected SortedMap m_oGEOBContentDescriptorToFrameMap = null;
    protected SortedMap m_oGRIDGroupSymbolToFrameMap = null;
    protected SortedMap m_oLINKContentsToFrameMap = null;
    protected SortedMap m_oPRIVContentsToFrameMap = null;
    protected SortedMap m_oPOPMEmailToFrameMap = null;
    protected SortedMap m_oSYLTLanguageAndContentDescriptorToFrameMap = null;
    protected SortedMap m_oTXXXDescriptionToFrameMap = null;
    protected SortedMap m_oUFIDOwnerIdentifierToFrameMap = null;
    protected SortedMap m_oUSLTLanguageAndContentDescriptorToFrameMap = null;
    protected SortedMap m_oWCOMUrlToFrameMap = null;
    protected SortedMap m_oWOARUrlToFrameMap = null;
    protected SortedMap m_oWXXXDescriptionToFrameMap = null;
    
    protected List m_oEncryptedFrameList = null;
    protected List m_oUnknownFrameList = null;
    
    public ID3V2_3_0Tag()
    {
        super(false, false, false);
        
        m_oAENCOwnerIdentifierToFrameMap = new TreeMap();
        m_oAPICDescriptionToFrameMap = new TreeMap();
        m_oCOMMLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oENCRMethodToFrameMap = new TreeMap();
        m_oGEOBContentDescriptorToFrameMap = new TreeMap();
        m_oGRIDGroupSymbolToFrameMap = new TreeMap();
        m_oLINKContentsToFrameMap =  new TreeMap();
        m_oPRIVContentsToFrameMap = new TreeMap();
        m_oPOPMEmailToFrameMap = new TreeMap();
        m_oSYLTLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oTXXXDescriptionToFrameMap = new TreeMap();
        m_oUFIDOwnerIdentifierToFrameMap = new TreeMap();
        m_oUSLTLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oWCOMUrlToFrameMap = new TreeMap();
        m_oWOARUrlToFrameMap = new TreeMap();
        m_oWXXXDescriptionToFrameMap = new TreeMap();
        
        m_oEncryptedFrameList = new ArrayList();
        m_oUnknownFrameList = new ArrayList();
    }

    private ID3V2_3_0Tag(boolean bUnsynchronizationUsedFlag,
                         boolean bExtendedHeaderFlag,
                         boolean bExperimentalFlag)
    {
        super(bUnsynchronizationUsedFlag,
              bExtendedHeaderFlag,
              bExperimentalFlag);
        
        m_oAENCOwnerIdentifierToFrameMap = new TreeMap();
        m_oAPICDescriptionToFrameMap = new TreeMap();
        m_oCOMMLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oENCRMethodToFrameMap = new TreeMap();
        m_oGEOBContentDescriptorToFrameMap = new TreeMap();
        m_oGRIDGroupSymbolToFrameMap = new TreeMap();
        m_oLINKContentsToFrameMap =  new TreeMap();
        m_oPRIVContentsToFrameMap = new TreeMap();
        m_oPOPMEmailToFrameMap = new TreeMap();
        m_oSYLTLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oTXXXDescriptionToFrameMap = new TreeMap();
        m_oUFIDOwnerIdentifierToFrameMap = new TreeMap();
        m_oUSLTLanguageAndContentDescriptorToFrameMap = new TreeMap();
        m_oWCOMUrlToFrameMap = new TreeMap();
        m_oWOARUrlToFrameMap = new TreeMap();
        m_oWXXXDescriptionToFrameMap = new TreeMap();
        
        m_oEncryptedFrameList = new ArrayList();
        m_oUnknownFrameList = new ArrayList();
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitID3V2_3_0Tag(this);
        
        ID3V2Frame[] aoID3V2Frame = getAllFrames();
        for (int i=0; i < aoID3V2Frame.length; i++)
        {
            aoID3V2Frame[i].accept(oID3Visitor);
        }
    }
    
    /** Internal method to read a tag from an input stream.
     *
     * @param oID3DIS the input stream to read a tag from
     * @return an ID3V2_3_0Tag object read from the input stream
     * @throws ID3Exception if an error occurs while reading the tag
     */
    static ID3V2Tag internalRead(ID3DataInputStream oID3DIS)
        throws ID3Exception
    {
        ArrayList oEncryptedFrameList = new ArrayList();
        
        try
        {
            // read flags
            int iFlags = oID3DIS.readUnsignedByte();
            boolean bUnsynchronizationUsedFlag = ((iFlags & 0x80) != 0);
            boolean bExtendedHeaderFlag = ((iFlags & 0x40) != 0);
            boolean bExperimentalFlag = ((iFlags & 0x20) != 0);
            if ((iFlags & 0x1f) > 0)
            {
                // we are supposed to fail if any unknown flags are encountered
                throw new ID3Exception("Encountered unknown header flags.");
            }
            
            // read tag size
            int iTagSize = oID3DIS.readID3Four();
            
            // if extended header exists, read it
            boolean bCRCFlag = false;
            long lExpectedCRCValue = 0;
            int iSizeOfPadding = 0;
            if (bExtendedHeaderFlag)
            {
                int iExtendedHeaderSize = oID3DIS.readBE32();
                // fix tag size to not include extended header we're now reading, nor the four byte length of the
                // extended header
                iTagSize -= (iExtendedHeaderSize + 4);
                if ((iExtendedHeaderSize != 6) && (iExtendedHeaderSize != 10))
                {
                    throw new ID3Exception("Extended header size must be either 6 or 10 bytes.  Read " + iExtendedHeaderSize + ".");
                }
                int iFlagOne = oID3DIS.readUnsignedByte();
                int iFlagTwo = oID3DIS.readUnsignedByte();
                // make sure there are no unknown flags set
                if ( ((iFlagOne & 0x7f) != 0) || (iFlagTwo != 0) )
                {
                    throw new ID3Exception("Extended header has unknown flags set.");
                }
                bCRCFlag = ((iFlagOne >> 7) != 0);
                // read size of padding
                iSizeOfPadding = oID3DIS.readBE32();
                // read CRC data if provided
                if (bCRCFlag)
                {
                    lExpectedCRCValue = oID3DIS.readUnsignedBE32();
                }
            }
            
            // create tag object
            ID3V2_3_0Tag oID3V2_3_0Tag = new ID3V2_3_0Tag(bUnsynchronizationUsedFlag,
                                                          bExtendedHeaderFlag,
                                                          bExperimentalFlag);
            
            // set CRC flag if in read tag
            if (bCRCFlag)
            {
                oID3V2_3_0Tag.setCRC(bCRCFlag);
            }
            
            // read all frames into array
            byte[] abyFrameData = new byte[iTagSize];
            oID3DIS.readFully(abyFrameData);
            
            // de-unsynchronize if necessary
            if (bUnsynchronizationUsedFlag)
            {
                abyFrameData = ID3Util.deunsynchronize(abyFrameData);
            }

            // check CRC against frame data if specified
            if (bCRCFlag)
            {
                CRC32 oCRC32 = new CRC32();
                oCRC32.update(abyFrameData, 0, iTagSize - iSizeOfPadding);
                long lCalculatedCRCValue = oCRC32.getValue();
                //System.out.println("Expected CRC value = " + lExpectedCRCValue);
                //System.out.println("Calculated CRC " + lCalculatedCRCValue + " on read bytes " + ID3Util.convertBytesToHexString(abyFrameData, true));
                
                if (lExpectedCRCValue != lCalculatedCRCValue)
                {
                    throw new ID3Exception("Expected and calculated CRC values for tag do not match.");
                }
            }
            
            // read individual frames and store them
            ByteArrayInputStream oFrameBAIS = new ByteArrayInputStream(abyFrameData);
            ID3DataInputStream oFrameID3DIS = new ID3DataInputStream(oFrameBAIS);
            int iPaddingLength = 0;
            while (oFrameID3DIS.available() > 4)
            {
                try
                {
                    ID3V2Frame oID3V2Frame = ID3V2Frame.read(oFrameID3DIS);
                    if (oID3V2Frame != null)
                    {
                        if (oID3V2Frame instanceof EncryptedID3V2Frame)
                        {
                            // store this encrypted frame for later (we have to do two passes.. because the encryption
                            // details may come after the encrypted frame, in the tag)
                            oEncryptedFrameList.add(oID3V2Frame);
                        }
                        else
                        {
                            storeID3V2Frame(oID3V2Frame, oID3V2_3_0Tag);
                        }
                    }
                    else
                    {
                        // we tried to read a tag and saw there wasn't one there, which means we read one byte of padding
                        iPaddingLength += 4;

                        break;
                    }
                }
                catch (ID3Exception ie)
                {
                    // if we are using strict reading, then this exception gets rethrown, otherwise it is ignored
                    if (ie instanceof InvalidFrameID3Exception)
                    {
                        if (ID3Tag.usingStrict())
                        {
                            throw ie;
                        }
                    }
                    else
                    {
                        throw ie;
                    }
                }
            }
            // all remaining bytes are padding (could be zero)
            iPaddingLength += oFrameID3DIS.available();
            
            // set padding length
            oID3V2_3_0Tag.m_iPaddingLength = iPaddingLength;
            
            // re-read encrypted frames that were saved (now that all ENCR frames in tag have been read)
            Iterator oEncryptedFrameIter = oEncryptedFrameList.iterator();
            while (oEncryptedFrameIter.hasNext())
            {
                EncryptedID3V2Frame oEncryptedID3V2Frame = (EncryptedID3V2Frame)oEncryptedFrameIter.next();
                
                byte[] abyEncryptedData = oEncryptedID3V2Frame.getEncryptedData();
                
                ByteArrayInputStream oEncBAIS = new ByteArrayInputStream(abyEncryptedData);
                ID3DataInputStream oEncID3DIS = new ID3DataInputStream(oEncBAIS);
                
                ID3V2Frame oID3V2Frame = ID3V2Frame.read(oEncID3DIS, oID3V2_3_0Tag.getENCRFrames());

                if ( ! (oID3V2Frame instanceof EncryptedID3V2Frame) )
                {
                    // we were able to decrypt the frame this time, so remove it from the encrypted frame list
                    oEncryptedFrameIter.remove();
                    
                    // and add the decrypted frame to the tag
                    storeID3V2Frame(oID3V2Frame, oID3V2_3_0Tag);
                }
                else
                {
                    // save this encrypted frame as is
                    oID3V2_3_0Tag.m_oEncryptedFrameList.add(oID3V2Frame);
                }
            }
            
            return oID3V2_3_0Tag;
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error reading tag.", e);
        }
    }
    
    /** Store a frame in this tag, using reflection to search for an appropriate mathod.
     *
     * @param oID3V2Frame the frame to be stored
     * @param oID3V2_3_0Tag the tag object to store the frame in
     * @throws Exception if there was an exception in the frame constructor, it is thrown from this method
     */
    private static void storeID3V2Frame(ID3V2Frame oID3V2Frame, ID3V2_3_0Tag oID3V2_3_0Tag)
        throws Exception
    {
        // search for the available method which can add this frame to the tag
        String sMethodName = null;
        Method oMethod = null;
        String[] asMethodSuffix = { "Frame", "TextInformationFrame", "UrlLinkFrame" };
        for (int i=0; i < asMethodSuffix.length; i++)
        {
            try
            {
                sMethodName = "add" + new String(oID3V2Frame.getFrameId()) + asMethodSuffix[i];
                oMethod = oID3V2_3_0Tag.getClass().getMethod(sMethodName, new Class[] { oID3V2Frame.getClass() });
                oMethod.invoke(oID3V2_3_0Tag, new Object[] { oID3V2Frame });
                return;
            }
            catch (NoSuchMethodException e) {}
            catch (IllegalAccessException e) {}
            catch (InvocationTargetException e) { throw (Exception)e.getCause(); }
            try
            {
                sMethodName = "set" + new String(oID3V2Frame.getFrameId()) + asMethodSuffix[i];
                oMethod = oID3V2_3_0Tag.getClass().getMethod(sMethodName, new Class[] { oID3V2Frame.getClass() });
                oMethod.invoke(oID3V2_3_0Tag, new Object[] { oID3V2Frame });
                return;
            }
            catch (NoSuchMethodException e) {}
            catch (IllegalAccessException e) {}
            catch (InvocationTargetException e) { throw (Exception)e.getCause(); }
        }
        
        // if we're here, this frame is unknown
        oID3V2_3_0Tag.m_oUnknownFrameList.add(oID3V2Frame);
    }
    
    public void write(OutputStream oOS)
        throws ID3Exception
    {
        try
        {
            ID3DataOutputStream oIDOS = new ID3DataOutputStream(oOS);
            
            // write ID3 header
            oIDOS.write("ID3".getBytes());
            // version and patch number
            oIDOS.write(3);
            oIDOS.write(0);
            
            // write all frames to a tag buffer
            ByteArrayOutputStream oTagBAOS = new ByteArrayOutputStream();
            Iterator oIter;
            // AENC
            oIter = m_oAENCOwnerIdentifierToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // APIC
            oIter = m_oAPICDescriptionToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // COMM
            oIter = m_oCOMMLanguageAndContentDescriptorToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // ENCR
            oIter = m_oENCRMethodToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // GEOB
            oIter = m_oGEOBContentDescriptorToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // GRID
            oIter = m_oGRIDGroupSymbolToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // LINK
            oIter = m_oLINKContentsToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // PRIV
            oIter = m_oPRIVContentsToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // POPM
            oIter = m_oPOPMEmailToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // SYLT
            oIter = m_oSYLTLanguageAndContentDescriptorToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // TXXX
            oIter = m_oTXXXDescriptionToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // UFID
            oIter = m_oUFIDOwnerIdentifierToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // USLT
            oIter = m_oUSLTLanguageAndContentDescriptorToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // WCOM
            oIter = m_oWCOMUrlToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // WOAR
            oIter = m_oWOARUrlToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // WXXX
            oIter = m_oWXXXDescriptionToFrameMap.values().iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }
            // write all single frames mapped from frame id to frame
            // (COMR,EQUA,ETCO,IPLS,MCDI,MLLT,OWNE,PCNT,POSS,RBUF,RVAD,RVRB,SYTC,TALB,TBPM,TCOM,TCON,TCOP,TDAT,TDLY,
            //  TENC,TEXT,TFLT,TIME,TIT1,TIT2,TIT3,TKEY,TLAN,TLEN,TMED,TOAL,TOFN,TOLY,TOPE,TORY,TOWN,TPE1,TPE2,TPE3,
            //  TPE4,TPOS,TPUB,TRCK,TRDA,TRSN,TRSO,TSIZ,TSRC,TSSE,TYER,USER,WCOP,WOAF,WOAS,WORS,WPAY,PUB)
            oIter = m_oFrameIdToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                String sFrameId = (String)oIter.next();
                
                ID3V2Frame oID3V2Frame = (ID3V2Frame)m_oFrameIdToFrameMap.get(sFrameId);
                
                oID3V2Frame.write(oTagBAOS);
            }
            
            // Unknown frames
            oIter = m_oUnknownFrameList.iterator();
            while (oIter.hasNext())
            {
                ((ID3V2Frame)oIter.next()).write(oTagBAOS);
            }

            byte[] abyTag = oTagBAOS.toByteArray();
            
            // unsynchronize the tag if option set and unsynchronizing is required
            boolean bUnsynchronized = false;
            if ((m_bUnsynchronizationUsedFlag) && (ID3Util.requiresUnsynchronization(abyTag)))
            {
                abyTag = ID3Util.unsynchronize(abyTag);
                bUnsynchronized = true;
            }
            
            // write flags
            int iFlags = 0;
            if (bUnsynchronized)    // specify if we _used_ the method, not if we were prepared to use it
            {
                iFlags |= 0x80;
            }
            if (m_bExtendedHeaderFlag)
            {
                iFlags |= 0x40;
            }
            if (m_bExperimentalFlag)
            {
                iFlags |= 0x20;
            }
            oIDOS.write(iFlags);
            
            // create the extended header if enabled
            byte[] abyExtendedHeader = null;
            if (m_bExtendedHeaderFlag)
            {
                ByteArrayOutputStream oExtendedHeaderBAOS = new ByteArrayOutputStream();
                ID3DataOutputStream oEHIDOS = new ID3DataOutputStream(oExtendedHeaderBAOS);
                
                // header size
                int iHeaderSize = m_bCRCDataFlag ? 10 : 6;  // size based on whether CRC present or not
                oEHIDOS.writeBE32(iHeaderSize);
                // flags
                int iFirstFlagByte = 0;
                if (m_bCRCDataFlag)
                {
                    iFirstFlagByte |= 0x80;
                }
                oEHIDOS.writeUnsignedByte(iFirstFlagByte);
                oEHIDOS.writeUnsignedByte(0); // second flag byte always zero
                // size of padding
                oEHIDOS.writeBE32(m_iPaddingLength);
                // CRC if enabled
                if (m_bCRCDataFlag)
                {
                    CRC32 oCRC32 = new CRC32();
                    oCRC32.update(abyTag);
                    //System.out.println("Writing CRC value " + oCRC32.getValue() + " for " + ID3Util.convertBytesToHexString(abyTag, true));
                    oEHIDOS.writeUnsignedBE32(oCRC32.getValue());
                }
                
                oEHIDOS.flush();
                abyExtendedHeader = oExtendedHeaderBAOS.toByteArray();
            }

            // write tag size (length of all frames), preceded possibly by extended header and including its length,
            // and also any padding length after the frames
            int iTagSize = abyTag.length;
            if (m_bExtendedHeaderFlag)
            {
                oIDOS.writeID3Four(iTagSize + abyExtendedHeader.length + m_iPaddingLength);
                oIDOS.write(abyExtendedHeader);
            }
            else
            {
                oIDOS.writeID3Four(iTagSize + m_iPaddingLength);
            }
            
            // write tag (frames)
            oIDOS.write(abyTag);
            
            // write padding.
            oIDOS.write(new byte[m_iPaddingLength]);
            
            oIDOS.flush();
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error writing tag.", e);
        }
    }

    /** Check to see if this tag contains at least one frame.
     *
     * @return true if this frame contains at least one frame, false otherwise
     */
    public boolean containsAtLeastOneFrame()
    {
        return ((m_oAENCOwnerIdentifierToFrameMap.size() > 0) ||
               (m_oAPICDescriptionToFrameMap.size() > 0) ||
               (m_oCOMMLanguageAndContentDescriptorToFrameMap.size() > 0) ||
               (m_oENCRMethodToFrameMap.size() > 0) ||
               (m_oGEOBContentDescriptorToFrameMap.size() > 0) ||
               (m_oGRIDGroupSymbolToFrameMap.size() > 0) ||
               (m_oLINKContentsToFrameMap.size() > 0) ||
               (m_oPRIVContentsToFrameMap.size() > 0) ||
               (m_oPOPMEmailToFrameMap.size() > 0) ||
               (m_oSYLTLanguageAndContentDescriptorToFrameMap.size() > 0) ||
               (m_oTXXXDescriptionToFrameMap.size() > 0) ||
               (m_oUFIDOwnerIdentifierToFrameMap.size() > 0) ||
               (m_oUSLTLanguageAndContentDescriptorToFrameMap.size() > 0) ||
               (m_oWCOMUrlToFrameMap.size() > 0) ||
               (m_oWOARUrlToFrameMap.size() > 0) ||
               (m_oWXXXDescriptionToFrameMap.size() > 0) || 
               (m_oFrameIdToFrameMap.size() > 0) ||
               (m_oUnknownFrameList.size() > 0));
    }

    public void update(ID3Subject oID3Subject)
        throws ID3Exception
    {
        if (oID3Subject instanceof ID3V2Frame)
        {
            ID3V2Frame oID3V2Frame = (ID3V2Frame)oID3Subject;
            
            validateFrameMapping(oID3V2Frame);
        }
        
        synchronizeEncryption();
    }
    
    private void validateFrameMapping(ID3V2Frame oID3V2Frame)
        throws ID3Exception
    {
        if (oID3V2Frame instanceof AENCID3V2Frame)
        {
            AENCID3V2Frame oAENC = (AENCID3V2Frame)oID3V2Frame;
            String sOwnerIdentifier = oAENC.getOwnerIdentifier();
            // check if there is a conflict
            AENCID3V2Frame oOtherAENC = (AENCID3V2Frame)m_oAENCOwnerIdentifierToFrameMap.get(sOwnerIdentifier);
            if ((oAENC != oOtherAENC) && (oOtherAENC != null))
            {
                throw new ID3Exception("Conflict between AENC frames with owner identifier [" + sOwnerIdentifier + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oAENCOwnerIdentifierToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oAENC == m_oAENCOwnerIdentifierToFrameMap.get(oKey))
                {
                    m_oAENCOwnerIdentifierToFrameMap.remove(oKey);
                    m_oAENCOwnerIdentifierToFrameMap.put(sOwnerIdentifier, oAENC);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof APICID3V2Frame)
        {
            APICID3V2Frame oAPIC = (APICID3V2Frame)oID3V2Frame;
            String sDescription = oAPIC.getDescription();
            // check if there is a conflict
            APICID3V2Frame oOtherAPIC = (APICID3V2Frame)m_oAPICDescriptionToFrameMap.get(sDescription);
            if ((oAPIC != oOtherAPIC) && (oOtherAPIC != null))
            {
                throw new ID3Exception("Conflict between APIC frames with description [" + sDescription + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oAPICDescriptionToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oAPIC == m_oAPICDescriptionToFrameMap.get(oKey))
                {
                    m_oAPICDescriptionToFrameMap.remove(oKey);
                    m_oAPICDescriptionToFrameMap.put(sDescription, oAPIC);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof COMMID3V2Frame)
        {
            COMMID3V2Frame oCOMM = (COMMID3V2Frame)oID3V2Frame;
            String sLanguage = oCOMM.getLanguage();
            String sShortDescription = oCOMM.getShortDescription();
            String sLanguageAndContentDescriptor = sLanguage + sShortDescription;
            // check if there is a conflict
            COMMID3V2Frame oOtherCOMM = (COMMID3V2Frame)m_oCOMMLanguageAndContentDescriptorToFrameMap.get(sLanguageAndContentDescriptor);
            if ((oCOMM != oOtherCOMM) && (oOtherCOMM != null))
            {
                throw new ID3Exception("Conflict between COMM frames with language [" + sLanguage + "] and short description [" + sShortDescription + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oCOMMLanguageAndContentDescriptorToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oCOMM == m_oCOMMLanguageAndContentDescriptorToFrameMap.get(oKey))
                {
                    m_oCOMMLanguageAndContentDescriptorToFrameMap.remove(oKey);
                    m_oCOMMLanguageAndContentDescriptorToFrameMap.put(sLanguageAndContentDescriptor, oCOMM);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof ENCRID3V2Frame)
        {
            ENCRID3V2Frame oENCR = (ENCRID3V2Frame)oID3V2Frame;
            byte byEncryptionMethodSymbol = oENCR.getEncryptionMethodSymbol();
            // check if there is a conflict
            ENCRID3V2Frame oOtherENCR = (ENCRID3V2Frame)m_oENCRMethodToFrameMap.get(new Byte(byEncryptionMethodSymbol));
            if ((oENCR != oOtherENCR) && (oOtherENCR != null))
            {
                throw new ID3Exception("Conflict between ENCR frames with the same method symbol [" + byEncryptionMethodSymbol + "].");
            }
            // check to see if there exists an ENCR frame with the same owner identifier
            Iterator oCheckIter = m_oENCRMethodToFrameMap.values().iterator();
            while (oCheckIter.hasNext())
            {
                ENCRID3V2Frame oCheckENCR = (ENCRID3V2Frame)oCheckIter.next();
                if ((oENCR != oCheckENCR) && (oENCR.getOwnerIdentifier().equals(oCheckENCR.getOwnerIdentifier())))
                {
                    throw new ID3Exception("Conflict between ENCR frames with the same method symbol [" + oCheckENCR.getOwnerIdentifier() + "].");
                }
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oENCRMethodToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oENCR == m_oENCRMethodToFrameMap.get(oKey))
                {
                    m_oENCRMethodToFrameMap.remove(oKey);
                    m_oENCRMethodToFrameMap.put(new Byte(byEncryptionMethodSymbol), oENCR);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof GEOBID3V2Frame)
        {
            GEOBID3V2Frame oGEOB = (GEOBID3V2Frame)oID3V2Frame;
            String sContentDescription = oGEOB.getContentDescription();
            // check if there is a conflict
            GEOBID3V2Frame oOtherGEOB = (GEOBID3V2Frame)m_oGEOBContentDescriptorToFrameMap.get(sContentDescription);
            if ((oGEOB != oOtherGEOB) && (oOtherGEOB != null))
            {
                throw new ID3Exception("Conflict between GEOB frames with content description [" + sContentDescription + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oGEOBContentDescriptorToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oGEOB == m_oGEOBContentDescriptorToFrameMap.get(oKey))
                {
                    m_oGEOBContentDescriptorToFrameMap.remove(oKey);
                    m_oGEOBContentDescriptorToFrameMap.put(sContentDescription, oGEOB);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof GRIDID3V2Frame)
        {
            GRIDID3V2Frame oGRID = (GRIDID3V2Frame)oID3V2Frame;
            byte byGroupSymbol = oGRID.getGroupSymbol();
            // check if there is a conflict
            GRIDID3V2Frame oOtherGRID = (GRIDID3V2Frame)m_oGRIDGroupSymbolToFrameMap.get(new Byte(byGroupSymbol));
            if ((oGRID != oOtherGRID) && (oOtherGRID != null))
            {
                throw new ID3Exception("Conflict between GRID frames with the same group symbol [" + byGroupSymbol + "].");
            }
            // check to see if there exists an GRID frame with the same owner identifier
            Iterator oCheckIter = m_oGRIDGroupSymbolToFrameMap.values().iterator();
            while (oCheckIter.hasNext())
            {
                GRIDID3V2Frame oCheckGRID = (GRIDID3V2Frame)oCheckIter.next();
                if ((oGRID != oCheckGRID) && (oGRID.getOwnerIdentifier().equals(oCheckGRID.getOwnerIdentifier())))
                {
                    throw new ID3Exception("Conflict between GRID frames with the same group symbol [" + oCheckGRID.getOwnerIdentifier() + "].");
                }
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oGRIDGroupSymbolToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oGRID == m_oGRIDGroupSymbolToFrameMap.get(oKey))
                {
                    m_oGRIDGroupSymbolToFrameMap.remove(oKey);
                    m_oGRIDGroupSymbolToFrameMap.put(new Byte(byGroupSymbol), oGRID);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof LINKID3V2Frame)
        {
            LINKID3V2Frame oLINK = (LINKID3V2Frame)oID3V2Frame;
            String sContents = new String(oLINK.getFrameId()) + oLINK.getLinkUrl() + oLINK.getAdditionalData();
            // check if there is a conflict
            LINKID3V2Frame oOtherLINK = (LINKID3V2Frame)m_oLINKContentsToFrameMap.get(sContents);
            if ((oLINK != oOtherLINK) && (oOtherLINK != null))
            {
                throw new ID3Exception("Conflict between LINK frames with frame ID [" + new String(oLINK.getFrameId()) +
                                       "], URL [" + oLINK.getLinkUrl() + "] and additional data [" + oLINK.getAdditionalData() + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oLINKContentsToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oLINK == m_oLINKContentsToFrameMap.get(oKey))
                {
                    m_oLINKContentsToFrameMap.remove(oKey);
                    m_oLINKContentsToFrameMap.put(sContents, oLINK);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof POPMID3V2Frame)
        {
            POPMID3V2Frame oPOPM = (POPMID3V2Frame)oID3V2Frame;
            String sEmailToUser = oPOPM.getEmailToUser();
            // check if there is a conflict
            POPMID3V2Frame oOtherPOPM = (POPMID3V2Frame)m_oPOPMEmailToFrameMap.get(sEmailToUser);
            if ((oPOPM != oOtherPOPM) && (oOtherPOPM != null))
            {
                throw new ID3Exception("Conflict between POPM frames with email address [" + sEmailToUser + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oPOPMEmailToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oPOPM == m_oPOPMEmailToFrameMap.get(oKey))
                {
                    m_oPOPMEmailToFrameMap.remove(oKey);
                    m_oPOPMEmailToFrameMap.put(sEmailToUser, oPOPM);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof PRIVID3V2Frame)
        {
            PRIVID3V2Frame oPRIV = (PRIVID3V2Frame)oID3V2Frame;
            String sHash = null;
            try
            {
                MessageDigest oMD = MessageDigest.getInstance("MD5");
                oMD.update(oPRIV.getPrivateData());
                byte[] abyDigest = oMD.digest();
                sHash = ID3Util.convertBytesToHexString(abyDigest, false);
            }
            catch (Exception e)
            {
                throw new ID3Exception("Error hashing private data in PRIV frame.", e);
            }
            String sContents = oPRIV.getOwnerIdentifier() + sHash;
            // check if there is a conflict
            PRIVID3V2Frame oOtherPRIV = (PRIVID3V2Frame)m_oPRIVContentsToFrameMap.get(sContents);
            if ((oPRIV != oOtherPRIV) && (oOtherPRIV != null))
            {
                throw new ID3Exception("Conflict between PRIV frames with owner identifier [" + oPRIV.getOwnerIdentifier() +
                                       "] and matching private data.");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oPRIVContentsToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oPRIV == m_oPRIVContentsToFrameMap.get(oKey))
                {
                    m_oPRIVContentsToFrameMap.remove(oKey);
                    m_oPRIVContentsToFrameMap.put(sContents, oPRIV);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof SYLTID3V2Frame)
        {
            SYLTID3V2Frame oSYLT = (SYLTID3V2Frame)oID3V2Frame;
            String sLanguageAndContentDescriptor = oSYLT.getLanguage() + oSYLT.getContentDescriptor();
            // check if there is a conflict
            SYLTID3V2Frame oOtherSYLT = (SYLTID3V2Frame)m_oSYLTLanguageAndContentDescriptorToFrameMap.get(sLanguageAndContentDescriptor);
            if ((oSYLT != oOtherSYLT) && (oOtherSYLT != null))
            {
                throw new ID3Exception("Conflict between SYLT frames with language [" + oSYLT.getLanguage() +
                                       "] and content descriptor [" + oSYLT.getContentDescriptor() + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oSYLTLanguageAndContentDescriptorToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oSYLT == m_oSYLTLanguageAndContentDescriptorToFrameMap.get(oKey))
                {
                    m_oSYLTLanguageAndContentDescriptorToFrameMap.remove(oKey);
                    m_oSYLTLanguageAndContentDescriptorToFrameMap.put(sLanguageAndContentDescriptor, oSYLT);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof TXXXTextInformationID3V2Frame)
        {
            TXXXTextInformationID3V2Frame oTXXX = (TXXXTextInformationID3V2Frame)oID3V2Frame;
            String sDescription = oTXXX.getDescription();
            // check if there is a conflict
            TXXXTextInformationID3V2Frame oOtherTXXX = (TXXXTextInformationID3V2Frame)m_oTXXXDescriptionToFrameMap.get(sDescription);
            if ((oTXXX != oOtherTXXX) && (oOtherTXXX != null))
            {
                throw new ID3Exception("Conflict between TXXX frames with description [" + sDescription + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oTXXXDescriptionToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oTXXX == m_oTXXXDescriptionToFrameMap.get(oKey))
                {
                    m_oTXXXDescriptionToFrameMap.remove(oKey);
                    m_oTXXXDescriptionToFrameMap.put(sDescription, oTXXX);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof UFIDID3V2Frame)
        {
            UFIDID3V2Frame oUFID = (UFIDID3V2Frame)oID3V2Frame;
            String sOwnerIdentifier = oUFID.getOwnerIdentifier();
            // check if there is a conflict
            UFIDID3V2Frame oOtherUFID = (UFIDID3V2Frame)m_oUFIDOwnerIdentifierToFrameMap.get(sOwnerIdentifier);
            if ((oUFID != oOtherUFID) && (oOtherUFID != null))
            {
                throw new ID3Exception("Conflict between UFID frames with owner identifier [" + sOwnerIdentifier + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oUFIDOwnerIdentifierToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oUFID == m_oUFIDOwnerIdentifierToFrameMap.get(oKey))
                {
                    m_oUFIDOwnerIdentifierToFrameMap.remove(oKey);
                    m_oUFIDOwnerIdentifierToFrameMap.put(sOwnerIdentifier, oUFID);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof USLTID3V2Frame)
        {
            USLTID3V2Frame oUSLT = (USLTID3V2Frame)oID3V2Frame;
            String sLanguageAndContentDescriptor = oUSLT.getLanguage() + oUSLT.getContentDescriptor();
            // check if there is a conflict
            USLTID3V2Frame oOtherUSLT = (USLTID3V2Frame)m_oUSLTLanguageAndContentDescriptorToFrameMap.get(sLanguageAndContentDescriptor);
            if ((oUSLT != oOtherUSLT) && (oOtherUSLT != null))
            {
                throw new ID3Exception("Conflict between USLT frames with language [" + oUSLT.getLanguage() +
                                       "] and content descriptor [" + oUSLT.getContentDescriptor() + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oUSLTLanguageAndContentDescriptorToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oUSLT == m_oUSLTLanguageAndContentDescriptorToFrameMap.get(oKey))
                {
                    m_oUSLTLanguageAndContentDescriptorToFrameMap.remove(oKey);
                    m_oUSLTLanguageAndContentDescriptorToFrameMap.put(sLanguageAndContentDescriptor, oUSLT);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof WCOMUrlLinkID3V2Frame)
        {
            WCOMUrlLinkID3V2Frame oWCOM = (WCOMUrlLinkID3V2Frame)oID3V2Frame;
            String sURL = oWCOM.getCommercialInformationUrl();
            // check if there is a conflict
            WCOMUrlLinkID3V2Frame oOtherWCOM = (WCOMUrlLinkID3V2Frame)m_oWCOMUrlToFrameMap.get(sURL);
            if ((oWCOM != oOtherWCOM) && (oOtherWCOM != null))
            {
                throw new ID3Exception("Conflict between WCOM frames with URL [" + sURL + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oWCOMUrlToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oWCOM == m_oWCOMUrlToFrameMap.get(oKey))
                {
                    m_oWCOMUrlToFrameMap.remove(oKey);
                    m_oWCOMUrlToFrameMap.put(sURL, oWCOM);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof WOARUrlLinkID3V2Frame)
        {
            WOARUrlLinkID3V2Frame oWOAR = (WOARUrlLinkID3V2Frame)oID3V2Frame;
            String sURL = oWOAR.getOfficialArtistWebPage();
            // check if there is a conflict
            WOARUrlLinkID3V2Frame oOtherWOAR = (WOARUrlLinkID3V2Frame)m_oWOARUrlToFrameMap.get(sURL);
            if ((oWOAR != oOtherWOAR) && (oOtherWOAR != null))
            {
                throw new ID3Exception("Conflict between WOAR frames with URL [" + sURL + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oWOARUrlToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oWOAR == m_oWOARUrlToFrameMap.get(oKey))
                {
                    m_oWOARUrlToFrameMap.remove(oKey);
                    m_oWOARUrlToFrameMap.put(sURL, oWOAR);
                    break;
                }
            }
        }
        else if (oID3V2Frame instanceof WXXXUrlLinkID3V2Frame)
        {
            WXXXUrlLinkID3V2Frame oWXXX = (WXXXUrlLinkID3V2Frame)oID3V2Frame;
            String sDescription = oWXXX.getDescription();
            // check if there is a conflict
            WXXXUrlLinkID3V2Frame oOtherWXXX = (WXXXUrlLinkID3V2Frame)m_oWXXXDescriptionToFrameMap.get(sDescription);
            if ((oWXXX != oOtherWXXX) && (oOtherWXXX != null))
            {
                throw new ID3Exception("Conflict between WXXX frames with description [" + sDescription + "].");
            }
            // if we are here, there is no conflict, so update mapping
            Iterator oIter = m_oWXXXDescriptionToFrameMap.keySet().iterator();
            while (oIter.hasNext())
            {
                Object oKey = oIter.next();
                if (oWXXX == m_oWXXXDescriptionToFrameMap.get(oKey))
                {
                    m_oWXXXDescriptionToFrameMap.remove(oKey);
                    m_oWXXXDescriptionToFrameMap.put(sDescription, oWXXX);
                    break;
                }
            }
        }
    }

    /** Sanity check, to see if this frame is in a consistent state for writing.  Specify encryption of a frame, without
     *  a corresponding ENCR frame, or the registration of the encryption agent, will cause this check to fail, for example.
     *
     * @throws ID3Exception if the frame is invalid for any reason
     */
    public void sanityCheck()
        throws ID3Exception
    {
        ID3V2Frame[] aoID3V2Frame = getAllFrames();
        ENCRID3V2Frame[] aoENCR = getENCRFrames();
        
        for (int i=0; i < aoID3V2Frame.length; i++)
        {
            ID3V2Frame oID3V2Frame = aoID3V2Frame[i];
            
            if (oID3V2Frame.isEncrypted())
            {
                for (int j=0; j < aoENCR.length; j++)
                {
                    if (oID3V2Frame.getEncryptionMethod() == aoENCR[j].getEncryptionMethodSymbol())
                    {
                        if (ID3Encryption.getInstance().lookupCryptoAgent(aoENCR[j].getOwnerIdentifier()) == null)
                        {
                            throw new ID3Exception("Tag sanity check failed.  At least one encrypted " +
                                                   new String(oID3V2Frame.getFrameId()) + " frame requires agent [" +
                                                   aoENCR[j].getOwnerIdentifier() + "], which has not been registered.");
                        }
                        else
                        {
                            return;
                        }
                    }
                }
                
                throw new ID3Exception("Tag sanity check failed.  At least one " + new String(oID3V2Frame.getFrameId()) +
                                       " frame encrypted with method symbol " + oID3V2Frame.getEncryptionMethod() +
                                       " does not have a matching ENCR frame to define the method.");
            }
        }
    }

    /** Go through all frame stored in this tag, ensuring that their encryption details are up-to-date.  This is
     *  required whenever frames are marked encrypted or not, or when ENCR frames are changed, etc.
     */
    private void synchronizeEncryption()
    {
        ID3V2Frame[] aoID3V2Frame = getAllFrames();
        
        for (int i=0; i < aoID3V2Frame.length; i++)
        {
            ID3V2Frame oID3V2Frame = aoID3V2Frame[i];
            
            if (oID3V2Frame.isEncrypted())
            {
                try
                {
                    ICryptoAgent oCryptoAgent = findCryptoAgent(oID3V2Frame.getEncryptionMethod());
                    byte[] abyEncryptionData = findEncryptionData(oID3V2Frame.getEncryptionMethod());

                    oID3V2Frame.setCryptoAgent(oCryptoAgent, abyEncryptionData);
                }
                catch (ID3Exception e) {}
            }
        }
    }

    /** Search for a crypto agent with a specific encryption method symbol.  This requires locating a matching ENCR
     *  frame, and then using its owner identifier to lookup a registered crypto agent.
     *
     * @param byEncryptionMethod the encryption method symbol to get a crypto agent for
     * @return a corresponding crypto agent, or null if one is not found
     */
    private ICryptoAgent findCryptoAgent(byte byEncryptionMethod)
    {
        ENCRID3V2Frame[] aoENCR = getENCRFrames();
        
        for (int i=0; i < aoENCR.length; i++)
        {
            ENCRID3V2Frame oENCRID3V2Frame = aoENCR[i];
            
            if ((oENCRID3V2Frame.getEncryptionMethodSymbol() & 0xff) == (byEncryptionMethod & 0xff))
            {
                String sOwnerIdentifier = oENCRID3V2Frame.getOwnerIdentifier();
                
                return ID3Encryption.getInstance().lookupCryptoAgent(sOwnerIdentifier);
            }
        }
        
        return null;
    }

    /** Search for the encryption data for a specific encryption method symbol.  This requires locating a matching ENCR
     *  frame, and then returning whatever encryption data may be stored in it.  This data is used as a parameter when
     *  calling an agent to encrypt or decrypt data.
     *
     * @param byEncryptionMethod the encryption method symbol to get encryption data for
     * @return the corresponding encryption data, or null if a match is not found
     */
    private byte[] findEncryptionData(byte byEncryptionMethod)
    {
        ENCRID3V2Frame[] aoENCR = getENCRFrames();
        
        for (int i=0; i < aoENCR.length; i++)
        {
            ENCRID3V2Frame oENCRID3V2Frame = aoENCR[i];
            
            if ((oENCRID3V2Frame.getEncryptionMethodSymbol() & 0xff) == (byEncryptionMethod & 0xff))
            {
                return oENCRID3V2Frame.getEncryptionData();
            }
        }
        
        return null;
    }

    /** Get all of the frame stored in this tag, in one array.
     *
     * @return all of the frames in this tag in an array
     */
    private ID3V2Frame[] getAllFrames()
    {
        List oFrameList = new ArrayList();
        
        oFrameList.addAll(m_oFrameIdToFrameMap.values());
        oFrameList.addAll(m_oAENCOwnerIdentifierToFrameMap.values());
        oFrameList.addAll(m_oAPICDescriptionToFrameMap.values());
        oFrameList.addAll(m_oCOMMLanguageAndContentDescriptorToFrameMap.values());
        oFrameList.addAll(m_oENCRMethodToFrameMap.values());
        oFrameList.addAll(m_oGEOBContentDescriptorToFrameMap.values());
        oFrameList.addAll(m_oGRIDGroupSymbolToFrameMap.values());
        oFrameList.addAll(m_oLINKContentsToFrameMap.values());
        oFrameList.addAll(m_oPRIVContentsToFrameMap.values());
        oFrameList.addAll(m_oPOPMEmailToFrameMap.values());
        oFrameList.addAll(m_oSYLTLanguageAndContentDescriptorToFrameMap.values());
        oFrameList.addAll(m_oTXXXDescriptionToFrameMap.values());
        oFrameList.addAll(m_oUFIDOwnerIdentifierToFrameMap.values());
        oFrameList.addAll(m_oUSLTLanguageAndContentDescriptorToFrameMap.values());
        oFrameList.addAll(m_oWCOMUrlToFrameMap.values());
        oFrameList.addAll(m_oWOARUrlToFrameMap.values());
        oFrameList.addAll(m_oWXXXDescriptionToFrameMap.values());
        
        return (ID3V2Frame[])oFrameList.toArray(new ID3V2Frame[0]);
    }

    /** Get all encrypted frames in this tag.  These are frames which could not be decrypted.
     *
     * @return an array of encrypted frames (possibly zero length)
     */
    public EncryptedID3V2Frame[] getEncryptedFrames()
    {
        return (EncryptedID3V2Frame[])m_oEncryptedFrameList.toArray(new EncryptedID3V2Frame[0]);
    }

    /** Get all unknown frames in this tag.  These are all frames which are not defined in the ID3 v2.3.0 spec.
     *
     * @return an array of unknown frames (possibly zero length)
     */
    public UnknownID3V2Frame[] getUnknownFrames()
    {
        return (UnknownID3V2Frame[])m_oUnknownFrameList.toArray(new UnknownID3V2Frame[0]);
    }
    
    /** Add an unknown frame to this tag.  This method is for frames which are not defined in the ID3 v2.3.0 spec.
     *
     * @param oUnknownID3V2Frame the unknown frame to add to the tag
     */
    public void addUnknownFrame(UnknownID3V2Frame oUnknownID3V2Frame)
        throws ID3Exception
    {
        if (ID3Tag.usingStrict())
        {
            throw new ID3Exception("Cannot add unknown frames to tag when strict mode is enabled.");
        }
        
        m_oUnknownFrameList.add(oUnknownID3V2Frame);
    }

    /** Add an audio encryption frame to this tag.  Multiple AENC frames can be added to a single tag, but each
     *  must have a unique owner identifier.
     *
     * @param oAENCID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an AENC frame with the same owner identifier
     */
    public void addAENCFrame(AENCID3V2Frame oAENCID3V2Frame)
        throws ID3Exception
    {
        if (oAENCID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null AENC frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oAENCOwnerIdentifierToFrameMap.containsKey(oAENCID3V2Frame.getOwnerIdentifier())))
        {
            throw new ID3Exception("Tag already contains AENC frame with matching owner identifier.");
        }
        m_oAENCOwnerIdentifierToFrameMap.put(oAENCID3V2Frame.getOwnerIdentifier(), oAENCID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oAENCID3V2Frame.addID3Observer(this);
        oAENCID3V2Frame.notifyID3Observers();
    }

    /** Get all AENC frames stored in this tag.
     *
     * @return an array of all AENC frames in this tag (zero-length array returned if there are none)
     */
    public AENCID3V2Frame[] getAENCFrames()
    {
        return (AENCID3V2Frame[])m_oAENCOwnerIdentifierToFrameMap.values().toArray(new AENCID3V2Frame[0]);
    }

    /** Remove a specific AENC frame from this tag.
     *
     * @param sOwnerIdentifier the owner identifier which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public AENCID3V2Frame removeAENCFrame(String sOwnerIdentifier)
    {
        if (sOwnerIdentifier == null)
        {
            throw new NullPointerException("Owner identifier is null.");
        }
        
        AENCID3V2Frame oAENC = (AENCID3V2Frame)m_oAENCOwnerIdentifierToFrameMap.remove(sOwnerIdentifier);
        
        if (oAENC != null)
        {
            oAENC.removeID3Observer(this);
        }

        return oAENC;
    }
    
    /** Add an attached picture frame to this tag.  Multiple APIC frames can be added to a single tag, but each
     *  must have a unique description.
     *
     * @param oAPICID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an APIC frame with the same description
     */
    public void addAPICFrame(APICID3V2Frame oAPICID3V2Frame)
        throws ID3Exception
    {
        if (oAPICID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null APIC frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oAPICDescriptionToFrameMap.containsKey(oAPICID3V2Frame.getDescription())))
        {
            throw new ID3Exception("Tag already contains APIC frame with matching description.");
        }
        m_oAPICDescriptionToFrameMap.put(oAPICID3V2Frame.getDescription(), oAPICID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oAPICID3V2Frame.addID3Observer(this);
        oAPICID3V2Frame.notifyID3Observers();
    }
    
    /** Get all APIC frames stored in this tag.
     *
     * @return an array of all APIC frames in this tag (zero-length array returned if there are none)
     */
    public APICID3V2Frame[] getAPICFrames()
    {
        return (APICID3V2Frame[])m_oAPICDescriptionToFrameMap.values().toArray(new APICID3V2Frame[0]);
    }
    
    /** Remove a specific APIC frame from this tag.
     *
     * @param sDescription the description which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public APICID3V2Frame removeAPICFrame(String sDescription)
    {
        if (sDescription == null)
        {
            throw new NullPointerException("Description is null.");
        }

        APICID3V2Frame oAPIC = (APICID3V2Frame)m_oAPICDescriptionToFrameMap.remove(sDescription);
        
        if (oAPIC != null)
        {
            oAPIC.removeID3Observer(this);
        }

        return oAPIC;
    }
    
    /** Add a comment frame to this tag.  Multiple COMM frames can be added to a single tag, but each
     *  must have a unique language and content descriptor.
     *
     * @param oCOMMID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an COMM frame with the same language and content descriptor
     */
    public void addCOMMFrame(COMMID3V2Frame oCOMMID3V2Frame)
        throws ID3Exception
    {
        if (oCOMMID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null COMM frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oCOMMLanguageAndContentDescriptorToFrameMap.containsKey(oCOMMID3V2Frame.getLanguage() + oCOMMID3V2Frame.getShortDescription())))
        {
            throw new ID3Exception("Tag already contains COMM frame with matching language and short description.");
        }
        m_oCOMMLanguageAndContentDescriptorToFrameMap.put(oCOMMID3V2Frame.getLanguage() + oCOMMID3V2Frame.getShortDescription(), oCOMMID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oCOMMID3V2Frame.addID3Observer(this);
        oCOMMID3V2Frame.notifyID3Observers();
    }
    
    /** Get all COMM frames stored in this tag.
     *
     * @return an array of all COMM frames in this tag (zero-length array returned if there are none)
     */
    public COMMID3V2Frame[] getCOMMFrames()
    {
        return (COMMID3V2Frame[])m_oCOMMLanguageAndContentDescriptorToFrameMap.values().toArray(new COMMID3V2Frame[0]);
    }
    
    /** Remove a specific COMM frame from this tag.
     *
     * @param sLanguage the language which jointly uniquely identifies the frame to be removed
     * @param sShortDescription the short description which jointly uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public COMMID3V2Frame removeCOMMFrame(String sLanguage, String sShortDescription)
    {
        if (sLanguage == null)
        {
            throw new NullPointerException("Language is null.");
        }
        if (sShortDescription == null)
        {
            sShortDescription = "";
        }
        
        COMMID3V2Frame oCOMM = (COMMID3V2Frame)m_oCOMMLanguageAndContentDescriptorToFrameMap.remove(sLanguage + sShortDescription);
        
        if (oCOMM != null)
        {
            oCOMM.removeID3Observer(this);
        }

        return oCOMM;
    }
    
    /** Set a commercial frame in this tag.  Only a single COMR frame can be set in a tag.
     *
     * @param oCOMRID3V2Frame the frame to be set
     */
    public COMRID3V2Frame setCOMRFrame(COMRID3V2Frame oCOMRID3V2Frame)
    {
        if (oCOMRID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null COMR frame in tag.");
        }

        COMRID3V2Frame oCOMR = (COMRID3V2Frame)m_oFrameIdToFrameMap.put("COMR", oCOMRID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oCOMRID3V2Frame.addID3Observer(this);
        try
        {
            oCOMRID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oCOMR != null)
        {
            oCOMR.removeID3Observer(this);
        }
        
        return oCOMR;
    }

    /** Get the COMR frame set in this tag.
     *
     * @return the COMR frame set in this tag, or null if none was set
     */
    public COMRID3V2Frame getCOMRFrame()
    {
        return (COMRID3V2Frame)m_oFrameIdToFrameMap.get("COMR");
    }

    /** Remove the COMR frame which was set in this tag.
     *
     * @return the previously set COMR frame, or null if it was never set
     */
    public COMRID3V2Frame removeCOMRFrame()
    {
        COMRID3V2Frame oCOMR = (COMRID3V2Frame)m_oFrameIdToFrameMap.remove("COMR");
        
        if (oCOMR != null)
        {
            oCOMR.removeID3Observer(this);
        }
        
        return oCOMR;
    }
    
    /** Add an encryption frame to this tag.  Multiple ENCR frames can be added to a single tag, but each
     *  must have a unique encryption method symbol.
     *
     * @param oENCRID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an ENCR frame with the same encryption method symbol
     */
    public void addENCRFrame(ENCRID3V2Frame oENCRID3V2Frame)
        throws ID3Exception
    {
        if (oENCRID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null ENCR frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oENCRMethodToFrameMap.containsKey(new Byte(oENCRID3V2Frame.getEncryptionMethodSymbol()))))
        {
            throw new ID3Exception("Tag already contains ENCR frame with matching method symbol.");
        }
        Iterator oIter = m_oENCRMethodToFrameMap.values().iterator();
        while (oIter.hasNext())
        {
            ENCRID3V2Frame oENCR = (ENCRID3V2Frame)oIter.next();
            if (oENCRID3V2Frame.getOwnerIdentifier().equals(oENCR.getOwnerIdentifier()))
            {
                throw new ID3Exception("Tag already contains ENCR frame with matching owner identifier.");
            }
        }
        
        m_oENCRMethodToFrameMap.put(new Byte(oENCRID3V2Frame.getEncryptionMethodSymbol()), oENCRID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oENCRID3V2Frame.addID3Observer(this);
        oENCRID3V2Frame.notifyID3Observers();
    }
    
    /** Get all ENCR frames stored in this tag.
     *
     * @return an array of all ENCR frames in this tag (zero-length array returned if there are none)
     */
    public ENCRID3V2Frame[] getENCRFrames()
    {
        return (ENCRID3V2Frame[])m_oENCRMethodToFrameMap.values().toArray(new ENCRID3V2Frame[0]);
    }
    
    /** Remove a specific ENCR frame from this tag.
     *
     * @param byEncryptionMethodSymbol the encryption method symbol which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public ENCRID3V2Frame removeENCRFrame(byte byEncryptionMethodSymbol)
    {
        ENCRID3V2Frame oENCR = (ENCRID3V2Frame)m_oENCRMethodToFrameMap.remove(new Byte(byEncryptionMethodSymbol));
        
        if (oENCR != null)
        {
            oENCR.removeID3Observer(this);
        }

        return oENCR;
    }

    /** Set a equalization frame in this tag.  Only a single EQUA frame can be set in a tag.
     *
     * @param oEQUAID3V2Frame the frame to be set
     */
    public EQUAID3V2Frame setEQUAFrame(EQUAID3V2Frame oEQUAID3V2Frame)
    {
        if (oEQUAID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null EQUA frame in tag.");
        }

        EQUAID3V2Frame oEQUA = (EQUAID3V2Frame)m_oFrameIdToFrameMap.put("EQUA", oEQUAID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oEQUAID3V2Frame.addID3Observer(this);
        try
        {
            oEQUAID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oEQUA != null)
        {
            oEQUA.removeID3Observer(this);
        }
        
        return oEQUA;
    }
    
    /** Get the EQUA frame set in this tag.
     *
     * @return the EQUA frame set in this tag, or null if none was set
     */
    public EQUAID3V2Frame getEQUAFrame()
    {
        return (EQUAID3V2Frame)m_oFrameIdToFrameMap.get("EQUA");
    }
    
    /** Remove the EQUA frame which was set in this tag.
     *
     * @return the previously set EQUA frame, or null if it was never set
     */
    public EQUAID3V2Frame removeEQUAFrame()
    {
        EQUAID3V2Frame oEQUA = (EQUAID3V2Frame)m_oFrameIdToFrameMap.remove("EQUA");
        
        if (oEQUA != null)
        {
            oEQUA.removeID3Observer(this);
        }
        
        return oEQUA;
    }

    /** Set a event timing codes frame in this tag.  Only a single ETCO frame can be set in a tag.
     *
     * @param oETCOID3V2Frame the frame to be set
     */
    public ETCOID3V2Frame setETCOFrame(ETCOID3V2Frame oETCOID3V2Frame)
    {
        if (oETCOID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null ETCO frame in tag.");
        }

        ETCOID3V2Frame oETCO = (ETCOID3V2Frame)m_oFrameIdToFrameMap.put("ETCO", oETCOID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oETCOID3V2Frame.addID3Observer(this);
        try
        {
            oETCOID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oETCO != null)
        {
            oETCO.removeID3Observer(this);
        }
        
        return oETCO;
    }
    
    /** Get the ETCO frame set in this tag.
     *
     * @return the ETCO frame set in this tag, or null if none was set
     */
    public ETCOID3V2Frame getETCOFrame()
    {
        return (ETCOID3V2Frame)m_oFrameIdToFrameMap.get("ETCO");
    }
    
    /** Remove the ETCO frame which was set in this tag.
     *
     * @return the previously set ETCO frame, or null if it was never set
     */
    public ETCOID3V2Frame removeETCOFrame()
    {
        ETCOID3V2Frame oETCO = (ETCOID3V2Frame)m_oFrameIdToFrameMap.remove("ETCO");
        
        if (oETCO != null)
        {
            oETCO.removeID3Observer(this);
        }
        
        return oETCO;
    }

    /** Add a general encapsulated object frame to this tag.  Multiple GEOB frames can be added to a single tag, but each
     *  must have a unique content descriptor.
     *
     * @param oGEOBID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an GEOB frame with the same content descriptor
     */
    public void addGEOBFrame(GEOBID3V2Frame oGEOBID3V2Frame)
        throws ID3Exception
    {
        if (oGEOBID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null GEOB frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oGEOBContentDescriptorToFrameMap.containsKey(oGEOBID3V2Frame.getContentDescription())))
        {
            throw new ID3Exception("Tag already contains GEOB frame with matching content descriptor.");
        }
        m_oGEOBContentDescriptorToFrameMap.put(oGEOBID3V2Frame.getContentDescription(), oGEOBID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oGEOBID3V2Frame.addID3Observer(this);
        oGEOBID3V2Frame.notifyID3Observers();
    }
    
    /** Get all GEOB frames stored in this tag.
     *
     * @return an array of all GEOB frames in this tag (zero-length array returned if there are none)
     */
    public GEOBID3V2Frame[] getGEOBFrames()
    {
        return (GEOBID3V2Frame[])m_oGEOBContentDescriptorToFrameMap.values().toArray(new GEOBID3V2Frame[0]);
    }
    
    /** Remove a specific GEOB frame from this tag.
     *
     * @param sContentDescriptor the content descriptor which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public GEOBID3V2Frame removeGEOBFrame(String sContentDescriptor)
    {
        if (sContentDescriptor == null)
        {
            throw new NullPointerException("Content descriptor is null.");
        }
        
        GEOBID3V2Frame oGEOB = (GEOBID3V2Frame)m_oGEOBContentDescriptorToFrameMap.remove(sContentDescriptor);
        
        if (oGEOB != null)
        {
            oGEOB.removeID3Observer(this);
        }

        return oGEOB;
    }

    /** Add a group identification registration frame to this tag.  Multiple GRID frames can be added to a single tag, but each
     *  must have a unique group symbol.
     *
     * @param oGRIDID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an GEOB frame with the same group symbol
     */
    public void addGRIDFrame(GRIDID3V2Frame oGRIDID3V2Frame)
        throws ID3Exception
    {
        if (oGRIDID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null GRID frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oGRIDGroupSymbolToFrameMap.containsKey(new Byte(oGRIDID3V2Frame.getGroupSymbol()))))
        {
            throw new ID3Exception("Tag already contains GRID frame with matching group symbol.");
        }
        m_oGRIDGroupSymbolToFrameMap.put(new Byte(oGRIDID3V2Frame.getGroupSymbol()), oGRIDID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oGRIDID3V2Frame.addID3Observer(this);
        oGRIDID3V2Frame.notifyID3Observers();
    }
    
    /** Get all GRID frames stored in this tag.
     *
     * @return an array of all GRID frames in this tag (zero-length array returned if there are none)
     */
    public GRIDID3V2Frame[] getGRIDFrames()
    {
        return (GRIDID3V2Frame[])m_oGRIDGroupSymbolToFrameMap.values().toArray(new GRIDID3V2Frame[0]);
    }
    
    /** Remove a specific GRID frame from this tag.
     *
     * @param byGroupSymbol the group symbol which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public GRIDID3V2Frame removeGRIDFrame(byte byGroupSymbol)
    {
        GRIDID3V2Frame oGRID = (GRIDID3V2Frame)m_oGRIDGroupSymbolToFrameMap.remove(new Byte(byGroupSymbol));
        
        if (oGRID != null)
        {
            oGRID.removeID3Observer(this);
        }

        return oGRID;
    }

    /** Set an involved people list frame in this tag.  Only a single IPLS frame can be set in a tag.
     *
     * @param oIPLSID3V2Frame the frame to be set
     */
    public IPLSID3V2Frame setIPLSFrame(IPLSID3V2Frame oIPLSID3V2Frame)
    {
        if (oIPLSID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null IPLS frame in tag.");
        }

        IPLSID3V2Frame oIPLS = (IPLSID3V2Frame)m_oFrameIdToFrameMap.put("IPLS", oIPLSID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oIPLSID3V2Frame.addID3Observer(this);
        try
        {
            oIPLSID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oIPLS != null)
        {
            oIPLS.removeID3Observer(this);
        }
        
        return oIPLS;
    }
    
    /** Get the IPLS frame set in this tag.
     *
     * @return the IPLS frame set in this tag, or null if none was set
     */
    public IPLSID3V2Frame getIPLSFrame()
    {
        return (IPLSID3V2Frame)m_oFrameIdToFrameMap.get("IPLS");
    }
    
    /** Remove the IPLS frame which was set in this tag.
     *
     * @return the previously set IPLS frame, or null if it was never set
     */
    public IPLSID3V2Frame removeIPLSFrame()
    {
        IPLSID3V2Frame oIPLS = (IPLSID3V2Frame)m_oFrameIdToFrameMap.remove("IPLS");
        
        if (oIPLS != null)
        {
            oIPLS.removeID3Observer(this);
        }
        
        return oIPLS;
    }

    /** Add a linked information frame to this tag.  Multiple LINK frames can be added to a single tag, but each
     *  must have unique contents.
     *
     * @param oLINKID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an LINK frame with the same contents
     */
    public void addLINKFrame(LINKID3V2Frame oLINKID3V2Frame)
        throws ID3Exception
    {
        if (oLINKID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null LINK frame to tag.");
        }
        String sContents = new String(oLINKID3V2Frame.getFrameIdentifier()) + oLINKID3V2Frame.getLinkUrl() + oLINKID3V2Frame.getAdditionalData();
        if (ID3Tag.usingStrict() && (m_oLINKContentsToFrameMap.containsKey(sContents)))
        {
            throw new ID3Exception("Tag already contains LINK frame with matching contents.");
        }
        m_oLINKContentsToFrameMap.put(sContents, oLINKID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oLINKID3V2Frame.addID3Observer(this);
        oLINKID3V2Frame.notifyID3Observers();
    }
    
    /** Get all LINK frames stored in this tag.
     *
     * @return an array of all LINK frames in this tag (zero-length array returned if there are none)
     */
    public LINKID3V2Frame[] getLINKFrames()
    {
        return (LINKID3V2Frame[])m_oLINKContentsToFrameMap.values().toArray(new LINKID3V2Frame[0]);
    }
    
    /** Remove a specific LINK frame from this tag.
     *
     * @param abyFrameIdentifier the frame identifier which joinly identifies the frame to be removed
     * @param sLinkUrl the link URL which jointly identifies the frame to be removed
     * @param sAdditionalData the additional data which jointly identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public LINKID3V2Frame removeLINKFrame(byte []abyFrameIdentifier, String sLinkUrl, String sAdditionalData)
    {
        String sContents = new String(abyFrameIdentifier) + sLinkUrl + sAdditionalData;
        
        LINKID3V2Frame oLINK = (LINKID3V2Frame)m_oLINKContentsToFrameMap.remove(sContents);
        
        if (oLINK != null)
        {
            oLINK.removeID3Observer(this);
        }

        return oLINK;
    }

    /** Set a music CD identifier frame in this tag.  Only a single MCDI frame can be set in a tag.
     *
     * @param oMCDIID3V2Frame the frame to be set
     */
    public MCDIID3V2Frame setMCDIFrame(MCDIID3V2Frame oMCDIID3V2Frame)
    {
        if (oMCDIID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null MCDI frame in tag.");
        }
        
        MCDIID3V2Frame oMCDI = (MCDIID3V2Frame)m_oFrameIdToFrameMap.put("MCDI", oMCDIID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oMCDIID3V2Frame.addID3Observer(this);
        try
        {
            oMCDIID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oMCDI != null)
        {
            oMCDI.removeID3Observer(this);
        }
        
        return oMCDI;
    }
    
    /** Get the MCDI frame set in this tag.
     *
     * @return the MCDI frame set in this tag, or null if none was set
     */
    public MCDIID3V2Frame getMCDIFrame()
    {
        return (MCDIID3V2Frame)m_oFrameIdToFrameMap.get("MCDI");
    }
    
    /** Remove the MCDI frame which was set in this tag.
     *
     * @return the previously set MCDI frame, or null if it was never set
     */
    public MCDIID3V2Frame removeMCDIFrame()
    {
        MCDIID3V2Frame oMCDI = (MCDIID3V2Frame)m_oFrameIdToFrameMap.remove("MCDI");
        
        if (oMCDI != null)
        {
            oMCDI.removeID3Observer(this);
        }
        
        return oMCDI;
    }

    /** Set an MPEG location lookup frame in this tag.  Only a single MLLT frame can be set in a tag.
     *
     * @param oMLLTID3V2Frame the frame to be set
     */
    public MLLTID3V2Frame setMLLTFrame(MLLTID3V2Frame oMLLTID3V2Frame)
    {
        if (oMLLTID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null MLLT frame in tag.");
        }

        MLLTID3V2Frame oMLLT = (MLLTID3V2Frame)m_oFrameIdToFrameMap.put("MLLT", oMLLTID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oMLLTID3V2Frame.addID3Observer(this);
        try
        {
            oMLLTID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oMLLT != null)
        {
            oMLLT.removeID3Observer(this);
        }
        
        return oMLLT;
    }
    
    /** Get the MLLT frame set in this tag.
     *
     * @return the MLLT frame set in this tag, or null if none was set
     */
    public MLLTID3V2Frame getMLLTFrame()
    {
        return (MLLTID3V2Frame)m_oFrameIdToFrameMap.get("MLLT");
    }
    
    /** Remove the MLLT frame which was set in this tag.
     *
     * @return the previously set MLLT frame, or null if it was never set
     */
    public MLLTID3V2Frame removeMLLTFrame()
    {
        MLLTID3V2Frame oMLLT = (MLLTID3V2Frame)m_oFrameIdToFrameMap.remove("MLLT");
        
        if (oMLLT != null)
        {
            oMLLT.removeID3Observer(this);
        }
        
        return oMLLT;
    }

    /** Set an ownership frame in this tag.  Only a single OWNE frame can be set in a tag.
     *
     * @param oOWNEID3V2Frame the frame to be set
     */
    public OWNEID3V2Frame setOWNEFrame(OWNEID3V2Frame oOWNEID3V2Frame)
    {
        if (oOWNEID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null OWNE frame in tag.");
        }
        
        OWNEID3V2Frame oOWNE = (OWNEID3V2Frame)m_oFrameIdToFrameMap.put("OWNE", oOWNEID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oOWNEID3V2Frame.addID3Observer(this);
        try
        {
            oOWNEID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oOWNE != null)
        {
            oOWNE.removeID3Observer(this);
        }
        
        return oOWNE;
    }
    
    /** Get the OWNE frame set in this tag.
     *
     * @return the OWNE frame set in this tag, or null if none was set
     */
    public OWNEID3V2Frame getOWNEFrame()
    {
        return (OWNEID3V2Frame)m_oFrameIdToFrameMap.get("OWNE");
    }
    
    /** Remove the OWNE frame which was set in this tag.
     *
     * @return the previously set OWNE frame, or null if it was never set
     */
    public OWNEID3V2Frame removeOWNEFrame()
    {
        OWNEID3V2Frame oOWNE = (OWNEID3V2Frame)m_oFrameIdToFrameMap.remove("OWNE");
        
        if (oOWNE != null)
        {
            oOWNE.removeID3Observer(this);
        }
        
        return oOWNE;
    }
    
    /** Add a private frame to this tag.  Multiple PRIV frames can be added to a single tag, but each
     *  must have unique contents.
     *
     * @param oPRIVID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an PRIV frame with the same contents
     */
    public void addPRIVFrame(PRIVID3V2Frame oPRIVID3V2Frame)
        throws ID3Exception
    {
        if (oPRIVID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null PRIV frame to tag.");
        }
        // get an MD5 hash of the private data, which we'll use as part of the key in our frame map
        String sHash = null;
        try
        {
            MessageDigest oMD = MessageDigest.getInstance("MD5");
            oMD.update(oPRIVID3V2Frame.getPrivateData());
            byte[] abyDigest = oMD.digest();
            sHash = ID3Util.convertBytesToHexString(abyDigest, false);
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error hashing private data in PRIV frame.", e);
        }
        
        String sContents = oPRIVID3V2Frame.getOwnerIdentifier() + sHash;
        if (ID3Tag.usingStrict() && (m_oPRIVContentsToFrameMap.containsKey(sContents)))
        {
            throw new ID3Exception("Tag already contains PRIV frame with matching contents.");
        }
        m_oPRIVContentsToFrameMap.put(sContents, oPRIVID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oPRIVID3V2Frame.addID3Observer(this);
        oPRIVID3V2Frame.notifyID3Observers();
    }
    
    /** Get all PRIV frames stored in this tag.
     *
     * @return an array of all PRIV frames in this tag (zero-length array returned if there are none)
     */
    public PRIVID3V2Frame[] getPRIVFrames()
    {
        return (PRIVID3V2Frame[])m_oPRIVContentsToFrameMap.values().toArray(new PRIVID3V2Frame[0]);
    }
    
    /** Remove a specific PRIV frame from this tag.
     *
     * @param sOwnerIdentifier the owner identifier which joinly identifies the frame to be removed
     * @param abyPrivateData the private data which jointly identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public PRIVID3V2Frame removePRIVFrame(String sOwnerIdentifier, byte[] abyPrivateData)
        throws ID3Exception
    {
        // get an MD5 hash of the private data, which we'll use as part of the key in our frame map
        String sHash = null;
        try
        {
            MessageDigest oMD = MessageDigest.getInstance("MD5");
            oMD.update(abyPrivateData);
            byte[] abyDigest = oMD.digest();
            sHash = ID3Util.convertBytesToHexString(abyDigest, false);
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error hashing private data.", e);
        }
        String sContents = sOwnerIdentifier + sHash;
        
        PRIVID3V2Frame oPRIV = (PRIVID3V2Frame)m_oPRIVContentsToFrameMap.remove(sContents);
        
        if (oPRIV != null)
        {
            oPRIV.removeID3Observer(this);
        }

        return oPRIV;
    }

    /** Set a play counter frame in this tag.  Only a single PCNT frame can be set in a tag.
     *
     * @param oPCNTID3V2Frame the frame to be set
     */
    public PCNTID3V2Frame setPCNTFrame(PCNTID3V2Frame oPCNTID3V2Frame)
    {
        if (oPCNTID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null PCNT frame in tag.");
        }
        PCNTID3V2Frame oPCNT = (PCNTID3V2Frame)m_oFrameIdToFrameMap.put("PCNT", oPCNTID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oPCNTID3V2Frame.addID3Observer(this);
        try
        {
            oPCNTID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oPCNT != null)
        {
            oPCNT.removeID3Observer(this);
        }
        
        return oPCNT;
    }
    
    /** Get the PCNT frame set in this tag.
     *
     * @return the PCNT frame set in this tag, or null if none was set
     */
    public PCNTID3V2Frame getPCNTFrame()
    {
        return (PCNTID3V2Frame)m_oFrameIdToFrameMap.get("PCNT");
    }
    
    /** Remove the PCNT frame which was set in this tag.
     *
     * @return the previously set PCNT frame, or null if it was never set
     */
    public PCNTID3V2Frame removePCNTFrame()
    {
        PCNTID3V2Frame oPCNT = (PCNTID3V2Frame)m_oFrameIdToFrameMap.remove("PCNT");
        
        if (oPCNT != null)
        {
            oPCNT.removeID3Observer(this);
        }
        
        return oPCNT;
    }

    /** Add a popularimeter frame to this tag.  Multiple POPM frames can be added to a single tag, but each
     *  must have a unique email address.
     *
     * @param oPOPMID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an POPM frame with the same email address
     */
    public void addPOPMFrame(POPMID3V2Frame oPOPMID3V2Frame)
        throws ID3Exception
    {
        if (oPOPMID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null POPM frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oPOPMEmailToFrameMap.containsKey(oPOPMID3V2Frame.getEmailToUser())))
        {
            throw new ID3Exception("Tag already contains POPM frame with matching email address.");
        }
        m_oPOPMEmailToFrameMap.put(oPOPMID3V2Frame.getEmailToUser(), oPOPMID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oPOPMID3V2Frame.addID3Observer(this);
        oPOPMID3V2Frame.notifyID3Observers();
    }
    
    /** Get all POPM frames stored in this tag.
     *
     * @return an array of all POPM frames in this tag (zero-length array returned if there are none)
     */
    public POPMID3V2Frame[] getPOPMFrames()
    {
        return (POPMID3V2Frame[])m_oPOPMEmailToFrameMap.values().toArray(new POPMID3V2Frame[0]);
    }
    
    /** Remove a specific POPM frame from this tag.
     *
     * @param sEmailToUser the email address which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public POPMID3V2Frame removePOPMFrame(String sEmailToUser)
    {
        POPMID3V2Frame oPOPM = (POPMID3V2Frame)m_oPOPMEmailToFrameMap.remove(sEmailToUser);
        
        if (oPOPM != null)
        {
            oPOPM.removeID3Observer(this);
        }

        return oPOPM;
    }

    /** Set a position synchronization frame in this tag.  Only a single POSS frame can be set in a tag.
     *
     * @param oPOSSID3V2Frame the frame to be set
     */
    public POSSID3V2Frame setPOSSFrame(POSSID3V2Frame oPOSSID3V2Frame)
    {
        if (oPOSSID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null POSS frame in tag.");
        }
        
        POSSID3V2Frame oPOSS = (POSSID3V2Frame)m_oFrameIdToFrameMap.put("POSS", oPOSSID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oPOSSID3V2Frame.addID3Observer(this);
        try
        {
            oPOSSID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oPOSS != null)
        {
            oPOSS.removeID3Observer(this);
        }
        
        return oPOSS;
    }
    
    /** Get the POSS frame set in this tag.
     *
     * @return the POSS frame set in this tag, or null if none was set
     */
    public POSSID3V2Frame getPOSSFrame()
    {
        return (POSSID3V2Frame)m_oFrameIdToFrameMap.get("POSS");
    }
    
    /** Remove the POSS frame which was set in this tag.
     *
     * @return the previously set POSS frame, or null if it was never set
     */
    public POSSID3V2Frame removePOSSFrame()
    {
        POSSID3V2Frame oPOSS = (POSSID3V2Frame)m_oFrameIdToFrameMap.remove("POSS");
        
        if (oPOSS != null)
        {
            oPOSS.removeID3Observer(this);
        }
        
        return oPOSS;
    }

    /** Set a recommended buffer size frame in this tag.  Only a single RBUF frame can be set in a tag.
     *
     * @param oRBUFID3V2Frame the frame to be set
     */
    public RBUFID3V2Frame setRBUFFrame(RBUFID3V2Frame oRBUFID3V2Frame)
    {
        if (oRBUFID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null RBUF frame in tag.");
        }
        
        RBUFID3V2Frame oRBUF = (RBUFID3V2Frame)m_oFrameIdToFrameMap.put("RBUF", oRBUFID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oRBUFID3V2Frame.addID3Observer(this);
        try
        {
            oRBUFID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oRBUF != null)
        {
            oRBUF.removeID3Observer(this);
        }
        
        return oRBUF;
    }
    
    /** Get the RBUF frame set in this tag.
     *
     * @return the RBUF frame set in this tag, or null if none was set
     */
    public RBUFID3V2Frame getRBUFFrame()
    {
        return (RBUFID3V2Frame)m_oFrameIdToFrameMap.get("RBUF");
    }
    
    /** Remove the RBUF frame which was set in this tag.
     *
     * @return the previously set RBUF frame, or null if it was never set
     */
    public RBUFID3V2Frame removeRBUFFrame()
    {
        RBUFID3V2Frame oRBUF = (RBUFID3V2Frame)m_oFrameIdToFrameMap.remove("RBUF");
        
        if (oRBUF != null)
        {
            oRBUF.removeID3Observer(this);
        }
        
        return oRBUF;
    }

    /** Set a relative volume adjustment frame in this tag.  Only a single RVAD frame can be set in a tag.
     *
     * @param oRVADID3V2Frame the frame to be set
     */
    public RVADID3V2Frame setRVADFrame(RVADID3V2Frame oRVADID3V2Frame)
    {
        if (oRVADID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null RVAD frame in tag.");
        }
        
        RVADID3V2Frame oRVAD = (RVADID3V2Frame)m_oFrameIdToFrameMap.put("RVAD", oRVADID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oRVADID3V2Frame.addID3Observer(this);
        try
        {
            oRVADID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oRVAD != null)
        {
            oRVAD.removeID3Observer(this);
        }
        
        return oRVAD;
    }
    
    /** Get the RVAD frame set in this tag.
     *
     * @return the RVAD frame set in this tag, or null if none was set
     */
    public RVADID3V2Frame getRVADFrame()
    {
        return (RVADID3V2Frame)m_oFrameIdToFrameMap.get("RVAD");
    }
    
    /** Remove the RVAD frame which was set in this tag.
     *
     * @return the previously set RVAD frame, or null if it was never set
     */
    public RVADID3V2Frame removeRVADFrame()
    {
        RVADID3V2Frame oRVAD = (RVADID3V2Frame)m_oFrameIdToFrameMap.remove("RVAD");
        
        if (oRVAD != null)
        {
            oRVAD.removeID3Observer(this);
        }
        
        return oRVAD;
    }

    /** Set a reverb frame in this tag.  Only a single RVRB frame can be set in a tag.
     *
     * @param oRVRBID3V2Frame the frame to be set
     */
    public RVRBID3V2Frame setRVRBFrame(RVRBID3V2Frame oRVRBID3V2Frame)
    {
        if (oRVRBID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null RVRB frame in tag.");
        }
        
        RVRBID3V2Frame oRVRB = (RVRBID3V2Frame)m_oFrameIdToFrameMap.put("RVRB", oRVRBID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oRVRBID3V2Frame.addID3Observer(this);
        try
        {
            oRVRBID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oRVRB != null)
        {
            oRVRB.removeID3Observer(this);
        }
        
        return oRVRB;
    }
    
    /** Get the RVRB frame set in this tag.
     *
     * @return the RVRB frame set in this tag, or null if none was set
     */
    public RVRBID3V2Frame getRVRBFrame()
    {
        return (RVRBID3V2Frame)m_oFrameIdToFrameMap.get("RVRB");
    }
    
    /** Remove the RVRB frame which was set in this tag.
     *
     * @return the previously set RVRB frame, or null if it was never set
     */
    public RVRBID3V2Frame removeRVRBFrame()
    {
        RVRBID3V2Frame oRVRB = (RVRBID3V2Frame)m_oFrameIdToFrameMap.remove("RVRB");
        
        if (oRVRB != null)
        {
            oRVRB.removeID3Observer(this);
        }
        
        return oRVRB;
    }
    
    /** Add a synchronized lyric/text frame to this tag.  Multiple SYLT frames can be added to a single tag, but each
     *  must have a unique language and content descriptor pair.
     *
     * @param oSYLTID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an SYLT frame with the same language and content descriptor
     */
    public void addSYLTFrame(SYLTID3V2Frame oSYLTID3V2Frame)
        throws ID3Exception
    {
        if (oSYLTID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null SYLT frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oSYLTLanguageAndContentDescriptorToFrameMap.containsKey(oSYLTID3V2Frame.getLanguage() + oSYLTID3V2Frame.getContentDescriptor())))
        {
            throw new ID3Exception("Tag already contains SYLT frame with matching language and short description.");
        }
        m_oSYLTLanguageAndContentDescriptorToFrameMap.put(oSYLTID3V2Frame.getLanguage() + oSYLTID3V2Frame.getContentDescriptor(), oSYLTID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oSYLTID3V2Frame.addID3Observer(this);
        oSYLTID3V2Frame.notifyID3Observers();
    }
    
    /** Get all SYLT frames stored in this tag.
     *
     * @return an array of all SYLT frames in this tag (zero-length array returned if there are none)
     */
    public SYLTID3V2Frame[] getSYLTFrames()
    {
        return (SYLTID3V2Frame[])m_oSYLTLanguageAndContentDescriptorToFrameMap.values().toArray(new SYLTID3V2Frame[0]);
    }
    
    /** Remove a specific SYLT frame from this tag.
     *
     * @param sLanguage the language which jointly identifies the frame to be removed
     * @param sShortDescription the content descriptor which jointly identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public SYLTID3V2Frame removeSYLTFrame(String sLanguage, String sShortDescription)
    {
        if (sLanguage == null)
        {
            throw new NullPointerException("Language is null.");
        }
        if (sShortDescription == null)
        {
            throw new NullPointerException("Short description is null.");
        }
        SYLTID3V2Frame oSYLT = (SYLTID3V2Frame)m_oSYLTLanguageAndContentDescriptorToFrameMap.remove(sLanguage + sShortDescription);
        
        if (oSYLT != null)
        {
            oSYLT.removeID3Observer(this);
        }

        return oSYLT;
    }

    /** Set a synchronized tempo codes frame in this tag.  Only a single SYTC frame can be set in a tag.
     *
     * @param oSYTCID3V2Frame the frame to be set
     */
    public SYTCID3V2Frame setSYTCFrame(SYTCID3V2Frame oSYTCID3V2Frame)
    {
        if (oSYTCID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null SYTC frame in tag.");
        }
        
        SYTCID3V2Frame oSYTC = (SYTCID3V2Frame)m_oFrameIdToFrameMap.put("SYTC", oSYTCID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oSYTCID3V2Frame.addID3Observer(this);
        try
        {
            oSYTCID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oSYTC != null)
        {
            oSYTC.removeID3Observer(this);
        }
        
        return oSYTC;
    }
    
    /** Get the SYTC frame set in this tag.
     *
     * @return the SYTC frame set in this tag, or null if none was set
     */
    public SYTCID3V2Frame getSYTCFrame()
    {
        return (SYTCID3V2Frame)m_oFrameIdToFrameMap.get("SYTC");
    }
    
    /** Remove the SYTC frame which was set in this tag.
     *
     * @return the previously set SYTC frame, or null if it was never set
     */
    public SYTCID3V2Frame removeSYTCFrame()
    {
        SYTCID3V2Frame oSYTC = (SYTCID3V2Frame)m_oFrameIdToFrameMap.remove("SYTC");
        
        if (oSYTC != null)
        {
            oSYTC.removeID3Observer(this);
        }
        
        return oSYTC;
    }
    
    /** Set an album/movie/show title frame in this tag.  Only a single TALB frame can be set in a tag.
     *
     * @param oTALBTextInformationID3V2Frame the frame to be set
     */
    public TALBTextInformationID3V2Frame setTALBTextInformationFrame(TALBTextInformationID3V2Frame oTALBTextInformationID3V2Frame)
    {
        if (oTALBTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TALB text information frame in tag.");
        }
        TALBTextInformationID3V2Frame oTALB = (TALBTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TALB", oTALBTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTALBTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTALBTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTALB != null)
        {
            oTALB.removeID3Observer(this);
        }
        
        return oTALB;
    }
    
    /** Get the TALB frame set in this tag.
     *
     * @return the TALB frame set in this tag, or null if none was set
     */
    public TALBTextInformationID3V2Frame getTALBTextInformationFrame()
    {
        return (TALBTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TALB");
    }
    
    /** Remove the TALB frame which was set in this tag.
     *
     * @return the previously set TALB frame, or null if it was never set
     */
    public TALBTextInformationID3V2Frame removeTALBTextInformationFrame()
    {
        TALBTextInformationID3V2Frame oTALB = (TALBTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TALB");
        
        if (oTALB != null)
        {
            oTALB.removeID3Observer(this);
        }
        
        return oTALB;
    }

    /** Set a BPM (beats per minute) frame in this tag.  Only a single TBPM frame can be set in a tag.
     *
     * @param oTBPMTextInformationID3V2Frame the frame to be set
     */
    public TBPMTextInformationID3V2Frame setTBPMTextInformationFrame(TBPMTextInformationID3V2Frame oTBPMTextInformationID3V2Frame)
    {
        if (oTBPMTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TBPM text information frame in tag.");
        }
        
        TBPMTextInformationID3V2Frame oTBPM = (TBPMTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TBPM", oTBPMTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTBPMTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTBPMTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTBPM != null)
        {
            oTBPM.removeID3Observer(this);
        }
        
        return oTBPM;
    }
    
    /** Get the TBPM frame set in this tag.
     *
     * @return the TBPM frame set in this tag, or null if none was set
     */
    public TBPMTextInformationID3V2Frame getTBPMTextInformationFrame()
    {
        return (TBPMTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TBPM");
    }
    
    /** Remove the TBPM frame which was set in this tag.
     *
     * @return the previously set TBPM frame, or null if it was never set
     */
    public TBPMTextInformationID3V2Frame removeTBPMTextInformationFrame()
    {
        TBPMTextInformationID3V2Frame oTBPM = (TBPMTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TBPM");
        
        if (oTBPM != null)
        {
            oTBPM.removeID3Observer(this);
        }
        
        return oTBPM;
    }

    /** Set a composer frame in this tag.  Only a single TCOM frame can be set in a tag.
     *
     * @param oTCOMTextInformationID3V2Frame the frame to be set
     */
    public TCOMTextInformationID3V2Frame setTCOMTextInformationFrame(TCOMTextInformationID3V2Frame oTCOMTextInformationID3V2Frame)
    {
        if (oTCOMTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TCOM text information frame in tag.");
        }
        
        TCOMTextInformationID3V2Frame oTCOM = (TCOMTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TCOM", oTCOMTextInformationID3V2Frame);
        
        if (oTCOM != null)
        {
            oTCOM.removeID3Observer(this);
        }
        
        return oTCOM;
    }
    
    /** Get the TCOM frame set in this tag.
     *
     * @return the TCOM frame set in this tag, or null if none was set
     */
    public TCOMTextInformationID3V2Frame getTCOMTextInformationFrame()
    {
        return (TCOMTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TCOM");
    }
    
    /** Remove the TCOM frame which was set in this tag.
     *
     * @return the previously set TCOM frame, or null if it was never set
     */
    public TCOMTextInformationID3V2Frame removeTCOMTextInformationFrame()
    {
        TCOMTextInformationID3V2Frame oTCOM = (TCOMTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TCOM");
        
        if (oTCOM != null)
        {
            oTCOM.removeID3Observer(this);
        }
        
        return oTCOM;
    }

    /** Set a content type frame in this tag.  Only a single TCON frame can be set in a tag.
     *
     * @param oTCONTextInformationID3V2Frame the frame to be set
     */
    public TCONTextInformationID3V2Frame setTCONTextInformationFrame(TCONTextInformationID3V2Frame oTCONTextInformationID3V2Frame)
    {
        if (oTCONTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TCON text information frame in tag.");
        }
        
        TCONTextInformationID3V2Frame oTCON = (TCONTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TCON", oTCONTextInformationID3V2Frame);
        
        if (oTCON != null)
        {
            oTCON.removeID3Observer(this);
        }
        
        return oTCON;
    }
    
    /** Get the TCON frame set in this tag.
     *
     * @return the TCON frame set in this tag, or null if none was set
     */
    public TCONTextInformationID3V2Frame getTCONTextInformationFrame()
    {
        return (TCONTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TCON");
    }
    
    /** Remove the TCON frame which was set in this tag.
     *
     * @return the previously set TCON frame, or null if it was never set
     */
    public TCONTextInformationID3V2Frame removeTCONTextInformationFrame()
    {
        TCONTextInformationID3V2Frame oTCON = (TCONTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TCON");
        
        if (oTCON != null)
        {
            oTCON.removeID3Observer(this);
        }
        
        return oTCON;
    }

    /** Set a copyright message frame in this tag.  Only a single TCOP frame can be set in a tag.
     *
     * @param oTCOPTextInformationID3V2Frame the frame to be set
     */
    public TCOPTextInformationID3V2Frame setTCOPTextInformationFrame(TCOPTextInformationID3V2Frame oTCOPTextInformationID3V2Frame)
    {
        if (oTCOPTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TCOP text information frame in tag.");
        }

        TCOPTextInformationID3V2Frame oTCOP = (TCOPTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TCOP", oTCOPTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTCOPTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTCOPTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTCOP != null)
        {
            oTCOP.removeID3Observer(this);
        }
        
        return oTCOP;
    }
    
    /** Get the TCOP frame set in this tag.
     *
     * @return the TCOP frame set in this tag, or null if none was set
     */
    public TCOPTextInformationID3V2Frame getTCOPTextInformationFrame()
    {
        return (TCOPTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TCOP");
    }
    
    /** Remove the TCOP frame which was set in this tag.
     *
     * @return the previously set TCOP frame, or null if it was never set
     */
    public TCOPTextInformationID3V2Frame removeTCOPTextInformationFrame()
    {
        TCOPTextInformationID3V2Frame oTCOP = (TCOPTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TCOP");
        
        if (oTCOP != null)
        {
            oTCOP.removeID3Observer(this);
        }
        
        return oTCOP;
    }

    /** Set a date frame in this tag.  Only a single TDAT frame can be set in a tag.
     *
     * @param oTDATTextInformationID3V2Frame the frame to be set
     */
    public TDATTextInformationID3V2Frame setTDATTextInformationFrame(TDATTextInformationID3V2Frame oTDATTextInformationID3V2Frame)
    {
        if (oTDATTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TDAT text information frame in tag.");
        }
        
        TDATTextInformationID3V2Frame oTDAT = (TDATTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TDAT", oTDATTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTDATTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTDATTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTDAT != null)
        {
            oTDAT.removeID3Observer(this);
        }
        
        return oTDAT;
    }
    
    /** Get the TDAT frame set in this tag.
     *
     * @return the TDAT frame set in this tag, or null if none was set
     */
    public TDATTextInformationID3V2Frame getTDATTextInformationFrame()
    {
        return (TDATTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TDAT");
    }
    
    /** Remove the TDAT frame which was set in this tag.
     *
     * @return the previously set TDAT frame, or null if it was never set
     */
    public TDATTextInformationID3V2Frame removeTDATTextInformationFrame()
    {
        TDATTextInformationID3V2Frame oTDAT = (TDATTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TDAT");
        
        if (oTDAT != null)
        {
            oTDAT.removeID3Observer(this);
        }
        
        return oTDAT;
    }

    /** Set a playlist delay frame in this tag.  Only a single TDLY frame can be set in a tag.
     *
     * @param oTDLYTextInformationID3V2Frame the frame to be set
     */
    public TDLYTextInformationID3V2Frame setTDLYTextInformationFrame(TDLYTextInformationID3V2Frame oTDLYTextInformationID3V2Frame)
    {
        if (oTDLYTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TDLY text information frame in tag.");
        }
        
        TDLYTextInformationID3V2Frame oTDLY = (TDLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TDLY", oTDLYTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTDLYTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTDLYTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTDLY != null)
        {
            oTDLY.removeID3Observer(this);
        }
        
        return oTDLY;
    }
    
    /** Get the TDLY frame set in this tag.
     *
     * @return the TDLY frame set in this tag, or null if none was set
     */
    public TDLYTextInformationID3V2Frame getTDLYTextInformationFrame()
    {
        return (TDLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TDLY");
    }
    
    /** Remove the TDLY frame which was set in this tag.
     *
     * @return the previously set TDLY frame, or null if it was never set
     */
    public TDLYTextInformationID3V2Frame removeTDLYTextInformationFrame()
    {
        TDLYTextInformationID3V2Frame oTDLY = (TDLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TDLY");
        
        if (oTDLY != null)
        {
            oTDLY.removeID3Observer(this);
        }
        
        return oTDLY;
    }

    /** Set an encoded by frame in this tag.  Only a single TENC frame can be set in a tag.
     *
     * @param oTENCTextInformationID3V2Frame the frame to be set
     */
    public TENCTextInformationID3V2Frame setTENCTextInformationFrame(TENCTextInformationID3V2Frame oTENCTextInformationID3V2Frame)
    {
        if (oTENCTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TENC text information frame in tag.");
        }

        TENCTextInformationID3V2Frame oTENC = (TENCTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TENC", oTENCTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTENCTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTENCTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTENC != null)
        {
            oTENC.removeID3Observer(this);
        }
        
        return oTENC;
    }
    
    /** Get the TEND frame set in this tag.
     *
     * @return the TENC frame set in this tag, or null if none was set
     */
    public TENCTextInformationID3V2Frame getTENCTextInformationFrame()
    {
        return (TENCTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TENC");
    }
    
    /** Remove the TENC frame which was set in this tag.
     *
     * @return the previously set TENC frame, or null if it was never set
     */
    public TENCTextInformationID3V2Frame removeTENCTextInformationFrame()
    {
        TENCTextInformationID3V2Frame oTENC = (TENCTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TENC");
        
        if (oTENC != null)
        {
            oTENC.removeID3Observer(this);
        }
        
        return oTENC;
    }

    /** Set a lyricist/text writer frame in this tag.  Only a single TEXT frame can be set in a tag.
     *
     * @param oTEXTTextInformationID3V2Frame the frame to be set
     */
    public TEXTTextInformationID3V2Frame setTEXTTextInformationFrame(TEXTTextInformationID3V2Frame oTEXTTextInformationID3V2Frame)
    {
        if (oTEXTTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TEXT text information frame in tag.");
        }

        TEXTTextInformationID3V2Frame oTEXT = (TEXTTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TEXT", oTEXTTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTEXTTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTEXTTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTEXT != null)
        {
            oTEXT.removeID3Observer(this);
        }
        
        return oTEXT;
    }
    
    /** Get the TEXT frame set in this tag.
     *
     * @return the TEXT frame set in this tag, or null if none was set
     */
    public TEXTTextInformationID3V2Frame getTEXTTextInformationFrame()
    {
        return (TEXTTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TEXT");
    }
    
    /** Remove the TEXT frame which was set in this tag.
     *
     * @return the previously set TEXT frame, or null if it was never set
     */
    public TEXTTextInformationID3V2Frame removeTEXTTextInformationFrame()
    {
        TEXTTextInformationID3V2Frame oTEXT = (TEXTTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TEXT");
        
        if (oTEXT != null)
        {
            oTEXT.removeID3Observer(this);
        }
        
        return oTEXT;
    }

    /** Set a file type frame in this tag.  Only a single TFLT frame can be set in a tag.
     *
     * @param oTFLTTextInformationID3V2Frame the frame to be set
     */
    public TFLTTextInformationID3V2Frame setTFLTTextInformationFrame(TFLTTextInformationID3V2Frame oTFLTTextInformationID3V2Frame)
    {
        if (oTFLTTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TFLT text information frame in tag.");
        }
        
        TFLTTextInformationID3V2Frame oTFLT = (TFLTTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TFLT", oTFLTTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTFLTTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTFLTTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTFLT != null)
        {
            oTFLT.removeID3Observer(this);
        }
        
        return oTFLT;
    }
    
    /** Get the TFLT frame set in this tag.
     *
     * @return the TFLT frame set in this tag, or null if none was set
     */
    public TFLTTextInformationID3V2Frame getTFLTTextInformationFrame()
    {
        return (TFLTTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TFLT");
    }
    
    /** Remove the TFLT frame which was set in this tag.
     *
     * @return the previously set TFLT frame, or null if it was never set
     */
    public TFLTTextInformationID3V2Frame removeTFLTTextInformationFrame()
    {
        TFLTTextInformationID3V2Frame oTFLT = (TFLTTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TFLT");
        
        if (oTFLT != null)
        {
            oTFLT.removeID3Observer(this);
        }
        
        return oTFLT;
    }

    /** Set a time frame in this tag.  Only a single TIME frame can be set in a tag.
     *
     * @param oTIMETextInformationID3V2Frame the frame to be set
     */
    public TIMETextInformationID3V2Frame setTIMETextInformationFrame(TIMETextInformationID3V2Frame oTIMETextInformationID3V2Frame)
    {
        if (oTIMETextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TIME text information frame in tag.");
        }
        
        TIMETextInformationID3V2Frame oTIME = (TIMETextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TIME", oTIMETextInformationID3V2Frame);
        
        if (oTIME != null)
        {
            oTIME.removeID3Observer(this);
        }
        
        return oTIME;
    }
    
    /** Get the TIME frame set in this tag.
     *
     * @return the TIME frame set in this tag, or null if none was set
     */
    public TIMETextInformationID3V2Frame getTIMETextInformationFrame()
    {
        return (TIMETextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TIME");
    }
    
    /** Remove the TIME frame which was set in this tag.
     *
     * @return the previously set TIME frame, or null if it was never set
     */
    public TIMETextInformationID3V2Frame removeTIMETextInformationFrame()
    {
        TIMETextInformationID3V2Frame oTIME = (TIMETextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TIME");
        
        if (oTIME != null)
        {
            oTIME.removeID3Observer(this);
        }
        
        return oTIME;
    }

    /** Set a content group description frame in this tag.  Only a single TIT1 frame can be set in a tag.
     *
     * @param oTIT1TextInformationID3V2Frame the frame to be set
     */
    public TIT1TextInformationID3V2Frame setTIT1TextInformationFrame(TIT1TextInformationID3V2Frame oTIT1TextInformationID3V2Frame)
    {
        if (oTIT1TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TIT1 text information frame in tag.");
        }
        
        TIT1TextInformationID3V2Frame oTIT1 = (TIT1TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TIT1", oTIT1TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTIT1TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTIT1TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTIT1 != null)
        {
            oTIT1.removeID3Observer(this);
        }
        
        return oTIT1;
    }
    
    /** Get the TIT1 frame set in this tag.
     *
     * @return the TIT1 frame set in this tag, or null if none was set
     */
    public TIT1TextInformationID3V2Frame getTIT1TextInformationFrame()
    {
        return (TIT1TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TIT1");
    }
    
    /** Remove the TIT1 frame which was set in this tag.
     *
     * @return the previously set TIT1 frame, or null if it was never set
     */
    public TIT1TextInformationID3V2Frame removeTIT1TextInformationFrame()
    {
        TIT1TextInformationID3V2Frame oTIT1 = (TIT1TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TIT1");
        
        if (oTIT1 != null)
        {
            oTIT1.removeID3Observer(this);
        }
        
        return oTIT1;
    }

    /** Set a title/songname/content description frame in this tag.  Only a single TIT2 frame can be set in a tag.
     *
     * @param oTIT2TextInformationID3V2Frame the frame to be set
     */
    public TIT2TextInformationID3V2Frame setTIT2TextInformationFrame(TIT2TextInformationID3V2Frame oTIT2TextInformationID3V2Frame)
    {
        if (oTIT2TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TIT2 text information frame in tag.");
        }
        
        TIT2TextInformationID3V2Frame oTIT2 = (TIT2TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TIT2", oTIT2TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTIT2TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTIT2TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTIT2 != null)
        {
            oTIT2.removeID3Observer(this);
        }
        
        return oTIT2;
    }
    
    /** Get the TIT2 frame set in this tag.
     *
     * @return the TIT2 frame set in this tag, or null if none was set
     */
    public TIT2TextInformationID3V2Frame getTIT2TextInformationFrame()
    {
        return (TIT2TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TIT2");
    }
    
    /** Remove the TIT2 frame which was set in this tag.
     *
     * @return the previously set TIT2 frame, or null if it was never set
     */
    public TIT2TextInformationID3V2Frame removeTIT2TextInformationFrame()
    {
        TIT2TextInformationID3V2Frame oTIT2 = (TIT2TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TIT2");
        
        if (oTIT2 != null)
        {
            oTIT2.removeID3Observer(this);
        }
        
        return oTIT2;
    }

    /** Set a subtitle/description refinement frame in this tag.  Only a single TIT3 frame can be set in a tag.
     *
     * @param oTIT3TextInformationID3V2Frame the frame to be set
     */
    public TIT3TextInformationID3V2Frame setTIT3TextInformationFrame(TIT3TextInformationID3V2Frame oTIT3TextInformationID3V2Frame)
    {
        if (oTIT3TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TIT3 text information frame in tag.");
        }
        
        TIT3TextInformationID3V2Frame oTIT3 = (TIT3TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TIT3", oTIT3TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTIT3TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTIT3TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTIT3 != null)
        {
            oTIT3.removeID3Observer(this);
        }
        
        return oTIT3;
    }
    
    /** Get the TIT3 frame set in this tag.
     *
     * @return the TIT3 frame set in this tag, or null if none was set
     */
    public TIT3TextInformationID3V2Frame getTIT3TextInformationFrame()
    {
        return (TIT3TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TIT3");
    }
    
    /** Remove the TIT3 frame which was set in this tag.
     *
     * @return the previously set TIT3 frame, or null if it was never set
     */
    public TIT3TextInformationID3V2Frame removeTIT3TextInformationFrame()
    {
        TIT3TextInformationID3V2Frame oTIT3 = (TIT3TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TIT3");
        
        if (oTIT3 != null)
        {
            oTIT3.removeID3Observer(this);
        }
        
        return oTIT3;
    }

    /** Set an initial key frame in this tag.  Only a single TKEY frame can be set in a tag.
     *
     * @param oTKEYTextInformationID3V2Frame the frame to be set
     */
    public TKEYTextInformationID3V2Frame setTKEYTextInformationFrame(TKEYTextInformationID3V2Frame oTKEYTextInformationID3V2Frame)
    {
        if (oTKEYTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TKEY text information frame in tag.");
        }
        
        TKEYTextInformationID3V2Frame oTKEY = (TKEYTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TKEY", oTKEYTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTKEYTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTKEYTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTKEY != null)
        {
            oTKEY.removeID3Observer(this);
        }
        
        return oTKEY;
    }
    
    /** Get the TKEY frame set in this tag.
     *
     * @return the TKEY frame set in this tag, or null if none was set
     */
    public TKEYTextInformationID3V2Frame getTKEYTextInformationFrame()
    {
        return (TKEYTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TKEY");
    }
    
    /** Remove the TKEY frame which was set in this tag.
     *
     * @return the previously set TKEY frame, or null if it was never set
     */
    public TKEYTextInformationID3V2Frame removeTKEYTextInformationFrame()
    {
        TKEYTextInformationID3V2Frame oTKEY = (TKEYTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TKEY");
        
        if (oTKEY != null)
        {
            oTKEY.removeID3Observer(this);
        }
        
        return oTKEY;
    }

    /** Set a language(s) frame in this tag.  Only a single TLAN frame can be set in a tag.
     *
     * @param oTLANTextInformationID3V2Frame the frame to be set
     */
    public TLANTextInformationID3V2Frame setTLANTextInformationFrame(TLANTextInformationID3V2Frame oTLANTextInformationID3V2Frame)
    {
        if (oTLANTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TLAN text information frame in tag.");
        }
        
        TLANTextInformationID3V2Frame oTLAN = (TLANTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TLAN", oTLANTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTLANTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTLANTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTLAN != null)
        {
            oTLAN.removeID3Observer(this);
        }
        
        return oTLAN;
    }
    
    /** Get the TLAN frame set in this tag.
     *
     * @return the TLAN frame set in this tag, or null if none was set
     */
    public TLANTextInformationID3V2Frame getTLANTextInformationFrame()
    {
        return (TLANTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TLAN");
    }
    
    /** Remove the TLAN frame which was set in this tag.
     *
     * @return the previously set TLAN frame, or null if it was never set
     */
    public TLANTextInformationID3V2Frame removeTLANTextInformationFrame()
    {
        TLANTextInformationID3V2Frame oTLAN = (TLANTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TLAN");
        
        if (oTLAN != null)
        {
            oTLAN.removeID3Observer(this);
        }
        
        return oTLAN;
    }

    /** Set a length frame in this tag.  Only a single TLEN frame can be set in a tag.
     *
     * @param oTLENTextInformationID3V2Frame the frame to be set
     */
    public TLENTextInformationID3V2Frame setTLENTextInformationFrame(TLENTextInformationID3V2Frame oTLENTextInformationID3V2Frame)
    {
        if (oTLENTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TLEN text information frame in tag.");
        }
        
        TLENTextInformationID3V2Frame oTLEN = (TLENTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TLEN", oTLENTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTLENTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTLENTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTLEN != null)
        {
            oTLEN.removeID3Observer(this);
        }
        
        return oTLEN;
    }
    
    /** Get the TLEN frame set in this tag.
     *
     * @return the TLEN frame set in this tag, or null if none was set
     */
    public TLENTextInformationID3V2Frame getTLENTextInformationFrame()
    {
        return (TLENTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TLEN");
    }
    
    /** Remove the TLEN frame which was set in this tag.
     *
     * @return the previously set TLEN frame, or null if it was never set
     */
    public TLENTextInformationID3V2Frame removeTLENTextInformationFrame()
    {
        TLENTextInformationID3V2Frame oTLEN = (TLENTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TLEN");
        
        if (oTLEN != null)
        {
            oTLEN.removeID3Observer(this);
        }
        
        return oTLEN;
    }

    /** Set a media type frame in this tag.  Only a single TMED frame can be set in a tag.
     *
     * @param oTMEDTextInformationID3V2Frame the frame to be set
     */
    public TMEDTextInformationID3V2Frame setTMEDTextInformationFrame(TMEDTextInformationID3V2Frame oTMEDTextInformationID3V2Frame)
    {
        if (oTMEDTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TMED text information frame in tag.");
        }
        
        TMEDTextInformationID3V2Frame oTMED = (TMEDTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TMED", oTMEDTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTMEDTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTMEDTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTMED != null)
        {
            oTMED.removeID3Observer(this);
        }
        
        return oTMED;
    }
    
    /** Get the TMED frame set in this tag.
     *
     * @return the TMED frame set in this tag, or null if none was set
     */
    public TMEDTextInformationID3V2Frame getTMEDTextInformationFrame()
    {
        return (TMEDTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TMED");
    }
    
    /** Remove the TMED frame which was set in this tag.
     *
     * @return the previously set TMED frame, or null if it was never set
     */
    public TMEDTextInformationID3V2Frame removeTMEDTextInformationFrame()
    {
        TMEDTextInformationID3V2Frame oTMED = (TMEDTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TMED");
        
        if (oTMED != null)
        {
            oTMED.removeID3Observer(this);
        }
        
        return oTMED;
    }

    /** Set a original album/movie/show title frame in this tag.  Only a single TOAL frame can be set in a tag.
     *
     * @param oTOALTextInformationID3V2Frame the frame to be set
     */
    public TOALTextInformationID3V2Frame setTOALTextInformationFrame(TOALTextInformationID3V2Frame oTOALTextInformationID3V2Frame)
    {
        if (oTOALTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TOAL text information frame in tag.");
        }
        
        TOALTextInformationID3V2Frame oTOAL = (TOALTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TOAL", oTOALTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTOALTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTOALTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTOAL != null)
        {
            oTOAL.removeID3Observer(this);
        }
        
        return oTOAL;
    }
    
    /** Get the TOAL frame set in this tag.
     *
     * @return the TOAL frame set in this tag, or null if none was set
     */
    public TOALTextInformationID3V2Frame getTOALTextInformationFrame()
    {
        return (TOALTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TOAL");
    }
    
    /** Remove the TOAL frame which was set in this tag.
     *
     * @return the previously set TOAL frame, or null if it was never set
     */
    public TOALTextInformationID3V2Frame removeTOALTextInformationFrame()
    {
        TOALTextInformationID3V2Frame oTOAL = (TOALTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TOAL");
        
        if (oTOAL != null)
        {
            oTOAL.removeID3Observer(this);
        }
        
        return oTOAL;
    }

    /** Set an original filename frame in this tag.  Only a single TOFN frame can be set in a tag.
     *
     * @param oTOFNTextInformationID3V2Frame the frame to be set
     */
    public TOFNTextInformationID3V2Frame setTOFNTextInformationFrame(TOFNTextInformationID3V2Frame oTOFNTextInformationID3V2Frame)
    {
        if (oTOFNTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TOFN text information frame in tag.");
        }
        
        TOFNTextInformationID3V2Frame oTOFN = (TOFNTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TOFN", oTOFNTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTOFNTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTOFNTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTOFN != null)
        {
            oTOFN.removeID3Observer(this);
        }
        
        return oTOFN;
    }
    
    /** Get the TOFN frame set in this tag.
     *
     * @return the TOFN frame set in this tag, or null if none was set
     */
    public TOFNTextInformationID3V2Frame getTOFNTextInformationFrame()
    {
        return (TOFNTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TOFN");
    }
    
    /** Remove the TOFN frame which was set in this tag.
     *
     * @return the previously set TOFN frame, or null if it was never set
     */
    public TOFNTextInformationID3V2Frame removeTOFNTextInformationFrame()
    {
        TOFNTextInformationID3V2Frame oTOFN = (TOFNTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TOFN");
        
        if (oTOFN != null)
        {
            oTOFN.removeID3Observer(this);
        }
        
        return oTOFN;
    }

    /** Set an original lyricist(s)/text writer(s) frame in this tag.  Only a single TOLY frame can be set in a tag.
     *
     * @param oTOLYTextInformationID3V2Frame the frame to be set
     */
    public TOLYTextInformationID3V2Frame setTOLYTextInformationFrame(TOLYTextInformationID3V2Frame oTOLYTextInformationID3V2Frame)
    {
        if (oTOLYTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TOLY text information frame in tag.");
        }
        
        TOLYTextInformationID3V2Frame oTOLY = (TOLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TOLY", oTOLYTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTOLYTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTOLYTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTOLY != null)
        {
            oTOLY.removeID3Observer(this);
        }
        
        return oTOLY;
    }
    
    /** Get the TOLY frame set in this tag.
     *
     * @return the TOLY frame set in this tag, or null if none was set
     */
    public TOLYTextInformationID3V2Frame getTOLYTextInformationFrame()
    {
        return (TOLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TOLY");
    }
    
    /** Remove the TOLY frame which was set in this tag.
     *
     * @return the previously set TOLY frame, or null if it was never set
     */
    public TOLYTextInformationID3V2Frame removeTOLYTextInformationFrame()
    {
        TOLYTextInformationID3V2Frame oTOLY = (TOLYTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TOLY");
        
        if (oTOLY != null)
        {
            oTOLY.removeID3Observer(this);
        }
        
        return oTOLY;
    }

    /** Set a original artist(s)/performer(s) frame in this tag.  Only a single TOPE frame can be set in a tag.
     *
     * @param oTOPETextInformationID3V2Frame the frame to be set
     */
    public TOPETextInformationID3V2Frame setTOPETextInformationFrame(TOPETextInformationID3V2Frame oTOPETextInformationID3V2Frame)
    {
        if (oTOPETextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TOPE text information frame in tag.");
        }
        
        TOPETextInformationID3V2Frame oTOPE = (TOPETextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TOPE", oTOPETextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTOPETextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTOPETextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTOPE != null)
        {
            oTOPE.removeID3Observer(this);
        }
        
        return oTOPE;
    }
    
    /** Get the TOPE frame set in this tag.
     *
     * @return the TOPE frame set in this tag, or null if none was set
     */
    public TOPETextInformationID3V2Frame getTOPETextInformationFrame()
    {
        return (TOPETextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TOPE");
    }
    
    /** Remove the TOPE frame which was set in this tag.
     *
     * @return the previously set TOPE frame, or null if it was never set
     */
    public TOPETextInformationID3V2Frame removeTOPETextInformationFrame()
    {
        TOPETextInformationID3V2Frame oTOPE = (TOPETextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TOPE");
        
        if (oTOPE != null)
        {
            oTOPE.removeID3Observer(this);
        }
        
        return oTOPE;
    }

    /** Set a original release year frame in this tag.  Only a single TORY frame can be set in a tag.
     *
     * @param oTORYTextInformationID3V2Frame the frame to be set
     */
    public TORYTextInformationID3V2Frame setTORYTextInformationFrame(TORYTextInformationID3V2Frame oTORYTextInformationID3V2Frame)
    {
        if (oTORYTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TORY text information frame in tag.");
        }
        
        TORYTextInformationID3V2Frame oTORY = (TORYTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TORY", oTORYTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTORYTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTORYTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTORY != null)
        {
            oTORY.removeID3Observer(this);
        }
        
        return oTORY;
    }
    
    /** Get the TORY frame set in this tag.
     *
     * @return the TORY frame set in this tag, or null if none was set
     */
    public TORYTextInformationID3V2Frame getTORYTextInformationFrame()
    {
        return (TORYTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TORY");
    }
    
    /** Remove the TORY frame which was set in this tag.
     *
     * @return the previously set TORY frame, or null if it was never set
     */
    public TORYTextInformationID3V2Frame removeTORYTextInformationFrame()
    {
        TORYTextInformationID3V2Frame oTORY = (TORYTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TORY");
        
        if (oTORY != null)
        {
            oTORY.removeID3Observer(this);
        }
        
        return oTORY;
    }

    /** Set a file owner/licensee frame in this tag.  Only a single TOWN frame can be set in a tag.
     *
     * @param oTOWNTextInformationID3V2Frame the frame to be set
     */
    public TOWNTextInformationID3V2Frame setTOWNTextInformationFrame(TOWNTextInformationID3V2Frame oTOWNTextInformationID3V2Frame)
    {
        if (oTOWNTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TOWN text information frame in tag.");
        }
        
        TOWNTextInformationID3V2Frame oTOWN = (TOWNTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TOWN", oTOWNTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTOWNTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTOWNTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTOWN != null)
        {
            oTOWN.removeID3Observer(this);
        }
        
        return oTOWN;
    }
    
    /** Get the TOWN frame set in this tag.
     *
     * @return the TOWN frame set in this tag, or null if none was set
     */
    public TOWNTextInformationID3V2Frame getTOWNTextInformationFrame()
    {
        return (TOWNTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TOWN");
    }
    
    /** Remove the TOWN frame which was set in this tag.
     *
     * @return the previously set TOWN frame, or null if it was never set
     */
    public TOWNTextInformationID3V2Frame removeTOWNTextInformationFrame()
    {
        TOWNTextInformationID3V2Frame oTOWN = (TOWNTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TOWN");
        
        if (oTOWN != null)
        {
            oTOWN.removeID3Observer(this);
        }
        
        return oTOWN;
    }

    /** Set a lead performer(s)/soloist(s) frame in this tag.  Only a single TPE1 frame can be set in a tag.
     *
     * @param oTPE1TextInformationID3V2Frame the frame to be set
     */
    public TPE1TextInformationID3V2Frame setTPE1TextInformationFrame(TPE1TextInformationID3V2Frame oTPE1TextInformationID3V2Frame)
    {
        if (oTPE1TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPE1 text information frame in tag.");
        }

        TPE1TextInformationID3V2Frame oTPE1 = (TPE1TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPE1", oTPE1TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPE1TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPE1TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPE1 != null)
        {
            oTPE1.removeID3Observer(this);
        }
        
        return oTPE1;
    }
    
    /** Get the TPE1 frame set in this tag.
     *
     * @return the TPE1 frame set in this tag, or null if none was set
     */
    public TPE1TextInformationID3V2Frame getTPE1TextInformationFrame()
    {
        return (TPE1TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPE1");
    }
    
    /** Remove the TPE1 frame which was set in this tag.
     *
     * @return the previously set TPE1 frame, or null if it was never set
     */
    public TPE1TextInformationID3V2Frame removeTPE1TextInformationFrame()
    {
        TPE1TextInformationID3V2Frame oTPE1 = (TPE1TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPE1");
        
        if (oTPE1 != null)
        {
            oTPE1.removeID3Observer(this);
        }
        
        return oTPE1;
    }

    /** Set a band/orchestra/accompaniment frame in this tag.  Only a single TPE2 frame can be set in a tag.
     *
     * @param oTPE2TextInformationID3V2Frame the frame to be set
     */
    public TPE2TextInformationID3V2Frame setTPE2TextInformationFrame(TPE2TextInformationID3V2Frame oTPE2TextInformationID3V2Frame)
    {
        if (oTPE2TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPE2 text information frame in tag.");
        }
        
        TPE2TextInformationID3V2Frame oTPE2 = (TPE2TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPE2", oTPE2TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPE2TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPE2TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPE2 != null)
        {
            oTPE2.removeID3Observer(this);
        }
        
        return oTPE2;
    }
    
    /** Get the TPE2 frame set in this tag.
     *
     * @return the TPE2 frame set in this tag, or null if none was set
     */
    public TPE2TextInformationID3V2Frame getTPE2TextInformationFrame()
    {
        return (TPE2TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPE2");
    }
    
    /** Remove the TPE2 frame which was set in this tag.
     *
     * @return the previously set TPE2 frame, or null if it was never set
     */
    public TPE2TextInformationID3V2Frame removeTPE2TextInformationFrame()
    {
        TPE2TextInformationID3V2Frame oTPE2 = (TPE2TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPE2");
        
        if (oTPE2 != null)
        {
            oTPE2.removeID3Observer(this);
        }
        
        return oTPE2;
    }

    /** Set a conductor/performer refinement frame in this tag.  Only a single TPE3 frame can be set in a tag.
     *
     * @param oTPE3TextInformationID3V2Frame the frame to be set
     */
    public TPE3TextInformationID3V2Frame setTPE3TextInformationFrame(TPE3TextInformationID3V2Frame oTPE3TextInformationID3V2Frame)
    {
        if (oTPE3TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPE3 text information frame in tag.");
        }
        
        TPE3TextInformationID3V2Frame oTPE3 = (TPE3TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPE3", oTPE3TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPE3TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPE3TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPE3 != null)
        {
            oTPE3.removeID3Observer(this);
        }
        
        return oTPE3;
    }
    
    /** Get the TPE3 frame set in this tag.
     *
     * @return the TPE3 frame set in this tag, or null if none was set
     */
    public TPE3TextInformationID3V2Frame getTPE3TextInformationFrame()
    {
        return (TPE3TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPE3");
    }
    
    /** Remove the TPE3 frame which was set in this tag.
     *
     * @return the previously set TPE3 frame, or null if it was never set
     */
    public TPE3TextInformationID3V2Frame removeTPE3TextInformationFrame()
    {
        TPE3TextInformationID3V2Frame oTPE3 = (TPE3TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPE3");
        
        if (oTPE3 != null)
        {
            oTPE3.removeID3Observer(this);
        }
        
        return oTPE3;
    }

    /** Set an interpreted, remixed or otherwise modified by frame in this tag.  Only a single TPE4 frame can be set in a tag.
     *
     * @param oTPE4TextInformationID3V2Frame the frame to be set
     */
    public TPE4TextInformationID3V2Frame setTPE4TextInformationFrame(TPE4TextInformationID3V2Frame oTPE4TextInformationID3V2Frame)
    {
        if (oTPE4TextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPE4 text information frame in tag.");
        }
        
        TPE4TextInformationID3V2Frame oTPE4 = (TPE4TextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPE4", oTPE4TextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPE4TextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPE4TextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPE4 != null)
        {
            oTPE4.removeID3Observer(this);
        }
        
        return oTPE4;
    }
    
    /** Get the TPE4 frame set in this tag.
     *
     * @return the TPE4 frame set in this tag, or null if none was set
     */
    public TPE4TextInformationID3V2Frame getTPE4TextInformationFrame()
    {
        return (TPE4TextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPE4");
    }
    
    /** Remove the TPE4 frame which was set in this tag.
     *
     * @return the previously set TPE4 frame, or null if it was never set
     */
    public TPE4TextInformationID3V2Frame removeTPE4TextInformationFrame()
    {
        TPE4TextInformationID3V2Frame oTPE4 = (TPE4TextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPE4");
        
        if (oTPE4 != null)
        {
            oTPE4.removeID3Observer(this);
        }
        
        return oTPE4;
    }

    /** Set a part of a set frame in this tag.  Only a single TPOS frame can be set in a tag.
     *
     * @param oTPOSTextInformationID3V2Frame the frame to be set
     */
    public TPOSTextInformationID3V2Frame setTPOSTextInformationFrame(TPOSTextInformationID3V2Frame oTPOSTextInformationID3V2Frame)
    {
        if (oTPOSTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPOS text information frame in tag.");
        }
        
        TPOSTextInformationID3V2Frame oTPOS = (TPOSTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPOS", oTPOSTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPOSTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPOSTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPOS != null)
        {
            oTPOS.removeID3Observer(this);
        }
        
        return oTPOS;
    }
    
    /** Get the TPOS frame set in this tag.
     *
     * @return the TPOS frame set in this tag, or null if none was set
     */
    public TPOSTextInformationID3V2Frame getTPOSTextInformationFrame()
    {
        return (TPOSTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPOS");
    }
    
    /** Remove the TPOS frame which was set in this tag.
     *
     * @return the previously set TPOS frame, or null if it was never set
     */
    public TPOSTextInformationID3V2Frame removeTPOSTextInformationFrame()
    {
        TPOSTextInformationID3V2Frame oTPOS = (TPOSTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPOS");
        
        if (oTPOS != null)
        {
            oTPOS.removeID3Observer(this);
        }
        
        return oTPOS;
    }

    /** Set a publisher frame in this tag.  Only a single TPUB frame can be set in a tag.
     *
     * @param oTPUBTextInformationID3V2Frame the frame to be set
     */
    public TPUBTextInformationID3V2Frame setTPUBTextInformationFrame(TPUBTextInformationID3V2Frame oTPUBTextInformationID3V2Frame)
    {
        if (oTPUBTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TPUB text information frame in tag.");
        }
        
        TPUBTextInformationID3V2Frame oTPUB = (TPUBTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TPUB", oTPUBTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTPUBTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTPUBTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTPUB != null)
        {
            oTPUB.removeID3Observer(this);
        }
        
        return oTPUB;
    }
    
    /** Get the TPUB frame set in this tag.
     *
     * @return the TPUB frame set in this tag, or null if none was set
     */
    public TPUBTextInformationID3V2Frame getTPUBTextInformationFrame()
    {
        return (TPUBTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TPUB");
    }
    
    /** Remove the TPUB frame which was set in this tag.
     *
     * @return the previously set TPUB frame, or null if it was never set
     */
    public TPUBTextInformationID3V2Frame removeTPUBTextInformationFrame()
    {
        TPUBTextInformationID3V2Frame oTPUB = (TPUBTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TPUB");
        
        if (oTPUB != null)
        {
            oTPUB.removeID3Observer(this);
        }
        
        return oTPUB;
    }

    /** Set a track number/position in set frame in this tag.  Only a single TRCK frame can be set in a tag.
     *
     * @param oTRCKTextInformationID3V2Frame the frame to be set
     */
    public TRCKTextInformationID3V2Frame setTRCKTextInformationFrame(TRCKTextInformationID3V2Frame oTRCKTextInformationID3V2Frame)
    {
        if (oTRCKTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TRCK text information frame in tag.");
        }
        
        TRCKTextInformationID3V2Frame oTRCK = (TRCKTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TRCK", oTRCKTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTRCKTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTRCKTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTRCK != null)
        {
            oTRCK.removeID3Observer(this);
        }
        
        return oTRCK;
    }
    
    /** Get the TRCK frame set in this tag.
     *
     * @return the TRCK frame set in this tag, or null if none was set
     */
    public TRCKTextInformationID3V2Frame getTRCKTextInformationFrame()
    {
        return (TRCKTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TRCK");
    }
    
    /** Remove the TRCK frame which was set in this tag.
     *
     * @return the previously set TRCK frame, or null if it was never set
     */
    public TRCKTextInformationID3V2Frame removeTRCKTextInformationFrame()
    {
        TRCKTextInformationID3V2Frame oTRCK = (TRCKTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TRCK");
        
        if (oTRCK != null)
        {
            oTRCK.removeID3Observer(this);
        }
        
        return oTRCK;
    }

    /** Set a recording dates frame in this tag.  Only a single TRDA frame can be set in a tag.
     *
     * @param oTRDATextInformationID3V2Frame the frame to be set
     */
    public TRDATextInformationID3V2Frame setTRDATextInformationFrame(TRDATextInformationID3V2Frame oTRDATextInformationID3V2Frame)
    {
        if (oTRDATextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TRDA text information frame in tag.");
        }
        
        TRDATextInformationID3V2Frame oTRDA = (TRDATextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TRDA", oTRDATextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTRDATextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTRDATextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTRDA != null)
        {
            oTRDA.removeID3Observer(this);
        }
        
        return oTRDA;
    }
    
    /** Get the TRDA frame set in this tag.
     *
     * @return the TRDA frame set in this tag, or null if none was set
     */
    public TRDATextInformationID3V2Frame getTRDATextInformationFrame()
    {
        return (TRDATextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TRDA");
    }
    
    /** Remove the TRDA frame which was set in this tag.
     *
     * @return the previously set TRDA frame, or null if it was never set
     */
    public TRDATextInformationID3V2Frame removeTRDATextInformationFrame()
    {
        TRDATextInformationID3V2Frame oTRDA = (TRDATextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TRDA");
        
        if (oTRDA != null)
        {
            oTRDA.removeID3Observer(this);
        }
        
        return oTRDA;
    }

    /** Set an internet radio station name frame in this tag.  Only a single TRSN frame can be set in a tag.
     *
     * @param oTRSNTextInformationID3V2Frame the frame to be set
     */
    public TRSNTextInformationID3V2Frame setTRSNTextInformationFrame(TRSNTextInformationID3V2Frame oTRSNTextInformationID3V2Frame)
    {
        if (oTRSNTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TRSN text information frame in tag.");
        }
        
        TRSNTextInformationID3V2Frame oTRSN = (TRSNTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TRSN", oTRSNTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTRSNTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTRSNTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTRSN != null)
        {
            oTRSN.removeID3Observer(this);
        }
        
        return oTRSN;
    }
    
    /** Get the TRSN frame set in this tag.
     *
     * @return the TRSN frame set in this tag, or null if none was set
     */
    public TRSNTextInformationID3V2Frame getTRSNTextInformationFrame()
    {
        return (TRSNTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TRSN");
    }
    
    /** Remove the TRSN frame which was set in this tag.
     *
     * @return the previously set TRSN frame, or null if it was never set
     */
    public TRSNTextInformationID3V2Frame removeTRSNTextInformationFrame()
    {
        TRSNTextInformationID3V2Frame oTRSN = (TRSNTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TRSN");
        
        if (oTRSN != null)
        {
            oTRSN.removeID3Observer(this);
        }
        
        return oTRSN;
    }

    /** Set an internet radio station owner frame in this tag.  Only a single TRSO frame can be set in a tag.
     *
     * @param oTRSOTextInformationID3V2Frame the frame to be set
     */
    public TRSOTextInformationID3V2Frame setTRSOTextInformationFrame(TRSOTextInformationID3V2Frame oTRSOTextInformationID3V2Frame)
    {
        if (oTRSOTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TRSO text information frame in tag.");
        }
        
        TRSOTextInformationID3V2Frame oTRSO = (TRSOTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TRSO", oTRSOTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTRSOTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTRSOTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTRSO != null)
        {
            oTRSO.removeID3Observer(this);
        }
        
        return oTRSO;
    }
    
    /** Get the TRSO frame set in this tag.
     *
     * @return the TRSO frame set in this tag, or null if none was set
     */
    public TRSOTextInformationID3V2Frame getTRSOTextInformationFrame()
    {
        return (TRSOTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TRSO");
    }
    
    /** Remove the TRSO frame which was set in this tag.
     *
     * @return the previously set TRSO frame, or null if it was never set
     */
    public TRSOTextInformationID3V2Frame removeTRSOTextInformationFrame()
    {
        TRSOTextInformationID3V2Frame oTRSO = (TRSOTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TRSO");
        
        if (oTRSO != null)
        {
            oTRSO.removeID3Observer(this);
        }
        
        return oTRSO;
    }

    /** Set a size frame in this tag.  Only a single TSIZ frame can be set in a tag.
     *
     * @param oTSIZTextInformationID3V2Frame the frame to be set
     */
    public TSIZTextInformationID3V2Frame setTSIZTextInformationFrame(TSIZTextInformationID3V2Frame oTSIZTextInformationID3V2Frame)
    {
        if (oTSIZTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TSIZ text information frame in tag.");
        }
        
        TSIZTextInformationID3V2Frame oTSIZ = (TSIZTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TSIZ", oTSIZTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTSIZTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTSIZTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTSIZ != null)
        {
            oTSIZ.removeID3Observer(this);
        }
        
        return oTSIZ;
    }
    
    /** Get the TSIZ frame set in this tag.
     *
     * @return the TSIZ frame set in this tag, or null if none was set
     */
    public TSIZTextInformationID3V2Frame getTSIZTextInformationFrame()
    {
        return (TSIZTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TSIZ");
    }
    
    /** Remove the TSIZ frame which was set in this tag.
     *
     * @return the previously set TSIZ frame, or null if it was never set
     */
    public TSIZTextInformationID3V2Frame removeTSIZTextInformationFrame()
    {
        TSIZTextInformationID3V2Frame oTSIZ = (TSIZTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TSIZ");
        
        if (oTSIZ != null)
        {
            oTSIZ.removeID3Observer(this);
        }
        
        return oTSIZ;
    }

    /** Set an ISRC (international standard recording code) frame in this tag.  Only a single TSRC frame can be set in a tag.
     *
     * @param oTSRCTextInformationID3V2Frame the frame to be set
     */
    public TSRCTextInformationID3V2Frame setTSRCTextInformationFrame(TSRCTextInformationID3V2Frame oTSRCTextInformationID3V2Frame)
    {
        if (oTSRCTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TSRC text information frame in tag.");
        }
        
        TSRCTextInformationID3V2Frame oTSRC = (TSRCTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TSRC", oTSRCTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTSRCTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTSRCTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTSRC != null)
        {
            oTSRC.removeID3Observer(this);
        }
        
        return oTSRC;
    }
    
    /** Get the TSRC frame set in this tag.
     *
     * @return the TSRC frame set in this tag, or null if none was set
     */
    public TSRCTextInformationID3V2Frame getTSRCTextInformationFrame()
    {
        return (TSRCTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TSRC");
    }
    
    /** Remove the TSRC frame which was set in this tag.
     *
     * @return the previously set TSRC frame, or null if it was never set
     */
    public TSRCTextInformationID3V2Frame removeTSRCTextInformationFrame()
    {
        TSRCTextInformationID3V2Frame oTSRC = (TSRCTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TSRC");
        
        if (oTSRC != null)
        {
            oTSRC.removeID3Observer(this);
        }
        
        return oTSRC;
    }

    /** Set a software/hardware and settings used for recording frame in this tag.  Only a single TSSE frame can be set in a tag.
     *
     * @param oTSSETextInformationID3V2Frame the frame to be set
     */
    public TSSETextInformationID3V2Frame setTSSETextInformationFrame(TSSETextInformationID3V2Frame oTSSETextInformationID3V2Frame)
    {
        if (oTSSETextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TSSE text information frame in tag.");
        }

        TSSETextInformationID3V2Frame oTSSE = (TSSETextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TSSE", oTSSETextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTSSETextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTSSETextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTSSE != null)
        {
            oTSSE.removeID3Observer(this);
        }
        
        return oTSSE;
    }
    
    /** Get the TSSE frame set in this tag.
     *
     * @return the TSSE frame set in this tag, or null if none was set
     */
    public TSSETextInformationID3V2Frame getTSSETextInformationFrame()
    {
        return (TSSETextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TSSE");
    }
    
    /** Remove the TSSE frame which was set in this tag.
     *
     * @return the previously set TSSE frame, or null if it was never set
     */
    public TSSETextInformationID3V2Frame removeTSSETextInformationFrame()
    {
        TSSETextInformationID3V2Frame oTSSE = (TSSETextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TSSE");
        
        if (oTSSE != null)
        {
            oTSSE.removeID3Observer(this);
        }
        
        return oTSSE;
    }
    
    /** Add a user-defined text information frame to this tag.  Multiple TXXX frames can be added to a single tag, but each
     *  must have a unique description.
     *
     * @param oTXXXTextInformationID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an TXXX frame with the same description
     */
    public void addTXXXTextInformationFrame(TXXXTextInformationID3V2Frame oTXXXTextInformationID3V2Frame)
        throws ID3Exception
    {
        if (oTXXXTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null TXXX frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oTXXXDescriptionToFrameMap.containsKey(oTXXXTextInformationID3V2Frame.getDescription())))
        {
            throw new ID3Exception("Tag already contains TXXX frame with matching description.");
        }
        m_oTXXXDescriptionToFrameMap.put(oTXXXTextInformationID3V2Frame.getDescription(), oTXXXTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTXXXTextInformationID3V2Frame.addID3Observer(this);
        oTXXXTextInformationID3V2Frame.notifyID3Observers();
    }
    
    /** Get all TXXX frames stored in this tag.
     *
     * @return an array of all TXXX frames in this tag (zero-length array returned if there are none)
     */
    public TXXXTextInformationID3V2Frame[] getTXXXTextInformationFrames()
    {
        return (TXXXTextInformationID3V2Frame[])m_oTXXXDescriptionToFrameMap.values().toArray(new TXXXTextInformationID3V2Frame[0]);
    }
    
    /** Remove a specific TXXX frame from this tag.
     *
     * @param sDescription the description which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public TXXXTextInformationID3V2Frame removeTXXXTextInformationFrame(String sDescription)
    {
        if (sDescription == null)
        {
            throw new NullPointerException("Description is null.");
        }
        
        TXXXTextInformationID3V2Frame oTXXX = (TXXXTextInformationID3V2Frame)m_oTXXXDescriptionToFrameMap.remove(sDescription);
        
        if (oTXXX != null)
        {
            oTXXX.removeID3Observer(this);
        }

        return oTXXX;
    }

    /** Set a year frame in this tag.  Only a single TYER frame can be set in a tag.
     *
     * @param oTYERTextInformationID3V2Frame the frame to be set
     */
    public TYERTextInformationID3V2Frame setTYERTextInformationFrame(TYERTextInformationID3V2Frame oTYERTextInformationID3V2Frame)
    {
        if (oTYERTextInformationID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null TYER text information frame in tag.");
        }
        
        TYERTextInformationID3V2Frame oTYER = (TYERTextInformationID3V2Frame)m_oFrameIdToFrameMap.put("TYER", oTYERTextInformationID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oTYERTextInformationID3V2Frame.addID3Observer(this);
        try
        {
            oTYERTextInformationID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oTYER != null)
        {
            oTYER.removeID3Observer(this);
        }
        
        return oTYER;
    }
    
    /** Get the TYER frame set in this tag.
     *
     * @return the TYER frame set in this tag, or null if none was set
     */
    public TYERTextInformationID3V2Frame getTYERTextInformationFrame()
    {
        return (TYERTextInformationID3V2Frame)m_oFrameIdToFrameMap.get("TYER");
    }
    
    /** Remove the TYER frame which was set in this tag.
     *
     * @return the previously set TYER frame, or null if it was never set
     */
    public TYERTextInformationID3V2Frame removeTYERTextInformationFrame()
    {
        TYERTextInformationID3V2Frame oTYER = (TYERTextInformationID3V2Frame)m_oFrameIdToFrameMap.remove("TYER");
        
        if (oTYER != null)
        {
            oTYER.removeID3Observer(this);
        }
        
        return oTYER;
    }
    
    /** Add a unique file identifier frame to this tag.  Multiple UFID frames can be added to a single tag, but each
     *  must have a unique owner identifier.
     *
     * @param oUFIDID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an UFID frame with the same description
     */
    public void addUFIDFrame(UFIDID3V2Frame oUFIDID3V2Frame)
        throws ID3Exception
    {
        if (oUFIDID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null UFID frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oUFIDOwnerIdentifierToFrameMap.containsKey(oUFIDID3V2Frame.getOwnerIdentifier())))
        {
            throw new ID3Exception("Tag already contains UFID frame with matching language and short description.");
        }
        m_oUFIDOwnerIdentifierToFrameMap.put(oUFIDID3V2Frame.getOwnerIdentifier(), oUFIDID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oUFIDID3V2Frame.addID3Observer(this);
        oUFIDID3V2Frame.notifyID3Observers();
    }
    
    /** Get all UFID frames stored in this tag.
     *
     * @return an array of all UFID frames in this tag (zero-length array returned if there are none)
     */
    public UFIDID3V2Frame[] getUFIDFrames()
    {
        return (UFIDID3V2Frame[])m_oUFIDOwnerIdentifierToFrameMap.values().toArray(new UFIDID3V2Frame[0]);
    }
    
    /** Remove a specific UFID frame from this tag.
     *
     * @param sOwnerIdentifier the owner identifier which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public UFIDID3V2Frame removeUFIDFrame(String sOwnerIdentifier)
    {
        if (sOwnerIdentifier == null)
        {
            throw new NullPointerException("Owner identifier is null.");
        }
        
        UFIDID3V2Frame oUFID = (UFIDID3V2Frame)m_oUFIDOwnerIdentifierToFrameMap.remove(sOwnerIdentifier);
        
        if (oUFID != null)
        {
            oUFID.removeID3Observer(this);
        }

        return oUFID;
    }

    /** Set a terms of use frame in this tag.  Only a single USER frame can be set in a tag.
     *
     * @param oUSERID3V2Frame the frame to be set
     */
    public USERID3V2Frame setUSERFrame(USERID3V2Frame oUSERID3V2Frame)
    {
        if (oUSERID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null USER frame in tag.");
        }
        
        USERID3V2Frame oUSER = (USERID3V2Frame)m_oFrameIdToFrameMap.put("USER", oUSERID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oUSERID3V2Frame.addID3Observer(this);
        try
        {
            oUSERID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oUSER != null)
        {
            oUSER.removeID3Observer(this);
        }
        
        return oUSER;
    }
    
    /** Get the USER frame set in this tag.
     *
     * @return the USER frame set in this tag, or null if none was set
     */
    public USERID3V2Frame getUSERFrame()
    {
        return (USERID3V2Frame)m_oFrameIdToFrameMap.get("USER");
    }
    
    /** Remove the USER frame which was set in this tag.
     *
     * @return the previously set USER frame, or null if it was never set
     */
    public USERID3V2Frame removeUSERFrame()
    {
        USERID3V2Frame oUSER = (USERID3V2Frame)m_oFrameIdToFrameMap.remove("USER");
        
        if (oUSER != null)
        {
            oUSER.removeID3Observer(this);
        }
        
        return oUSER;
    }
    
    /** Add a unsynchronized lyric/text frame to this tag.  Multiple USLT frames can be added to a single tag, but each
     *  must have a unique language and content descriptor pair.
     *
     * @param oUSLTID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an USLT frame with the same language and content descriptor
     */
    public void addUSLTFrame(USLTID3V2Frame oUSLTID3V2Frame)
        throws ID3Exception
    {
        if (oUSLTID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null USLT frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oUSLTLanguageAndContentDescriptorToFrameMap.containsKey(oUSLTID3V2Frame.getLanguage() + oUSLTID3V2Frame.getContentDescriptor())))
        {
            throw new ID3Exception("Tag already contains USLT frame with matching language and short description.");
        }
        m_oUSLTLanguageAndContentDescriptorToFrameMap.put(oUSLTID3V2Frame.getLanguage() + oUSLTID3V2Frame.getContentDescriptor(), oUSLTID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oUSLTID3V2Frame.addID3Observer(this);
        oUSLTID3V2Frame.notifyID3Observers();
    }
    
    /** Get all USLT frames stored in this tag.
     *
     * @return an array of all USLT frames in this tag (zero-length array returned if there are none)
     */
    public USLTID3V2Frame[] getUSLTFrames()
    {
        return (USLTID3V2Frame[])m_oUSLTLanguageAndContentDescriptorToFrameMap.values().toArray(new USLTID3V2Frame[0]);
    }
    
    /** Remove a specific USLT frame from this tag.
     *
     * @param sLanguage the language which jointly identifies the frame to be removed
     * @param sShortDescription the content descriptor which jointly identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public USLTID3V2Frame removeUSLTFrame(String sLanguage, String sShortDescription)
    {
        if (sLanguage == null)
        {
            throw new NullPointerException("Language is null.");
        }
        if (sShortDescription == null)
        {
            throw new NullPointerException("Short description is null.");
        }
        
        USLTID3V2Frame oUSLT = (USLTID3V2Frame)m_oUSLTLanguageAndContentDescriptorToFrameMap.remove(sLanguage + sShortDescription);
        
        if (oUSLT != null)
        {
            oUSLT.removeID3Observer(this);
        }

        return oUSLT;
    }

    /** Add a commercial information frame to this tag.  Multiple WCOM frames can be added to a single tag, but each
     *  must have a unique URL.
     *
     * @param oWCOMUrlLinkID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an WCOM frame with the same description
     */
    public void addWCOMUrlLinkFrame(WCOMUrlLinkID3V2Frame oWCOMUrlLinkID3V2Frame)
        throws ID3Exception
    {
        if (oWCOMUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null WCOM frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oWCOMUrlToFrameMap.containsKey(oWCOMUrlLinkID3V2Frame.getCommercialInformationUrl())))
        {
            throw new ID3Exception("Tag already contains WCOM frame with matching URL.");
        }
        m_oWCOMUrlToFrameMap.put(oWCOMUrlLinkID3V2Frame.getCommercialInformationUrl(), oWCOMUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWCOMUrlLinkID3V2Frame.addID3Observer(this);
        oWCOMUrlLinkID3V2Frame.notifyID3Observers();
    }
    
    /** Get all WCOM frames stored in this tag.
     *
     * @return an array of all WCOM frames in this tag (zero-length array returned if there are none)
     */
    public WCOMUrlLinkID3V2Frame[] getWCOMUrlLinkFrames()
    {
        return (WCOMUrlLinkID3V2Frame[])m_oWCOMUrlToFrameMap.values().toArray(new WCOMUrlLinkID3V2Frame[0]);
    }
    
    /** Remove a specific WCOM frame from this tag.
     *
     * @param sCommercialInformationUrl the commercial information URL which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public WCOMUrlLinkID3V2Frame removeWCOMUrlLinkFrame(String sCommercialInformationUrl)
    {
        if (sCommercialInformationUrl == null)
        {
            throw new NullPointerException("Commercial information URL is null.");
        }
        
        WCOMUrlLinkID3V2Frame oWCOM = (WCOMUrlLinkID3V2Frame)m_oWCOMUrlToFrameMap.remove(sCommercialInformationUrl);
        
        if (oWCOM != null)
        {
            oWCOM.removeID3Observer(this);
        }

        return oWCOM;
    }

    /** Set a copyright/legal information frame in this tag.  Only a single WCOP frame can be set in a tag.
     *
     * @param oWCOPUrlLinkID3V2Frame the frame to be set
     */
    public WCOPUrlLinkID3V2Frame setWCOPUrlLinkFrame(WCOPUrlLinkID3V2Frame oWCOPUrlLinkID3V2Frame)
    {
        if (oWCOPUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WCOPUrlLink frame in tag.");
        }
        
        WCOPUrlLinkID3V2Frame oWCOP = (WCOPUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WCOP", oWCOPUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWCOPUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWCOPUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWCOP != null)
        {
            oWCOP.removeID3Observer(this);
        }
        
        return oWCOP;
    }
    
    /** Get the WCOP frame set in this tag.
     *
     * @return the WCOP frame set in this tag, or null if none was set
     */
    public WCOPUrlLinkID3V2Frame getWCOPUrlLinkFrame()
    {
        return (WCOPUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WCOP");
    }
    
    /** Remove the WCOP frame which was set in this tag.
     *
     * @return the previously set WCOP frame, or null if it was never set
     */
    public WCOPUrlLinkID3V2Frame removeWCOPUrlLinkFrame()
    {
        WCOPUrlLinkID3V2Frame oWCOP = (WCOPUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WCOP");
        
        if (oWCOP != null)
        {
            oWCOP.removeID3Observer(this);
        }
        
        return oWCOP;
    }
    
    /** Set an official audio file webpage frame in this tag.  Only a single WOAF frame can be set in a tag.
     *
     * @param oWOAFUrlLinkID3V2Frame the frame to be set
     */
    public WOAFUrlLinkID3V2Frame setWOAFUrlLinkFrame(WOAFUrlLinkID3V2Frame oWOAFUrlLinkID3V2Frame)
    {
        if (oWOAFUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WOAFUrlLink frame in tag.");
        }

        WOAFUrlLinkID3V2Frame oWOAF = (WOAFUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WOAF", oWOAFUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWOAFUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWOAFUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWOAF != null)
        {
            oWOAF.removeID3Observer(this);
        }
        
        return oWOAF;
    }
    
    /** Get the WOAF frame set in this tag.
     *
     * @return the WOAF frame set in this tag, or null if none was set
     */
    public WOAFUrlLinkID3V2Frame getWOAFUrlLinkFrame()
    {
        return (WOAFUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WOAF");
    }
    
    /** Remove the WOAF frame which was set in this tag.
     *
     * @return the previously set WOAF frame, or null if it was never set
     */
    public WOAFUrlLinkID3V2Frame removeWOAFUrlLinkFrame()
    {
        WOAFUrlLinkID3V2Frame oWOAF = (WOAFUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WOAF");
        
        if (oWOAF != null)
        {
            oWOAF.removeID3Observer(this);
        }
        
        return oWOAF;
    }

    /** Add an official artist/performer webpage frame to this tag.  Multiple WOAR frames can be added to a single tag, but each
     *  must have a unique URL.
     *
     * @param oWOARUrlLinkID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an WOAR frame with the same description
     */
    public void addWOARUrlLinkFrame(WOARUrlLinkID3V2Frame oWOARUrlLinkID3V2Frame)
        throws ID3Exception
    {
        if (oWOARUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null WOAR frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oWOARUrlToFrameMap.containsKey(oWOARUrlLinkID3V2Frame.getOfficialArtistWebPage())))
        {
            throw new ID3Exception("Tag already contains WOAR frame with matching URL.");
        }
        m_oWOARUrlToFrameMap.put(oWOARUrlLinkID3V2Frame.getOfficialArtistWebPage(), oWOARUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWOARUrlLinkID3V2Frame.addID3Observer(this);
        oWOARUrlLinkID3V2Frame.notifyID3Observers();
    }
    
    /** Get all WOAR frames stored in this tag.
     *
     * @return an array of all WOAR frames in this tag (zero-length array returned if there are none)
     */
    public WOARUrlLinkID3V2Frame[] getWOARUrlLinkFrames()
    {
        return (WOARUrlLinkID3V2Frame[])m_oWOARUrlToFrameMap.values().toArray(new WOARUrlLinkID3V2Frame[0]);
    }
    
    /** Remove a specific WOAR frame from this tag.
     *
     * @param sOfficialArtistWebPageUrl the official artist webpage URL which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public WOARUrlLinkID3V2Frame removeWOARUrlLinkFrame(String sOfficialArtistWebPageUrl)
    {
        if (sOfficialArtistWebPageUrl == null)
        {
            throw new NullPointerException("Official artist webpage URL is null.");
        }
        
        WOARUrlLinkID3V2Frame oWOAR = (WOARUrlLinkID3V2Frame)m_oWOARUrlToFrameMap.remove(sOfficialArtistWebPageUrl);
        
        if (oWOAR != null)
        {
            oWOAR.removeID3Observer(this);
        }

        return oWOAR;
    }

    /** Set an official audio source webpage frame in this tag.  Only a single WOAS frame can be set in a tag.
     *
     * @param oWOASUrlLinkID3V2Frame the frame to be set
     */
    public WOASUrlLinkID3V2Frame setWOASUrlLinkFrame(WOASUrlLinkID3V2Frame oWOASUrlLinkID3V2Frame)
    {
        if (oWOASUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WOASUrlLink frame in tag.");
        }
        
        WOASUrlLinkID3V2Frame oWOAS = (WOASUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WOAS", oWOASUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWOASUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWOASUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWOAS != null)
        {
            oWOAS.removeID3Observer(this);
        }
        
        return oWOAS;
    }
    
    /** Get the WOAS frame set in this tag.
     *
     * @return the WOAS frame set in this tag, or null if none was set
     */
    public WOASUrlLinkID3V2Frame getWOASUrlLinkFrame()
    {
        return (WOASUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WOAS");
    }
    
    /** Remove the WOAS frame which was set in this tag.
     *
     * @return the previously set WOAS frame, or null if it was never set
     */
    public WOASUrlLinkID3V2Frame removeWOASUrlLinkFrame()
    {
        WOASUrlLinkID3V2Frame oWOAS = (WOASUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WOAS");
        
        if (oWOAS != null)
        {
            oWOAS.removeID3Observer(this);
        }
        
        return oWOAS;
    }

    /** Set an official internet radio station homepage frame in this tag.  Only a single WORS frame can be set in a tag.
     *
     * @param oWORSUrlLinkID3V2Frame the frame to be set
     */
    public WORSUrlLinkID3V2Frame setWORSUrlLinkFrame(WORSUrlLinkID3V2Frame oWORSUrlLinkID3V2Frame)
    {
        if (oWORSUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WORSUrlLink frame in tag.");
        }
        
        WORSUrlLinkID3V2Frame oWORS = (WORSUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WORS", oWORSUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWORSUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWORSUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWORS != null)
        {
            oWORS.removeID3Observer(this);
        }
        
        return oWORS;
    }
    
    /** Get the WORS frame set in this tag.
     *
     * @return the WORS frame set in this tag, or null if none was set
     */
    public WORSUrlLinkID3V2Frame getWORSUrlLinkFrame()
    {
        return (WORSUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WORS");
    }
    
    /** Remove the WORS frame which was set in this tag.
     *
     * @return the previously set WORS frame, or null if it was never set
     */
    public WORSUrlLinkID3V2Frame removeWORSUrlLinkFrame()
    {
        WORSUrlLinkID3V2Frame oWORS = (WORSUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WORS");
        
        if (oWORS != null)
        {
            oWORS.removeID3Observer(this);
        }
        
        return oWORS;
    }

    /** Set a payment frame in this tag.  Only a single WPAY frame can be set in a tag.
     *
     * @param oWPAYUrlLinkID3V2Frame the frame to be set
     */
    public WPAYUrlLinkID3V2Frame setWPAYUrlLinkFrame(WPAYUrlLinkID3V2Frame oWPAYUrlLinkID3V2Frame)
    {
        if (oWPAYUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WPAYUrlLink frame in tag.");
        }
        
        WPAYUrlLinkID3V2Frame oWPAY = (WPAYUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WPAY", oWPAYUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWPAYUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWPAYUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWPAY != null)
        {
            oWPAY.removeID3Observer(this);
        }
        
        return oWPAY;
    }
    
    /** Get the WPAY frame set in this tag.
     *
     * @return the WPAY frame set in this tag, or null if none was set
     */
    public WPAYUrlLinkID3V2Frame getWPAYUrlLinkFrame()
    {
        return (WPAYUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WPAY");
    }
    
    /** Remove the WPAY frame which was set in this tag.
     *
     * @return the previously set WPAY frame, or null if it was never set
     */
    public WPAYUrlLinkID3V2Frame removeWPAYUrlLinkFrame()
    {
        WPAYUrlLinkID3V2Frame oWPAY = (WPAYUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WPAY");
        
        if (oWPAY != null)
        {
            oWPAY.removeID3Observer(this);
        }
        
        return oWPAY;
    }

    /** Set a publisher's official webpage frame in this tag.  Only a single WPUB frame can be set in a tag.
     *
     * @param oWPUBUrlLinkID3V2Frame the frame to be set
     */
    public WPUBUrlLinkID3V2Frame setWPUBUrlLinkFrame(WPUBUrlLinkID3V2Frame oWPUBUrlLinkID3V2Frame)
    {
        if (oWPUBUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to set null WPUBUrlLink frame in tag.");
        }
        
        WPUBUrlLinkID3V2Frame oWPUB = (WPUBUrlLinkID3V2Frame)m_oFrameIdToFrameMap.put("WPUB", oWPUBUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWPUBUrlLinkID3V2Frame.addID3Observer(this);
        try
        {
            oWPUBUrlLinkID3V2Frame.notifyID3Observers();
        }
        catch (Exception e) {}

        // this tag will no longer listen for changes to the removed frame
        if (oWPUB != null)
        {
            oWPUB.removeID3Observer(this);
        }
        
        return oWPUB;
    }
    
    /** Get the WPUB frame set in this tag.
     *
     * @return the WPUB frame set in this tag, or null if none was set
     */
    public WPUBUrlLinkID3V2Frame getWPUBUrlLinkFrame()
    {
        return (WPUBUrlLinkID3V2Frame)m_oFrameIdToFrameMap.get("WPUB");
    }
    
    /** Remove the WPUB frame which was set in this tag.
     *
     * @return the previously set WPUB frame, or null if it was never set
     */
    public WPUBUrlLinkID3V2Frame removeWPUBUrlLinkFrame()
    {
        WPUBUrlLinkID3V2Frame oWPUB = (WPUBUrlLinkID3V2Frame)m_oFrameIdToFrameMap.remove("WPUB");
        
        if (oWPUB != null)
        {
            oWPUB.removeID3Observer(this);
        }
        
        return oWPUB;
    }

    /** Add a user-defined URL link frame to this tag.  Multiple WXXX frames can be added to a single tag, but each
     *  must have a unique description.
     *
     * @param oWXXXUrlLinkID3V2Frame the frame to be added
     * @throws ID3Exception if this tag already contains an WXXX frame with the same description
     */
    public void addWXXXUrlLinkFrame(WXXXUrlLinkID3V2Frame oWXXXUrlLinkID3V2Frame)
        throws ID3Exception
    {
        if (oWXXXUrlLinkID3V2Frame == null)
        {
            throw new NullPointerException("Attempt to add null WXXX frame to tag.");
        }
        if (ID3Tag.usingStrict() && (m_oWXXXDescriptionToFrameMap.containsKey(oWXXXUrlLinkID3V2Frame.getDescription())))
        {
            throw new ID3Exception("Tag already contains WXXX frame with matching description.");
        }
        m_oWXXXDescriptionToFrameMap.put(oWXXXUrlLinkID3V2Frame.getDescription(), oWXXXUrlLinkID3V2Frame);
        
        // set this tag object up as a listener for changes to this frame
        oWXXXUrlLinkID3V2Frame.addID3Observer(this);
        oWXXXUrlLinkID3V2Frame.notifyID3Observers();
    }
    
    /** Get all WXXX frames stored in this tag.
     *
     * @return an array of all WXXX frames in this tag (zero-length array returned if there are none)
     */
    public WXXXUrlLinkID3V2Frame[] getWXXXUrlLinkFrames()
    {
        return (WXXXUrlLinkID3V2Frame[])m_oWXXXDescriptionToFrameMap.values().toArray(new WXXXUrlLinkID3V2Frame[0]);
    }
    
    /** Remove a specific WXXX frame from this tag.
     *
     * @param sDescription the description which uniquely identifies the frame to be removed
     * @return the removed frame, or null if no matching frame exists
     */
    public WXXXUrlLinkID3V2Frame removeWXXXUrlLinkFrame(String sDescription)
    {
        if (sDescription == null)
        {
            throw new NullPointerException("Description is null.");
        }
        
        WXXXUrlLinkID3V2Frame oWXXX = (WXXXUrlLinkID3V2Frame)m_oWXXXDescriptionToFrameMap.remove(sDescription);
        
        if (oWXXX != null)
        {
            oWXXX.removeID3Observer(this);
        }

        return oWXXX;
    }
    
    public void setArtist(String sArtist)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTPE1TextInformationFrame();
        
        // set new frame
        this.setTPE1TextInformationFrame(new TPE1TextInformationID3V2Frame(sArtist));
    }
    
    public String getArtist()
    {
        TPE1TextInformationID3V2Frame oTPE1 = this.getTPE1TextInformationFrame();
        
        if (oTPE1 != null)
        {
            // build artist string from result (will probably not need combining, if convenience methods only used)
            String sArtist = "";
            String[] asLeadPerformer = oTPE1.getLeadPerformers();
            for (int i=0; i < asLeadPerformer.length-1; i++)
            {
                sArtist += asLeadPerformer[i] + "/";
            }
            sArtist += asLeadPerformer[asLeadPerformer.length-1];
            
            return sArtist;
        }
        else
        {
            return null;
        }
    }
    
    public void setTitle(String sTitle)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTIT2TextInformationFrame();
        
        // set new frame
        this.setTIT2TextInformationFrame(new TIT2TextInformationID3V2Frame(sTitle));
    }
    
    public String getTitle()
    {
        TIT2TextInformationID3V2Frame oTIT2 = this.getTIT2TextInformationFrame();
        
        if (oTIT2 != null)
        {
            return oTIT2.getTitle();
        }
        else
        {
            return null;
        }
    }
    
    public void setAlbum(String sAlbum)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTALBTextInformationFrame();
        
        // set new frame
        this.setTALBTextInformationFrame(new TALBTextInformationID3V2Frame(sAlbum));
    }
    
    public String getAlbum()
    {
        TALBTextInformationID3V2Frame oTALB = this.getTALBTextInformationFrame();
        
        if (oTALB != null)
        {
            return oTALB.getAlbum();
        }
        else
        {
            return null;
        }
    }
    
    public void setYear(int iYear)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTYERTextInformationFrame();
        
        // set new frame
        this.setTYERTextInformationFrame(new TYERTextInformationID3V2Frame(iYear));
    }
    
    public int getYear()
        throws ID3Exception
    {
        TYERTextInformationID3V2Frame oTYER = this.getTYERTextInformationFrame();
        
        if (oTYER != null)
        {
            return oTYER.getYear();
        }
        else
        {
            throw new ID3Exception("No year has been set.");
        }
    }
    
    public void setTrackNumber(int iTrackNumber)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTRCKTextInformationFrame();
        
        // set new frame
        this.setTRCKTextInformationFrame(new TRCKTextInformationID3V2Frame(iTrackNumber));
    }
    
    public void setTrackNumber(int iTrackNumber, int iTotalTracks)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTRCKTextInformationFrame();
        
        // set new frame
        this.setTRCKTextInformationFrame(new TRCKTextInformationID3V2Frame(iTrackNumber, iTotalTracks));
    }
    
    public int getTrackNumber()
        throws ID3Exception
    {
        TRCKTextInformationID3V2Frame oTRCK = this.getTRCKTextInformationFrame();
        
        if (oTRCK != null)
        {
            return oTRCK.getTrackNumber();
        }
        else
        {
            throw new ID3Exception("No track number has been set.");
        }
    }
    
    public int getTotalTracks()
        throws ID3Exception
    {
        TRCKTextInformationID3V2Frame oTRCK = this.getTRCKTextInformationFrame();
        
        if (oTRCK != null)
        {
            return oTRCK.getTotalTracks();
        }
        else
        {
            throw new ID3Exception("No total tracks number has been set.");
        }
    }
    
    public void setGenre(String sGenre)
        throws ID3Exception
    {
        // remove any current frame
        this.removeTCONTextInformationFrame();
        
        // set new frame
        ContentType oContentType = new ContentType();
        oContentType.setRefinement(sGenre);
        this.setTCONTextInformationFrame(new TCONTextInformationID3V2Frame(oContentType));
    }
    
    public String getGenre()
    {
        TCONTextInformationID3V2Frame oTCON = this.getTCONTextInformationFrame();
        
        if (oTCON != null)
        {
            return oTCON.getContentType().toString();
        }
        else
        {
            return null;
        }
    }
    
    public void setComment(String sComment)
        throws ID3Exception
    {
        // remove any current comment
        this.removeCOMMFrame("eng", null);
        
        // set new frame
        this.addCOMMFrame(new COMMID3V2Frame("eng", null, sComment));
    }
    
    public String getComment()
    {
        COMMID3V2Frame oCOMM = null;
        COMMID3V2Frame[] aoCOMM = this.getCOMMFrames();
        for (int i=0; i < aoCOMM.length; i++)
        {
            if (aoCOMM[i].getShortDescription().equals(""))
            {
                oCOMM = aoCOMM[i];
            }
        }
        
        if (oCOMM != null)
        {
            return oCOMM.getActualText();
        }
        else
        {
            return null;
        }
    }
}