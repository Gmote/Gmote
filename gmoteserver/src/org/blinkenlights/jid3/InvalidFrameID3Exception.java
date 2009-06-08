/*
 * InvalidFrameID3Exception.java
 *
 * Created on October 10, 2004, 11:24 PM
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
 * $Id: InvalidFrameID3Exception.java,v 1.2 2005/02/06 18:11:30 paul Exp $
 */

package org.blinkenlights.jid3;

import java.io.*;

/** Exception generated when a frame read from a file is invalid.
 *
 * @author  paul
 */
public class InvalidFrameID3Exception extends ID3Exception
{
    /** Creates a new instance of InvalidFrameID3Exception */
    public InvalidFrameID3Exception()
    {
        super();
    }

    public InvalidFrameID3Exception(String arg0)
    {
        super(arg0);
    }

    public InvalidFrameID3Exception(Throwable arg0)
    {
        super(arg0);
    }

    public InvalidFrameID3Exception(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }
    
    public static String getStackTrace(Throwable oThrowable)
    {
        StringWriter oSW = new StringWriter();
        oThrowable.printStackTrace(new PrintWriter(oSW));
        return oSW.getBuffer().toString();
    }
}
