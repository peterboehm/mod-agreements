package org.olf

import grails.rest.*
import grails.converters.*
import grails.gorm.multitenancy.CurrentTenant
import org.hibernate.sql.JoinType
import org.olf.erm.SubscriptionAgreement 
import org.olf.kb.*

@CurrentTenant

class ExportController {
	static responseFormats = ['json', 'xml']
	
	def index() {
		// Declare a list of maps containing some test data
		List some_test_data = [
			[ title:'title-1', url:'url-1', identifier:'identifier-1' ], 
			[ title:'title-2', url:'url-2', identifier:'identifier-2' ] 
			]
			
		def items = ErmResource.withCriteria {
		// Only return objects of type PlatformTitleInstance or PackageContentItem
			or {
				eq ('class', PlatformTitleInstance)
			  	eq ('class', PackageContentItem)
			}
		// This is only half the story! We need to add extra restrictions so we only return resources for which we have
		// a live entitlement that connects the resource to an agreement
		}
		
		withFormat {
			csv {
				response.setHeader("Content-disposition", "attachment;filename=\"MyExportFilename.csv\"")
				response.contentType = "text/csv"
				def out = response.outputStream 
				out.withWriter { 
					writer -> writer.write("name,uri,identifier\n")
				  /*some_test_data.each {
					 row -> writer.write("\"${row.title}\",\"${row.url}\",\"${row.identifier}\"\n");
				    }
				 */
					// The query above will give us a list of ErmResource objects. ErmResource is a superclass for
					// PackageContentItem, Pkg, PlatformTitleInstance and TitleInstance and ErmResource has a name (Title)
					// but we restrict the result only to PlatformTitleInstance and PackageContentItem
					// The block of code below will likely need extending to take different actions based on the instanceof the row
					
					items.each { row ->
						writer.write("\"${row.name}\",\"Find the url\",\"find the identifier\"\n");
					}
					writer.flush()
					writer.close() 
				}
				out.flush()
			}
		}
	}
}
