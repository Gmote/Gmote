/*
 * JID3Tagger.java
 *
 * Created on September 28, 2004, 12:43 AM
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
 * $Id: JID3Tagger.java,v 1.6 2005/02/06 18:11:24 paul Exp $
 */

package org.blinkenlights.jid3.exe;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;
import org.blinkenlights.jid3.util.*;

/** Command line utility which can be used to set tag values in individual files.
 *
 * @author  paul
 */
public class JID3Tagger
{
    private Map m_oCmdLineMap = null;
    
    /** Creates a new instance of JID3Tagger */
    private JID3Tagger(Map oCmdLineMap)
    {
        m_oCmdLineMap = oCmdLineMap;
    }
    
    private void display()
        throws Exception
    {
        String[] asFilename = (String[])m_oCmdLineMap.get("filenames");
        
        for (int i=0; i < asFilename.length; i++)
        {
            System.out.println("\n[" + asFilename[i] + "]");
            File oSourceFile = new File(asFilename[i]);
            MP3File oMP3File = new MP3File(oSourceFile);
            
            ID3Tag[] aoTag = oMP3File.getTags();
            for (int j=0; j < aoTag.length; j++)
            {
                if (aoTag[j] instanceof ID3V1_0Tag)
                {
                    System.out.println("\nV1.0:");
                }
                else if (aoTag[j] instanceof ID3V1_1Tag)
                {
                    System.out.println("\nV1.1:");
                }
                else if (aoTag[j] instanceof ID3V2_3_0Tag)
                {
                    System.out.println("\nV2.3.0:");
                }
                System.out.println(aoTag[j].toString());
            }
        }
    }
    
    private void tag()
        throws Exception
    {
        String[] asFilename = (String[])m_oCmdLineMap.get("filenames");
        
        for (int i=0; i < asFilename.length; i++)
        {
            File oSourceFile = new File(asFilename[i]);
            MP3File oMP3File = new MP3File(oSourceFile);
            
            if (m_oCmdLineMap.containsKey("1"))
            {
                ID3V1_1Tag oID3V1_1Tag = new ID3V1_1Tag();
                if (m_oCmdLineMap.containsKey("album"))
                {
                    oID3V1_1Tag.setAlbum((String)m_oCmdLineMap.get("album"));
                }
                if (m_oCmdLineMap.containsKey("artist"))
                {
                    oID3V1_1Tag.setArtist((String)m_oCmdLineMap.get("artist"));
                }
                if (m_oCmdLineMap.containsKey("comment"))
                {
                    oID3V1_1Tag.setComment((String)m_oCmdLineMap.get("comment"));
                }
                if (m_oCmdLineMap.containsKey("genre"))
                {
                    String sGenre = (String)m_oCmdLineMap.get("genre");
                    oID3V1_1Tag.setGenre(ID3V1Tag.Genre.lookupGenre(sGenre));
                }
                if (m_oCmdLineMap.containsKey("title"))
                {
                    oID3V1_1Tag.setTitle((String)m_oCmdLineMap.get("title"));
                }
                if (m_oCmdLineMap.containsKey("year"))
                {
                    oID3V1_1Tag.setYear(((Integer)m_oCmdLineMap.get("year")).toString());
                }
                if (m_oCmdLineMap.containsKey("track"))
                {
                    oID3V1_1Tag.setAlbumTrack(((Integer)m_oCmdLineMap.get("track")).intValue());
                }
                oMP3File.setID3Tag(oID3V1_1Tag);
            }
            if (m_oCmdLineMap.containsKey("2"))
            {
                ID3V2_3_0Tag oID3V2_3_0Tag = new ID3V2_3_0Tag();
                //HACK: Need to have padding at the end of the tag, or Winamp won't see the last frame (at least 6 bytes seem to be required).
                oID3V2_3_0Tag.setPaddingLength(16);
                if (m_oCmdLineMap.containsKey("album"))
                {
                    oID3V2_3_0Tag.setAlbum((String)m_oCmdLineMap.get("album"));
                }
                if (m_oCmdLineMap.containsKey("artist"))
                {
                    oID3V2_3_0Tag.setArtist((String)m_oCmdLineMap.get("artist"));
                }
                if (m_oCmdLineMap.containsKey("comment"))
                {
                    oID3V2_3_0Tag.setComment((String)m_oCmdLineMap.get("comment"));
                }
                if (m_oCmdLineMap.containsKey("genre"))
                {
                    oID3V2_3_0Tag.setGenre((String)m_oCmdLineMap.get("genre"));
                }
                oMP3File.setID3Tag(oID3V2_3_0Tag);
                if (m_oCmdLineMap.containsKey("title"))
                {
                    oID3V2_3_0Tag.setTitle((String)m_oCmdLineMap.get("title"));
                }
                if (m_oCmdLineMap.containsKey("year"))
                {
                    oID3V2_3_0Tag.setYear(((Integer)m_oCmdLineMap.get("year")).intValue());
                }
                if (m_oCmdLineMap.containsKey("track"))
                {
                    if (m_oCmdLineMap.containsKey("total"))
                    {
                        oID3V2_3_0Tag.setTrackNumber(((Integer)m_oCmdLineMap.get("track")).intValue(),
                                                     ((Integer)m_oCmdLineMap.get("total")).intValue());
                    }
                    else
                    {
                        oID3V2_3_0Tag.setTrackNumber(((Integer)m_oCmdLineMap.get("track")).intValue());
                    }
                }
            }
            
            oMP3File.sync();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("JID3 library version " + ID3Util.getVersion() + "\n\n" + ID3Util.getLicense());            
            String sHelp = "Usage: java -jar jid3.jar <options> filename [filename...]\n" +
                           "\n" +
                           " Options:\n\n" +
                           "  --display                 Display tags contained in specified file(s)\n" +
                           "  --1                       Write V1.1 tag to file (default)\n" +
                           "  --2                       Write V2.3.0 tag to file\n" +
                           "  --album=<album>           Set album value\n" +
                           "  --artist=<artist>         Set artist value\n" +
                           "  --comment=<comment>       Set comment value\n" +
                           "  --genre=<genre>           Set genre value (specify as string)\n" +
                           "  --title=<title>           Set title value\n" +
                           "  --track=<track>[/<total>] Set track value (total tracks optional for v2.3.0)\n" +
                           "  --year=<year>             Set year value";
            System.out.println("\n\n" + sHelp);
        }
        else
        {
            try
            {
                Map oCmdLineMap = parseCommandLineArgs(args);

                JID3Tagger oJID3Tagger = new JID3Tagger(oCmdLineMap);

                if ((oCmdLineMap.get("display") != null) && oCmdLineMap.get("display").equals(Boolean.TRUE))
                {
                    oJID3Tagger.display();
                }
                else
                {
                    oJID3Tagger.tag();
                }
            }
            catch (Exception e)
            {
                System.err.println("Tag failed: " + ID3Exception.getStackTrace(e));
            }
        }
    }
    
