/* ID3Tag.java
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
 * $Id: ID3Tag.java,v 1.6 2005/02/06 18:11:30 paul Exp $
 */

package org.blinkenlights.jid3;

import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Base class for all ID3 tags.
 */
public abstract class ID3Tag implements ID3Visitable
{
    /** Indication of whether invalid frames read from a file should generate an exception (strict),
     *  or be ignored (non-strict).
     */
    private static boolean s_bUseStrict = false;
    
    /**
     * Constructor.
     */
    public ID3Tag()
    {
    }

    /** Represent a tag as string for debugging purposes.
     *
     * @return a string representation of the contents of the tag
     */
    abstract public String toString();

    /** Set whether strict mode should be used or not (default non-strict).  When reading tags from a file, in strict
     *  mode any invalid frames will genarate an exception.  In non-strict mode, any invalid frames will simply
     *  be ignored.
     *
     * @param bUseStrict whether strict mode should be used or not
     */
    public static void useStrict(boolean bUseStrict)
    {
        s_bUseStrict = bUseStrict;
    }

    /** Check whether strict mode is currently set or not.
     *
     * @return true if strict mode is enabled, false otherwise
     */
    public static boolean usingStrict()
    {
        return s_bUseStrict;
    }
}
