/*
 * ID3Visitable.java
 *
 * Created on January 14, 2005, 8:02 PM
 *
 * Copyright (C)2005 Paul Grebenc
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
 * $Id: ID3Visitable.java,v 1.2 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.util;

/** A visitable interface, which allows for the implementation of the visitor pattern.  All tags, and all
 *  frames within V2 tags, are visitable.
 *
 * @author  paul
 */
public interface ID3Visitable
{
    /** Accept a visitor.
     *
     * @param oID3Visitor a visitor of this object
     */
    public void accept(ID3Visitor oID3Visitor);
}
