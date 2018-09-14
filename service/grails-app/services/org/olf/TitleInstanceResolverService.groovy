package org.olf

import grails.gorm.multitenancy.Tenants
import org.olf.kb.RemoteKB
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import org.olf.kb.TitleInstance
import org.olf.kb.Identifier
import org.olf.kb.IdentifierNamespace
import org.olf.kb.IdentifierOccurrence
import org.olf.kb.Work
import org.olf.general.RefdataValue
import org.olf.general.RefdataCategory

/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Transactional
public class TitleInstanceResolverService {

  private static final float MATCH_THRESHOLD = 0.75f;
  private static final String TEXT_MATCH_TITLE_QRY = 'select * from title_instance WHERE ti_title % :qrytitle AND similarity(ti_title, :qrytitle) > :threshold ORDER BY  similarity(ti_title, :qrytitle) desc LIMIT 20'

  private static def class_one_namespaces = [
    'zdb',
    'isbn',
    'issn',
    'eissn',
    'doi'
  ];

  /**
   * Given a -valid- title citation with the minimum properties below, attempt to resolve the citation
   * into a local instance record. If no instance record is located, create one, and perform the necessary
   * cross-matching to create Inventory Instance records. The map contains a representation that is
   * the same as the attached JSON.
   *
   * {
   *   "title": "Nordic Psychology",
   *   "instanceMedium": "electronic",
   *   "instanceMedia": "journal",
   *   "instanceIdentifiers": [ 
   *     {
   *       "namespace": "issn",
   *       "value": "1234-5678"
   *     } ],
   *   "siblingInstanceIdentifiers": [ 
   *     {
   *       "namespace": "issn",
   *       "value": "1901-2276"
   *     } ]
   *   }
   */
  public TitleInstance resolve(Map citation) {
    // log.debug("TitleInstanceResolverService::resolve(${citation})");
    TitleInstance result = null;

    List<TitleInstance> candidate_list = classOneMatch(citation.instanceIdentifiers);
    int num_class_one_identifiers = countClassOneIDs(citation.instanceIdentifiers);
    int num_matches = candidate_list.size()
    List<TitleInstance> siblings = []

    // We weren't able to match directly on an identifier for this instance - see if we have an identifier
    // for a sibling instance we can use to narrow down the list.
    if ( num_matches == 0 ) {
      siblings.addAll(siblingMatch(citation))
      //num_matches = candidate_list.size()
      // Look through siblings looking for one with instanceMedium==electronic
      // candidate_list = siblings.findAll { it.instanceMedium == 'electronic' }
      // num_matches = candidate_list.size()
      // if ( num_matches > 0 ) {
      //   log.debug("Matched ${num_matches} titles via sibling identifier");
      // }
    }

    // If we didn't have a class one identifier AND we weren't able to match anything try to do a fuzzy match as a last resort
    if ( ( num_matches == 0 ) && ( num_class_one_identifiers == 0 ) ) {
      // log.debug("No matches on identifier - try a fuzzy text match on title(${citation.title})");
      // No matches - try a simple title match
      candidate_list = titleMatch(citation.title,MATCH_THRESHOLD);
      num_matches = candidate_list.size()
    }

    if ( candidate_list != null ) {
      switch ( num_matches ) {
        case(0):
          // log.debug("No title match");
          result = createNewTitleInstance(citation);
          break;
        case(1):
          // log.debug("Exact match.");
          result = candidate_list.get(0);
          checkForEnrichment(result, citation);
          break;
        default:
          log.warn("title matched ${num_matches} records with a threshold >= ${MATCH_THRESHOLD} . Unable to continue. Matching IDs: ${candidate_list.collect { it.id }}");
          // throw new RuntimeException("Title match returned too many items (${num_matches})");
          break;
      }
    }

    return result;
  }  

