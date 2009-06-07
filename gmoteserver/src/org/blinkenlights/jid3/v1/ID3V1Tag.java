/*
 * ID3V1Tag.java
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
 * $Id: ID3V1Tag.java,v 1.16 2005/05/05 05:15:00 paul Exp $
 */

package org.blinkenlights.jid3.v1;

import java.io.*;

import org.blinkenlights.jid3.*;

/**
 * @author paul
 *
 * The base class for ID3 V1 tags.
 *
 */
public abstract class ID3V1Tag extends ID3Tag
{
    protected String m_sTitle = null;
    protected String m_sArtist = null;
    protected String m_sAlbum = null;
    protected String m_sYear = null;
    protected String m_sComment = null;
    protected Genre m_oGenre = null;

    /** 
     * Constructor.
     */
    public ID3V1Tag()
    {
        super();
    }
    
    /** Set the title of the recording.
     *
     * @param sTitle the title of the recording (truncated to 30 characters, if longer)
     */
    public void setTitle(String sTitle)
    {
        if (sTitle.length() > 30)
        {
            sTitle = sTitle.substring(0, 30);
        }
        
        m_sTitle = sTitle;
    }

    /** Get the title of the recording.
     *
     * @return the set title for the recording
     */
    public String getTitle()
    {
        return m_sTitle;
    }

    /** Set the artist for the recording.
     *
     * @param sArtist the artist of the recording (truncated to 30 characters, if longer)
     */
    public void setArtist(String sArtist)
    {
        if (sArtist.length() > 30)
        {
            sArtist = sArtist.substring(0, 30);
        }
        
        m_sArtist = sArtist;
    }

    /** Get the artist of the recording.
     *
     * @return the set artist for the recording
     */
    public String getArtist()
    {
        return m_sArtist;
    }

    /** Set the name of the album from which the recording comes.
     *
     * @param sAlbum the name of the album (truncated to 30 characters, if longer)
     */
    public void setAlbum(String sAlbum)
    {
        if (sAlbum.length() > 30)
        {
            sAlbum = sAlbum.substring(0, 30);
        }
        
        m_sAlbum = sAlbum;
    }

    /** Get the name of the album from which the recording comes.
     *
     * @return the set name of the album
     */
    public String getAlbum()
    {
        return m_sAlbum;
    }

    /** Set the year in which the recording was made.
     *
     * @param sYear the year of the recording (up to 4 characters, should be numeric)
     */
    public void setYear(String sYear)
    {
        if (sYear.length() > 4)
        {
            sYear = sYear.substring(0, 4);
        }
        
        m_sYear = sYear;
    }

    /** Get the year in which the recording was made.
     *
     * @return the set year of the recording
     */
    public String getYear()
    {
        return m_sYear;
    }

    /** Set the comment field.
     *
     * @param sComment a comment field (truncated to 30 characters, if longer, or 28 characters in a v1.1 tag)
     */
    abstract public void setComment(String sComment);
    
    /** Get the comment.
     *
     * @return the set comment
     */
    public String getComment()
    {
        return m_sComment;
    }

    /** Set the genre of the recording, using one of the predefined genre values.
     *
     * @param oGenre the genre of the recording
     */
    public void setGenre(Genre oGenre)
    {
        m_oGenre = oGenre;
    }

    /** Get the genre of the recording.
     *
     * @return the predefined genre of the recording
     */
    public Genre getGenre()
    {
        return m_oGenre;
    }

