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

package se.su.it.ldap.orm.mixin

import org.apache.directory.api.ldap.model.cursor.SearchCursor
import org.apache.directory.api.ldap.model.entry.Attribute
import org.apache.directory.api.ldap.model.entry.DefaultEntry
import org.apache.directory.api.ldap.model.entry.Entry
import org.apache.directory.api.ldap.model.entry.ModificationOperation
import org.apache.directory.api.ldap.model.message.*
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.mina.util.ConcurrentHashSet
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.metaclass.OwnedMetaClass
import se.su.it.ldap.orm.connection.ConnectionFactory
import se.su.it.ldap.orm.connection.Status

class GroovyLdapSchema {

  static ConnectionFactory connectionFactory = ConnectionFactory.instance

  Dn dn

  private ConcurrentHashSet<String> dirtyProperties = []

  Status create() {
    def connection = connectionFactory.connection

    Entry entry = new DefaultEntry(dn: dn)

    def mapper = AttributeMapper.getInstance(this.class)
    entry.add mapper.generateAttributes(this)

    AddRequest request = new AddRequestImpl()
    request.setEntry(entry)

    ResultResponse response = connection.add(request)

    // Clean the dirty properties
    if (response?.ldapResult?.resultCode == ResultCodeEnum.SUCCESS) {
      emptyDirtyPropertyList()
    }

    return Status.newInstance(response)
  }

  Status modify() {
    def connection = connectionFactory.connection

    ModifyRequest request = new ModifyRequestImpl(name: dn)

    def mapper = AttributeMapper.getInstance(this.class)

    // Get the dirty attributes
    List<Attribute> attributes = mapper.generateAttributes(this, dirtyProperties)

    attributes.each { Attribute attribute ->
      request.addModification(attribute, ModificationOperation.REPLACE_ATTRIBUTE)
    }

    ResultResponse response = connection.modify(request)

    // Clean the dirty properties
    if (response?.ldapResult?.resultCode == ResultCodeEnum.SUCCESS) {
      emptyDirtyPropertyList()
    }

    return Status.newInstance(response)
  }

  @Override
  void setProperty(String property, Object newValue) {
    AttributeMapper mapper = AttributeMapper.getInstance(this.class)

    if (mapper.schemaHasAttribute(property)) {
      this.dirtyProperties << property
    }

    //this.properties[property] = newValue
    if (metaClass instanceof OwnedMetaClass && metaClass.owner.class.declaredFields.find { it.name == property }) {
      metaClass.owner.@"$property" = newValue
    }
    else {
      this.invokeMethod("set${MetaClassHelper.capitalize(property) }", newValue)
    }
  }

  void emptyDirtyPropertyList() {
    dirtyProperties = []
  }

  static Object find(Map args) {
    args.limit = 1

    def ret = findAll(args)
    ret ? ret[0] : null
  }

  static Object[] findAll(Map args) {
    def connection = connectionFactory.connection
    SearchRequest request = new SearchRequestImpl()
    request.setBase(new Dn(args.base ?: ''))
    request.setFilter(args.filter as String)
    request.setScope(args.scope ?: SearchScope.SUBTREE)

    if (args.limit) {
      request.setSizeLimit(args.limit)
    }

    if (args.attributes) {
      request.addAttributes(args.attributes as String[])
    }

    SearchCursor cursor = connection.search(request)

    List<Object> entries = new ArrayList<Object>()

    while(!cursor.done) {
      if (cursor.isEntry()) {
        entries << cursor.getEntry()
      }

      cursor.next()
    }

    entries
  }
}