  /**
   * Return a list of the siblings for this instance. Sometimes vendors identify a title by citing the issn of the print edition.
   * we model these as 2 different title instances, linked by a common work. This method looks up/creates any sibling instances.
   * In reality, it currently only creates print instances when an ISSN is present.
   */
  private List<TitleInstance> siblingMatch(Map citation) {

    List<TitleInstance> candidate_list = []

    // Lets try and match based on sibling identifiers. 
    // Our first "alternate" matching strategy. Often, KBART files contain the ISSN of the print edition of an electronic work.
    // The line is not suggesting that buying an electronic package includes copies of the physical item, its more a way of saying
    // "The electronic item described by this line relates to the print item identified by X".
    // In the bibframe nomenclature, the print and electronic items are two separate instances. Therefore, creating an electronic
    // identifier with the ID of the print item does not seem sensible. HOWEVER, we would still like to be able to be able to match
    // a title if we know that it is a sibling of a print identifier.
    int num_class_one_identifiers_for_sibling = countClassOneIDs(citation.siblingInstanceIdentifiers)

    Map issn_id = citation.siblingInstanceIdentifiers.find { it.namespace == 'issn' } ;
    String issn = issn_id?.value;

    if ( issn ) {
      Map sibling_citation = [
        "title": citation.title,
        "instanceMedium": "print",
        "instanceMedia": "journal",
        "instanceIdentifiers": [ 
          [
            "namespace": "issn",
            "value": issn
          ] ]
        ]

      candidate_list = [ this.resolve(sibling_citation) ];
    }

    return candidate_list;
  }

  private TitleInstance createNewTitleInstance(Map citation, Work work = null) {

    TitleInstance result = null;

    // With the introduction of fuzzy title matching, we are relaxing this constraint and
    // will expect to enrich titles without identifiers when we next see a record. BUT
    // this needs elaboration and experimentation.
    //
    // boolean title_is_valid =  ( ( citation.title?.length() > 0 ) && ( citation.instanceIdentifiers.size() > 0 ) )
    // 
    boolean title_is_valid = ( ( citation.title != null ) &&
                               ( citation.title.length() > 0 ) );

    // Validate
    if ( title_is_valid == true ) {

      if ( work == null ) {
        work = new Work(title:citation.title).save(flush:true, failOnError:true);
      }

      def medium = null;
      if ( ( citation.instanceMedium ) && ( citation.instanceMedium.trim().length() > 0 ) ) {
        medium = RefdataCategory.lookupOrCreate('InstanceMedium', citation.instanceMedium, citation.instanceMedium);
      }

      def media = null;
      if ( ( citation.instanceMedia ) && ( citation.instanceMedia.trim().length() > 0 ) )  {
        media = RefdataCategory.lookupOrCreate('InstanceMedia', citation.instanceMedia, citation.instanceMedia);
      }

      result = new TitleInstance(
         title: citation.title,
         medium: medium,
         instanceMedia: media,
         work: work
      )

      result.save(flush:true, failOnError:true);

      // Iterate over all idenifiers in the citation and add them to the title record. We manually create the identifier occurrence 
      // records rather than using the groovy collection, but it makes little difference.
      citation.instanceIdentifiers.each { id ->
        def id_lookup = lookupOrCreateIdentifier(id.value, id.namespace);
        RefdataValue approved_io_status = RefdataCategory.lookupOrCreate('IOStatus','APPROVED')
        def io_record = new IdentifierOccurrence(
                                                 title: result, 
                                                 identifier: id_lookup,
                                                 status:approved_io_status).save(flush:true, failOnError:true);
      }
    }
    else { 
      log.error("Create title failed validation checks - insufficient data to create a title record");
      // We will return null, which means no title
      // throw new RuntimeException("Insufficient detail to create title instance record");
    }

    // Refresh the newly minted title so we have access to all the related objects (eg Identifiers)
    result.refresh();
    result;
  }

  /**
   * Check to see if the citation has properties that we really want to pull through to
   * the DB. In particular, for the case where we have created a stub title record without
   * an identifier, we will need to add identifiers to that record when we see a record that
   * suggests identifiers for that title match.
   */ 
  private void checkForEnrichment(TitleInstance title, Map citation) {
    return;
  }

