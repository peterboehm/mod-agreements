package org.olf.erm

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.olf.general.DocumentAttachment
import org.olf.general.Org
import org.olf.kb.ErmTitleList
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.domain.traits.Clonable
import com.k_int.web.toolkit.refdata.CategoryId
import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.tags.Tag

import grails.gorm.MultiTenant
import groovy.util.logging.Slf4j

/**
 * Subscription agreement - object holding details about an SA connecting a resource list (Composed Of packages and platform-titles).
 */
@Slf4j
public class SubscriptionAgreement extends ErmTitleList implements CustomProperties,MultiTenant<SubscriptionAgreement>, Clonable<SubscriptionAgreement> {
   
  static transients = ['cancellationDeadline', 'startDate', 'endDate', 'currentPeriod']
  static cloneStaticValues = [
    periods: { [new Period('owner': delegate, 'startDate': LocalDate.now())] },
    name: { "Copy of: ${owner.name}" /* Owner is the current object. */ }
  ]  
  static copyByCloning = ['supplementaryDocs', 'docs', 'externalLicenseDocs']
  
  String description
  String id
  String name
  String localReference
  String vendorReference
  String attachedLicenceId
  String licenseNote
  LocalDate renewalDate
  LocalDate nextReviewDate

  /**
   * By default the RefdataCategory would be generated from the concatenation
   * of the class name and the property name. So the below property of agreementType
   * would result in a category named SubscriptionAgreement.AgreementType
   *
   * If we wanted to share a category across multiple Classes (like a global "Yes_No"),
   * or just wanted to specify the category, we can use the \@CategoryId annotation.
   *
   * @CategoryId('AgreementType') - Would create a category named 'AgreementType' for values stored here.
   */
  @Deprecated
  @Defaults(['Draft', 'Trial', 'Current']) // Defaults to create for this property.
  RefdataValue agreementType

  @Defaults(['Definitely renew', 'For review', 'Definitely cancel'])
  RefdataValue renewalPriority

  @CategoryId(defaultInternal=true)
  @Defaults(['Draft', 'Requested', 'In negotiation', 'Active', 'Closed'])
  RefdataValue agreementStatus

  @Defaults(['Cancelled', 'Ceased', 'Superseded', 'Rejected'])
  RefdataValue reasonForClosure

  @CategoryId(value='Global.Yes_No', defaultInternal=true)
  @Defaults(['Yes', 'No'])
  RefdataValue isPerpetual

  @CategoryId(value='Global.Yes_No', defaultInternal=true)
  @Defaults(['Yes', 'No'])
  RefdataValue contentReviewNeeded

  Boolean enabled

  Org vendor
  
  Set<Period> periods = []

  Set<Entitlement> items
  Set<AlternateName> alternateNames
  
  private Period currentPeriod
  Period getCurrentPeriod () {
    log.debug "Get current period"
    if (!currentPeriod) {
      LocalDate ld
      
      // Use the request if possible
      RequestAttributes attributes = RequestContextHolder.getRequestAttributes()
      if(attributes && attributes instanceof GrailsWebRequest) {
        
        GrailsWebRequest gwr = attributes as GrailsWebRequest
        
        log.debug "Is within a request context"
        TimeZone tz = RequestContextUtils.getTimeZone(gwr.currentRequest) ?: TimeZone.getDefault()
        
        log.debug "Using TZ ${tz}"
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), tz.toZoneId())
        
        log.debug "Now in ${tz} is ${zdt}"
        ld = zdt.toLocalDate()
        
        log.debug "LocalDate of ${ld} extracted for query"
      } else {
        log.debug "Is not within a request context, using default TZ (${TimeZone.getDefault()})"
        ld = LocalDate.now()
      }
      
