/*
 * ID3Util.java
 *
 * Created on 2-Jan-2004
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
 * $Id: ID3Util.java,v 1.21 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;

/**
 * @author paul
 *
 * A collection of random utility functions used throughout the project.
 *
 */
public class ID3Util
{
    /** License. */
    private static final String LICENSE = "Copyright (C)2003-2005 Paul Grebenc\n\n" +
                                          "This library is free software; you can redistribute it and/or\n" +
                                          "modify it under the terms of the GNU Lesser General Public\n" +
                                          "License as published by the Free Software Foundation; either\n" +
                                          "version 2.1 of the License, or (at your option) any later version.\n\n" +
                                          "This library is distributed in the hope that it will be useful,\n" +
                                          "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                                          "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n" +
                                          "Lesser General Public License for more details.\n\n" +
                                          "You should have received a copy of the GNU Lesser General Public\n" +
                                          "License along with this library; if not, write to the Free Software\n" +
                                          "Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA";
    
    /** Get the version of this library. */
    public static String getVersion()
    {
        try
        {
            // read library version string from properties file
            InputStream oPropIS = ID3Util.class.getResourceAsStream("/jid3.properties");
            Properties oProperties = new Properties();
            oProperties.load(oPropIS);
            String sVersion = oProperties.getProperty("jid3.version");
            
            // date timestamp of properties file
            // get properties file as URL
            URL oJID3URL = ID3Util.class.getResource("/jid3.properties");
            // turn URL into a filename
            String sJarFile = oJID3URL.toExternalForm();
            sJarFile = sJarFile.replaceFirst("^jar:/?/?", "");
            sJarFile = sJarFile.replaceFirst("^file:", "");
            sJarFile = sJarFile.replaceFirst("!.*$", "");
            // open jar file to get at the properties file and check its time
            JarFile oJarFile = new JarFile(sJarFile);
            JarEntry oJarEntry = oJarFile.getJarEntry("jid3.properties");
            long lLastModified = oJarEntry.getTime();
            Date oDate = new Date(lLastModified);
            
            return sVersion + " (" + oDate.toString() + ")";
        }
        catch (Exception e)
        {
            return e.toString();
        }
    }
    
    /** Get the license for this library. */
    public static String getLicense()
    {
        return LICENSE;
    }
    
    public static void main(String[] args)
    {
        System.out.println("JID3 library version " + getVersion() + "\n\n" + getLicense());
    }
    
    /** Utility method to report all tags found by printing them to stdout.
     * 
     * @param aoID3Tag an array of ID3Tag objects
     * @throws Exception
     */
    public static void printTags(ID3Tag[] aoID3Tag)
        throws Exception
    {
        System.out.println("Number of tag sets: " + aoID3Tag.length);
        for (int i=0; i < aoID3Tag.length; i++)
        {
            if (aoID3Tag[i] instanceof ID3V1_0Tag)
            {
                System.out.println("ID3V1_0Tag:");
                System.out.println(aoID3Tag[i].toString());
            }
            else if (aoID3Tag[i] instanceof ID3V1_1Tag)
            {
                System.out.println("ID3V1_1Tag:");
                System.out.println(aoID3Tag[i].toString());
            }
            else if (aoID3Tag[i] instanceof ID3V2_3_0Tag)
            {
                System.out.println("ID3V2_3_0Tag:");
                System.out.println(aoID3Tag[i].toString());
            }
        }
    }
    
    /** Check if a byte array will require unsynchronization before being written as a tag.
     * If the byte array contains any $FF bytes, then it will require unsynchronization.
     *
     * @param abySource the byte array to be examined
     * @return true if unsynchronization is required, false otherwise
     */
    public static boolean requiresUnsynchronization(byte[] abySource)
    {
        for (int i=0; i < abySource.length-1; i++)
        {
            if (((abySource[i] & 0xff) == 0xff) && ((abySource[i+1] & 0xff) >= 224))
            {
                return true;
            }
        }
        
        return false;
    }

