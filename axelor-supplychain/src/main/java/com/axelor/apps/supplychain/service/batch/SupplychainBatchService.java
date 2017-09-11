/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SupplychainBatchService extends AbstractBatchService {

	@Inject
	protected BatchSubscription batchSubscription;

	@Override
	protected Class<? extends Model> getModelClass() {
		return SupplychainBatch.class;
	}

	@Override
	public Batch run(Model batchModel) throws AxelorException {

		Batch batch;
		SupplychainBatch supplychainBatch = (SupplychainBatch) batchModel;

		switch (supplychainBatch.getActionSelect()) {
		case SupplychainBatchRepository.ACTION_BILL_SUB:
			batch = billSubscriptions(supplychainBatch);
			break;
		case SupplychainBatchRepository.ACTION_INVOICE_OUTGOING_STOCK_MOVES:
			batch = invoiceOutgoingStockMoves(supplychainBatch);
			break;
		case SupplychainBatchRepository.ACTION_INVOICE_ORDERS:
			batch = invoiceOrders(supplychainBatch);
			break;
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), supplychainBatch.getActionSelect(), supplychainBatch.getCode()), IException.INCONSISTENCY);
		}

		return batch;
	}

	public Batch billSubscriptions(SupplychainBatch supplychainBatch){
		return batchSubscription.run(supplychainBatch);
	}

	public Batch invoiceOutgoingStockMoves(SupplychainBatch supplychainBatch) {
		return Beans.get(BatchOutgoingStockMoveInvoicing.class).run(supplychainBatch);
	}

	public Batch invoiceOrders(SupplychainBatch supplychainBatch) {
		switch (supplychainBatch.getInvoiceOrdersTypeSelect()) {
		case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_SALE:
			return Beans.get(BatchOrderInvoicingSale.class).run(supplychainBatch);
		case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_PURCHASE:
			return Beans.get(BatchOrderInvoicingPurchase.class).run(supplychainBatch);
		default:
			throw new IllegalArgumentException(
					String.format("Unknown invoice orders type: %d", supplychainBatch.getInvoiceOrdersTypeSelect()));
		}
	}

}