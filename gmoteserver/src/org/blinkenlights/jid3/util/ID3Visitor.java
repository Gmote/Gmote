/*
 * ID3Visitor.java
 *
 * Created on January 14, 2005, 7:50 PM
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
 * $Id: ID3Visitor.java,v 1.3 2005/02/06 18:11:25 paul Exp $
 */

package org.blinkenlights.jid3.util;

import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;

/** Abstract base class from which tag and frame visitors can be derived, as part of the implementation
 *  of a visitor pattern.  This class contains do-nothing methods for all visit methods, so only the methods
 *  required need be implemented in any derived class.  Note that instructing a V2 tag object to accept a
 *  visitor will cause all of the frames contained in it to be visited.
 *
 *  See the AllTests test class for a sample implementation of a visitor.
 *
 * @author  paul
 */
abstract public class ID3Visitor
{
    public void visitID3V1_0Tag(ID3V1_0Tag oID3V1_0Tag) {};
    public void visitID3V1_1Tag(ID3V1_1Tag oID3V1_1Tag) {};

    public void visitID3V2_3_0Tag(ID3V2_3_0Tag oID3V2_3_0Tag) {};
    
    public void visitAENCID3V2Frame(AENCID3V2Frame oAENCID3V2Frame) {};
    public void visitAPICID3V2Frame(APICID3V2Frame oAPICID3V2Frame) {};
    public void visitCOMMID3V2Frame(COMMID3V2Frame oCOMMID3V2Frame) {};
    public void visitCOMRID3V2Frame(COMRID3V2Frame oCOMRID3V2Frame) {};
    public void visitENCRID3V2Frame(ENCRID3V2Frame oENCRID3V2Frame) {};
    public void visitEQUAID3V2Frame(EQUAID3V2Frame oEQUAID3V2Frame) {};
    public void visitETCOID3V2Frame(ETCOID3V2Frame oETCOID3V2Frame) {};
    public void visitGEOBID3V2Frame(GEOBID3V2Frame oGEOBID3V2Frame) {};
    public void visitGRIDID3V2Frame(GRIDID3V2Frame oGRIDID3V2Frame) {};
    public void visitIPLSID3V2Frame(IPLSID3V2Frame oIPLSID3V2Frame) {};
    public void visitLINKID3V2Frame(LINKID3V2Frame oLINKID3V2Frame) {};
    public void visitMCDIID3V2Frame(MCDIID3V2Frame oMCDIID3V2Frame) {};
    public void visitMLLTID3V2Frame(MLLTID3V2Frame oMLLTID3V2Frame) {};
    public void visitOWNEID3V2Frame(OWNEID3V2Frame oOWNEID3V2Frame) {};
    public void visitPCNTID3V2Frame(PCNTID3V2Frame oPCNTID3V2Frame) {};
    public void visitPOPMID3V2Frame(POPMID3V2Frame oPOPMID3V2Frame) {};
    public void visitPOSSID3V2Frame(POSSID3V2Frame oPOSSID3V2Frame) {};
    public void visitPRIVID3V2Frame(PRIVID3V2Frame oPRIVID3V2Frame) {};
    public void visitRBUFID3V2Frame(RBUFID3V2Frame oRBUFID3V2Frame) {};
    public void visitRVADID3V2Frame(RVADID3V2Frame oRVADID3V2Frame) {};
    public void visitRVRBID3V2Frame(RVRBID3V2Frame oRVRBID3V2Frame) {};
    public void visitSYLTID3V2Frame(SYLTID3V2Frame oSYLTID3V2Frame) {};
    public void visitSYTCID3V2Frame(SYTCID3V2Frame oSYTCID3V2Frame) {};
    public void visitTALBTextInformationID3V2Frame(TALBTextInformationID3V2Frame oTALBTextInformationID3V2Frame) {};
    public void visitTBPMTextInformationID3V2Frame(TBPMTextInformationID3V2Frame oTBPMTextInformationID3V2Frame) {};
    public void visitTCOMTextInformationID3V2Frame(TCOMTextInformationID3V2Frame oTCOMTextInformationID3V2Frame) {};
    public void visitTCONTextInformationID3V2Frame(TCONTextInformationID3V2Frame oTCONTextInformationID3V2Frame) {};
    public void visitTCOPTextInformationID3V2Frame(TCOPTextInformationID3V2Frame oTCOPTextInformationID3V2Frame) {};
    public void visitTDATTextInformationID3V2Frame(TDATTextInformationID3V2Frame oTDATTextInformationID3V2Frame) {};
    public void visitTDLYTextInformationID3V2Frame(TDLYTextInformationID3V2Frame oTDLYTextInformationID3V2Frame) {};
    public void visitTENCTextInformationID3V2Frame(TENCTextInformationID3V2Frame oTENCTextInformationID3V2Frame) {};
    public void visitTEXTTextInformationID3V2Frame(TEXTTextInformationID3V2Frame oTEXTTextInformationID3V2Frame) {};
    public void visitTFLTTextInformationID3V2Frame(TFLTTextInformationID3V2Frame oTFLTTextInformationID3V2Frame) {};
    public void visitTIMETextInformationID3V2Frame(TIMETextInformationID3V2Frame oTIMETextInformationID3V2Frame) {};
    public void visitTIT1TextInformationID3V2Frame(TIT1TextInformationID3V2Frame oTIT1TextInformationID3V2Frame) {};
    public void visitTIT2TextInformationID3V2Frame(TIT2TextInformationID3V2Frame oTIT2TextInformationID3V2Frame) {};
    public void visitTIT3TextInformationID3V2Frame(TIT3TextInformationID3V2Frame oTIT3TextInformationID3V2Frame) {};
    public void visitTKEYTextInformationID3V2Frame(TKEYTextInformationID3V2Frame oTKEYTextInformationID3V2Frame) {};
    public void visitTLANTextInformationID3V2Frame(TLANTextInformationID3V2Frame oTLANTextInformationID3V2Frame) {};
    public void visitTLENTextInformationID3V2Frame(TLENTextInformationID3V2Frame oTLENTextInformationID3V2Frame) {};
    public void visitTMEDTextInformationID3V2Frame(TMEDTextInformationID3V2Frame oTMEDTextInformationID3V2Frame) {};
    public void visitTOALTextInformationID3V2Frame(TOALTextInformationID3V2Frame oTOALTextInformationID3V2Frame) {};
    public void visitTOFNTextInformationID3V2Frame(TOFNTextInformationID3V2Frame oTOFNTextInformationID3V2Frame) {};
    public void visitTOLYTextInformationID3V2Frame(TOLYTextInformationID3V2Frame oTOLYTextInformationID3V2Frame) {};
    public void visitTOPETextInformationID3V2Frame(TOPETextInformationID3V2Frame oTOPETextInformationID3V2Frame) {};
    public void visitTORYTextInformationID3V2Frame(TORYTextInformationID3V2Frame oTORYTextInformationID3V2Frame) {};
    public void visitTOWNTextInformationID3V2Frame(TOWNTextInformationID3V2Frame oTOWNTextInformationID3V2Frame) {};
    public void visitTPE1TextInformationID3V2Frame(TPE1TextInformationID3V2Frame oTPE1TextInformationID3V2Frame) {};
    public void visitTPE2TextInformationID3V2Frame(TPE2TextInformationID3V2Frame oTPE2TextInformationID3V2Frame) {};
    public void visitTPE3TextInformationID3V2Frame(TPE3TextInformationID3V2Frame oTPE3TextInformationID3V2Frame) {};
    public void visitTPE4TextInformationID3V2Frame(TPE4TextInformationID3V2Frame oTPE4TextInformationID3V2Frame) {};
    public void visitTPOSTextInformationID3V2Frame(TPOSTextInformationID3V2Frame oTPOSTextInformationID3V2Frame) {};
    public void visitTPUBTextInformationID3V2Frame(TPUBTextInformationID3V2Frame oTPUBTextInformationID3V2Frame) {};
    public void visitTRCKTextInformationID3V2Frame(TRCKTextInformationID3V2Frame oTRCKTextInformationID3V2Frame) {};
    public void visitTRDATextInformationID3V2Frame(TRDATextInformationID3V2Frame oTRDATextInformationID3V2Frame) {};
    public void visitTRSNTextInformationID3V2Frame(TRSNTextInformationID3V2Frame oTRSNTextInformationID3V2Frame) {};
    public void visitTRSOTextInformationID3V2Frame(TRSOTextInformationID3V2Frame oTRSOTextInformationID3V2Frame) {};
    public void visitTSIZTextInformationID3V2Frame(TSIZTextInformationID3V2Frame oTSIZTextInformationID3V2Frame) {};
    public void visitTSRCTextInformationID3V2Frame(TSRCTextInformationID3V2Frame oTSRCTextInformationID3V2Frame) {};
    public void visitTSSETextInformationID3V2Frame(TSSETextInformationID3V2Frame oTSSETextInformationID3V2Frame) {};
    public void visitTXXXTextInformationID3V2Frame(TXXXTextInformationID3V2Frame oTXXXTextInformationID3V2Frame) {};
    public void visitTYERTextInformationID3V2Frame(TYERTextInformationID3V2Frame oTYERTextInformationID3V2Frame) {};
    public void visitUFIDID3V2Frame(UFIDID3V2Frame oUFIDID3V2Frame) {};
    public void visitUSERID3V2Frame(USERID3V2Frame oUSERID3V2Frame) {};
    public void visitUSLTID3V2Frame(USLTID3V2Frame oUSLTID3V2Frame) {};
    public void visitWCOMUrlLinkID3V2Frame(WCOMUrlLinkID3V2Frame oWCOMUrlLinkID3V2Frame) {};
    public void visitWCOPUrlLinkID3V2Frame(WCOPUrlLinkID3V2Frame oWCOPUrlLinkID3V2Frame) {};
    public void visitWOAFUrlLinkID3V2Frame(WOAFUrlLinkID3V2Frame oWOAFUrlLinkID3V2Frame) {};
    public void visitWOARUrlLinkID3V2Frame(WOARUrlLinkID3V2Frame oWOARUrlLinkID3V2Frame) {};
    public void visitWOASUrlLinkID3V2Frame(WOASUrlLinkID3V2Frame oWOASUrlLinkID3V2Frame) {};
    public void visitWORSUrlLinkID3V2Frame(WORSUrlLinkID3V2Frame oWORSUrlLinkID3V2Frame) {};
    public void visitWPAYUrlLinkID3V2Frame(WPAYUrlLinkID3V2Frame oWPAYUrlLinkID3V2Frame) {};
    public void visitWPUBUrlLinkID3V2Frame(WPUBUrlLinkID3V2Frame oWPUBUrlLinkID3V2Frame) {};
    public void visitWXXXUrlLinkID3V2Frame(WXXXUrlLinkID3V2Frame oWXXXUrlLinkID3V2Frame) {};
    public void visitEncryptedID3V2Frame(EncryptedID3V2Frame oEncryptedID3V2Frame) {};
    public void visitUnknownID3V2Frame(UnknownID3V2Frame oUnknownID3V2Frame) {};
    public void visitUnknownTextInformationID3V2Frame(UnknownTextInformationID3V2Frame oUnknownTextInformationID3V2Frame) {};
    public void visitUnknownUrlLinkID3V2Frame(UnknownUrlLinkID3V2Frame oUnknownUrlLinkID3V2Frame) {};
}
