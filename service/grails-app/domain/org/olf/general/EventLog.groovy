package org.olf.general

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.Defaults

class EventLog implements MultiTenant<EventLog> {
  String id
  String message
  Date timestamp
  String recordId
  String recordData

  static mapping = {
            id column: 'el_id', generator: 'uuid2', length:36
       message column: 'el_message'
     timestamp column: 'el_timestamp'
      recordId column: 'el_record_id'
    recordData column: 'el_record_data'
  }
  static constraints = {
       message(nullable:true, blank:false)
     timestamp(nullable:true, blank:false)
      recordId(nullable:true, blank:false)
    recordData(nullable:true, blank:false)
  }
}

