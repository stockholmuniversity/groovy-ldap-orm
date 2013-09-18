package se.su.it.ldap.orm.mixin

import org.apache.directory.api.ldap.model.entry.Attribute
import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class OrmInstantiator {

  static Map foo = [:]

  Class schema
  Map<String, String> attributeMap = [:]

  public OrmInstantiator(Class schema) {
    this.schema = schema
    attributeMap = generateAttributeMap()
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

    object
  }

  private Map<String, String> generateAttributeMap() {
    Map mappings = [:]

    schema.declaredFields.each { Field field ->
      if (!Modifier.isStatic(field.modifiers) && hasGetter(schema, field.name) && hasSetter(schema, field.name)) {
        mappings.put field.name, field.name
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
