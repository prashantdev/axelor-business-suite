package com.axelor.apps.gst.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.web.ProductController;
import com.axelor.apps.gst.report.IReportGst;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductGstController extends ProductController {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public void getGstRate(ActionRequest req, ActionResponse res) {

		Product product = req.getContext().asType(Product.class);
		ProductCategory productCategory = product.getProductCategory();
		BigDecimal gstRate = productCategory.getGstRate();
		res.setValue("gstRate", gstRate);
	}

	public void invoiceBtn(ActionRequest request, ActionResponse response) throws AxelorException {
		Product prod = request.getContext().asType(Product.class);

		if ((request.getContext().get("_ids")) != null) {

			String tem = request.getContext().get("_ids").toString();
			// tem = tem.substring(1, tem.length() - 1).replaceAll(" ", "");
			// String[] ss = tem.split(",");
			tem = tem.replaceAll("[", "(").replaceAll("]", ")").replaceAll(" ", "");
			Tax tax = Beans.get(TaxRepository.class).all().filter("self.name = 'GST'").fetchOne();
			TaxLine taxLine = Beans.get(TaxLineRepository.class).all().filter("self.tax = (?)", tax).fetchOne();

			Company company = Beans.get(CompanyRepository.class).all().filter("self.name = 'Axelor'").fetchOne();
			Partner partner = company.getPartner();
			List<PartnerAddress> address = partner.getPartnerAddressList();
			Address address2 = new Address();
			for (PartnerAddress partnerAddress : address) {
				if (partnerAddress.getIsInvoicingAddr() == true) {
					address2 = partnerAddress.getAddress();
					break;
				}
			}
			List<InvoiceLine> liInvo = new ArrayList<InvoiceLine>();
			BigDecimal amt = new BigDecimal(1);
			BigDecimal gst = new BigDecimal(2);
			List<Product> productList = Beans.get(ProductRepository.class).all().filter("self.id in ?", tem).fetch();
			for (Product product : productList) {
				//product = Beans.get(ProductRepository.class).all().filter("self.id = (?)", string).fetchOne();

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
				inVoi.setNetAmount(inVoi.getQty().multiply(inVoi.getPrice()));
				if (company.getAddress().getState() != address2.getState()) {
					inVoi.setIgst(inVoi.getNetAmount().multiply(inVoi.getGstRate()));
				} else {
					inVoi.setSgst((inVoi.getNetAmount().multiply(inVoi.getGstRate())).divide(gst));
					inVoi.setCgst((inVoi.getNetAmount().multiply(inVoi.getGstRate())).divide(gst));
				}
				inVoi.setGrossAmt((inVoi.getNetAmount().add(inVoi.getCgst()).add(inVoi.getIgst())));
				liInvo.add(inVoi);
			}

			response.setView(
					ActionView.define("Create Invoice").model(Invoice.class.getName()).add("form", "invoice-gst-form")
							.context("_invoiceItem", liInvo).context("_partner", partner).map());
			return;
		}
		response.setView(ActionView.define("Create Invoice").model(Invoice.class.getName())
				.add("form", "invoice-gst-form").map());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void printProductCatalog(ActionRequest request, ActionResponse response) throws AxelorException {

		User user = Beans.get(UserService.class).getUser();

		int currentYear = Beans.get(AppBaseService.class).getTodayDateTime().getYear();
		String productIds = "";

		List<Integer> lstSelectedProduct = (List<Integer>) request.getContext().get("_ids");

		if (lstSelectedProduct != null) {
			productIds = Joiner.on(",").join(lstSelectedProduct);
		}

		String name = I18n.get("Product Catalog");
		App app = Beans.get(AppRepository.class).all().filter("self.name = 'GST'").fetchOne();

		if (app.getActive()) {
			String fileLink = ReportFactory.createReport(IReportGst.PRODUCT_CATALOG_GST, name + "-${date}")
					.addParam("UserId", user.getId()).addParam("CurrYear", Integer.toString(currentYear))
					.addParam("ProductIds", productIds).addParam("Locale", ReportSettings.getPrintingLocale(null))
					.generate().getFileLink();

			logger.debug("Printing " + name);
			response.setView(ActionView.define(name).add("html", fileLink).map());
			return;
		}
		String fileLink = ReportFactory.createReport(IReport.PRODUCT_CATALOG, name + "-${date}")
				.addParam("UserId", user.getId()).addParam("CurrYear", Integer.toString(currentYear))
				.addParam("ProductIds", productIds).addParam("Locale", ReportSettings.getPrintingLocale(null))
				.generate().getFileLink();

		logger.debug("Printing " + name);
		response.setView(ActionView.define(name).add("html", fileLink).map());
	}

	@Override
	public void printProductSheet(ActionRequest request, ActionResponse response) throws AxelorException {
		try {
			Product product = request.getContext().asType(Product.class);
			User user = Beans.get(UserService.class).getUser();

			String name = I18n.get("Product") + " " + product.getCode();

			if (user.getActiveCompany() == null) {
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
						I18n.get(IExceptionMessage.PRODUCT_NO_ACTIVE_COMPANY));
			}
			App app = Beans.get(AppRepository.class).all().filter("self.name = 'GST'").fetchOne();

			if (app.getActive()) {
				String fileLink = ReportFactory.createReport(IReportGst.PRODUCT_SHEET_GST, name + "-${date}")
						.addParam("ProductId", product.getId()).addParam("CompanyId", user.getActiveCompany().getId())
						.addParam("Locale", ReportSettings.getPrintingLocale(null)).generate().getFileLink();

				logger.debug("Printing " + name);
				response.setView(ActionView.define(name).add("html", fileLink).map());
				return;
			}
			String fileLink = ReportFactory.createReport(IReport.PRODUCT_SHEET, name + "-${date}")
					.addParam("ProductId", product.getId()).addParam("CompanyId", user.getActiveCompany().getId())
					.addParam("Locale", ReportSettings.getPrintingLocale(null)).generate().getFileLink();

			logger.debug("Printing " + name);
			response.setView(ActionView.define(name).add("html", fileLink).map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
}
