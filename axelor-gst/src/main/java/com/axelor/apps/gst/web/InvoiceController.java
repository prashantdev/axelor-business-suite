/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceController {

  /*
   * It will Calculate net Amount from invoice line.
   */
  public void netCalculator(ActionRequest req, ActionResponse res) {

    Invoice invoice = req.getContext().asType(Invoice.class);
    List<InvoiceLine> invoiceItem = invoice.getInvoiceLineList();
    BigDecimal amt = new BigDecimal(0);
    for (InvoiceLine invoiceLine : invoiceItem) {

      amt = amt.add(invoiceLine.getNetAmount());
    }
    BigDecimal netAmt = amt.divide(new BigDecimal(invoiceItem.size()));
    res.setValue("netAmount", netAmt);

    BigDecimal amtIgst = new BigDecimal(0);
    for (InvoiceLine invoiceLine : invoiceItem) {

      amtIgst = amtIgst.add(invoiceLine.getIgst());
    }
    BigDecimal netIgst = amtIgst.divide(new BigDecimal(invoiceItem.size()));
    res.setValue("netIgst", netIgst);

    BigDecimal amtCgst = new BigDecimal(0);
    for (InvoiceLine invoiceLine : invoiceItem) {

      amtCgst = amtCgst.add(invoiceLine.getCgst());
    }
    BigDecimal netCgst = amtCgst.divide(new BigDecimal(invoiceItem.size()));
    res.setValue("netCgst", netCgst);

    BigDecimal amtSgst = new BigDecimal(0);
    for (InvoiceLine invoiceLine : invoiceItem) {

      amtSgst = amtSgst.add(invoiceLine.getSgst());
    }
    BigDecimal netSgst = amtSgst.divide(new BigDecimal(invoiceItem.size()));
    res.setValue("netSgst", netSgst);

    BigDecimal amtGross = new BigDecimal(0);
    for (InvoiceLine invoiceLine : invoiceItem) {

      amtGross = amtGross.add(invoiceLine.getGrossAmt());
    }
    BigDecimal netGross = amtGross.divide(new BigDecimal(invoiceItem.size()));
    res.setValue("grossAmt", netGross);
  }
}
