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
      if (attributeMap.containsValue(attribute.id) && schema.metaClass.hasProperty(object, attribute.id)) {
        schema.metaClass.setProperty(object, attribute.id, attribute.get().string)
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
