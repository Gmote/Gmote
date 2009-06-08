/*
 * TCONTextInformationID3V2Frame.java
 *
 * Created on 8-Jan-2004
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
 * $Id: TCONTextInformationID3V2Frame.java,v 1.10 2005/02/06 18:11:18 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Text frame which contains the content type of the track (ie. genre details).
 */
public class TCONTextInformationID3V2Frame extends TextInformationID3V2Frame
{
    private ContentType m_oContentType = null;
    
    /** Constructor.
     *
     * @param oContentType the content type (genre) of this track
     */
    public TCONTextInformationID3V2Frame(ContentType oContentType)
    {
        super("");
        
        m_oContentType = oContentType;
        
        m_sInformation = oContentType.toString();
    }

    public TCONTextInformationID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        super(oIS);
        
        try
        {
            m_oContentType = convertStringToContentType(m_sInformation);
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception("Encountered a corrupt TCON year frame.", e);
        }

    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitTCONTextInformationID3V2Frame(this);
    }

    protected byte[] getFrameId()
    {
        return "TCON".getBytes();
    }
    
    public String toString()
    {
        return "Content type: [" + m_sInformation + "]";
    }

    /** Set the content type (genre) of this frame.
     *
     * @param oContentType the content type of this track
     * @return the previous content type
     */
    public ContentType setContentType(ContentType oContentType)
    {
        ContentType oOldContentType = m_oContentType;
        
        m_oContentType = oContentType;
        
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_sInformation = oContentType.toString();

        return oOldContentType;
    }

    /** Get the content type from this frame.
     *
     * @return the current ContentType
     */
    public ContentType getContentType()
    {
        return m_oContentType;
    }

    /** Internal method to convert a string in the format stored in a frame, to a content type object.
     *
     * @param sContentType a string to be parsed
     * @return a ContentType object representing the string value
     * @throws ID3Exception if there is any error parsing the string value
     */
    private ContentType convertStringToContentType(String sContentType)
        throws ID3Exception
    {
        ContentType oContentType = new ContentType();
        
        try
        {
            String sPiece = null;
            while ((sPiece = getNextPiece(sContentType)).length() > 0)
            {
                // is piece a refinement?
                if ((sPiece.charAt(0) != '(') || (sPiece.startsWith("((")))
                {
                    oContentType.setRefinement(sPiece);
                }
                // cover?
                else if (sPiece.toUpperCase().equals("(CR)"))
                {
                    oContentType.setIsCover(true);
                }
                // remix?
                else if (sPiece.toUpperCase().equals("(RX)"))
                {
                    oContentType.setIsRemix(true);
                }
                // valid genre?
                else
                {
                    String sGenreValue = sPiece.substring(1, sPiece.length()-1);
                    int iGenreValue = Integer.parseInt(sGenreValue);
                    try
                    {
                        ContentType.Genre oGenre = ContentType.Genre.lookupGenre(iGenreValue);
                        oContentType.setGenre(oGenre);
                    }
                    catch (Exception e)
                    {
                        if (ID3Tag.usingStrict())
                        {
                            throw e;
                        }
                        // else, skip what we can't parse
                    }
                }

                sContentType = sContentType.substring(sPiece.length());
            }
        }
        catch (ID3Exception e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ID3Exception("Encountered corrupt content type value in tag.", e);
        }
        
        return oContentType;
    }
    
    /** Internal method to help in parsing a string value to a ContentType.
     */
    private String getNextPiece(String sContentType)
    {
        // there's nothing in an empty string
        if (sContentType.length() == 0)
        {
            return "";
        }
        
        // it's all refinement
        if ((sContentType.charAt(0) != '(') || (sContentType.startsWith("((")))
        {
            return sContentType;
        }
        
        // there's a piece to return here
        return sContentType.substring(0, sContentType.indexOf(')')+1);
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TCONTextInformationID3V2Frame)))
        {
            return false;
        }
        
        TCONTextInformationID3V2Frame oOtherTCON = (TCONTextInformationID3V2Frame)oOther;
        
        return (m_oContentType.equals(oOtherTCON.m_oContentType) &&
                m_oTextEncoding.equals(oOtherTCON.m_oTextEncoding) &&
                m_sInformation.equals(oOtherTCON.m_sInformation));
    }
}
