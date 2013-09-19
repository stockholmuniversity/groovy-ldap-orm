package se.su.it.ldap.orm.connection

import org.apache.directory.api.ldap.model.message.ResultResponse

class Status {

  int ldapResultCode
  String message

  public static newInstance(ResultResponse response) {
    int value = response?.ldapResult?.resultCode?.value
    String message = response?.ldapResult?.resultCode?.message

    new Status(ldapResultCode: value, message: message)
  }
}
