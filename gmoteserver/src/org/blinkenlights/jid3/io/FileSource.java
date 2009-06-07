/* 
 * FileSource.java
 *
 * Created on November 13, 2005, 9:30 PM
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
 * $Id: FileSource.java,v 1.1 2005/11/22 02:07:29 paul Exp $
 */

package org.blinkenlights.jid3.io;

import java.io.*;

/**
 *  A concrete implementation of the IFileSource interface, allowing access to standard
 *  java.io.File objects.
 */
public class FileSource implements IFileSource
{
    private File m_oFile;
    
    /** Creates a new instance of FileSource */
    public FileSource(File oFile)
    {
        m_oFile = oFile;
    }
    
    public IFileSource createTempFile(String sPrefix, String sSuffix)
        throws IOException
    {
        File oTmpFile = File.createTempFile("id3.", ".tmp", m_oFile.getAbsoluteFile().getParentFile());
        
        return new FileSource(oTmpFile);
    }
    
    public boolean delete()
    {
        return m_oFile.delete();
    }
    
    public String getName()
    {
        return m_oFile.getName();
    }
    
    public InputStream getInputStream()
        throws FileNotFoundException
    {
        return new FileInputStream(m_oFile);
    }
    
    public OutputStream getOutputStream()
        throws FileNotFoundException
    {
        return new FileOutputStream(m_oFile);
    }
    
    public long length()
    {
        return m_oFile.length();
    }
    
    public boolean renameTo(String sFilename)
    {
        return m_oFile.renameTo(new File(sFilename));
    }
    
    public boolean renameTo(IFileSource oFileSource)
        throws IOException
    {
        if ( ! (oFileSource instanceof FileSource))
        {
            throw new IOException("Cannot rename between different file source types.");
        }
        return m_oFile.renameTo(((FileSource)oFileSource).m_oFile);
    }
    
    public String toString()
    {
        return m_oFile.toString();
    }
}
