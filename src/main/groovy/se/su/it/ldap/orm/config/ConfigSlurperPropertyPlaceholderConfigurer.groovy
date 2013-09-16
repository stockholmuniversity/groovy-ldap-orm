package se.su.it.ldap.orm.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer

class ConfigSlurperPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

  @Autowired
  ConfigManager configManager

  @Override
  protected void loadProperties(Properties props) throws IOException {
    props.putAll(configManager.config.toProperties())
  }
}
