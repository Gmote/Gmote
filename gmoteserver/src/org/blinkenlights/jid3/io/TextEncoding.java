/*
 * TextEncoding.java
 *
 * Created on September 25, 2004, 9:27 PM
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
 * $Id: TextEncoding.java,v 1.2 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.io;

import org.blinkenlights.jid3.*;

/** Text encoding representation used in v2 frames.
 *
 * @author  paul
 */
public class TextEncoding
{
    private byte m_byEncoding;

    private TextEncoding(byte byEncoding)
    {
        m_byEncoding = byEncoding;
    }

    /** Get the text encoding object represented by a given integer value.
     *
     * @param iEncoding the value corresponding to a given text encoding
     * @return the matching text encoding object
     * @throws ID3Exception if no matching encoding exists
     */
    public static TextEncoding getTextEncoding(int iEncoding)
        throws ID3Exception
    {
        return getTextEncoding((byte)iEncoding);
    }

    /** Get the text encoding object represented by a given byte value.
     *
     * @param byEncoding the value corresponding to a given text encoding
     * @return the matching text encoding object
     * @throws ID3Exception if no matching encoding exists
     */
    public static TextEncoding getTextEncoding(byte byEncoding)
        throws ID3Exception
    {
        switch (byEncoding)
        {
            case (byte)0x00:
                return ISO_8859_1;
            case (byte)0x01:
                return UNICODE;
            default:
                throw new ID3Exception("Unknown text encoding value " + byEncoding + ".");
        }
    }

    /** Get the byte value corresponding to this text encoding.
     *
     * @return the corresponding byte value
     */
    public byte getEncodingValue()
    {
        return m_byEncoding;
    }

    /** Get the Java encoding string matching this text encoding.
     *
     * @return the matching encoding string
     */
    public String getEncodingString()
    {
        switch (m_byEncoding)
        {
            case (byte)0x00:
                return "ISO-8859-1";
            case (byte)0x01:
                return "Unicode";
            default:
                return null;    // can't happen because we control construction of this object
        }
    }

    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof TextEncoding)))
        {
            return false;
        }

        TextEncoding oOtherTextEncoding = (TextEncoding)oOther;

        return (m_byEncoding == oOtherTextEncoding.m_byEncoding);
    }

    public static final TextEncoding ISO_8859_1 = new TextEncoding((byte)0x00);
    public static final TextEncoding UNICODE = new TextEncoding((byte)0x01);

    private static TextEncoding s_oDefaultTextEncoding = ISO_8859_1;

    /** Get the default text encoding which will be used in v2 frames, when not specified.
     *
     * @return the default text encoding used when not specified
     */
    public static TextEncoding getDefaultTextEncoding()
    {
        return s_oDefaultTextEncoding;
    }

    /** Set the default text encoding to be used in v2 frames, when not specified.
     *
     * @param oTextEncoding the default text encoding to be used when not specified
     */
    public static void setDefaultTextEncoding(TextEncoding oTextEncoding)
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Default text encoding cannot be null.");
        }
        s_oDefaultTextEncoding = oTextEncoding;
    }
}
