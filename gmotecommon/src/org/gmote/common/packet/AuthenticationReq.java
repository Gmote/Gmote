/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gmote.common.packet;

import org.gmote.common.Protocol.Command;

public class AuthenticationReq extends AbstractPacket {

  private static final long serialVersionUID = 1L;

  String challenge;
  String serverVersion;
  
  /**
   * Constructor.
   * @param challenge the challenge to pose to the client. The client must be able to hash this.
   * @param serverVersion the version of the server. This may help if we change our authentication mechanism in the future.
   */
  public AuthenticationReq(String challenge, String serverVersion) {
    super(Command.AUTH_REQ);
    this.challenge = challenge;
    this.serverVersion = serverVersion;
  }
  
  public String getChallenge() {
    return challenge;
  }

  public String getServerVersion() {
    return serverVersion;
  }
}