      currentPeriod = periods.find { Period p ->
        p.startDate <= ld && (p.endDate == null || p.endDate >= ld)
      }
    }
    currentPeriod
  }
  
  LocalDate getCancellationDeadline() {
    currentPeriod?.cancellationDeadline
  }
  
  private LocalDate startDate = null
  LocalDate getStartDate() {
    if (startDate == null) {
    
      if (currentPeriod) {
        startDate = currentPeriod.startDate
        return startDate
      }
      
      LocalDate earliest = null
      periods.each { Period p ->
        if (earliest == null || p.startDate < earliest) earliest = p.startDate
      }
      
      startDate = earliest
    }
    startDate
  }
  
  private LocalDate endDate = null
  LocalDate getEndDate() {
    if (endDate == null) {
      
      if (currentPeriod) {
        endDate = currentPeriod.endDate
        return endDate
      }
      
      LocalDate latest = null
      periods.each { Period p ->
        if (latest == null || p.endDate > latest) latest = p.endDate
      }
      
      endDate = latest
    }
    endDate
  }
  
  static hasMany = [
         alternateNames: AlternateName,
                  items: Entitlement,
           historyLines: SAEventHistory,
               contacts: InternalContact,
                   tags: Tag,
                   orgs: SubscriptionAgreementOrg,
    externalLicenseDocs: DocumentAttachment,
                   docs: DocumentAttachment,
         linkedLicenses: RemoteLicenseLink,
      supplementaryDocs: DocumentAttachment,
     usageDataProviders: UsageDataProvider,
                periods: Period,
    inwardRelationships: AgreementRelationship,
   outwardRelationships: AgreementRelationship,
  ]

  static mappedBy = [
    alternateNames: 'owner',
    items: 'owner',
    historyLines: 'owner',
    contacts: 'owner',
    orgs: 'owner',
    linkedLicenses: 'owner',
    usageDataProviders: 'owner',
    periods: 'owner',
    inwardRelationships: 'inward',
    outwardRelationships: 'outward'
  ]

  static mapping = {
             description column:'sa_description', type: 'text'
                      id column:'sa_id', generator: 'uuid2', length:36
                 version column:'sa_version'
                    name column:'sa_name'
          localReference column:'sa_local_reference'
         vendorReference column:'sa_vendor_reference'
             renewalDate column:'sa_renewal_date'
          nextReviewDate column:'sa_next_review_date'
           agreementType column:'sa_agreement_type'
         renewalPriority column:'sa_renewal_priority'
         agreementStatus column:'sa_agreement_status'
         reasonForClosure column:'sa_reason_for_closure'
             isPerpetual column:'sa_is_perpetual'
     contentReviewNeeded column:'sa_content_review_needed'
                 enabled column:'sa_enabled'
                  vendor column:'sa_vendor_fk'
       attachedLicenceId column:'sa_licence_fk'
	   		     licenseNote column:'sa_license_note'
          alternateNames cascade: 'all-delete-orphan'
                   items cascade: 'all-delete-orphan', lazy: false
                contacts cascade: 'all-delete-orphan', lazy: false
            historyLines cascade: 'all-delete-orphan'
                    tags cascade: 'save-update'
                 periods cascade: 'all-delete-orphan', lazy: false
                    orgs cascade: 'all-delete-orphan', lazy: false
                    docs cascade: 'all-delete-orphan', lazy: false
     externalLicenseDocs cascade: 'all-delete-orphan', joinTable: [name: 'subscription_agreement_ext_lic_doc', key: 'saeld_sa_fk', column: 'saeld_da_fk']
          linkedLicenses cascade: 'all-delete-orphan'
       supplementaryDocs cascade: 'all-delete-orphan', joinTable: [name: 'subscription_agreement_supp_doc', key: 'sasd_sa_fk', column: 'sasd_da_fk']
      usageDataProviders cascade: 'all-delete-orphan'
     inwardRelationships cascade: 'all-delete-orphan', lazy: false
    outwardRelationships cascade: 'all-delete-orphan', lazy: false
        customProperties cascade: 'all-delete-orphan'
  }

  static constraints = {
                    name(nullable:false, blank:false, unique: true)
          localReference(nullable:true, blank:false)
         vendorReference(nullable:true, blank:false)
             renewalDate(nullable:true, blank:false)
          nextReviewDate(nullable:true, blank:false)
           agreementType(nullable:true, blank:false)
        reasonForClosure(nullable:true, blank:false)
         renewalPriority(nullable:true, blank:false)
         agreementStatus(nullable:false)
             isPerpetual(nullable:true, blank:false)
     contentReviewNeeded(nullable:true, blank:false)
                 enabled(nullable:true, blank:false)
             description(nullable:true, blank:false)
                  vendor(nullable:true, blank:false)
       attachedLicenceId(nullable:true, blank:false)
             licenseNote(nullable:true, blank:false)
                 periods(nullable:false, minSize: 1, validator:Period.PERIOD_COLLECTION_VALIDATOR, sort:'startDate')
              
          linkedLicenses(validator: { Collection<RemoteLicenseLink> license_links ->
            
            int controlling_count = ((license_links?.findAll({ RemoteLicenseLink license -> license.status?.value == 'controlling' })?.size()) ?: 0)
            ( controlling_count > 1 ? [ 'only.one.controlling.license' ] : true )
          })
  }

  def beforeValidate() {
    checkAgreementStatus()
  }
  
  public void checkAgreementStatus () {
    
    // Null out the reasonForClosure if agreement status is not closed
    if (agreementStatus?.value != 'closed') {
      reasonForClosure = null
    }
  }
  
  /**
   * Need to resolve the conflict manually and add the call to the clonable method here. 
   */
  @Override
  public SubscriptionAgreement clone () {
    Clonable.super.clone()
  }
}
