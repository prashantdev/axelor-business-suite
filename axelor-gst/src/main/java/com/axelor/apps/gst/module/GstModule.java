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
package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.web.ProductController;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.gst.service.invoice.InvoiceGstServiceImpl;
import com.axelor.apps.gst.service.invoice.print.InvoicePrintServiceGstImpl;
import com.axelor.apps.gst.web.ProductGstController;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(InvoiceServiceProjectImpl.class).to(InvoiceGstServiceImpl.class);
    bind(InvoicePrintServiceImpl.class).to(InvoicePrintServiceGstImpl.class);
    bind(ProductController.class).to(ProductGstController.class);
  }
}