    /** Unsynchronize an array of bytes.
     * In order to prevent a media player from incorrectly interpreting the contents of a tag, all $FF bytes
     * followed by a byte with value >=224 must be followed by a $00 byte (thus, $FF $F0 sequences become $FF $00 $F0).
     *
     * NOTE:  Unsynchronization is not always necessary, if no false sync patterns exist in the data.  Only if the
     *        length of the returned byte array is greater than that of the source array, was an unsynchronization
     *        modification applied.
     *
     * @param abySource a byte array to be unsynchronized
     * @return a unsynchronized representation of the source
     */
    public static byte[] unsynchronize(byte[] abySource)
    {
        ByteArrayInputStream oBAIS = new ByteArrayInputStream(abySource);
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();

        boolean bUnsynchronizationUsed = false;
        while (oBAIS.available() > 0)
        {
            int iVal = oBAIS.read();
            
            oBAOS.write(iVal);
            if (iVal == 0xff)
            {
                // if byte is $FF, we must check the following byte if there is one
                if (oBAIS.available() > 0)
                {
                    oBAIS.mark(1);  // remember where we were, if we don't need to unsynchronize
                    int iNextVal = oBAIS.read();
                    if (iNextVal >= 224)
                    {
                        // we need to unsynchronize here
                        oBAOS.write(0);
                        oBAOS.write(iNextVal);
                        bUnsynchronizationUsed = true;
                    }
                    else
                    {
                        oBAIS.reset();
                    }
                }
            }
        }

        // if we needed to unsynchronize anything, and this tag ends with 0xff, we have to append a zero byte,
        // which will be removed on de-unsynchronization later
        if (bUnsynchronizationUsed && ((abySource[abySource.length-1] & 0xff) == 0xff))
        {
            oBAOS.write(0);
        }
        
        return oBAOS.toByteArray();
    }

    /** De-unsynchronize an array of bytes.
     * This method takes an array of bytes which has already been unsynchronized, and it reverses
     * this process, returning the original unencoded array of bytes.
     *
     * NOTE:  De-unsynchronizing a byte array which was not unsynchronized can cause data corruption.
     *
     * @param abySource a byte array to be unencoded
     * @return the original byte array
     */
    public static byte[] deunsynchronize(byte[] abySource)
    {
        ByteArrayInputStream oBAIS = new ByteArrayInputStream(abySource);
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
        
        while (oBAIS.available() > 0)
        {
            int iVal = oBAIS.read();
            
            oBAOS.write(iVal);
            if (iVal == 0xff)
            {
                // we are skipping (what should be) a $00 byte (otherwise, GIGO)
                oBAIS.read();
            }
        }
        
        return oBAOS.toByteArray();
    }

    /** A utility method which, given an array of bytes, will return a character string containing
     * the hex values of the bytes, optionally separated by colons (ie.&nbsp;2F:01:A9:3C:etc.), which
     * may be useful in debugging output or elsewhere.
     * @param abyRawBytes the byte array to be converted
     * @param bIncludeColons whether to include colons in output string or not
     * @return a string containing a hex representation of the byte array
     */
    public static String convertBytesToHexString(byte[] abyRawBytes, boolean bIncludeColons) 
    {
        StringBuffer sbBuffer = new StringBuffer();
        
        for (int iNum = 0; iNum < abyRawBytes.length; iNum++)
        {
            int iVal;
            if (abyRawBytes[iNum] < 0) { iVal = abyRawBytes[iNum] + 256; }
            else { iVal = abyRawBytes[iNum]; }
            String sHexVal = Integer.toHexString(iVal);
            if (sHexVal.length() == 1)
            {
                sbBuffer.append("0");
            }
            sbBuffer.append(Integer.toHexString(iVal));
            if ((bIncludeColons) && (iNum < (abyRawBytes.length - 1)))
            {
                sbBuffer.append(":");
            }
        }
        
        return sbBuffer.toString();
    }

    /** Copy a file.
     * 
     * @param sSource source filename
     * @param sDestination destination filename
     * @throws Exception
     */
    public static void copy(String sSource, String sDestination)
        throws Exception
    {
        FileInputStream oFIS = null;
        FileOutputStream oFOS = null;
        try
        {
            oFIS = new FileInputStream(sSource);
            oFOS = new FileOutputStream(sDestination);

            byte[] abyBuffer = new byte[16384];
            int iNumRead;
            while ((iNumRead = oFIS.read(abyBuffer)) != -1)
            {
                oFOS.write(abyBuffer, 0, iNumRead);
            }
            oFOS.flush();
        }
        finally
        {
            try { oFIS.close(); } catch (Exception e) {}
            try { oFOS.close(); } catch (Exception e) {}
        }
    }
    
