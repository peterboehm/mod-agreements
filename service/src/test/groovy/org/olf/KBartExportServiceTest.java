package org.olf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.olf.erm.SubscriptionAgreement;

import static org.junit.Assert.assertEquals;
import static org.olf.export.ExportConstants.KBART2_HEADER;

public class KBartExportServiceTest {

  private KBartExportService service;

  @Mock
  private SubscriptionAgreement agreement;

  @Before
  public void setUp() throws Exception {
    service = new KBartExportService();
  }

  @After
  public void tearDown() throws Exception {
    service = null;
  }


  /** MODERM-23 - Scenario 1 */
  @Test
  public void returnEmptyKBartHeader() {
    String emptyKBartHeader = service.getTitlesInActiveAgreements();
    assertEquals(KBART2_HEADER, emptyKBartHeader);
  }
}
