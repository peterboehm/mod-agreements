package org.olf.general

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.Defaults

class Log implements MultiTenant<Log> {
  String id
  String message
  Date dateCreated
  String origin
  String detail

  static mapping = {
              id column: 'log_id', generator: 'uuid2', length:36
         message column: 'log_message'
     dateCreated column: 'log_datecreated'
          origin column: 'log_origin'
          detail column: 'log_detail'
  }
  static constraints = {
         message(nullable:true, blank:false)
     dateCreated(nullable:true, blank:false)
          origin(nullable:true, blank:false)
          detail(nullable:true, blank:false)
  }
}

