/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.order;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class OrderTypeListController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			String[] orderTypeList = request.getParameterValues("orderTypeId");
			if (orderTypeList != null) {
				OrderService os = Context.getOrderService();
				
				String deleted = msa.getMessage("general.deleted");
				String notDeleted = msa.getMessage("OrderType.cannot.delete");
				for (String p : orderTypeList) {
					try {
						os.purgeOrderType(os.getOrderType(Integer.valueOf(p)));
						if (!success.equals(""))
							success += "<br/>";
						success += p + " " + deleted;
					}
					catch (DataIntegrityViolationException e) {
						error = handleOrderTypeIntegrityException(e, error, notDeleted);
					}
					catch (APIException e) {
						error = handleOrderTypeIntegrityException(e, error, notDeleted);
					}
				}
			} else
				error = msa.getMessage("OrderType.select");
			
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * Logs an order type delete data integrity violation exception and returns a user friedly
	 * message of the problem that occured.
	 * 
	 * @param e the exception.
	 * @param error the error message.
	 * @param notDeleted the not deleted error message.
	 * @return the formatted error message.
	 */
	private String handleOrderTypeIntegrityException(Exception e, String error, String notDeleted) {
		log.warn("Error deleting order type", e);
		if (!error.equals(""))
			error += "<br/>";
		error += notDeleted;
		return error;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		List<OrderType> orderTypeList = new Vector<OrderType>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			OrderService os = Context.getOrderService();
			orderTypeList = os.getAllOrderTypes();
		}
		
		return orderTypeList;
	}
	
}