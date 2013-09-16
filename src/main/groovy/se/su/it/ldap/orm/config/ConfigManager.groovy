package se.su.it.ldap.orm.config

class ConfigManager {

  public static final String CONFIG_FILE = 'config_defaults.groovy'

  private ConfigObject config

  public ConfigManager() {
    config = new ConfigSlurper().parse(CONFIG_FILE)
  }

  public void loadConfig(ConfigObject customConfig) throws IOException {
    config.merge(customConfig)
  }

  public ConfigObject getConfig() {
    config
  }
}
