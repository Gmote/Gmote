/* 
 * IFileSource.java
 *
 * Created on November 13, 2005, 9:28 PM
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
 * $Id: IFileSource.java,v 1.1 2005/11/22 02:07:29 paul Exp $
 */

package org.blinkenlights.jid3.io;

import java.io.*;

/**
 *  An interface for all file sources for MP3s (allows non java.io.File sources to be used,
 *  such as NFS XFile objects).
 */
public interface IFileSource
{
    public IFileSource createTempFile(String sPrefix, String sSuffix) throws IOException;
    
    public boolean delete();
    
    public String getName();
    
    public InputStream getInputStream() throws FileNotFoundException;
    
    public OutputStream getOutputStream() throws FileNotFoundException;
    
    public long length();
    
    public boolean renameTo(IFileSource oFileSource) throws IOException;
    
    public String toString();
}
