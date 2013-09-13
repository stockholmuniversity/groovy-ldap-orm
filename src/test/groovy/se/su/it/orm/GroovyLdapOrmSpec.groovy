package se.su.it.orm

import se.su.it.ldap.orm.GroovyLdapOrm
import spock.lang.Specification

class GroovyLdapOrmSpec extends Specification {

  def "Test"() {
    expect:
    new GroovyLdapOrm().configManager.ldapConnectionConfig.ldapHost == 'foo'
  }
}
