/*
 * ContentType.java
 *
 * Created on June 17, 2004, 12:27 AM
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
 * $Id: ContentType.java,v 1.6 2005/02/06 18:11:17 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.util.*;

import org.blinkenlights.jid3.*;

/**
 *
 * @author  paul
 *
 * Represents the content type of the track (ie. its genre details).
 *
 */
public class ContentType
{
    private Set m_oGenreSet = null;
    private String m_sRefinement = null;
    private boolean m_bIsRemix;
    private boolean m_bIsCover;
    
    /** Creates a new instance of ContentType */
    public ContentType()
    {
        m_oGenreSet = new TreeSet();
        m_bIsRemix = false;
        m_bIsCover = false;
    }

    /** Compare ContentTypes for equality by genre and refinement values.
     *
     * @param oOther object to check against
     * @return true if equal, false otherwise
     */
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof ContentType)))
        {
            return false;
        }
        
        ContentType oOtherContentType = (ContentType)oOther;
        
        if (!m_oGenreSet.equals(oOtherContentType.m_oGenreSet))
        {
            return false;
        }
        if ( ((m_sRefinement == null) && (oOtherContentType.m_sRefinement != null)) ||
             ((m_sRefinement != null) && (oOtherContentType.m_sRefinement == null)) ||
             (!m_sRefinement.equals(oOtherContentType.m_sRefinement)) )
        {
            return false;
        }
        if (m_bIsRemix != oOtherContentType.m_bIsRemix)
        {
            return false;
        }
        if (m_bIsCover != oOtherContentType.m_bIsCover)
        {
            return false;
        }
        
        return true;
    }

    /** Set a refinement for the genre of the recording repesented by this content type.  Refinements are
     *  free-form text, and can be used where no existing genre accurately describes the content.
     *
     * @param sRefinement a refinement description for this content type
     */
    public void setRefinement(String sRefinement)
    {
        m_sRefinement = sRefinement;
    }

    /** Get the current refinement, if set, for this content type.
     *
     * @return the refinement currently specified, or null if no refinement has been set
     */
    public String getRefinement()
    {
        return m_sRefinement;
    }

    /** Toggle whether or not the recording described by this content type is a remix.
     *
     * @param bIsRemix whether this is a remix or not
     */
    public void setIsRemix(boolean bIsRemix)
    {
        m_bIsRemix = bIsRemix;
    }

    /** Check whether or not this content type describes a recording which is a remix.
     *
     * @return true if the recording is a remix, false otherwise
     */
    public boolean isRemix()
    {
        return m_bIsRemix;
    }
    
    /** Toggle whether or not the recording described by this content type is a cover.
     *
     * @param bIsCover whether this is a cover or not
     */
    public void setIsCover(boolean bIsCover)
    {
        m_bIsCover = bIsCover;
    }
    
    /** Check whether or not this content type describes a recording which is a cover.
     *
     * @return true if the recording is a cover, false otherwise
     */
    public boolean isCover()
    {
        return m_bIsCover;
    }
    
    /** Set a given genre for this recording.  More than one genre can be set independently.
     *
     * @param oGenre the genre to be set
     * @return true if this genre was not already set, false otherwise
     */
    public boolean setGenre(Genre oGenre)
    {
        return m_oGenreSet.add(oGenre);
    }

    /** Unset a given genre for this recording.
     *
     * @param oGenre the genre to be unset
     * @return true if this genre was previously set, false otherwise
     */
    public boolean unsetGenre(Genre oGenre)
    {
        return m_oGenreSet.remove(oGenre);
    }
    
    /** Check whether a given genre is set.
     *
     * @param oGenre the genre to check
     * @return true if the genre is set, false otherwise
     */
    public boolean isSet(Genre oGenre)
    {
        return m_oGenreSet.contains(oGenre);
    }

    /** Get all of the set genres in this content type.
     *
     * @return an array of all of the set genres
     */
    public Genre[] getGenres()
    {
        return (Genre[])m_oGenreSet.toArray(new Genre[0]);
    }
    
    /** Get the string representation of this content type.
     *
     * @return a string representing the content type, as it is stored in the frame
     */
    public String toString()
    {
        // build text string
        StringBuffer sbContentType = new StringBuffer();
        // genre byte values in sequence, surrounded by brackets
        ContentType.Genre[] aoGenre = getGenres();
        for (int i=0; i < aoGenre.length; i++)
        {
            sbContentType.append("(" + aoGenre[i].getByteValue() + ")");
        }
        // cover code if specified
        if (isCover())
        {
            sbContentType.append("(CR)");
        }
        // remix code if specified
        if (isRemix())
        {
            sbContentType.append("(RX)");
        }
        // refinement comes last, if specified
        if (getRefinement() != null)
        {
            String sRefinement = getRefinement();
            // if refinement begins with opening bracket, we must double it as an escape sequence
            if (sRefinement.startsWith("("))
            {
                sbContentType.append("(");
            }
            sbContentType.append(sRefinement);
        }
        
        return sbContentType.toString();
    }
    
    /** A class representing the predefined genres. */
    public static class Genre implements Comparable
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
            return m_byGenre;
        }

        /** Return the text string representing this genre. */
        public String toString()
        {
            return m_sGenre;
        }

        public int compareTo(Object oOther)
        {
            if ( ! (oOther instanceof Genre) )
            {
                throw new ClassCastException();
            }
            
            Genre oOtherGenre = (Genre)oOther;
            
            return (m_byGenre - oOtherGenre.m_byGenre);
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
        public static final Genre HipHop = new Genre((byte)7, "HipHop");
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

        /** Return genre object represented by this byte value. */
        static Genre lookupGenre(int iType)
            throws ID3Exception
        {
            try
            {
                return s_aoGenre[iType];
            }
            catch (Exception e)
            {
                throw new ID3Exception("Unknown genre code " + iType + ".");
            }
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
            EXT_DanceHall
        };
    }
}
