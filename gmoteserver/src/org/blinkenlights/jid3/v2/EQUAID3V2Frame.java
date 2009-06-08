/*
 * EQUAID3V2Frame.java
 *
 * Created on Jan 26, 2004
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
 * $Id: EQUAID3V2Frame.java,v 1.9 2005/02/06 18:11:15 paul Exp $
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
 * Frame containing equalization information for the playback of the track.
 */
public class EQUAID3V2Frame extends ID3V2Frame
{
    private byte m_byAdjustmentBits;
    private Map m_oFrequencyToAdjustmentMap = null;

    /** Constructor.
     *
     * @param byAdjustmentBits the number of bits of precision each adjustment contains
     */
    public EQUAID3V2Frame(byte byAdjustmentBits)
    {
        m_byAdjustmentBits = byAdjustmentBits;
        
        m_oFrequencyToAdjustmentMap = new HashMap();
    }

    public EQUAID3V2Frame(InputStream oIS)
        throws ID3Exception
    {
        try
        {
            ID3DataInputStream oFrameDataID3DIS = new ID3DataInputStream(oIS);
            
            // adjustment bits
            m_byAdjustmentBits = (byte)oFrameDataID3DIS.readUnsignedByte();
            
            m_oFrequencyToAdjustmentMap = new HashMap();

            // read adjustments
            while (oFrameDataID3DIS.available() > 0)
            {
                // read increment/decrement choice, and frequency
                int iIncrementAndFrequency = oFrameDataID3DIS.readBEUnsigned16();
                boolean bIncrement = (iIncrementAndFrequency & 32768) > 0;
                int iFrequency = (iIncrementAndFrequency & 32767);
                byte[] abyAdjustment = new byte[m_byAdjustmentBits/8];
                oFrameDataID3DIS.readFully(abyAdjustment);
                Adjustment oAdjustment = new Adjustment(bIncrement, iFrequency, abyAdjustment);
                m_oFrequencyToAdjustmentMap.put(new Integer(iFrequency), oAdjustment);
            }
        }
        catch (Exception e)
        {
            throw new InvalidFrameID3Exception(e);
        }
    }

    public void accept(ID3Visitor oID3Visitor)
    {
        oID3Visitor.visitEQUAID3V2Frame(this);
    }

    /** Set the number of bits of precision for each adjustment.
     *
     * @param byAdjustmentBits the number of bits of precision
     */
    public void setAdjustmentBits(byte byAdjustmentBits)
    {
        m_byAdjustmentBits = byAdjustmentBits;
    }

    /** Get the number of bits of precision for each adjustment.
     *
     * @return the number of bits of precision
     */
    public byte getAdjustmentBits()
    {
        return m_byAdjustmentBits;
    }
    
    /** Set an adjustment for a given frequency.  A new adjustment for a given frequency will
     * replace any existing one.
     *
     * @param oAdjustment the adjustment to be set
     */
    public void setAdjustment(Adjustment oAdjustment)
    {
        m_oFrequencyToAdjustmentMap.put(new Integer(oAdjustment.getFrequency()), oAdjustment);
    }

    /** Get the currently set adjustment for a given frequency.
     *
     * @return the set adjustment for the given frequency, or null if no adjustment has been set for it
     * @throws ID3Exception if the frequency specified is outside the range from 0-32767Hz
     */
    public Adjustment getAdjustment(int iFrequency)
        throws ID3Exception
    {
        if ((iFrequency < 0) || (iFrequency > 32767))
        {
            throw new ID3Exception("Valid frequency range for EQUA adjustments is from 0-32767Hz.");
        }
        
        return (Adjustment)m_oFrequencyToAdjustmentMap.get(new Integer(iFrequency));
    }

    /** Remove an existing adjustment.
     *
     * @param iFrequency the frequency of the adjustment to remove
     * @return the removed adjustment, or null if no adjustment was set at the specified frequency
     * @throws ID3Exception if the frequency specified is outside the range from 0-32767Hz
     */
    public Adjustment removeAdjustment(int iFrequency)
        throws ID3Exception
    {
        if ((iFrequency < 0) || (iFrequency > 32767))
        {
            throw new ID3Exception("Valid frequency range for EQUA adjustments is from 0-32767Hz.");
        }
        
        return (Adjustment)m_oFrequencyToAdjustmentMap.remove(new Integer(iFrequency));
    }

    /** Get all adjustments which have been set.
     *
     * @return an array of all adjustments which have been set
     */
    public Adjustment[] getAdjustments()
    {
        return (Adjustment[])m_oFrequencyToAdjustmentMap.values().toArray(new Adjustment[0]);
    }

