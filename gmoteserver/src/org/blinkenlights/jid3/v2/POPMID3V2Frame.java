/*
 * POPMID3V2Frame.java
 *
 * Created on September 3, 2004, 11:11 PM
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
 * $Id: POPMID3V2Frame.java,v 1.11 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing a popularimeter.
 *
 * @author  paul
 */
public class POPMID3V2Frame extends ID3V2Frame
{
    private String m_sEmailToUser = null;
    private int m_iPopularity;
    //NOTE: No support for play counts greater than 2^31 (is a value out of this range realistic???)
    private int m_iPlayCount;
    
    /** Creates a new instance of POPMID3V2Frame.
     *
     * @param sEmailToUser the email address of the user to be informed of this track's popularity
     * @param iPopularity the popularity rating of this track (1=worst, 255=best, 0=unknown)
     * @param iPlayCount the current playcount of this file (must be positive)
     * @throws ID3Exception if sEmailToUser not provided, or if either iPopularity or iPlayCount values are out of range
     */
    public POPMID3V2Frame(String sEmailToUser, int iPopularity, int iPlayCount)
        throws ID3Exception
    {
        if ((sEmailToUser == null) || (sEmailToUser.length() == 0))
        {
            throw new ID3Exception("Email address is required in POPM frame.");
        }
        m_sEmailToUser = sEmailToUser;
        if ((iPopularity < 0) || (iPopularity > 255))
        {
            throw new ID3Exception("Popularity must be between 0 and 255 in POPM frame.");
        }
        m_iPopularity = iPopularity;
        if (iPlayCount < 0)
        {
            throw new ID3Exception("Play count cannot be negative in POPM frame.");
        }
        m_iPlayCount = iPlayCount;
    }
    
    /** Creates a new instance of POPMID3V2Frame, not specifying the play count value.
     *
     * @param sEmailToUser the email address of the user to be informed of this track's popularity
     * @param iPopularity the popularity rating of this track (1=worst, 255=best, 0=unknown)
     * @throws ID3Exception if sEmailToUser not provided, or if either iPopularity or iPlayCount values are out of range
     */
    public POPMID3V2Frame(String sEmailToUser, int iPopularity)
        throws ID3Exception
    {
        if ((sEmailToUser == null) || (sEmailToUser.length() == 0))
        {
            throw new ID3Exception("Email address is required in POPM frame.");
        }
        m_sEmailToUser = sEmailToUser;
        if ((iPopularity < 0) || (iPopularity > 255))
        {
            throw new ID3Exception("Popularity must be between 0 and 255 in POPM frame.");
        }
        m_iPopularity = iPopularity;
        m_iPlayCount = -1;
    }

    public POPMID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // email to user
            m_sEmailToUser = oFrameDataID3DIS.readStringToNull();
            
            // popularity
            m_iPopularity = oFrameDataID3DIS.readUnsignedByte();
            
            // play count (optional)
            if (oFrameDataID3DIS.available() > 0)
            {
                if (oFrameDataID3DIS.available() == 4)
                {
                    m_iPlayCount = oFrameDataID3DIS.readBE32();
                }
                else
                {
                    // overflow (seriously, this will never _really_ happen...)
                    m_iPlayCount = Integer.MAX_VALUE;
                }
            }
            else
            {
                // no play count
                m_iPlayCount = -1;
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitPOPMID3V2Frame(this);
    }
    
