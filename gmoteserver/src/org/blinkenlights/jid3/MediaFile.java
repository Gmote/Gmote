/*
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
 * $Id: MediaFile.java,v 1.9 2005/11/22 02:10:41 paul Exp $
 */

package org.blinkenlights.jid3;

import java.io.*;

import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;

/**
 * @author paul
 *
 * The base class for all files which can contain ID3 tags.
 */
abstract public class MediaFile
{
    protected IFileSource m_oFileSource = null;
    protected ID3V1Tag m_oID3V1Tag = null;
    protected ID3V2Tag m_oID3V2Tag = null;

    /** Constructor for a media file object, representing a given file.
     *
     * @param oSourceFile the file which this object represents
     */
    public MediaFile(File oSourceFile)
    {
        m_oFileSource = new FileSource(oSourceFile);
    }
    
    public MediaFile(IFileSource oFileSource)
    {
        m_oFileSource = oFileSource;
    }

    /** Update the actual file to reflect the current state of the tags as set in this object.
     *
     * @throws ID3Exception if an error occurs updating the file
     */
    abstract public void sync() throws ID3Exception;

    /** Get any tags stored in the file.
     *
     * @return an array of tags which are contained in the file
     * @throws ID3Exception if there is any error reading the file
     */
    abstract public ID3Tag[] getTags() throws ID3Exception;

    /** Get the ID3 V1 tag from this file.
     *
     * @return a V1 tag object, if V1 tags are contained in the file
     * @throws ID3Exception if there is an error reading the file
     */
    abstract public ID3V1Tag getID3V1Tag() throws ID3Exception;

    /** Get the ID3 V2 tag from this file.
     *
     * @return a V2 tag object, if V2 tags are contained in the file
     * @throws ID3Exception if there is an error reading the file
     */
    abstract public ID3V2Tag getID3V2Tag() throws ID3Exception;
    
    /** Remove both V1 and V2 tags from this file if present.
     *
     * @throws ID3Exception if there is an error processing the file
     */
    abstract public void removeTags() throws ID3Exception;
    
    /** Remove V1 tags from this file if present.
     *
     * @throws ID3Exception if there is an error processing the file
     */
    abstract public void removeID3V1Tag() throws ID3Exception;
    
    /** Remove V2 tags from this file if present.
     *
     * @throws ID3Exception if there is an error processing the file
     */
    abstract public void removeID3V2Tag() throws ID3Exception;

    /** Set an ID3 tag in this object.  Note this method does not cause the contents of the actual file to be modified.
     *
     * @param oID3Tag tag to be set for this media file
     * @return If any tags were overwritten by the new tags, they are returned.
     *         Otherwise, null is returned.
     */    
    public ID3Tag setID3Tag(ID3Tag oID3Tag)
    {
        if (oID3Tag instanceof ID3V1Tag)
        {
            ID3V1Tag oOldID3V1Tag = m_oID3V1Tag;
            m_oID3V1Tag = (ID3V1Tag)oID3Tag;
            
            return oOldID3V1Tag;
        }
        else if (oID3Tag instanceof ID3V2Tag)
        {
            ID3V2Tag oOldID3V2Tag = m_oID3V2Tag;
            m_oID3V2Tag = (ID3V2Tag)oID3Tag;
            
            return oOldID3V2Tag;
        }
        
        return null;
    }
}
