package se.su.it.ldap.orm.annotations

import java.lang.annotation.*

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AttributeLanguage {
  String name() default null;
  String lang() default 'en';
}
