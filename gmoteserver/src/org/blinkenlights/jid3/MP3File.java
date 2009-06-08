/* 
 * MP3File.java
 *
 * Created on 7-Oct-2003
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
 * $Id: MP3File.java,v 1.20 2005/11/22 02:09:14 paul Exp $
 */

package org.blinkenlights.jid3;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;

/**
 * @author paul
 *
 * A class representing an MP3 file.
 */
public class MP3File extends MediaFile
{
    /** Construct an object representing the MP3 file specified.
     *
     * @param oSourceFile a File pointing to the source MP3 file
     */
    public MP3File(File oSourceFile)
    {
        super(oSourceFile);
    }
    
    public MP3File(IFileSource oFileSource)
    {
        super(oFileSource);
    }

    /* (non-Javadoc)
     * @see org.blinkenlights.id3.MediaFile#sync()
     */
    public void sync()
        throws ID3Exception
    {
        // before we write anything, check first that if there is a v2 tag to be sync'ed, it is a valid tag to write
        // (it is not valid unless it has at least one frame)
        if ((m_oID3V2Tag != null) && ( ! m_oID3V2Tag.containsAtLeastOneFrame()))
        {
            throw new ID3Exception("This file has an ID3 V2 tag which cannot be written because it does not contain at least one frame.");
        }

        if (m_oID3V1Tag != null)
        {
            // need to update the V1 tags
            v1Sync();
        }
        if (m_oID3V2Tag != null)
        {
            // need to update the V2 tags
            v2Sync();
        }
    }

    /** Update the contents of the actual MP3 file to reflect the current ID3 V1 tag settings of the object.
     *
     * @throws ID3Exception if an error updating the file occurs
     */
    private void v1Sync()
        throws ID3Exception
    {
        IFileSource oTmpFileSource = null;
        InputStream oSourceIS = null;
        OutputStream oTmpOS = null;

        try
        {
            // open source file for reading
            try
            {
                oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());
            }
            catch (Exception e)
            {
                throw new ID3Exception("Error opening [" + m_oFileSource.getName() + "]", e);
            }

            try
            {
                // create temporary file to work with
                try
                {
                    oTmpFileSource = m_oFileSource.createTempFile("id3.", ".tmp");
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Unable to create temporary file.", e);
                }

                // open temp file for writing
                try
                {
                    oTmpOS = oTmpFileSource.getOutputStream();
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Error opening temporary file for writing.", e);
                }

                try
                {
                    // copy over all of the source file up to but not including the V1 tags,
                    // if they are present
                    long lFileLength = m_oFileSource.length();
                    // copy over all of the file up to the last 128 bytes (in 64k blocks for speed, while remaining memory efficient)
                    byte[] abyBuffer = new byte[65536];
                    long lCopied = 0;
                    long lTotalToCopy = lFileLength - 128;
                    while (lCopied < lTotalToCopy)
                    {
                        long lLeftToCopy = lTotalToCopy - lCopied;
                        long lToCopyNow = (lLeftToCopy >= 65536) ? 65536 : lLeftToCopy;
                        oSourceIS.read(abyBuffer, 0, (int)lToCopyNow);
                        oTmpOS.write(abyBuffer, 0, (int)lToCopyNow);
                        lCopied += lToCopyNow;
                    }


                    // check next three bytes of source file which indicate whether this file already
                    // has a V1 tag on it or not
                    byte[] abyCheckTag = new byte[3];
                    oSourceIS.read(abyCheckTag);
                    if ( ! ((abyCheckTag[0] == 'T') && (abyCheckTag[1] == 'A') && (abyCheckTag[2] == 'G')))
                    {
                        // no V1 tag on this file... copy the rest of it over (3 + 125 = 128 bytes)
                        oTmpOS.write(abyCheckTag);
                        for (int i=0; i < 125; i++)
                        {
                            oTmpOS.write(oSourceIS.read());
                        }
                    }

                    // append V1 tag information to the end of the data copied from the source file
                    // to the temporary file
                    m_oID3V1Tag.write(oTmpOS);

                    // we're done
                    oTmpOS.flush();
                }
                finally
                {
                    oTmpOS.close();
                }
            }
            finally
            {
                oSourceIS.close();
            }

            // move temp file to original source file
            if (! m_oFileSource.delete())
            {
                //HACK:  This is a hack, to get around the fact that at least some JVMs are buggy, in that files which
                //       have been closed are hung onto, pending garbage collection.  By suggesting garbage collection,
                //       the next time, the delete -magically- works.
                int iFails = 1;
                int iDelay = 1;
                while (!m_oFileSource.delete())
                {
                    System.gc();    // this will close the open file
                    Thread.sleep(iDelay);
                    iFails++;
                    iDelay *= 2;
                    if (iFails > 10)
                    {
                        throw new ID3Exception("Unable to delete original file.");
                    }
                }
            }
            if (! oTmpFileSource.renameTo(m_oFileSource))
            {
                throw new ID3Exception("Unable to rename temporary file " + oTmpFileSource.toString() + " to " + m_oFileSource.toString() + ".");
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error processing [" + m_oFileSource.getName() + "].", e);
        }
    }
    
