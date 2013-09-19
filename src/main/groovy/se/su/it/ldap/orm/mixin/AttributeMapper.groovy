package se.su.it.ldap.orm.mixin

import org.apache.directory.api.ldap.model.entry.Attribute
import org.apache.directory.api.ldap.model.entry.DefaultAttribute
import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

class AttributeMapper {

  private static List<String> excludedProperties = ['dn', 'metaClass']
  private static ConcurrentHashMap<String, AttributeMapper> attributeMappers = [:]

  private Class schema
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
              !excludedProperties.contains(field.name)) {
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
