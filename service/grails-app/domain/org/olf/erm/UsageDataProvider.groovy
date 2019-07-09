package org.olf.erm;

import com.k_int.okapi.remote_resources.RemoteOkapiLink
import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.MultiTenant

public class UsageDataProvider extends RemoteOkapiLink implements MultiTenant<UsageDataProvider> {
	String id 
	String version
	String usageDataProviderId
	String usageDataProviderNote
  
	static belongsTo = [ owner: SubscriptionAgreement ]
  
	static mapping = {
	                 id column: 'udp_id', generator: 'uuid2', length:36
	            version column: 'udp_version'
	              owner column: 'udp_owner_fk'
    usageDataProviderId column: 'udp_remote_id'
  usageDataProviderNote column: 'udp_note', type: 'text'
                  
	}
  
	static constraints = {
		owner(nullable:false, blank:false)
		usageDataProviderId(nullable:true, blank:false)
        usageDataProviderNote(nullable:true, blank:false)
	}

	@Override
	public String remoteUri() {
		return 'eusage/usage-data-providers';
	}
}
