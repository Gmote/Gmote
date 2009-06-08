/*
 * IPLSID3V2Frame.java
 *
 * Created on Feb 1, 2004
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
 * $Id: IPLSID3V2Frame.java,v 1.9 2005/02/06 18:11:23 paul Exp $
 */

package org.blinkenlights.jid3.v2;

import java.io.*;
import java.util.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.io.*;
import org.blinkenlights.jid3.util.*;

/**
 * @author paul
 *
 * Frame containing an involved people list.
 */
public class IPLSID3V2Frame extends ID3V2Frame
{
    private TextEncoding m_oTextEncoding;
    private SortedMap m_oPeopleMap = null;
    
    /** Constructor. */
    public IPLSID3V2Frame()
    {
        m_oTextEncoding = TextEncoding.getDefaultTextEncoding();
        m_oPeopleMap = new TreeMap();
    }

    public IPLSID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // text encoding
            m_oTextEncoding = TextEncoding.getTextEncoding(oFrameDataID3DIS.readUnsignedByte());
            
            // involved persons
            m_oPeopleMap = new TreeMap();
            while (oFrameDataID3DIS.available() > 0)
            {
                String sInvolvement = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
                String sPerson = oFrameDataID3DIS.readStringToNull(m_oTextEncoding);
                if ((sInvolvement == null) || (sPerson == null))
                {
                    throw new ID3Exception("IPLS frame missing involvement or person.");
                }
                addInvolvedPerson(new InvolvedPerson(sInvolvement, sPerson));
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }
    
    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitIPLSID3V2Frame(this);
    }
    
    /** Add an involved person to the list.
     *
     * @param oInvolvedPerson the involved person to be added
     */
    public void addInvolvedPerson(InvolvedPerson oInvolvedPerson)
    {
        // create set mapping if not there
        if ( ! m_oPeopleMap.containsKey(oInvolvedPerson.getInvolvement()))
        {
            m_oPeopleMap.put(oInvolvedPerson.getInvolvement(), new TreeSet());
        }
        
        // add involved person to mapped set
        Set oIPSet = (Set)m_oPeopleMap.get(oInvolvedPerson.getInvolvement());
        oIPSet.add(oInvolvedPerson);
    }

    /** Get all involved persons with a given involvement.
     *
     * @param sInvolvement the involvement for which a list of involved persons is to be returned
     * @return an array of matching involved persons
     */
    public InvolvedPerson[] getInvolvedPersons(String sInvolvement)
    {
        Set oIPSet = (Set)m_oPeopleMap.get(sInvolvement);
        
        if (oIPSet != null)
        {
            return (InvolvedPerson[])oIPSet.toArray(new InvolvedPerson[0]);
        }
        else
        {
            return new InvolvedPerson[0];
        }
    }

    /** Removed all persons with a given involvement.
     *
     * @param sInvolvement the involvement for which all involved persons are to be removed
     * @return an array of involved persons which previously matched the specified involvement
     */
    public InvolvedPerson[] removedInvolvedPersons(String sInvolvement)
    {
        Set oIPSet = (Set)m_oPeopleMap.remove(sInvolvement);
        
        if (oIPSet != null)
        {
            return (InvolvedPerson[])oIPSet.toArray(new InvolvedPerson[0]);
        }
        else
        {
            return new InvolvedPerson[0];
        }
    }
    
    /** Set the text encoding to be used for the involved people in this frame.
     *
     * @param oTextEncoding the text encoding to be used for this frame
     */
    public void setTextEncoding(TextEncoding oTextEncoding)
    {
        if (oTextEncoding == null)
        {
            throw new NullPointerException("Text encoding cannot be null.");
        }
        m_oTextEncoding = oTextEncoding;
    }

    /** Get the text encoding used for the involved people in this frame.
     *
     * @return the text encoding to be used for this frame
     */
    public TextEncoding getTextEncoding()
    {
        return m_oTextEncoding;
    }
    
    protected byte[] getFrameId()
    {
        return "IPLS".getBytes();
    }
    
    public String toString()
    {
        StringBuffer sbOutput = new StringBuffer();
        sbOutput.append("InvolvedPersons: Involvments =");
        if (m_oPeopleMap.values().size() > 0)
        {
            Iterator oSetIter = m_oPeopleMap.values().iterator();
            while (oSetIter.hasNext())
            {
                Set oInvolvedPersonSet = (Set)oSetIter.next();
                Iterator oIPIter = oInvolvedPersonSet.iterator();
                while (oIPIter.hasNext())
                {
                    InvolvedPerson oIP = (InvolvedPerson)oIPIter.next();
                    sbOutput.append("\nInvolvement=" + oIP.getInvolvement() + ", Person=" + oIP.getPerson());
                }
            }
        }
        else
        {
            sbOutput.append(" none");
        }
        
        return sbOutput.toString();
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // text encoding
        oIDOS.writeUnsignedByte(m_oTextEncoding.getEncodingValue());
        // involvements
        Iterator oSetIter = m_oPeopleMap.values().iterator();
        while (oSetIter.hasNext())
        {
            Set oInvolvedPersonSet = (Set)oSetIter.next();
            Iterator oIPIter = oInvolvedPersonSet.iterator();
            while (oIPIter.hasNext())
            {
                InvolvedPerson oIP = (InvolvedPerson)oIPIter.next();
                
                oIDOS.write(oIP.getInvolvement().getBytes(m_oTextEncoding.getEncodingString()));
                // null after involvement
                if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
                {
                    oIDOS.writeUnsignedByte(0);
                }
                else
                {
                    oIDOS.writeUnsignedByte(0);
                    oIDOS.writeUnsignedByte(0);
                }
                oIDOS.write(oIP.getPerson().getBytes(m_oTextEncoding.getEncodingString()));
                // null after person
                if (m_oTextEncoding.equals(TextEncoding.ISO_8859_1))
                {
                    oIDOS.writeUnsignedByte(0);
                }
                else
                {
                    oIDOS.writeUnsignedByte(0);
                    oIDOS.writeUnsignedByte(0);
                }
            }
        }
    }
    
    /** Involved persons. */
    public static class InvolvedPerson implements Comparable
    {
        private String m_sInvolvement = null;
        private String m_sPerson = null;
        
        /** Constructor.
         *
         * @param sInvolvement the involvement of this given person
         * @param sPerson the name of the person
         * @throws ID3Exception if either sInvolvement or sPerson are either null or zero-length strings
         */
        public InvolvedPerson(String sInvolvement, String sPerson)
            throws ID3Exception
        {
            if ((sInvolvement == null) || (sInvolvement.length() == 0))
            {
                throw new ID3Exception("Involved persons in IPLS frames must have a defined involvement.");
            }
            m_sInvolvement = sInvolvement;
            if ((sPerson == null) || (sPerson.length() == 0))
            {
                throw new ID3Exception("Involved persons in IPLS frames must have a defined person name.");
            }
            m_sPerson = sPerson;
        }

        /** Get the involvement of this person.
         * 
         * @return the involvement of this person
         */
        public String getInvolvement()
        {
            return m_sInvolvement;
        }
        
        /** Get the name of the involved person.
         *
         * @return the name of the involved person
         */
        public String getPerson()
        {
            return m_sPerson;
        }
        
        public int compareTo(Object oOther)
        {
            InvolvedPerson oIP = (InvolvedPerson)oOther;

            return m_sPerson.compareTo(oIP.m_sPerson);
        }
        
        public int hashCode()
        {
            return m_sPerson.hashCode();
        }
    
        public boolean equals(Object oOther)
        {
            if ((oOther == null) || (!(oOther instanceof InvolvedPerson)))
            {
                return false;
            }

            InvolvedPerson oOtherIP = (InvolvedPerson)oOther;

            return ( (((m_sInvolvement == null) && (oOtherIP.m_sInvolvement == null)) || m_sInvolvement.equals(oOtherIP.m_sInvolvement)) &&
                     (((m_sPerson == null) && (oOtherIP.m_sPerson == null)) || m_sPerson.equals(oOtherIP.m_sPerson)) );
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof IPLSID3V2Frame)))
        {
            return false;
        }
        
        IPLSID3V2Frame oOtherIPLS = (IPLSID3V2Frame)oOther;
        
        return ( m_oTextEncoding.equals(oOtherIPLS.m_oTextEncoding) &&
                 m_oPeopleMap.equals(oOtherIPLS.m_oPeopleMap));
    }
}
