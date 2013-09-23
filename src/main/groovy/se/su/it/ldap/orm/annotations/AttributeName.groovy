package se.su.it.ldap.orm.annotations

import java.lang.annotation.*

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AttributeName {
  String value()
}