    protected byte[] getFrameId()
    {
        return "EQUA".getBytes();
    }
    
    public String toString()
    {
        // "equalization" is misspelled in the spec
        return "Equalization: Adjustment Bits = " + m_byAdjustmentBits;
    }
    
    protected void writeBody(ID3DataOutputStream oIDOS)
        throws IOException
    {
        // adjustment bits
        oIDOS.writeUnsignedByte(m_byAdjustmentBits);
        
        // adjustments
        Adjustment[] aoAdjustment = getAdjustments();
        for (int i=0; i < aoAdjustment.length; i++)
        {
            aoAdjustment[i].write(oIDOS);
        }
    }
    
    public boolean equals(Object oOther)
    {
        if ((oOther == null) || (!(oOther instanceof EQUAID3V2Frame)))
        {
            return false;
        }
        
        EQUAID3V2Frame oOtherEQUA = (EQUAID3V2Frame)oOther;
        
        return ((m_byAdjustmentBits == oOtherEQUA.m_byAdjustmentBits) &&
                m_oFrequencyToAdjustmentMap.equals(oOtherEQUA.m_oFrequencyToAdjustmentMap));
    }

    /** Adjustment details for specific frequencies in EQUA frame.
     */
    public class Adjustment
    {
        private boolean m_bIncrement;
        private int m_iFrequency;
        private byte[] m_abyAdjustment;
        
        /** Constructor.
         *
         * Note: Adjustment bytes must be provided explicitly, because they could be most of least significant
         *       byte order.  This is up to the implementor.
         *
         * @param bIncrement true if this adjustment is a volume boost, false otherwise
         * @param iFrequency the frequency to be adjusted (0-32767Hz)
         * @param abyAdjustment the adjustment bytes (note the number of bits must correspond to the adjustment
         *        bits precision set for this frame, although the format for this frame is otherwise undefined)
         * @throws ID3Exception if the frequency specified is not in the valid range
         * @throws ID3Exception if the adjustment bytes are not provided
         */
        public Adjustment(boolean bIncrement, int iFrequency, byte[] abyAdjustment)
            throws ID3Exception
        {
            m_bIncrement = bIncrement;
            
            if ((iFrequency < 0) || (iFrequency > 32767))
            {
                throw new ID3Exception("The valid frequency range for EQUA frame is from 0 to 32767Hz.");
            }
            m_iFrequency = iFrequency;
            
            if ((abyAdjustment == null) || (abyAdjustment.length == 0))
            {
                throw new ID3Exception("Adjustment bytes must be specified for EQUA frame.");
            }
            m_abyAdjustment = abyAdjustment;
        }
        
        /** Check if this adjustment is a volume boost.
         *
         * @return true is the adjustment is a volume boost, false otherwise
         */
        public boolean isIncrement()
        {
            return m_bIncrement;
        }
        
        /** Check if this adjustment is a volume decrease.  (Note this method is the negative value of isIncrement().
         *
         * @return true if this adjustment is a volume decrease, false otherwise.
         */
        public boolean isDecrement()
        {
            return !m_bIncrement;
        }
        
        /** Get the frequency to be adjusted.
         *
         * @return the frequency to be adjusted, in Hertz.
         */
        public int getFrequency()
        {
            return m_iFrequency;
        }

        /** Get the frequency adjustment bytes.  Note, the specific application needs to know how these
         * were written, to make any proper use of them.
         *
         * @return the frequency adjustment bytes
         */
        public byte[] getAdjustment()
        {
            return m_abyAdjustment;
        }
        
        private void write(ID3DataOutputStream oIDOS)
            throws IOException
        {
            int iIncrementAndFrequency = m_iFrequency;
            if (m_bIncrement)
            {
                iIncrementAndFrequency |= 32768;
            }
            oIDOS.writeBEUnsigned16(iIncrementAndFrequency);
            oIDOS.write(m_abyAdjustment);
        }
    
        public boolean equals(Object oOther)
        {
            if ((oOther == null) || (!(oOther instanceof Adjustment)))
            {
                return false;
            }

            Adjustment oOtherAdjustment = (Adjustment)oOther;

            return ((m_bIncrement == oOtherAdjustment.m_bIncrement) &&
                    (m_iFrequency == oOtherAdjustment.m_iFrequency) &&
                    Arrays.equals(m_abyAdjustment, oOtherAdjustment.m_abyAdjustment));
        }
    }
}
