/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.ldap.orm.connection

import org.apache.directory.api.ldap.model.exception.LdapException
import org.apache.directory.api.ldap.model.message.BindResponse
import org.apache.directory.api.ldap.model.message.LdapResult
import org.apache.directory.api.ldap.model.message.ResultCodeEnum
import org.apache.directory.ldap.client.api.LdapConnection
import org.apache.directory.ldap.client.api.LdapConnectionPool
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import org.apache.directory.ldap.client.api.SaslGssApiRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import se.su.it.ldap.orm.config.ConfigManager

@Singleton
class ConnectionFactory {

  @Autowired
  LdapConnectionPool ldapConnectionPool

  @Autowired
  ConfigManager configManager

  @Autowired
  ApplicationContext applicationContext

  public LdapConnection getConnection() {
    def config = configManager.config

    LdapConnection connection = ldapConnectionPool.getConnection()
    connection.setTimeOut(config.ldap.connection.timeout)

    if (config.ldap.connection.gssapi && connection instanceof LdapNetworkConnection) {
      SaslGssApiRequest saslGssApiRequest = applicationContext.getBean('saslGssApiRequest')
      saslGssApiRequest.loginContextName = config.ldap.connection.loginContextName

      BindResponse response = connection.bind(saslGssApiRequest)

      LdapResult ldapResult = response?.getLdapResult()
      if(!ResultCodeEnum.SUCCESS == ldapResult?.getResultCode() ||
              !connection.connected || !connection.authenticated) {
        throw new LdapException(this.class.name + " - Could not bind connection (${ldapResult.resultCode}):" + ldapResult.diagnosticMessage)
      }
    }

    if (!connection.connected || !connection.authenticated) {
      connection.anonymousBind() // Anonymous bind
    }

    connection
  }
}
