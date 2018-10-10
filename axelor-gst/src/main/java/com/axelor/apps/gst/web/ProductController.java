package com.axelor.apps.gst.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ProductController {

  public void getGstRate(ActionRequest req, ActionResponse res) {

    Product product = req.getContext().asType(Product.class);
    ProductCategory productCategory = product.getProductCategory();
    BigDecimal gstRate = productCategory.getGstRate();
    res.setValue("gstRate", gstRate);
  }

  public void invoiceBtn(ActionRequest request, ActionResponse response) throws AxelorException {
    Product product = request.getContext().asType(Product.class);

    if ((request.getContext().get("_ids")) != null) {

      String tem = request.getContext().get("_ids").toString();
      tem = tem.substring(1, tem.length() - 1).replaceAll(" ", "");
      String[] ss = tem.split(",");
      Tax tax = Beans.get(TaxRepository.class).all().filter("self.name = 'GST'").fetchOne();
      TaxLine taxLine =
          Beans.get(TaxLineRepository.class).all().filter("self.tax = (?)", tax).fetchOne();
      List<InvoiceLine> liInvo = new ArrayList<InvoiceLine>();

      BigDecimal amt = new BigDecimal(1);
      for (String string : ss) {
        product =
            Beans.get(ProductRepository.class).all().filter("self.id = (?)", string).fetchOne();

        InvoiceLine inVoi = new InvoiceLine();
        inVoi.setProduct(product);
        inVoi.setGstRate(product.getGstRate());
        inVoi.setHsbn(product.getHsbn());
        inVoi.setPrice(product.getSalePrice());
        inVoi.setProductName(product.getFullName());
        inVoi.setUnit(product.getUnit());
        inVoi.setQty(amt);
        inVoi.setTaxLine(taxLine);
        inVoi.setExTaxTotal(product.getSalePrice());
        liInvo.add(inVoi);
      }

      /*	BigDecimal amount =
      quantity
          .multiply(price)
          .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);*/

      List<InvoiceLineTax> invoiceTax = new ArrayList<InvoiceLineTax>();
      InvoiceLineTax invoTax = new InvoiceLineTax();
      invoTax.setTaxLine(taxLine);
      invoTax.setExTaxBase(product.getSalePrice());
      invoiceTax.add(invoTax);

      response.setView(
          ActionView.define("Create Invoice")
              .model(Invoice.class.getName())
              .add("form", "invoice-gst-form")
              .context("_invoiceItem", liInvo)
              .context("_invoiceLineTax", invoiceTax)
              .map());
      return;
    }
    response.setView(
        ActionView.define("Create Invoice")
            .model(Invoice.class.getName())
            .add("form", "invoice-gst-form")
            .map());
  }

  public void productReport(ActionRequest req, ActionResponse res) {
    if ((req.getContext().get("_ids")) != null) {
      String tem = req.getContext().get("_ids").toString();
      tem = tem.substring(1, tem.length() - 1).replaceAll(" ", "");
      req.getContext().put("_param", tem);
      return;
    }
    List<Product> productList = Beans.get(ProductRepository.class).all().fetch();
    List<Long> longList = new ArrayList<>();
    for (Product product2 : productList) {
      Long lonId = product2.getId();
      longList.add(lonId);
    }
    String str = StringUtils.join(longList, ',');
    req.getContext().put("_param", str);
  }

  public String addAttachmentPath() {

    String attachmentPath = AppSettings.get().getPath("file.upload.dir", "");
    attachmentPath =
        attachmentPath.endsWith(File.separator) ? attachmentPath : attachmentPath + File.separator;
    return attachmentPath;
  }
}