  /**
   * Given an identifier in a citation { value:'1234-5678', namespace:'isbn' } lookup or create an identifier in the DB to represent that info
   */
  private Identifier lookupOrCreateIdentifier(String value, String namespace) {
    Identifier result = null;
    def identifier_lookup = Identifier.executeQuery('select id from Identifier as id where id.value = :value and id.ns.value = :ns',[value:value, ns:namespace]);
    switch(identifier_lookup.size() ) {
      case 0:
        IdentifierNamespace ns = lookupOrCreateIdentifierNamespace(namespace);
        result = new Identifier(ns:ns, value:value).save(flush:true, failOnError:true);
        break;
      case 1:
        result = identifier_lookup.get(0);
        break;
      default:
        throw new RuntimeException("Matched multiple identifiers for ${id}");
        break;
    }
    return result;
  }

  private IdentifierNamespace lookupOrCreateIdentifierNamespace(String ns) {
    def ns_lookup = IdentifierNamespace.findByValue(ns);
    if ( ns_lookup == null ) {
      ns_lookup = new IdentifierNamespace(value:ns).save(flush:true, failOnError:true);
    }
    return ns_lookup;
  }

  /**
   * Attempt a fuzzy match on the title
   */
  private List<TitleInstance> titleMatch(String title, float threshold) {

    List<TitleInstance> result = new ArrayList<TitleInstance>()
    TitleInstance.withSession { session ->

      final sqlQuery = session.createSQLQuery(TEXT_MATCH_TITLE_QRY)

      try {
        result = sqlQuery.with {
          addEntity(TitleInstance)
          // Set query title - I know this looks a little odd, we have to manually quote this and handle any
          // relevant escaping... So this code will probably not be good enough long term.
          setString('qrytitle',title);
          setFloat('threshold',threshold)
   
          // Get all results.
          list()
        }
      }
      catch ( Exception e ) {
        log.error("Problem attempting to run SQL Query ${TEXT_MATCH_TITLE_QRY} on string ${title} with threshold 0.6f",e);
      }
    }
 
    return result
  }

  private int countClassOneIDs(List identifiers) {
    int result = 0;
    identifiers.each { id ->
      if ( class_one_namespaces?.contains(id.namespace.toLowerCase()) ) {
        result++;
      }
    }
    return result;
  }

  /**
   * Being passed a map of namespace, value pair maps, attempt to locate any title instances with class 1 identifiers (ISSN, ISBN, DOI)
   */
  private List<TitleInstance> classOneMatch(List identifiers) {
    // We want to build a list of all the title instance records in the system that match the identifiers. Hopefully this will return 0 or 1 records.
    // If it returns more than 1 then we are in a sticky situation, and cleverness is needed.
    List<TitleInstance> result = new ArrayList<TitleInstance>()

    def num_class_one_identifiers = 0;

    identifiers.each { id ->
      if ( class_one_namespaces?.contains(id.namespace.toLowerCase()) ) {

        num_class_one_identifiers++;

        // Look up each identifier
        // log.debug("${id} - try class one match");
        def id_matches = Identifier.executeQuery('select id from Identifier as id where id.value = :value and id.ns.value = :ns',[value:id.value, ns:id.namespace])

        assert ( id_matches.size() <= 1 )

        // For each matched (It should only ever be 1)
        id_matches.each { matched_id ->
          // For each occurrence where the STATUS is APPROVED
          matched_id.occurrences.each { io ->
            if ( io.status?.value == 'APPROVED' ) {
              if ( result.contains(io.title) ) {
                // We have already seen this title, so don't add it again
              }
              else {
                // log.debug("Adding title ${io.title.id} ${io.title.title} to matches for ${matched_id}");
                result << io.title
              }
            }
            else {
              throw new RuntimeException("Match on non-approved");
            }
          }
        }
      }
      else {
        // log.debug("Identifier ${id} not from a class one namespace");
      }
    }

    // log.debug("At end of classOneMatch, resut contains ${result.size()} titles");
    return result;
  }
}