    /** Set the popularity values for this frame.
     *
     * @param sEmailToUser the email address of the user to be informed of this track's popularity
     * @param iPopularity the popularity rating of this track (1=worst, 255=best, 0=unknown)
     * @param iPlayCount the current playcount of this file (must be positive)
     * @throws ID3Exception if sEmailToUser not provided, or if either iPopularity or iPlayCount values are out of range
     * @throws ID3Exception if this frame is in a tag with another POPM frame which would have the same email address
     */
    public void setPopularity(String sEmailToUser, int iPopularity, int iPlayCount)
        throws ID3Exception
    {
        String sOrigEmailToUser = m_sEmailToUser;
        int iOrigPopularity = m_iPopularity;
        int iOrigPlayCount = m_iPlayCount;
        
        if ((sEmailToUser == null) || (sEmailToUser.length() == 0))
        {
            throw new ID3Exception("Email address is required in POPM frame.");
        }
        if ((iPopularity < 0) || (iPopularity > 255))
        {
            throw new ID3Exception("Popularity must be between 0 and 255 in POPM frame.");
        }
        if (iPlayCount < 0)
        {
            throw new ID3Exception("Play count cannot be negative in POPM frame.");
        }

        m_sEmailToUser = sEmailToUser;
        m_iPopularity = iPopularity;
        m_iPlayCount = iPlayCount;
        
        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sEmailToUser = sOrigEmailToUser;
            m_iPopularity = iOrigPopularity;
            m_iPlayCount = iOrigPlayCount;
            
            throw e;
        }
    }
    
    /** Set the popularity values for this frame, not specifying a play count value.
     *
     * @param sEmailToUser the email address of the user to be informed of this track's popularity
     * @param iPopularity the popularity rating of this track (1=worst, 255=best, 0=unknown)
     * @throws ID3Exception if sEmailToUser not provided, or if either iPopularity or iPlayCount values are out of range
     * @throws ID3Exception if this frame is in a tag with another AENC frame which would have the same email address
     */
    public void setPopularity(String sEmailToUser, int iPopularity)
        throws ID3Exception
    {
        String sOrigEmailToUser = m_sEmailToUser;
        int iOrigPopularity = m_iPopularity;
        int iOrigPlayCount = m_iPlayCount;
        
        if ((sEmailToUser == null) || (sEmailToUser.length() == 0))
        {
            throw new ID3Exception("Email address is required in POPM frame.");
        }
        if ((iPopularity < 0) || (iPopularity > 255))
        {
            throw new ID3Exception("Popularity must be between 0 and 255 in POPM frame.");
        }

        m_sEmailToUser = sEmailToUser;
        m_iPopularity = iPopularity;
        m_iPlayCount = -1;

        // try this update, and reverse it if it generates and error
        try
        {
            notifyID3Observers();
        }
        catch (ID3Exception e)
        {
            m_sEmailToUser = sOrigEmailToUser;
            m_iPopularity = iOrigPopularity;
            m_iPlayCount = iOrigPlayCount;
            
            throw e;
        }
    }
    
    /** Get the email address of the user to be informed about the popularity of this track.
     *
     * @return the email address of the user
     */
    public String getEmailToUser()
    {
        return m_sEmailToUser;
    }
    
    /** Get the popularity rating of this track (1=worst, 255=best, 0=unknown)
     *
     * @return the popularity of this track
     */
    public int getPopularity()
    {
        return m_iPopularity;
    }
    
    /** Get the play count value for this track.
     *
     * @return the play count for this track (-1 = not specified)
     */
    public int getPlayCount()
    {
        return m_iPlayCount;
    }
    
    protected byte[] getFrameId()
    {
        return "POPM".getBytes();
    }
    
    public String toString()
    {
        return "Popularimeter: Email To User=[" + m_sEmailToUser + "], Popularity=" + m_iPopularity +
               "], Play Count=[" + m_iPlayCount + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // email address plus null
        oIDOS.write(m_sEmailToUser.getBytes());
        oIDOS.write(0);
        // popularity
        oIDOS.write(m_iPopularity);
        // play count
        if (m_iPlayCount >= 0)
        {
            oIDOS.writeBE32(m_iPlayCount);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof POPMID3V2Frame)))
        {
            return false;
        }
        
        POPMID3V2Frame oOtherPOPM = (POPMID3V2Frame)oOther;
        
        return (m_sEmailToUser.equals(oOtherPOPM.m_sEmailToUser) &&
                (m_iPopularity == oOtherPOPM.m_iPopularity) &&
                (m_iPlayCount == oOtherPOPM.m_iPlayCount));
    }
}
