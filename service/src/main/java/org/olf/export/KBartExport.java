package org.olf.export;

import java.util.ArrayList;
import java.util.List;

import static org.olf.export.ExportConstants.*;

public class KBartExport {

  private List<String> titlesInActiveAgreements = new ArrayList<>();

  public String getExportString() {
    StringBuilder kBartExportString = new StringBuilder(KBART2_HEADER);
    for (String title : titlesInActiveAgreements) {
      kBartExportString.append("\n");
      kBartExportString.append(title);
    }
    return kBartExportString.toString();
  }
}
