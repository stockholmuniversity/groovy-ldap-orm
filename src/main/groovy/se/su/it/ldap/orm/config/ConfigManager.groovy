package se.su.it.ldap.orm.config

import org.apache.directory.ldap.client.api.LdapConnection
import org.apache.directory.ldap.client.api.LdapConnectionPool
import org.springframework.beans.factory.annotation.Autowired

class ConfigManager {

  @Autowired
  private LdapConnectionPool connectionPool

  public LdapConnection getConnection() {
    connectionPool.connection
  }
}