    /** Update the contents of the actual MP3 file to reflect the current ID3 V2 tag settings of the object.
     *
     * @throws ID3Exception if an error updating the file occurs
     */
    private void v2Sync()
        throws ID3Exception
    {
        IFileSource oTmpFileSource = null;
        InputStream oSourceIS = null;
        OutputStream oTmpOS = null;
        
        // check first if this tag can be written (ie. unregistered crypto agents, etc.)
        m_oID3V2Tag.sanityCheck();
        
        try
        {
            // open source file for reading
            try
            {
                oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());
            }
            catch (Exception e)
            {
                throw new ID3Exception("Error opening [" + m_oFileSource.getName() + "]", e);
            }

            try
            {
                // create temporary file to work with
                try
                {
                    oTmpFileSource = m_oFileSource.createTempFile("id3.", ".tmp");
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Unable to create temporary file.", e);
                }

                // open temp file for writing
                try
                {
                    oTmpOS = new BufferedOutputStream(oTmpFileSource.getOutputStream());
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Error opening temporary file for writing.", e);
                }

                try
                {
                    // write tag to beginning of file (if we were going to write somewhere other than the beginning,
                    // we'd have to deal with that somewhere in this method)
                    m_oID3V2Tag.write(oTmpOS);

                    // copy over the MP3 data from the source file to the temp file
                    // (need to check if there is a V2 tag in the source file, and skip over them if they exist)
                    byte[] abyCheckTag = new byte[3];
                    oSourceIS.read(abyCheckTag);
                    if ((abyCheckTag[0] == 'I') && (abyCheckTag[1] == 'D') && (abyCheckTag[2] == '3'))
                    {
                        // there is a tag in this file.. skip over them
                        // read version information
                        int iVersion = oSourceIS.read();
                        int iPatch = oSourceIS.read();
                        if (iVersion > 4)
                        {
                            // close and remove temp file
                            oTmpOS.close();
                            oTmpFileSource.delete();

                            throw new ID3Exception("Will not overwrite tag of version greater than 2.4.0.");
                        }
                        // skip flags
                        oSourceIS.skip(1);
                        // get tag length
                        byte[] abyTagLength = new byte[4];
                        if (oSourceIS.read(abyTagLength) != 4)
                        {
                            throw new ID3Exception("Error reading existing ID3 tag.");
                        }
                        ID3DataInputStream oID3DIS = new ID3DataInputStream(new ByteArrayInputStream(abyTagLength));
                        long iTagLength = oID3DIS.readID3Four();
                        oID3DIS.close();
                        while (iTagLength > 0)
                        {
                            long iNumSkipped = oSourceIS.skip(iTagLength);
                            if (iNumSkipped == 0)
                            {
                                throw new ID3Exception("Error reading existing ID3 tag.");
                            }
                            iTagLength -= iNumSkipped;
                        }
                    }
                    else
                    {
                        // there is no tag in this file...
                        oTmpOS.write(abyCheckTag);
                    }

                    // copy over rest of the file
                    byte[] abyBuffer = new byte[65536];
                    int iNumRead;
                    while ((iNumRead = oSourceIS.read(abyBuffer)) != -1)
                    {
                        oTmpOS.write(abyBuffer, 0, iNumRead);
                    }

                    // we're done
                    oTmpOS.flush();
                }
                finally
                {
                    oTmpOS.close();
                }
            }
            finally
            {
                oSourceIS.close();
            }
            
            // move temp file to original source file
            if (! m_oFileSource.delete())
            {
                //HACK:  This is a hack, to get around the fact that at least some JVMs are buggy, in that files which
                //       have been closed are hung onto, pending garbage collection.  By suggesting garbage collection,
                //       the next time, the delete -magically- works.
                int iFails = 1;
                int iDelay = 1;
                while (!m_oFileSource.delete())
                {
                    System.gc();    // this will close the open file
                    Thread.sleep(iDelay);
                    iFails++;
                    iDelay *= 2;
                    if (iFails > 10)
                    {
                        throw new ID3Exception("Unable to delete original file.");
                    }
                }
            }
            if (! oTmpFileSource.renameTo(m_oFileSource))
            {
                throw new ID3Exception("Unable to rename temporary file " + oTmpFileSource.toString() + " to " + m_oFileSource.toString() + ".");
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error processing [" + m_oFileSource.getName() + "].", e);
        }
    }

