package se.su.it.ldap.orm

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import se.su.it.ldap.orm.config.ConfigManager

class GroovyLdapOrm {

  ConfigManager configManager

  public GroovyLdapOrm(ConfigObject customConfig) {
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("beans.xml");
    configManager = applicationContext.getBean('configManager')

    configManager.loadConfig(customConfig)
  }

  public void init() {

  }
}