    /** Read an ID3 V1 tag from an input stream.
     *
     * @param oIS the input stream from which to read a V1 tag
     * @return an object representing the tag just read
     * @throws ID3Exception if an error occurs while reading the tag
     */
    public static ID3V1Tag read(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            // title (30 bytes)
            byte[] abyTitle = new byte[30];
            if (oIS.read(abyTitle) != 30)
            {
                throw new ID3Exception("Unexpected EOF while reading title.");
            }
            String sTitle = new String(abyTitle, 0, indexOfFirstNull(abyTitle));
            // artist (30 bytes)
            byte[] abyArtist = new byte[30];
            if (oIS.read(abyArtist) != 30)
            {
                throw new ID3Exception("Unexpected EOF while reading artist.");
            }
            String sArtist = new String(abyArtist, 0, indexOfFirstNull(abyArtist));
            // album (30 bytes)
            byte[] abyAlbum = new byte[30];
            if (oIS.read(abyAlbum) != 30)
            {
                throw new ID3Exception("Unexpected EOF while reading album.");
            }
            String sAlbum = new String(abyAlbum, 0, indexOfFirstNull(abyAlbum));
            // year (4 bytes)
            byte[] abyYear = new byte[4];
            if (oIS.read(abyYear) != 4)
            {
                throw new ID3Exception("Unexpected EOF while reading year.");
            }
            String sYear = new String(abyYear, 0, indexOfFirstNull(abyYear));
            // comment (30 bytes) possibly including an album track # as the last byte
            String sComment;
            boolean bHasTrackNum;
            int iTrackNum = -1;
            byte[] abyTemp = new byte[30];
            if (oIS.read(abyTemp) != 30)
            {
                throw new ID3Exception("Unexpected EOF while reading comment.");
            }
            if ((abyTemp[28] == 0) && (abyTemp[29] != 0))
            {
                // this is a v1.1 tag with comment and track number
                byte[] abyComment = new byte[29];
                System.arraycopy(abyTemp, 0, abyComment, 0, 29);
                sComment = new String(abyComment, 0, indexOfFirstNull(abyComment));
                iTrackNum = abyTemp[29] & 0xff;
                bHasTrackNum = true;
            }
            else
            {
                // this is just a comment
                sComment = new String(abyTemp, 0, indexOfFirstNull(abyTemp));
                bHasTrackNum = false;
            }
            // genre
            int iGenre = oIS.read();
            
            // create appropriate ID3V1Tag object and set properties
            ID3V1Tag oID3V1Tag = null;
            if (bHasTrackNum)
            {
                // v1.1 tag
                oID3V1Tag = new ID3V1_1Tag();
                ((ID3V1_1Tag)oID3V1Tag).setAlbumTrack(iTrackNum);
            }
            else
            {
                // v1.0 tag
                oID3V1Tag = new ID3V1_0Tag();
            }
            oID3V1Tag.setTitle(sTitle);
            oID3V1Tag.setArtist(sArtist);
            oID3V1Tag.setAlbum(sAlbum);
            oID3V1Tag.setYear(sYear);
            oID3V1Tag.setComment(sComment);
            try
            {
                oID3V1Tag.setGenre(Genre.lookupGenre(iGenre));
            }
            catch (ID3Exception e)
            {
                if (ID3Tag.usingStrict())
                {
                    throw e;
                }
                else
                {
                    oID3V1Tag.setGenre(new Genre((byte)iGenre, "Unknown"));
                }
            }
            
            return oID3V1Tag;
        }
        catch (Exception e)
        {
            throw new ID3Exception(e);
        }
    }

    /** Write tag to output stream.
     *
     * @param oOS output stream to which tag is to be written
     * @throws ID3Exception if an error occurs while writing the tag
     */
    abstract public void write(OutputStream oOS) throws ID3Exception;
    
    /* (non-Javadoc)
     * @see org.blinkenlights.id3.ID3Tag#toString()
     */
    public String toString()
    {
        // lookup genre string, if possible (print byte value otherwise)
        String sGenre = null;
        try
        {
            sGenre = ID3V1Tag.Genre.lookupGenre(m_oGenre.m_byGenre).toString();
        }
        catch (Exception e)
        {
            sGenre = Byte.toString(m_oGenre.m_byGenre);
        }
        
        return "SongTitle = [" + m_sTitle + "]\n" +
               "Artist = [" + m_sArtist + "]\n" +
               "Album = [" + m_sAlbum + "]\n" +
               "Year = [" + m_sYear + "]\n" +
               "Comment = [" + m_sComment + "]\n" +
               "Genre = " + sGenre; //m_oGenre.m_byGenre;
    }

    /** Utility method to find the index of the first zero byte in a byte array.
     *
     * @param abyString the byte array to examine
     * @return the index position of the first zero byte in the array, or the length of the array if no zeros exist
     */
    static private int indexOfFirstNull(byte[] abyString)
    {
        for (int i=0; i < abyString.length; i++)
        {
            if (abyString[i] == 0)
            {
                return i;
            }
        }
        
        return abyString.length;
    }
    
    /** A class representing the predefined genres. */
    public static class Genre
    {
        /** Store the actual byte value representing the genre in the tag. */
        private byte m_byGenre;
        
        /** A string representation of the genre which clients can display for users. */
        private String m_sGenre;
        
        /** Private constructor.  Genres are predefined. */
        private Genre(byte byGenre, String sGenre)
        {
            m_byGenre = byGenre;
            m_sGenre = sGenre;
        }
        
        /** Return the actual byte value used to represent this genre. */
        public int getByteValue()
        {
            return (m_byGenre & 0xff);
        }

        /** Equality test returns if two genre objects represent the same genre. */
        public boolean equals(Genre oGenre)
        {
            if ( (oGenre == null) || ( ! (oGenre instanceof Genre)) )
            {
                return false;
            }
            
            return (oGenre.m_byGenre == this.m_byGenre);
        }
        
        public String toString()
        {
            return m_sGenre;
        }

        /** Predefined genre. */
        public static final Genre Blues = new Genre((byte)0, "Blues");
        /** Predefined genre. */
        public static final Genre ClassicRock = new Genre((byte)1, "Classic Rock");
        /** Predefined genre. */
        public static final Genre Country = new Genre((byte)2, "Country");
        /** Predefined genre. */
        public static final Genre Dance = new Genre((byte)3, "Dance");
        /** Predefined genre. */
        public static final Genre Disco = new Genre((byte)4, "Disco");
        /** Predefined genre. */
        public static final Genre Funk = new Genre((byte)5, "Funk");
        /** Predefined genre. */
        public static final Genre Grunge = new Genre((byte)6, "Grunge");
        /** Predefined genre. */
        public static final Genre HipHop = new Genre((byte)7, "Hip-Hop");
        /** Predefined genre. */
        public static final Genre Jazz = new Genre((byte)8, "Jazz");
        /** Predefined genre. */
        public static final Genre Metal = new Genre((byte)9, "Metal");
        /** Predefined genre. */
        public static final Genre NewAge = new Genre((byte)10, "New Age");
        /** Predefined genre. */
        public static final Genre Oldies = new Genre((byte)11, "Oldies");
        /** Predefined genre. */
        public static final Genre Other = new Genre((byte)12, "Other");
        /** Predefined genre. */
        public static final Genre Pop = new Genre((byte)13, "Pop");
        /** Predefined genre. */
        public static final Genre RhythmBlues = new Genre((byte)14, "R&B");
        /** Predefined genre. */
        public static final Genre Rap = new Genre((byte)15, "Rap");
        /** Predefined genre. */
        public static final Genre Reggae = new Genre((byte)16, "Reggae");
        /** Predefined genre. */
        public static final Genre Rock = new Genre((byte)17, "Rock");
        /** Predefined genre. */
        public static final Genre Techno = new Genre((byte)18, "Techno");
        /** Predefined genre. */
        public static final Genre Industrial = new Genre((byte)19, "Industrial");
        /** Predefined genre. */
        public static final Genre Alternative = new Genre((byte)20, "Alternative");
        /** Predefined genre. */
        public static final Genre Ska = new Genre((byte)21, "Ska");
        /** Predefined genre. */
        public static final Genre DeathMetal = new Genre((byte)22, "Death Metal");
        /** Predefined genre. */
        public static final Genre Pranks = new Genre((byte)23, "Pranks");
        /** Predefined genre. */
        public static final Genre Soundtrack = new Genre((byte)24, "Soundtrack");
        /** Predefined genre. */
        public static final Genre EuroTechno = new Genre((byte)25, "Euro-Techno");
        /** Predefined genre. */
        public static final Genre Ambient = new Genre((byte)26, "Ambient");
        /** Predefined genre. */
        public static final Genre TripHop = new Genre((byte)27, "Trip-Hop");
        /** Predefined genre. */
        public static final Genre Vocal = new Genre((byte)28, "Vocal");
        /** Predefined genre. */
        public static final Genre JazzFunk = new Genre((byte)29, "Jazz-Funk");
        /** Predefined genre. */
        public static final Genre Fusion = new Genre((byte)30, "Fusion");
        /** Predefined genre. */
        public static final Genre Trance = new Genre((byte)31, "Trance");
        /** Predefined genre. */
        public static final Genre Classical = new Genre((byte)32, "Classical");
        /** Predefined genre. */
        public static final Genre Instrumental = new Genre((byte)33, "Instrumental");
        /** Predefined genre. */
        public static final Genre Acid = new Genre((byte)34, "Acid");
        /** Predefined genre. */
        public static final Genre House = new Genre((byte)35, "House");
        /** Predefined genre. */
        public static final Genre Game = new Genre((byte)36, "Game");
        /** Predefined genre. */
        public static final Genre SoundClip = new Genre((byte)37, "Sound Clip");
        /** Predefined genre. */
        public static final Genre Gospel = new Genre((byte)38, "Gospel");
        /** Predefined genre. */
        public static final Genre Noise = new Genre((byte)39, "Noise");
        /** Predefined genre. */
        public static final Genre AlternativeRock = new Genre((byte)40, "Alternative Rock");
        /** Predefined genre. */
        public static final Genre Bass = new Genre((byte)41, "Bass");
        /** Predefined genre. */
        public static final Genre Soul = new Genre((byte)42, "Soul");
        /** Predefined genre. */
        public static final Genre Punk = new Genre((byte)43, "Punk");
        /** Predefined genre. */
        public static final Genre Space = new Genre((byte)44, "Space");
        /** Predefined genre. */
        public static final Genre Meditative = new Genre((byte)45, "Meditative");
        /** Predefined genre. */
        public static final Genre InstrumentalPop = new Genre((byte)46, "Instrumental Pop");
        /** Predefined genre. */
        public static final Genre InstrumentalRock = new Genre((byte)47, "Instrumental Rock");
        /** Predefined genre. */
        public static final Genre Ethnic = new Genre((byte)48, "Ethnic");
        /** Predefined genre. */
        public static final Genre Gothic = new Genre((byte)49, "Gothic");
        /** Predefined genre. */
        public static final Genre DarkWave = new Genre((byte)50, "Dark Wave");
        /** Predefined genre. */
        public static final Genre TechnoIndustrial = new Genre((byte)51, "Techno-Industrial");
        /** Predefined genre. */
        public static final Genre Electronic = new Genre((byte)52, "Electronic");
        /** Predefined genre. */
        public static final Genre PopFolk = new Genre((byte)53, "Pop-Folk");
        /** Predefined genre. */
        public static final Genre EuroDance = new Genre((byte)54, "Euro-Dance");
        /** Predefined genre. */
        public static final Genre Dream = new Genre((byte)55, "Dream");
        /** Predefined genre. */
        public static final Genre SouthernRock = new Genre((byte)56, "Southern Rock");
        /** Predefined genre. */
        public static final Genre Comedy = new Genre((byte)57, "Comedy");
        /** Predefined genre. */
        public static final Genre Cult = new Genre((byte)58, "Cult");
        /** Predefined genre. */
        public static final Genre Gangsta = new Genre((byte)59, "Gangsta");
        /** Predefined genre. */
        public static final Genre Top40 = new Genre((byte)60, "Top 40");
        /** Predefined genre. */
        public static final Genre ChristianRap = new Genre((byte)61, "Christian Rap");
        /** Predefined genre. */
        public static final Genre PopFunk = new Genre((byte)62, "Pop-Funk");
        /** Predefined genre. */
        public static final Genre Jungle = new Genre((byte)63, "Jungle");
        /** Predefined genre. */
        public static final Genre NativeAmerican = new Genre((byte)64, "Native American");
        /** Predefined genre. */
        public static final Genre Cabaret = new Genre((byte)65, "Cabaret");
        /** Predefined genre. */
        public static final Genre NewWave = new Genre((byte)66, "New Wave");
        /** Predefined genre. */
        public static final Genre Psychedelic = new Genre((byte)67, "Psychedelic");     // this is the correct spelling
        /** Predefined genre. */
        public static final Genre Rave = new Genre((byte)68, "Rave");
        /** Predefined genre. */
        public static final Genre ShowTunes = new Genre((byte)69, "Show Tunes");
        /** Predefined genre. */
        public static final Genre Trailer = new Genre((byte)70, "Trailer");
        /** Predefined genre. */
        public static final Genre LowFi = new Genre((byte)71, "Low-Fi");
        /** Predefined genre. */
        public static final Genre Tribal = new Genre((byte)72, "Tribal");
        /** Predefined genre. */
        public static final Genre AcidPunk = new Genre((byte)73, "Acid Punk");
        /** Predefined genre. */
        public static final Genre AcidJazz = new Genre((byte)74, "Acid Jazz");
        /** Predefined genre. */
        public static final Genre Polka = new Genre((byte)75, "Polka");
        /** Predefined genre. */
        public static final Genre Retro = new Genre((byte)76, "Retro");
        /** Predefined genre. */
        public static final Genre Musical = new Genre((byte)77, "Musical");
        /** Predefined genre. */
        public static final Genre RockNRoll = new Genre((byte)78, "Rock'N'Roll");
        /** Predefined genre. */
        public static final Genre HardRock = new Genre((byte)79, "Hard Rock");
        /** Predefined genre. */
        public static final Genre EXT_Folk = new Genre((byte)80, "Folk");
        /** Predefined genre. */
        public static final Genre EXT_FolkRock = new Genre((byte)81, "Folk Rock");
        /** Predefined genre. */
        public static final Genre EXT_NationalFolk = new Genre((byte)82, "National Folk");
        /** Predefined genre. */
        public static final Genre EXT_Swing = new Genre((byte)83, "Swing");
        /** Predefined genre. */
        public static final Genre EXT_FastFusion = new Genre((byte)84, "Fast Fusion");
        /** Predefined genre. */
        public static final Genre EXT_Bebop = new Genre((byte)85, "Bebop");
        /** Predefined genre. */
        public static final Genre EXT_Latin = new Genre((byte)86, "Latin");
        /** Predefined genre. */
        public static final Genre EXT_Revival = new Genre((byte)87, "Revival");
        /** Predefined genre. */
        public static final Genre EXT_Celtic = new Genre((byte)88, "Celtic");
        /** Predefined genre. */
        public static final Genre EXT_Bluegrass = new Genre((byte)89, "Bluegrass");
        /** Predefined genre. */
        public static final Genre EXT_AvanteGarde = new Genre((byte)90, "Avante Garde");
        /** Predefined genre. */
        public static final Genre EXT_GothicRock = new Genre((byte)91, "Gothic Rock");
        /** Predefined genre. */
        public static final Genre EXT_ProgressiveRock = new Genre((byte)92, "Progressive Rock");
        /** Predefined genre. */
        public static final Genre EXT_PsychedelicRock = new Genre((byte)93, "Psychedelic Rock");
        /** Predefined genre. */
        public static final Genre EXT_SymphonicRock = new Genre((byte)94, "Symphonic Rock");
        /** Predefined genre. */
        public static final Genre EXT_SlowRock = new Genre((byte)95, "Slow Rock");
        /** Predefined genre. */
        public static final Genre EXT_BigBand = new Genre((byte)96, "Big Band");
        /** Predefined genre. */
        public static final Genre EXT_Chorus = new Genre((byte)97, "Chorus");
        /** Predefined genre. */
        public static final Genre EXT_EasyListening = new Genre((byte)98, "Easy Listening");
        /** Predefined genre. */
        public static final Genre EXT_Acoustic = new Genre((byte)99, "Acoustic");
        /** Predefined genre. */
        public static final Genre EXT_Humour = new Genre((byte)100, "Humour");
        /** Predefined genre. */
        public static final Genre EXT_Speech = new Genre((byte)101, "Speech");
        /** Predefined genre. */
        public static final Genre EXT_Chanson = new Genre((byte)102, "Chanson");
        /** Predefined genre. */
        public static final Genre EXT_Opera = new Genre((byte)103, "Opera");
        /** Predefined genre. */
        public static final Genre EXT_ChamberMusic = new Genre((byte)104, "Chamber Music");
        /** Predefined genre. */
        public static final Genre EXT_Sonata = new Genre((byte)105, "Sonata");
        /** Predefined genre. */
        public static final Genre EXT_Symphony = new Genre((byte)106, "Symphony");
        /** Predefined genre. */
        public static final Genre EXT_BootyBass = new Genre((byte)107, "Booty Bass");
        /** Predefined genre. */
        public static final Genre EXT_Primus = new Genre((byte)108, "Primus");
        /** Predefined genre. */
        public static final Genre EXT_PornGroove = new Genre((byte)109, "Porn Groove");
        /** Predefined genre. */
        public static final Genre EXT_Satire = new Genre((byte)110, "Satire");
        /** Predefined genre. */
        public static final Genre EXT_SlowJam = new Genre((byte)111, "Slow Jam");
        /** Predefined genre. */
        public static final Genre EXT_Club = new Genre((byte)112, "Club");
        /** Predefined genre. */
        public static final Genre EXT_Tango = new Genre((byte)113, "Tango");
        /** Predefined genre. */
        public static final Genre EXT_Samba = new Genre((byte)114, "Samba");
        /** Predefined genre. */
        public static final Genre EXT_Folklore = new Genre((byte)115, "Folklore");
        /** Predefined genre. */
        public static final Genre EXT_Ballad = new Genre((byte)116, "Ballad");
        /** Predefined genre. */
        public static final Genre EXT_PowerBallad = new Genre((byte)117, "Power Ballad");
        /** Predefined genre. */
        public static final Genre EXT_RhythmicSoul = new Genre((byte)118, "Rhythmic Soul");
        /** Predefined genre. */
        public static final Genre EXT_Freestyle = new Genre((byte)119, "Freestyle");
        /** Predefined genre. */
        public static final Genre EXT_Duet = new Genre((byte)120, "Duet");
        /** Predefined genre. */
        public static final Genre EXT_PunkRock = new Genre((byte)121, "Punk Rock");
        /** Predefined genre. */
        public static final Genre EXT_DrumSolo = new Genre((byte)122, "Drum Solo");
        /** Predefined genre. */
        public static final Genre EXT_ACappella = new Genre((byte)123, "A Cappella");
        /** Predefined genre. */
        public static final Genre EXT_EuroHouse = new Genre((byte)124, "Euro-House");
        /** Predefined genre. */
        public static final Genre EXT_DanceHall = new Genre((byte)125, "Dance Hall");
        /** Predefined genre. */
        public static final Genre Nullsoft_Goa = new Genre((byte)126, "Goa");
        /** Predefined genre. */
        public static final Genre Nullsoft_DrumAndBass = new Genre((byte)127, "Drum & Bass");
        /** Predefined genre. */
        public static final Genre Nullsoft_ClubHouse = new Genre((byte)128, "Club-House");
        /** Predefined genre. */
        public static final Genre Nullsoft_Hardcore = new Genre((byte)129, "Hardcore");
        /** Predefined genre. */
        public static final Genre Nullsoft_Terror = new Genre((byte)130, "Terror");
        /** Predefined genre. */
        public static final Genre Nullsoft_Indie = new Genre((byte)131, "Indie");
        /** Predefined genre. */
        public static final Genre Nullsoft_BritPop = new Genre((byte)132, "BritPop");
        /** Predefined genre. */
        public static final Genre Nullsoft_Negerpunk = new Genre((byte)133, "Negerpunk");
        /** Predefined genre. */
        public static final Genre Nullsoft_PolskPunk = new Genre((byte)134, "Polsk Punk");
        /** Predefined genre. */
        public static final Genre Nullsoft_Beat = new Genre((byte)135, "Beat");
        /** Predefined genre. */
        public static final Genre Nullsoft_ChristianGangstaRap = new Genre((byte)136, "Christian Gangsta Rap");
        /** Predefined genre. */
        public static final Genre Nullsoft_HeavyMetal = new Genre((byte)137, "Heavy Metal");
        /** Predefined genre. */
        public static final Genre Nullsoft_BlackMetal = new Genre((byte)138, "Black Metal");
        /** Predefined genre. */
        public static final Genre Nullsoft_Crossover = new Genre((byte)139, "Crossover");
        /** Predefined genre. */
        public static final Genre Nullsoft_ContemporaryChristian = new Genre((byte)140, "Contemporary Christian");
        /** Predefined genre. */
        public static final Genre Nullsoft_ChristianRock = new Genre((byte)141, "Christian Rock");
        /** Predefined genre. */
        public static final Genre Nullsoft_Merengue = new Genre((byte)142, "Merengue");
        /** Predefined genre. */
        public static final Genre Nullsoft_Salsa = new Genre((byte)143, "Salsa");
        /** Predefined genre. */
        public static final Genre Nullsoft_ThrashMetal = new Genre((byte)144, "Thrash Metal");
        /** Predefined genre. */
        public static final Genre Nullsoft_Anime = new Genre((byte)145, "Anime");
        /** Predefined genre. */
        public static final Genre Nullsoft_Jpop = new Genre((byte)146, "Jpop");
        /** Predefined genre. */
        public static final Genre Nullsoft_Synthpop = new Genre((byte)147, "Synthpop");
        /** Some people use a byte value of 255 to imply that the genre is undefined. */
        public static final Genre Undefined = new Genre((byte)255, "Undefined");
        
        /** Return the genre object represented by a byte value.
         *
         * @param iType the value code for a specific V1 genre
         * @return the corresponding Genre object
         * @throws ID3Exception if no matching genre exists
         */
        public static Genre lookupGenre(int iType)
            throws ID3Exception
        {
            try
            {
                // special case, some people use 255 to mean the genre has not been defined
                if (iType == 255)
                {
                    return Undefined;
                }
                
                return s_aoGenre[iType];
            }
            catch (Exception e)
            {
                throw new ID3Exception("Invalid V1 genre code " + iType + ".");
            }
        }

        /** Return the genre object represented by a string value.
         *
         * @param sGenre the string name of a specific V1 genre (case-insensitive)
         * @return the corresponding Genre object
         * @throws ID3Exception if no matching genre exists
         */
        public static Genre lookupGenre(String sGenre)
            throws ID3Exception
        {
            for (int i=0; i < s_aoGenre.length; i++)
            {
                if (s_aoGenre[i].m_sGenre.equalsIgnoreCase(sGenre))
                {
                    return s_aoGenre[i];
                }
            }
            
            throw new ID3Exception("Unknown V1 genre [" + sGenre + "].");
        }

        /** Lookup table used to match byte value to genre. */
        private static Genre[] s_aoGenre =
        {
            Blues,
            ClassicRock,
            Country,
            Dance,
            Disco,
            Funk,
            Grunge,
            HipHop,
            Jazz,
            Metal,
            NewAge,
            Oldies,
            Other,
            Pop,
            RhythmBlues,
            Rap,
            Reggae,
            Rock,
            Techno,
            Industrial,
            Alternative,
            Ska,
            DeathMetal,
            Pranks,
            Soundtrack,
            EuroTechno,
            Ambient,
            TripHop,
            Vocal,
            JazzFunk,
            Fusion,
            Trance,
            Classical,
            Instrumental,
            Acid,
            House,
            Game,
            SoundClip,
            Gospel,
            Noise,
            AlternativeRock,
            Bass,
            Soul,
            Punk,
            Space,
            Meditative,
            InstrumentalPop,
            InstrumentalRock,
            Ethnic,
            Gothic,
            DarkWave,
            TechnoIndustrial,
            Electronic,
            PopFolk,
            EuroDance,
            Dream,
            SouthernRock,
            Comedy,
            Cult,
            Gangsta,
            Top40,
            ChristianRap,
            PopFunk,
            Jungle,
            NativeAmerican,
            Cabaret,
            NewWave,
            Psychedelic,
            Rave,
            ShowTunes,
            Trailer,
            LowFi,
            Tribal,
            AcidPunk,
            AcidJazz,
            Polka,
            Retro,
            Musical,
            RockNRoll,
            HardRock,
            EXT_Folk,
            EXT_FolkRock,
            EXT_NationalFolk,
            EXT_Swing,
            EXT_FastFusion,
            EXT_Bebop,
            EXT_Latin,
            EXT_Revival,
            EXT_Celtic,
            EXT_Bluegrass,
            EXT_AvanteGarde,
            EXT_GothicRock,
            EXT_ProgressiveRock,
            EXT_PsychedelicRock,
            EXT_SymphonicRock,
            EXT_SlowRock,
            EXT_BigBand,
            EXT_Chorus,
            EXT_EasyListening,
            EXT_Acoustic,
            EXT_Humour,
            EXT_Speech,
            EXT_Chanson,
            EXT_Opera,
            EXT_ChamberMusic,
            EXT_Sonata,
            EXT_Symphony,
            EXT_BootyBass,
            EXT_Primus,
            EXT_PornGroove,
            EXT_Satire,
            EXT_SlowJam,
            EXT_Club,
            EXT_Tango,
            EXT_Samba,
            EXT_Folklore,
            EXT_Ballad,
            EXT_PowerBallad,
            EXT_RhythmicSoul,
            EXT_Freestyle,
            EXT_Duet,
            EXT_PunkRock,
            EXT_DrumSolo,
            EXT_ACappella,
            EXT_EuroHouse,
            EXT_DanceHall,
            Nullsoft_Goa,
            Nullsoft_DrumAndBass,
            Nullsoft_ClubHouse,
            Nullsoft_Hardcore,
            Nullsoft_Terror,
            Nullsoft_Indie,
            Nullsoft_BritPop,
            Nullsoft_Negerpunk,
            Nullsoft_PolskPunk,
            Nullsoft_Beat,
            Nullsoft_ChristianGangstaRap,
            Nullsoft_HeavyMetal,
            Nullsoft_BlackMetal,
            Nullsoft_Crossover,
            Nullsoft_ContemporaryChristian,
            Nullsoft_ChristianRock,
            Nullsoft_Merengue,
            Nullsoft_Salsa,
            Nullsoft_ThrashMetal,
            Nullsoft_Anime,
            Nullsoft_Jpop,
            Nullsoft_Synthpop
        };
    }
}
