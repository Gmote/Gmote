/*
 * ID3V1_0Tag.java
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
 * $Id: ID3V1_0Tag.java,v 1.8 2005/02/06 18:11:26 paul Exp $
 */

package org.blinkenlights.jid3.v1;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * ID3 V1.0 tag object.
 */
public class ID3V1_0Tag extends ID3V1Tag
{
    /**
     * Constructor for ID3 V1.0 Tag.
     */
    public ID3V1_0Tag()
    {
        super();
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitID3V1_0Tag(this);
    }
    
    public void setComment(String sComment)
    {
        if (sComment.length() > 30)
        {
            sComment = sComment.substring(0, 30);
        }
        
        m_sComment = sComment;
    }
    
    public void write(OutputStream oOS)
        throws ID3Exception
    {
        try
        {
            oOS.write("TAG".getBytes());
            // song title
            if (getTitle() != null)
            {
                byte[] abySongTitle = getTitle().getBytes();
                oOS.write(abySongTitle);
                oOS.write(new byte[30 - abySongTitle.length]);  // padding to equal 30 bytes for song title
            }
            else
            {
                oOS.write(new byte[30]);    // no value, just padding
            }
            // artist
            if (getArtist() != null)
            {
                byte[] abyArtist = getArtist().getBytes();
                oOS.write(abyArtist);
                oOS.write(new byte[30 - abyArtist.length]); // padding to equal 30 bytes for artist
            }
            else
            {
                oOS.write(new byte[30]);    // no value, just padding
            }
            // album
            if (getAlbum() != null)
            {
                byte[] abyAlbum = getAlbum().getBytes();
                oOS.write(abyAlbum);
                oOS.write(new byte[30 - abyAlbum.length]);  // padding to equal 30 bytes for album
            }
            else
            {
                oOS.write(new byte[30]);
            }
            // year
            if (getYear() != null)
            {
                byte[] abyYear = getYear().getBytes();
                oOS.write(abyYear);
                oOS.write(new byte[4 - abyYear.length]);    // padding to equal 4 bytes for year
            }
            else
            {
                oOS.write(new byte[4]);     // no value, just padding
            }
            // comment
            if (getComment() != null)
            {
                byte[] abyComment = getComment().getBytes();
                oOS.write(abyComment);
                oOS.write(new byte[30 - abyComment.length]);    // padding to equal 30 bytes for comment
            }
            else
            {
                oOS.write(new byte[30]);    // no value, just padding
            }
            // genre
            if (getGenre() != null)
            {
                oOS.write(getGenre().getByteValue());
            }
            else
            {
                oOS.write(0);
            }
        }
        catch (Exception e)
        {
            throw new ID3Exception(e);
        }
    }        
}
