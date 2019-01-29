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
	String headline;
	
	def index() {
		headline="name,uri,identifier\n";
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
				response.setHeader("Content-disposition", "attachment;filename=\"ExportFilename.csv\"")
				response.contentType = "text/csv"
				def out = response.outputStream
				out.withWriter {
					writer -> writer.write(headline)
				  /*some_test_data.each {
					 row -> writer.write("\"${row.title}\",\"${row.url}\",\"${row.identifier}\"\n");
					}
				 */
					// The query above will give us a list of ErmResource objects. ErmResource is a superclass for
					// PackageContentItem, Pkg, PlatformTitleInstance and TitleInstance and ErmResource has a name (Title)
					// but we restrict the result only to PlatformTitleInstance and PackageContentItem
					// The block of code below will likely need extending to take different actions based on the instanceof the row
					
					items.each { row ->
						writer.write("\"${row.name}\",\"Find the url\",\"${row.id}\"\n");
					}
					writer.flush()
					writer.close()
				}
				out.flush()
			}
		}
	}
	
	def kbartExport() {
		//def result = [:]
		//respond result
		
		headline="publication_title,print_identifier,online_identifier,date_first_issue_online,num_first_vol_online,num_first_issue_online,date_last_issue_online,num_last_vol_online,num_last_issue_online,title_url,first_author,title_id,embargo_info,coverage_depth,notes,publisher_name,publication_type,date_monograph_published_print,date_monograph_published_online,monograph_volume,monograph_edition,first_editor,parent_publication_id,preceding_publication_title_id,access_type\n";
		
		withFormat {
			csv {
				response.setHeader("Content-disposition", "attachment;filename=\"ExportFilename.csv\"")
				response.contentType = "text/csv"
				def out = response.outputStream
				out.withWriter {
					writer -> writer.write(headline)
				 	writer.flush()
					writer.close()
				}
				out.flush()
			}
		}
	}
}
