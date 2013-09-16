package se.su.it.ldap.orm.config

import spock.lang.Specification

class ConfigManagerSpec extends Specification {

  def "ConfigManager.loadConfig should merge supplied config"() {
    setup:
    def config = Mock(ConfigObject)
    def configManager = new ConfigManager(config: config)
    ConfigObject co = new ConfigObject()

    when:
    configManager.loadConfig(co)

    then:
    1 * config.merge(co)
  }

  def "ConfigManager.getConfig should return the config"() {
    setup:
    def config = new ConfigObject()
    def configManager = new ConfigManager(config: config)

    when:
    def ret = configManager.config

    then:
    ret == config
  }
}