    private static Map parseCommandLineArgs(String[] args)
        throws ID3Exception
    {
        Map oMap = new HashMap();
        
        List oFilenameList = new ArrayList();
        
        int i = 0;
        while (i < args.length)
        {
            if (args[i].startsWith("--album="))
            {
                oMap.put("album", args[i].replaceFirst("--album=", ""));
            }
            else if (args[i].startsWith("--artist="))
            {
                oMap.put("artist", args[i].replaceFirst("--artist=", ""));
            }
            else if (args[i].startsWith("--comment="))
            {
                oMap.put("comment", args[i].replaceFirst("--comment=", ""));
            }
            else if (args[i].startsWith("--genre="))
            {
                oMap.put("genre", args[i].replaceFirst("--genre=", ""));
            }
            else if (args[i].startsWith("--title="))
            {
                oMap.put("title", args[i].replaceFirst("--title=", ""));
            }
            else if (args[i].startsWith("--year="))
            {
                try
                {
                    oMap.put("year", Integer.valueOf(args[i].replaceFirst("--year=", "")));
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Invalid year value specified.");
                }
            }
            else if (args[i].startsWith("--track="))
            {
                try
                {
                    String sTrack = args[i].replaceFirst("--track=", "");
                    int iTrackNumber;
                    int iTotalTracks;
                    if (sTrack.indexOf('/') > 0)
                    {
                        String[] asParts = sTrack.split("/", 2);
                        oMap.put("track", Integer.valueOf(asParts[0]));
                        oMap.put("total", Integer.valueOf(asParts[1]));
                    }
                    else
                    {
                        oMap.put("track", Integer.valueOf(sTrack));
                    }
                }
                catch (Exception e)
                {
                    throw new ID3Exception("Invalid track value specified.");
                }
            }
            else if (args[i].equals("--display"))
            {
                oMap.put("display", Boolean.TRUE);
            }
            else if (args[i].equals("--1"))
            {
                oMap.put("1", Boolean.TRUE);
            }
            else if (args[i].equals("--2"))
            {
                oMap.put("2", Boolean.TRUE);
            }
            else
            {
                oFilenameList.add(args[i]);
            }
            
            i++;
        }
        
        // add filenames
        if (oFilenameList.size() == 0)
        {
            throw new ID3Exception("At least one filename must be specified.");
        }
        oMap.put("filenames", oFilenameList.toArray(new String[0]));
        
        // if not version has been chosen, we will just do a v1.1 tag
        if ((!oMap.containsKey("1")) && (!oMap.containsKey("2")))
        {
            oMap.put("1", Boolean.TRUE);
        }
        
        return oMap;
    }
}
