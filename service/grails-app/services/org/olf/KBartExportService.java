package org.olf;

import grails.gorm.services.Service;
import grails.gorm.transactions.Transactional;
import org.olf.export.KBartExport;

@Transactional
@Service
public class KBartExportService {

  KBartExport kBartExport = new KBartExport();
  public String getTitlesInActiveAgreements() {

    return kBartExport.getExportString();
  }
}
