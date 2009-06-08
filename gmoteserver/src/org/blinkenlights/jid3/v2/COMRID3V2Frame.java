/*
 * COMRID3V2Frame.java
 *
 * Created on Jan 18, 2004
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
 * $Id: COMRID3V2Frame.java,v 1.11 2005/02/06 18:11:20 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Frame containing commercials for the recording in this track.
 */
public class COMRID3V2Frame extends ID3V2Frame
{
    public final static byte RECEIVED_AS_OTHER = 0;
    public final static byte RECEIVED_AS_STANDARD_ALBUM = 1;
    public final static byte RECEIVED_AS_COMPRESSED_AUDIO_ON_CD = 2;
    public final static byte RECEIVED_AS_FILE_OVER_THE_INTERNET = 3;
    public final static byte RECEIVED_AS_STREAM_OVER_THE_INTERNET= 4;
    public final static byte RECEIVED_AS_NOTE_SHEETS = 5;
    public final static byte RECEIVED_AS_NOTE_SHEETS_IN_A_BOOK_WITH_OTHER_SHEETS = 6;
    public final static byte RECEIVED_AS_MUSIC_ON_OTHER_MEDIA = 7;
    public final static byte RECEIVED_AS_NON_MUSICAL_MERCHANDISE = 8;
    
    private TextEncoding m_oTextEncoding;
    private String m_sPrice = null;
    private String m_sValidUntil = null;
    private String m_sContactUrl = null;
    private byte m_byReceivedAs;
    private String m_sNameOfSeller = null;
    private String m_sDescription = null;
    private String m_sPictureMimeType = null;
    private byte[] m_abySellerLogoData = null;

