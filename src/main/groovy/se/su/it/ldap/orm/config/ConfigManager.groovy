package se.su.it.ldap.orm.config

import org.apache.directory.ldap.client.api.LdapConnectionConfig
import org.springframework.beans.factory.annotation.Autowired
import se.su.it.ldap.orm.GroovyLdapOrm

class ConfigManager {

  @Autowired
  LdapConnectionConfig ldapConnectionConfig

}
