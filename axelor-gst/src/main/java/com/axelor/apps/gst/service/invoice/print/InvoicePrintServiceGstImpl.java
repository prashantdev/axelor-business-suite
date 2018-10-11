package com.axelor.apps.gst.service.invoice.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.gst.report.IReportGst;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class InvoicePrintServiceGstImpl extends InvoicePrintServiceImpl {

  @Override
  public ReportSettings prepareReportSettings(Invoice invoice) throws AxelorException {

    App app = Beans.get(AppRepository.class).all().filter("self.name = 'GST'").fetchOne();

    if (invoice.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(IExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
              invoice.getInvoiceId()),
          invoice);
    }
    String locale = ReportSettings.getPrintingLocale(invoice.getPartner());

    String title = I18n.get("Invoice");
    if (invoice.getInvoiceId() != null) {
      title += " " + invoice.getInvoiceId();
    }

    if (app.getActive()) {
      ReportSettings reportSetting =
          ReportFactory.createReport(IReportGst.INVOICE_GST, title + " - ${date}");

      return reportSetting.addParam("InvoiceId", invoice.getId()).addParam("Locale", locale);
    }
    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.INVOICE, title + " - ${date}");

    return reportSetting.addParam("InvoiceId", invoice.getId()).addParam("Locale", locale);
  }
}
