/*
 * ID3Observer.java
 *
 * Created on December 14, 2004, 9:19 PM
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
 * $Id: ID3Observer.java,v 1.3 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.util;

import org.blinkenlights.jid3.*;

/**
 *
 * @author  paul
 */
public interface ID3Observer
{
    public void update(ID3Subject oID3Subject) throws ID3Exception;
}
