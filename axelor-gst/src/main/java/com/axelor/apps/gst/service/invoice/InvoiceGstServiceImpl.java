package com.axelor.apps.gst.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceGstServiceImpl extends InvoiceServiceProjectImpl {

  @Inject
  public InvoiceGstServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService);
  }

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Invoice compute(final Invoice invoice) throws AxelorException {

    log.debug("Calcul de la facture");

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator() {

          @Override
          public Invoice generate() throws AxelorException {

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.addAll(invoice.getInvoiceLineList());

            populate(invoice, invoiceLines);
            App app = Beans.get(AppRepository.class).all().filter("self.name = 'GST'").fetchOne();

            if (app.getActive()) {
              BigDecimal amt = new BigDecimal(0);
              BigDecimal amtIgst = new BigDecimal(0);
              BigDecimal amtCgst = new BigDecimal(0);
              BigDecimal amtSgst = new BigDecimal(0);
              BigDecimal amtGross = new BigDecimal(0);
              for (InvoiceLine invoiceLine : invoiceLines) {

                amt = amt.add(invoiceLine.getNetAmount());
                amtIgst = amtIgst.add(invoiceLine.getIgst());
                amtCgst = amtCgst.add(invoiceLine.getCgst());
                amtSgst = amtSgst.add(invoiceLine.getSgst());
                amtGross = amtGross.add(invoiceLine.getGrossAmt());
              }
              invoice.setNetAmount(amt);
              invoice.setNetCgst(amtCgst);
              invoice.setNetIgst(amtIgst);
              invoice.setNetSgst(amtSgst);
              invoice.setGrossAmt(amtGross);
              return invoice;
            }
            return invoice;
          }
        };

    Invoice invoice1 = invoiceGenerator.generate();
    invoice1.setAdvancePaymentInvoiceSet(this.getDefaultAdvancePaymentInvoice(invoice1));
    return invoice1;
  }
}
