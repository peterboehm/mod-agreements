package org.olf.general

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.Defaults

class EventLog implements MultiTenant<EventLog> {
  String id
  String message
  Date dateCreated
  String origin
  String detail

  static mapping = {
              id column: 'el_id', generator: 'uuid2', length:36
         message column: 'el_message'
     dateCreated column: 'el_datecreated'
          origin column: 'el_origin'
          detail column: 'el_detail'
  }
  static constraints = {
         message(nullable:true, blank:false)
     dateCreated(nullable:true, blank:false)
          origin(nullable:true, blank:false)
          detail(nullable:true, blank:false)
  }
}