    /* (non-Javadoc)
     * @see org.blinkenlights.id3.MediaFile#getTags()
     */
    public ID3Tag[] getTags()
        throws ID3Exception
    {
        List oID3TagList = new ArrayList();
        
        // get ID3V1Tag if they exist
        ID3V1Tag oID3V1Tag = getID3V1Tag();
        if (oID3V1Tag != null)
        {
            oID3TagList.add(oID3V1Tag);
        }
        
        // get ID3V2Tag if they exist
        ID3V2Tag oID3V2Tag = getID3V2Tag();
        if (oID3V2Tag != null)
        {
            oID3TagList.add(oID3V2Tag);
        }
        
        return (ID3Tag[])oID3TagList.toArray(new ID3Tag[0]);
    }

    public ID3V1Tag getID3V1Tag()
        throws ID3Exception
    {
        try
        {
            InputStream oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());

            try
            {
                // copy over all of the file up to the last 128 bytes
                long lFileLength = m_oFileSource.length();
                oSourceIS.skip(lFileLength - 128);

                // check if V1 tag is present
                byte[] abyCheckTag = new byte[3];
                oSourceIS.read(abyCheckTag);
                if ((abyCheckTag[0] == 'T') && (abyCheckTag[1] == 'A') && (abyCheckTag[2] == 'G'))
                {
                    // there is a tag, we must read it
                    ID3V1Tag oID3V1Tag = ID3V1Tag.read(oSourceIS);

                    return oID3V1Tag;
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                oSourceIS.close();
            }
        }
        catch (Exception e)
        {
            throw new ID3Exception(e);
        }
    }
    
