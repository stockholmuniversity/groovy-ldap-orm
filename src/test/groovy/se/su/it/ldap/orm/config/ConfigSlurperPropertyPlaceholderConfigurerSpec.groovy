package se.su.it.ldap.orm.config

import spock.lang.Specification

class ConfigSlurperPropertyPlaceholderConfigurerSpec extends Specification {

  def "loadProperties should put all ConfigManager.config properties to prop"() {
    setup:
    Properties properties = Mock(Properties)
    def configurer = new ConfigSlurperPropertyPlaceholderConfigurer()
    def configManager = Mock(ConfigManager)
    def config = Mock(ConfigObject)
    def configProps = Mock(Properties)

    configurer.configManager = configManager
    configManager.config >> config

    when:
    configurer.loadProperties(properties)

    then:
    1 * properties.putAll(configProps)
    1 * config.toProperties() >> configProps
  }
}
