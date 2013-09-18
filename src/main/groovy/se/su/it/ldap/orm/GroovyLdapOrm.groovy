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

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import se.su.it.ldap.orm.config.ConfigManager
import se.su.it.ldap.orm.mixin.GroovyLdapSchema
import se.su.it.ldap.orm.mixin.OrmInstantiator
import org.apache.directory.api.ldap.model.entry.Entry

class GroovyLdapOrm {

  ApplicationContext applicationContext
  ConfigManager configManager

  Map<String, OrmInstantiator> initiators = [:]

  public GroovyLdapOrm(ConfigObject customConfig) {
    configManager = ConfigManager.instance
    configManager.loadConfig(customConfig)

    applicationContext = new ClassPathXmlApplicationContext("beans.xml");
  }

  public void init() {
    configManager.config.schemas?.each { Class schema ->
      schema.mixin GroovyLdapSchema
      schema.metaClass.static.methodMissing = { String name, Object[] args ->
        OrmInstantiator instantiator = initiators.get(delegate.name)

        /** Add the declared fields of the schema to the ldap search */
        if (name.startsWith('find') && args?.size() && args[0] instanceof Map && !args[0].attributes) {
          args[0].attributes =  instantiator.attributeMap.keySet()
        }

        def ret = GroovyLdapSchema.invokeMethod(name, args)

        if (ret instanceof Object[]) {
          return ret.collect { Entry entry ->
            instantiator.newSchemaInstance(entry.attributes)
          }
        }
        else if (ret instanceof Entry) {
          return instantiator.newSchemaInstance(ret.attributes)
        }

      }

      initiators.put(schema.name, new OrmInstantiator(schema))
    }
  }
}
