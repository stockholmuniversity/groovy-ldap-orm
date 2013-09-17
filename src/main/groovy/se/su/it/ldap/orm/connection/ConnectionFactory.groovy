package se.su.it.ldap.orm.connection

import org.apache.directory.api.ldap.model.exception.LdapException
import org.apache.directory.api.ldap.model.message.BindResponse
import org.apache.directory.api.ldap.model.message.LdapResult
import org.apache.directory.api.ldap.model.message.ResultCodeEnum
import org.apache.directory.ldap.client.api.LdapConnection
import org.apache.directory.ldap.client.api.LdapConnectionPool
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

    BindResponse response

    if (config.ldap.connection.gssapi) {
      SaslGssApiRequest saslGssApiRequest = applicationContext.getBean('saslGssApiRequest')
      saslGssApiRequest.loginContextName = config.ldap.connection.loginContextName

      response = connection.bind(saslGssApiRequest)
    }
    else {
      response = connection.bind()
    }

    LdapResult ldapResult = response.getLdapResult()
    if(!ResultCodeEnum.SUCCESS == ldapResult.getResultCode() ||
            !connection.isConnected() || !connection.isAuthenticated()) {
      throw new LdapException(this.class.name + " - Could not bind connection (${ldapResult.resultCode}):" + ldapResult.diagnosticMessage)
    }

    connection
  }
}
