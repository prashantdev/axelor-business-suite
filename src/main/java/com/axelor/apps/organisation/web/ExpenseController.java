/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class ExpenseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExpenseController.class);
	
	public void checkValidationStatus(ActionRequest request, ActionResponse response) {
	
		Expense expense = request.getContext().asType(Expense.class);
		List<ExpenseLine> list = expense.getExpenseLineList();
		boolean checkFileReceived = false;
		
		if(list != null && !list.isEmpty()) {
			for(ExpenseLine expenseLine : list) {
				if(expenseLine.getFileReceived() == 2) {
					checkFileReceived = true;
					break;
				}
			}
		}
		if ((list != null && list.isEmpty()) || checkFileReceived) {
			response.setValue("validationStatusSelect", 2);
			response.setFlash("All expenses proof haven't been provided (file received for each expense line).");
		}
		else {
			response.setValue("validationStatusSelect", 1);
		}
	}
	
	/**
	 * Fonction appeler par le notes de frais
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printExpenses(ActionRequest request, ActionResponse response) {


		Expense expense = request.getContext().asType(Expense.class);

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.EXPENSE)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("ExpenseId", expense.getId().toString())
				.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+expense.getUserInfo().getFullName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
}