    /** Compare two files.
     * 
     * @param sFileOne filename
     * @param sFileTwo filename
     * @return true if identical, false otherwise
     * @throws Exception
     */
    public static void compare(String sFileOne, String sFileTwo)
        throws Exception
    {
        File oOneFile = new File(sFileOne);
        File oTwoFile = new File(sFileTwo);

        // check that lengths are the same        
        if (oOneFile.length() != oTwoFile.length())
        {
            throw new Exception("File lengths differ.");
        }
        
        FileInputStream oFIS1 = new FileInputStream(oOneFile);
        FileInputStream oFIS2 = new FileInputStream(oTwoFile);
        try
        {
            int c;

            // lengths are equal, so check that contents are the same
            int i=0;
            while ((c = oFIS1.read()) != -1)
            {
                if (oFIS2.read() != c)
                {
                    throw new Exception("File contents differ at position " + i + ".");
                }
                i++;
            }
        }
        finally
        {
            oFIS1.close();
            oFIS2.close();
        }
    }
    
    /** Convert a clipboard buffer copied from the frhed hex editor, to an array of bytes.  This method is used
     *  in test cases, to compare the results of the test with the expected values (as verified in frhed).
     *
     * @param sInput the content of the clipboard buffer (in frhed format)
     * @return a byte array representing the input string
     * @throws Exception if there is either an error parsing the input string
     */
    public static byte[] convertFrhedToByteArray(String sInput)
        throws Exception
    {
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
        
        StringReader oSR = new StringReader(sInput);
        int iChar;
        while ((iChar = oSR.read()) != -1)
        {
            char c = (char)iChar;
            if (c == '\\')
            {
                // this is an escaped character, so copy the next one over unmodified
                iChar = oSR.read();
                oBAOS.write(iChar);
            }
            else if (c == '<')
            {
                // copy string up to colon
                StringBuffer sbEncoding = new StringBuffer();
                while ((iChar = oSR.read()) != ':')
                {
                    sbEncoding.append((char)iChar);
                }
                String sEncoding = sbEncoding.toString();
                
                if (sEncoding.equals("bh"))
                {
                    // read value up to closing right angle bracket
                    StringBuffer sbValue = new StringBuffer();
                    while ((iChar = oSR.read()) != '>')
                    {
                        sbValue.append((char)iChar);
                    }
                    String sValue = sbValue.toString();
                    
                    // convert value from hex string to byte
                    oBAOS.write(Integer.parseInt(sValue, 16));
                }
                else
                {
                    throw new Exception("Unknown encoding type: " + sEncoding);
                }
            }
            else
            {
                // copy value over directly
                oBAOS.write(iChar);
            }
        }
        
        return oBAOS.toByteArray();
    }
    
    /** Compare the starting bytes of a file against a given byte array.  Used for testing.
     *  This method returning nothing if successful.
     *
     * @param oFile the file to verify
     * @param abyExpected an array of bytes which are expected to start the specified file
     * @throws Exception if the contents do not match
     * @throws IOException if there was an error reading the file
     */
    public static void compareFilePrefix(File oFile, byte[] abyExpected)
        throws Exception
    {
        InputStream oBIS = new BufferedInputStream(new FileInputStream(oFile));
        try
        {
            ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
            ByteArrayInputStream oBAIS = new ByteArrayInputStream(abyExpected);

            for (int i=0; i < abyExpected.length; i++)
            {
                int iFileByte = oBIS.read();
                oBAOS.write(iFileByte);
                int iExpectedByte = oBAIS.read();

                if (iFileByte != iExpectedByte)
                {
                    throw new Exception("File contents [" + ID3Util.convertBytesToHexString(oBAOS.toByteArray(), true) +
                                        "] do not match expected bytes [" + ID3Util.convertBytesToHexString(abyExpected, true) + "].");
                }
            }
        }
        finally
        {
            oBIS.close();
        }
    }
}
