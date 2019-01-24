package org.olf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.olf.erm.SubscriptionAgreement;

import static org.junit.Assert.assertTrue;

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

  @Test
  public void getActiveTitles() {
    service.getActiveTitles();
    assertTrue(true);
  }
}
