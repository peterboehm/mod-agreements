package org.olf.kb

import grails.gorm.MultiTenant


/**
 * mod-erm representation of a package
 */
public class Package implements MultiTenant<Package> {

  String id
  String name
  String source
  String reference

  static mapping = {
                   id column:'pkg_id', generator: 'uuid', length:36
              version column:'pkg_version'
                 name column:'pkg_name'
               source column:'pkg_source'
            reference column:'pkg_reference'
  }

  static constraints = {
          name(nullable:false, blank:false)
        source(nullable:false, blank:false)
     reference(nullable:false, blank:false)
  }


}