    public ID3V2Tag getID3V2Tag()
        throws ID3Exception
    {
        //TODO: We're only checking for v2.3.0 tags here now.  We'd otherwise have to find
        //      the "ID3" identifier in the file first.
        try
        {
            InputStream oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());
            ID3DataInputStream oSourceID3DIS = new ID3DataInputStream(oSourceIS);

            try
            {
                // check if v2 tag is present
                byte[] abyCheckTag = new byte[3];
                oSourceID3DIS.readFully(abyCheckTag);
                if ((abyCheckTag[0] == 'I') && (abyCheckTag[1] == 'D') && (abyCheckTag[2] == '3'))
                {
                    return ID3V2Tag.read(oSourceID3DIS);
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                oSourceID3DIS.close();
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error reading tags from file.", e);
        }
    }
    
    public void removeTags()
        throws ID3Exception
    {
        removeID3V1Tag();
        
        removeID3V2Tag();
    }
    
    public void removeID3V1Tag()
        throws ID3Exception
    {
        IFileSource oTmpFileSource = null;
        InputStream oSourceIS = null;
        OutputStream oTmpOS = null;

        try
        {
            // open source file for reading
            try
            {
                oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());
            }
            catch (Exception e)
            {
                throw new ID3Exception("Error opening [" + m_oFileSource.getName() + "]", e);
            }

            try
            {
                // create temporary file to work with
                try
                {
                    oTmpFileSource = m_oFileSource.createTempFile("id3.", ".tmp");
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Unable to create temporary file.", e);
                }

                // open temp file for writing
                try
                {
                    oTmpOS = new BufferedOutputStream(oTmpFileSource.getOutputStream());
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Error opening temporary file for writing.", e);
                }

                try
                {
                    // copy over all of the source file up to but not including the V1 tags,
                    // if they are present
                    long lFileLength = m_oFileSource.length();
                    // copy over all of the file up to the last 128 bytes (in 64k blocks for speed, while remaining memory efficient)
                    byte[] abyBuffer = new byte[65536];
                    long lCopied = 0;
                    long lTotalToCopy = lFileLength - 128;
                    while (lCopied < lTotalToCopy)
                    {
                        long lLeftToCopy = lTotalToCopy - lCopied;
                        long lToCopyNow = (lLeftToCopy >= 65536) ? 65536 : lLeftToCopy;
                        oSourceIS.read(abyBuffer, 0, (int)lToCopyNow);
                        oTmpOS.write(abyBuffer, 0, (int)lToCopyNow);
                        lCopied += lToCopyNow;
                    }

                    // check next three bytes of source file which indicate whether this file already
                    // has a V1 tag on it or not
                    byte[] abyCheckTag = new byte[3];
                    oSourceIS.read(abyCheckTag);
                    if ( ! ((abyCheckTag[0] == 'T') && (abyCheckTag[1] == 'A') && (abyCheckTag[2] == 'G')))
                    {
                        // no V1 tag on this file... copy the rest of it over (3 + 125 = 128 bytes)
                        oTmpOS.write(abyCheckTag);
                        for (int i=0; i < 125; i++)
                        {
                            oTmpOS.write(oSourceIS.read());
                        }
                    }

                    // we're done
                    oTmpOS.flush();
                }
                finally
                {
                    oTmpOS.close();
                }
            }
            finally
            {
                oSourceIS.close();
            }
            
            // move temp file to original source file
            if (! m_oFileSource.delete())
            {
                //HACK:  This is a hack, to get around the fact that at least some JVMs are buggy, in that files which
                //       have been closed are hung onto, pending garbage collection.  By suggesting garbage collection,
                //       the next time, the delete -magically- works.
                int iFails = 1;
                int iDelay = 1;
                while (!m_oFileSource.delete())
                {
                    System.gc();    // this will close the open file
                    Thread.sleep(iDelay);
                    iFails++;
                    iDelay *= 2;
                    if (iFails > 10)
                    {
                        throw new ID3Exception("Unable to delete original file.");
                    }
                }
            }
            if (! oTmpFileSource.renameTo(m_oFileSource))
            {
                throw new ID3Exception("Unable to rename temporary file " + oTmpFileSource.toString() + " to " + m_oFileSource.toString() + ".");
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error processing [" + m_oFileSource.getName() + "].", e);
        }
    }
    
    public void removeID3V2Tag()
        throws ID3Exception
    {
        IFileSource oTmpFileSource = null;
        InputStream oSourceIS = null;
        OutputStream oTmpOS = null;
        
        // create temporary file to work with
        try
        {
            oTmpFileSource = m_oFileSource.createTempFile("id3.", ".tmp");
        }
        catch (Exception e)
        {
            throw new ID3Exception("Unable to create temporary file.", e);
        }
        
        try
        {
            // open source file for reading
            try
            {
                oSourceIS = new BufferedInputStream(m_oFileSource.getInputStream());
            }
            catch (Exception e)
            {
                throw new ID3Exception("Error opening [" + m_oFileSource.getName() + "]", e);
            }

            try
            {
                // open temp file for writing
                try
                {
                    oTmpOS = new BufferedOutputStream(oTmpFileSource.getOutputStream());
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Error opening temporary file for writing.", e);
                }

                try
                {
                    // copy over the MP3 data from the source file to the temp file
                    // (need to check if there is a V2 tag in the source file, and skip over them if they exist)
                    byte[] abyCheckTag = new byte[3];
                    oSourceIS.read(abyCheckTag);
                    if ((abyCheckTag[0] == 'I') && (abyCheckTag[1] == 'D') && (abyCheckTag[2] == '3'))
                    {
                        // there is a tag in this file.. skip over it
                        // read version information
                        int iVersion = oSourceIS.read();
                        int iPatch = oSourceIS.read();
                        // skip flags
                        oSourceIS.skip(1);
                        // get tag length
                        byte[] abyTagLength = new byte[4];
                        if (oSourceIS.read(abyTagLength) != 4)
                        {
                            throw new ID3Exception("Error reading existing ID3 tags.");
                        }
                        ID3DataInputStream oID3DIS = new ID3DataInputStream(new ByteArrayInputStream(abyTagLength));
                        long iTagLength = oID3DIS.readID3Four();
                        oID3DIS.close();
                        while (iTagLength > 0)
                        {
                            long iNumSkipped = oSourceIS.skip(iTagLength);
                            if (iNumSkipped == 0)
                            {
                                throw new ID3Exception("Error reading existing ID3 tag.");
                            }
                            iTagLength -= iNumSkipped;
                        }
                    }
                    else
                    {
                        // there are no tags in this file...
                        oTmpOS.write(abyCheckTag);
                    }

                    // copy over rest of the file
                    byte[] abyBuffer = new byte[65536];
                    int iNumRead;
                    while ((iNumRead = oSourceIS.read(abyBuffer)) != -1)
                    {
                        oTmpOS.write(abyBuffer, 0, iNumRead);
                    }

                    // we're done
                    oTmpOS.flush();
                }
                finally
                {
                    oTmpOS.close();
                }
            }
            finally
            {
                oSourceIS.close();
            }
            
            // move temp file to original source file
            if (! m_oFileSource.delete())
            {
                //HACK:  This is a hack, to get around the fact that at least some JVMs are buggy, in that files which
                //       have been closed are hung onto, pending garbage collection.  By suggesting garbage collection,
                //       the next time, the delete -magically- works.
                int iFails = 1;
                int iDelay = 1;
                while (!m_oFileSource.delete())
                {
                    System.gc();    // this will close the open file
                    Thread.sleep(iDelay);
                    iFails++;
                    iDelay *= 2;
                    if (iFails > 10)
                    {
                        throw new ID3Exception("Unable to delete original file.");
                    }
                }
            }
            if (! oTmpFileSource.renameTo(m_oFileSource))
            {
                throw new ID3Exception("Unable to rename temporary file " + oTmpFileSource.toString() + " to " + m_oFileSource.toString() + ".");
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Error processing [" + m_oFileSource.getName() + "].", e);
        }
    }
}
