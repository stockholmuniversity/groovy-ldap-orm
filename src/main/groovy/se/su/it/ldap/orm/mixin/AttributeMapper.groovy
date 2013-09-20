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

import org.apache.directory.api.ldap.model.entry.Attribute
import org.apache.directory.api.ldap.model.entry.DefaultAttribute
import org.codehaus.groovy.runtime.MetaClassHelper
import se.su.it.ldap.orm.annotations.AttributeLanguage
import se.su.it.ldap.orm.annotations.AttributeName

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

class AttributeMapper {

  private static List<String> excludedProperties = ['dn', 'metaClass']
  private static ConcurrentHashMap<String, AttributeMapper> attributeMappers = [:]

  private Class schema

  /** The mapper between the schema and the ldap entry.
   *  The key is the schema property
   *  The value is the ldap entry attribute */
  private Map<String, String> attributeMap = [:]

  private AttributeMapper(Class schema) {
    this.schema = schema
    attributeMap = generateAttributeMap()
  }

  public static synchronized AttributeMapper getInstance(Class schema) {
    if (attributeMappers.containsKey(schema.name)) {
      return attributeMappers.get(schema.name)
    }

    AttributeMapper mapper = new AttributeMapper(schema)
    attributeMappers.put(schema.name, mapper)

    return mapper
  }

  public Map<String, String> getAttributeMap() {
    return attributeMap.clone() as Map
  }

  public boolean schemaHasAttribute(String attributeName) {
    attributeMap.containsKey(attributeName)
  }

  public Object newSchemaInstance(Collection<Attribute> attributes) {
    Object object = schema.newInstance()

    attributes?.each { attribute ->
      def name = attribute.upId
      if (attributeMap.containsValue(name) && schema.metaClass.hasProperty(object, name)) {
        MetaProperty prop = schema.metaClass.properties.find { it.name == name }

        Object value
        switch (prop.type) {
          case Set:
            value = attribute*.string
            break
          case String:
            value = attribute*.string
            value = value?.first()
            break
        }

        if(value) {
          schema.metaClass.setProperty(object, name, value)
        }
      }
    }

    object.emptyDirtyPropertyList()

    object
  }

  public Attribute[] generateAttributes(GroovyObject object) {
    generateAttributes(object, attributeMap.keySet())
  }

  public Attribute[] generateAttributes(GroovyObject object, Set<String> dirtyProperties) {
    Collection<Attribute> attributes = []

    for(property in dirtyProperties) {
      if (object.hasProperty(property) && attributeMap.containsKey(property)) {
        Object propVal = object.getProperty(property)
        if (propVal) {
          Attribute attribute = new DefaultAttribute(attributeMap.get(property))
          if (propVal instanceof String) {
            attribute.add(propVal)
            attributes << attribute
          }
          else if (propVal instanceof Set) {
            attribute.add(propVal as String[])
            attributes << attribute
          }
        }
      }
    }

    return attributes
  }

  private Map<String, String> generateAttributeMap() {
    Map mappings = [:]

    def fields = (schema.declaredFields + GroovyLdapSchema.declaredFields)
    fields.each { Field field ->
      if (!Modifier.isStatic(field.modifiers) &&
              hasGetter(schema, field.name) &&
              hasSetter(schema, field.name) &&
              !excludedProperties.contains(field.name)){

        def realFieldName = null

        if(field.isAnnotationPresent(AttributeName)) {
          realFieldName = field.getAnnotation(AttributeName)?.value()
        }

        if (field.isAnnotationPresent(AttributeLanguage)) {
          String fieldName = realFieldName ?: field.getAnnotation(AttributeLanguage)?.name()
          String lang = field.getAnnotation(AttributeLanguage)?.lang()

          if (fieldName && lang) {
            realFieldName = "${fieldName};lang-${lang}"
          }
        }

        mappings.put(field.name, realFieldName ?: field.name)
      }
    }

    mappings
  }

  static boolean hasGetter(Class schema, String fieldName) {
    String getter = "get${MetaClassHelper.capitalize(fieldName) }"
    schema.metaClass.methods.find { it.name == getter } != null
  }

  static boolean hasSetter(Class schema, String fieldName) {
    String getter = "set${MetaClassHelper.capitalize(fieldName) }"
    schema.metaClass.methods.find { it.name == getter } != null
  }
}
