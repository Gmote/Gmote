/*
 * OWNEID3V2Frame.java
 *
 * Created on August 30, 2004, 2:05 AM
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
 * $Id: OWNEID3V2Frame.java,v 1.9 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/** Frame containing ownership information.
 *
 * @author  paul
 */
public class OWNEID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private String m_sPricePaid = null;
    private String m_sDateOfPurchase = null;
    private String m_sSeller = null;
    
    /** Creates a new instance of OWNEID3V2Frame 
     *
     * @param sPricePaid a price(s) string (a price string consists of a three letter ISO-4217 currency code,
     *        followed by an amount, where "." is used as the decimal separator).  Multiple prices may be separated
     *        by a "/" characters
     * @param sDateOfPurchase the date of the purchase, in the format YYYYMMDD
     * @param sSeller the name of the seller of this file
     * @throws ID3Exception if sPricePaid is null or not in a valid format, if sDateOfPurchase is null or
     *                      not in the correct format, or if sSeller is null
     */
    public OWNEID3V2Frame(String sPricePaid,
                          String sDateOfPurchase,
                          String sSeller)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sPricePaid == null)
        {
            throw new ID3Exception("Price paid required in OWNE frame.");
        }
        if ( ! sPricePaid.matches("(?uis)(\\w{3}\\d*\\.?\\d+/?)+"))
        {
            throw new ID3Exception("Invalid OWNE frame price string.");
        }
        m_sPricePaid = sPricePaid;
        if (sDateOfPurchase == null)
        {
            throw new ID3Exception("Date of purchase required in OWNE frame.");
        }
        if ( ! sDateOfPurchase.matches("(?uis)\\d{8}"))
        {
            throw new ID3Exception("Invalid date of purchase format in OWNE frame.");
        }
        m_sDateOfPurchase = sDateOfPurchase;
        if (sSeller == null)
        {
            throw new ID3Exception("Seller required in OWNE frame.");
        }
        m_sSeller = sSeller;
    }
    
    public OWNEID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            
            // price paid (read to null)
            m_sPricePaid = oFrameDataID3DIS.readStringToNull();
            
            // date of purchase (eight digits)
            byte[] abyDateOfPurchase = new byte[8];
            oFrameDataID3DIS.readFully(abyDateOfPurchase);
            m_sDateOfPurchase = new String(abyDateOfPurchase);

            // seller (to end)
            byte[] abySeller = new byte[oFrameDataID3DIS.available()];
            oFrameDataID3DIS.readFully(abySeller);
            m_sSeller = new String(abySeller, m_oTextEncoding.getEncodingString());
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitOWNEID3V2Frame(this);
    }
    
    /** Set ownership information.
     * 
     * @param sPricePaid a price(s) string (a price string consists of a three letter ISO-4217 currency code,
     *        followed by an amount, where "." is used as the decimal separator).  Multiple prices may be separated
     *        by a "/" characters
     * @param sDateOfPurchase the date of the purchase, in the format YYYYMMDD
     * @param sSeller the name of the seller of this file
     * @throws ID3Exception if sPricePaid is null or not in a valid format, if sDateOfPurchase is null or
     *                      not in the correct format, or if sSeller is null
     */
    public void setOwnershipInformation(String sPricePaid, String sDateOfPurchase, String sSeller)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sPricePaid == null)
        {
            throw new ID3Exception("Price paid required in OWNE frame.");
        }
        if ( ! sPricePaid.matches("(?uis)(\\w{3}\\d*\\.?\\d+/?)+"))
        {
            throw new ID3Exception("Invalid OWNE frame price paid string.");
        }
        m_sPricePaid = sPricePaid;
        if (sDateOfPurchase == null)
        {
            throw new ID3Exception("Date of purchase required in OWNE frame.");
        }
        if ( ! sDateOfPurchase.matches("(?uis)\\d{8}"))
        {
            throw new ID3Exception("Invalid OWNE frame date of purchase.");
        }
        m_sDateOfPurchase = sDateOfPurchase;
        if (sSeller == null)
        {
            throw new ID3Exception("Seller required in OWNE frame.");
        }
        m_sSeller = sSeller;
    }
    
    /** Get price paid.
     *
     * @return the price paid
     */
    public String getPricePaid()
    {
        return m_sPricePaid;
    }
    
    /** Get the date of purchase.
     *
     * @return the date of purchase
     */
    public String getDateOfPurchase()
    {
        return m_sDateOfPurchase;
    }
    
    /** Get the name of the seller.
     *
     * @return the name of the seller
     */
    public String getSeller()
    {
        return m_sSeller;
    }

    /** Set the text encoding to be used for the seller in this frame.
     *
     * @param oTextEncoding the text encoding to be used for this frame
     */
    public void setTextEncoding(TextEncoding oTextEncoding)
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Text encoding cannot be null.");
        }
        m_oTextEncoding = oTextEncoding;
    }

    /** Get the text encoding used for the seller in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "OWNE".getBytes();
    }
    
    public String toString()
    {
        return "Ownership Frame: Price paid=[" + m_sPricePaid + "], Date of purchase=[" + m_sDateOfPurchase +
               "], Seller=[" + m_sSeller + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // price paid string
        oIDOS.write(m_sPricePaid.getBytes());
        oIDOS.writeUnsignedByte(0);
        // date of purchase until
        oIDOS.write(m_sDateOfPurchase.getBytes());
        // seller
        oIDOS.write(m_sSeller.getBytes(m_oTextEncoding.getEncodingString()));
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof OWNEID3V2Frame)))
        {
            return false;
        }
        
        OWNEID3V2Frame oOtherOWNE = (OWNEID3V2Frame)oOther;
        
        return (m_oTextEncoding.equals(oOtherOWNE.m_oTextEncoding) &&
                m_sPricePaid.equals(oOtherOWNE.m_sPricePaid) &&
                m_sDateOfPurchase.equals(oOtherOWNE.m_sDateOfPurchase) &&
                m_sSeller.equals(oOtherOWNE.m_sSeller));
    }
}
