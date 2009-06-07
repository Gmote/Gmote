/*
 * ID3CryptException.java
 *
 * Created on May 13, 2004, 1:03 AM
 *
 * Copyright (C)2004 Paul Grebenc
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
 * $Id: ID3CryptException.java,v 1.2 2005/02/06 18:11:26 paul Exp $
 */

package org.blinkenlights.jid3.crypt;

import org.blinkenlights.jid3.*;

/**
 *
 * @author  paul
 */
public class ID3CryptException extends ID3Exception
{
    public ID3CryptException()
    {
        super();
    }

    public ID3CryptException(String arg0)
    {
        super(arg0);
    }

    public ID3CryptException(Throwable arg0)
    {
        super(arg0);
    }

    public ID3CryptException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }
}
