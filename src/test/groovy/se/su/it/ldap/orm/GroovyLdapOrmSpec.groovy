/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.ldap.orm

import org.springframework.context.support.ClassPathXmlApplicationContext
import se.su.it.ldap.orm.config.ConfigManager
import se.su.it.ldap.orm.mixin.GroovyLdapSchema
import spock.lang.Specification

class GroovyLdapOrmSpec extends Specification {

  def setup() {
    ConfigManager.metaClass.static.getInstance = { new ConfigManager() }
  }

  def cleanup() {
    ConfigManager.metaClass = null
  }

  def "Constructor should load custom config to config manager"() {
    setup:
    def customConfig = new ConfigObject()

    GroovyMock(ClassPathXmlApplicationContext, global: true)
    def configManager = GroovyMock(ConfigManager)
    ConfigManager.metaClass.static.getInstance = { configManager }

    when:
    new GroovyLdapOrm(customConfig)

    then:
    1 * configManager.loadConfig(customConfig)
  }

  def "init should apply mixin to configured schema classes"() {
    setup:
    def customConfig = new ConfigObject()
    customConfig.schemas = [
            DummySchema
    ]
    GroovyMock(DummySchema, global: true)

    when:
    new GroovyLdapOrm(customConfig).init()

    then:
    1 * DummySchema.mixin(GroovyLdapSchema)
  }

  class DummySchema {

  }
}