    /** Constructor.
     *
     * @param sPrice a price(s) string (a price string consists of a three letter ISO-4217 currency code,
     *        followed by an amount, where "." is used as the decimal separator).  Multiple prices may be separated
     *        by a "/" characters.
     * @param sValidUntil the date the prices offer is valid until, in the format YYYYMMDD
     * @param sContactUrl an URL at which contact can be made with the seller
     * @param byReceivedAs byte specifying how the track will be delivered when purchased
     * @param sNameOfSeller the name of the seller
     * @param sDescription short description of the product
     * @param sPictureMimeType the mime type of the picture (only "image/jpeg" and "image/png" are allowed
     *        by the ID3 specification)
     * @param abySellerLogoData the image data containing the seller's logo
     *
     * @throws ID3Exception if sPrice is null or invalid
     * @throws ID3Exception if sValidUntil is null or invalid
     * @throws ID3Exception if sContactUrl is null
     * @throws ID3Exception if sNameOfSeller is null
     * @throws ID3Exception if sDecription is null
     */
    public COMRID3V2Frame(String sPrice,
                          String sValidUntil,
                          String sContactUrl,
                          byte byReceivedAs,
                          String sNameOfSeller,
                          String sDescription,
                          String sPictureMimeType,
                          byte[] abySellerLogoData)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sPrice == null)
        {
            throw new ID3Exception("Price required in COMR frame.");
        }
        if ( ! sPrice.matches("(?uis)(\\w{3}\\d*\\.?\\d+/?)+"))
        {
            throw new ID3Exception("Invalid COMR frame price string.");
        }
        m_sPrice = sPrice;
        if (sValidUntil == null)
        {
            throw new ID3Exception("Valid until valud required in COMR frame.");
        }
        if ( ! sValidUntil.matches("(?uis)\\d{8}"))
        {
            throw new ID3Exception("Invalid COMR frame valid until date.");
        }
        m_sValidUntil = sValidUntil;
        if (sContactUrl == null)
        {
            throw new ID3Exception("Contact URL required in COMR frame.");
        }
        m_sContactUrl = sContactUrl;
        m_byReceivedAs = byReceivedAs;
        if (sNameOfSeller == null)
        {
            throw new ID3Exception("Name of seller required in COMR frame.");
        }
        m_sNameOfSeller = sNameOfSeller;
        if (sDescription == null)
        {
            throw new ID3Exception("Description required in COMR frame.");
        }
        m_sDescription = sDescription;
        m_sPictureMimeType = sPictureMimeType;
        if (m_sPictureMimeType == null)
        {
            m_sPictureMimeType = "image/";
        }
        m_abySellerLogoData = abySellerLogoData;
    }
    
    public COMRID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        // Parse out the text encoding and text string from the raw data
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            
            // price (read to null)
            m_sPrice = oFrameDataID3DIS.readStringToNull();
            
            // valid until
            byte[] abyValidUntil = new byte[8];
            oFrameDataID3DIS.readFully(abyValidUntil);
            m_sValidUntil = new String(abyValidUntil);
            
            // contact url (read to null)
            m_sContactUrl = oFrameDataID3DIS.readStringToNull();
            
            // received as
            m_byReceivedAs = (byte)oFrameDataID3DIS.readUnsignedByte();

            // name of seller (read to null)
            m_sNameOfSeller = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);

            // description (read to null)
            m_sDescription = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
            
            // is there a company logo picture coming?
            if (oFrameDataID3DIS.available() > 0)
            {
                // company logo mime type (read to null)
                m_sPictureMimeType = oFrameDataID3DIS.readStringToNull();
                
                // company logo picture data
                m_abySellerLogoData = new byte[oFrameDataID3DIS.available()];
                oFrameDataID3DIS.readFully(m_abySellerLogoData);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitCOMRID3V2Frame(this);
    }

    /** Set commercial information.
     *
     * @param sPrice a price(s) string (a price string consists of a three letter ISO-4217 currency code,
     *        followed by an amount, where "." is used as the decimal separator).  Multiple prices may be separated
     *        by a "/" characters.
     * @param sValidUntil the date the prices offer is valid until, in the format YYYYMMDD
     * @param sContactUrl an URL at which contact can be made with the seller
     * @param byReceivedAs byte specifying how the track will be delivered when purchased
     * @param sNameOfSeller the name of the seller
     * @param sDescription short description of the product
     * @param sPictureMimeType the mime type of the picture (only "image/jpeg" and "image/png" are allowed
     *        by the ID3 specification)
     * @param abySellerLogoData the image data containing the seller's logo
     *
     * @throws ID3Exception if sPrice is null or invalid
     * @throws ID3Exception if sValidUntil is null or invalid
     * @throws ID3Exception if sContactUrl is null
     * @throws ID3Exception if sNameOfSeller is null
     * @throws ID3Exception if sDecription is null
     */
    public void setCommercialInformation(String sPrice,
                                         String sValidUntil,
                                         String sContactUrl,
                                         byte byReceivedAs,
                                         String sNameOfSeller,
                                         String sDescription,
                                         String sPictureMimeType,
                                         byte[] abySellerLogoData)
        throws ID3Exception
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        if (sPrice == null)
        {
            throw new ID3Exception("Price required in COMR frame.");
        }
        if ( ! sPrice.matches("(?uis)(\\w{3}\\d*\\.?\\d+/?)+"))
        {
            throw new ID3Exception("Invalid COMR frame price string.");
        }
        m_sPrice = sPrice;
        if (sValidUntil == null)
        {
            throw new ID3Exception("Valid until date required in COMR frame.");
        }
        if ( ! sValidUntil.matches("(?uis)\\d{8}"))
        {
            throw new ID3Exception("Invalid COMR frame valid until date.");
        }
        m_sValidUntil = sValidUntil;
        if (sContactUrl == null)
        {
            throw new ID3Exception("Contact URL required in COMR frame.");
        }
        m_sContactUrl = sContactUrl;
        m_byReceivedAs = byReceivedAs;
        if (sNameOfSeller == null)
        {
            throw new ID3Exception("Name of seller required in COMR frame.");
        }
        m_sNameOfSeller = sNameOfSeller;
        if (sDescription == null)
        {
            throw new ID3Exception("Description required in COMR frame.");
        }
        m_sDescription = sDescription;
        m_sPictureMimeType = sPictureMimeType;
        if (m_sPictureMimeType == null)
        {
            m_sPictureMimeType = "image/";
        }
        m_abySellerLogoData = abySellerLogoData;
    }
    
    /** Get price.
     *
     * @return price string
     */
    public String getPrice()
    {
        return m_sPrice;
    }
    
    /** Get valid until date.
     *
     * @return valid until date
     */
    public String getValidUntilDate()
    {
        return m_sValidUntil;
    }
    
    /** Get contact URL string.
     *
     * @return the contact URL string
     */
    public String getContactUrl()
    {
        return m_sContactUrl;
    }
    
    /** Get received as format.
     *
     * @return the byte specifying the received as format
     */
    public byte getReceivedAsFormat()
    {
        return m_byReceivedAs;
    }
    
    /** Get name of seller.
     *
     * @return the name of the seller
     */
    public String getNameOfSeller()
    {
        return m_sNameOfSeller;
    }
    
    /** Get description of item.
     *
     * @return the description of the item being sold
     */
    public String getDescription()
    {
        return m_sDescription;
    }
    
    /** Get mime type of the seller logo image.
     *
     * @return mime type of the seller logo image
     */
    public String getSellerLogoMimeType()
    {
        return m_sPictureMimeType;
    }
    
    /** Get image data for the seller logo.
     *
     * @return image data for the seller logo
     */
    public byte[] getSellerLogoData()
    {
        return m_abySellerLogoData;
    }

    /** Set the text encoding to be used for the name of seller and description in this frame.
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

    /** Get the text encoding used for the name of seller and description in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "COMR".getBytes();
    }
    
    public String toString()
    {
        return "Commercial Frame: Price=[" + m_sPrice + "], Valid Until=[" + m_sValidUntil + "], Contact URL=[" +
               m_sContactUrl + "], Received As=" + m_byReceivedAs + ", Name Of Seller=[" + m_sNameOfSeller +
               "], Description=[" + m_sDescription + "], Picture Mime Type=[" + m_sPictureMimeType + "]";
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // price string
        oIDOS.write(m_sPrice.getBytes());
        oIDOS.writeUnsignedByte(0);
        // valid until
        oIDOS.write(m_sValidUntil.getBytes());
        // contact url
        oIDOS.write(m_sContactUrl.getBytes());
        oIDOS.writeUnsignedByte(0);
        // received as
        oIDOS.writeUnsignedByte(m_byReceivedAs);
        // name of seller
        if (m_sNameOfSeller != null)
        {
            oIDOS.write(m_sNameOfSeller.getBytes(m_oTextEncoding.getEncodingString()));
        }
        // null terminating optional name of seller
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // description
        if (m_sDescription != null)
        {
            oIDOS.write(m_sDescription.getBytes(m_oTextEncoding.getEncodingString()));
        }
        // null terminating optional description
        if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
        {
            oIDOS.writeUnsignedByte(0);
        }
        else
        {
            oIDOS.writeUnsignedByte(0);
            oIDOS.writeUnsignedByte(0);
        }
        // optional company logo image
        if (m_abySellerLogoData != null)
        {
            // image mime type (optional, "image/" assumed if not set)
            if (m_sPictureMimeType != null)
            {
                oIDOS.write(m_sPictureMimeType.getBytes());
            }
            oIDOS.writeUnsignedByte(0); // terminating null
            
            // actual image data
            oIDOS.write(m_abySellerLogoData);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof COMRID3V2Frame)))
        {
            return false;
        }
        
        COMRID3V2Frame oOtherCOMR = (COMRID3V2Frame)oOther;
        
        return (m_oTextEncoding.equals(oOtherCOMR.m_oTextEncoding) &&
                m_sPrice.equals(oOtherCOMR.m_sPrice) &&
                m_sValidUntil.equals(oOtherCOMR.m_sValidUntil) &&
                m_sContactUrl.equals(oOtherCOMR.m_sContactUrl) &&
                (m_byReceivedAs == oOtherCOMR.m_byReceivedAs) &&
                m_sNameOfSeller.equals(oOtherCOMR.m_sNameOfSeller) &&
                m_sDescription.equals(oOtherCOMR.m_sDescription) &&
                m_sPictureMimeType.equals(oOtherCOMR.m_sPictureMimeType) &&
                Arrays.equals(m_abySellerLogoData, oOtherCOMR.m_abySellerLogoData));
    }
}
