package se.su.it.ldap.orm.filter

class Filter {

  private String filter

  public Filter(String filter) {
    this.filter = filter ?: ''

    if (! (this.filter ==~ /^\(.*\)$/)) {
      this.filter = "(${this.filter})"
    }
  }

  static Filter and(List<Filter> filter) {
    new Filter("(&${filter*.toString()?.join('')})")
  }

  static Filter or(List<Filter> filter) {
    new Filter("(|${filter*.toString()?.join('')})")
  }

  String toString() {
    filter
  }
}
