package se.su.it.ldap.orm

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

class GroovyLdapOrm {

  public GroovyLdapOrm(){
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("beans.xml");
    applicationContext.getBean('ldapConnectionConfig')
  }

  public void init() {

  }
}
