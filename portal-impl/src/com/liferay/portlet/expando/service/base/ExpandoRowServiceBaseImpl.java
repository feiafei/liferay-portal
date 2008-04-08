/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.expando.service.base;

import com.liferay.counter.service.CounterLocalService;
import com.liferay.counter.service.CounterLocalServiceFactory;
import com.liferay.counter.service.CounterService;
import com.liferay.counter.service.CounterServiceFactory;

import com.liferay.portal.service.impl.PrincipalBean;

import com.liferay.portlet.expando.service.ExpandoColumnLocalService;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceFactory;
import com.liferay.portlet.expando.service.ExpandoColumnService;
import com.liferay.portlet.expando.service.ExpandoColumnServiceFactory;
import com.liferay.portlet.expando.service.ExpandoRowLocalService;
import com.liferay.portlet.expando.service.ExpandoRowLocalServiceFactory;
import com.liferay.portlet.expando.service.ExpandoRowService;
import com.liferay.portlet.expando.service.ExpandoTableLocalService;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceFactory;
import com.liferay.portlet.expando.service.ExpandoTableService;
import com.liferay.portlet.expando.service.ExpandoTableServiceFactory;
import com.liferay.portlet.expando.service.ExpandoValueLocalService;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceFactory;
import com.liferay.portlet.expando.service.ExpandoValueService;
import com.liferay.portlet.expando.service.ExpandoValueServiceFactory;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnFinderUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnPersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoColumnUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoRowFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoRowFinderUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoRowPersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoRowUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoTablePersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoTableUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoValueFinder;
import com.liferay.portlet.expando.service.persistence.ExpandoValueFinderUtil;
import com.liferay.portlet.expando.service.persistence.ExpandoValuePersistence;
import com.liferay.portlet.expando.service.persistence.ExpandoValueUtil;

import org.springframework.beans.factory.InitializingBean;

/**
 * <a href="ExpandoRowServiceBaseImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public abstract class ExpandoRowServiceBaseImpl extends PrincipalBean
	implements ExpandoRowService, InitializingBean {
	public ExpandoColumnLocalService getExpandoColumnLocalService() {
		return expandoColumnLocalService;
	}

	public void setExpandoColumnLocalService(
		ExpandoColumnLocalService expandoColumnLocalService) {
		this.expandoColumnLocalService = expandoColumnLocalService;
	}

	public ExpandoColumnService getExpandoColumnService() {
		return expandoColumnService;
	}

	public void setExpandoColumnService(
		ExpandoColumnService expandoColumnService) {
		this.expandoColumnService = expandoColumnService;
	}

	public ExpandoColumnPersistence getExpandoColumnPersistence() {
		return expandoColumnPersistence;
	}

	public void setExpandoColumnPersistence(
		ExpandoColumnPersistence expandoColumnPersistence) {
		this.expandoColumnPersistence = expandoColumnPersistence;
	}

	public ExpandoColumnFinder getExpandoColumnFinder() {
		return expandoColumnFinder;
	}

	public void setExpandoColumnFinder(ExpandoColumnFinder expandoColumnFinder) {
		this.expandoColumnFinder = expandoColumnFinder;
	}

	public ExpandoRowLocalService getExpandoRowLocalService() {
		return expandoRowLocalService;
	}

	public void setExpandoRowLocalService(
		ExpandoRowLocalService expandoRowLocalService) {
		this.expandoRowLocalService = expandoRowLocalService;
	}

	public ExpandoRowPersistence getExpandoRowPersistence() {
		return expandoRowPersistence;
	}

	public void setExpandoRowPersistence(
		ExpandoRowPersistence expandoRowPersistence) {
		this.expandoRowPersistence = expandoRowPersistence;
	}

	public ExpandoRowFinder getExpandoRowFinder() {
		return expandoRowFinder;
	}

	public void setExpandoRowFinder(ExpandoRowFinder expandoRowFinder) {
		this.expandoRowFinder = expandoRowFinder;
	}

	public ExpandoTableLocalService getExpandoTableLocalService() {
		return expandoTableLocalService;
	}

	public void setExpandoTableLocalService(
		ExpandoTableLocalService expandoTableLocalService) {
		this.expandoTableLocalService = expandoTableLocalService;
	}

	public ExpandoTableService getExpandoTableService() {
		return expandoTableService;
	}

	public void setExpandoTableService(ExpandoTableService expandoTableService) {
		this.expandoTableService = expandoTableService;
	}

	public ExpandoTablePersistence getExpandoTablePersistence() {
		return expandoTablePersistence;
	}

	public void setExpandoTablePersistence(
		ExpandoTablePersistence expandoTablePersistence) {
		this.expandoTablePersistence = expandoTablePersistence;
	}

	public ExpandoValueLocalService getExpandoValueLocalService() {
		return expandoValueLocalService;
	}

	public void setExpandoValueLocalService(
		ExpandoValueLocalService expandoValueLocalService) {
		this.expandoValueLocalService = expandoValueLocalService;
	}

	public ExpandoValueService getExpandoValueService() {
		return expandoValueService;
	}

	public void setExpandoValueService(ExpandoValueService expandoValueService) {
		this.expandoValueService = expandoValueService;
	}

	public ExpandoValuePersistence getExpandoValuePersistence() {
		return expandoValuePersistence;
	}

	public void setExpandoValuePersistence(
		ExpandoValuePersistence expandoValuePersistence) {
		this.expandoValuePersistence = expandoValuePersistence;
	}

	public ExpandoValueFinder getExpandoValueFinder() {
		return expandoValueFinder;
	}

	public void setExpandoValueFinder(ExpandoValueFinder expandoValueFinder) {
		this.expandoValueFinder = expandoValueFinder;
	}

	public CounterLocalService getCounterLocalService() {
		return counterLocalService;
	}

	public void setCounterLocalService(CounterLocalService counterLocalService) {
		this.counterLocalService = counterLocalService;
	}

	public CounterService getCounterService() {
		return counterService;
	}

	public void setCounterService(CounterService counterService) {
		this.counterService = counterService;
	}

	public void afterPropertiesSet() {
		if (expandoColumnLocalService == null) {
			expandoColumnLocalService = ExpandoColumnLocalServiceFactory.getImpl();
		}

		if (expandoColumnService == null) {
			expandoColumnService = ExpandoColumnServiceFactory.getImpl();
		}

		if (expandoColumnPersistence == null) {
			expandoColumnPersistence = ExpandoColumnUtil.getPersistence();
		}

		if (expandoColumnFinder == null) {
			expandoColumnFinder = ExpandoColumnFinderUtil.getFinder();
		}

		if (expandoRowLocalService == null) {
			expandoRowLocalService = ExpandoRowLocalServiceFactory.getImpl();
		}

		if (expandoRowPersistence == null) {
			expandoRowPersistence = ExpandoRowUtil.getPersistence();
		}

		if (expandoRowFinder == null) {
			expandoRowFinder = ExpandoRowFinderUtil.getFinder();
		}

		if (expandoTableLocalService == null) {
			expandoTableLocalService = ExpandoTableLocalServiceFactory.getImpl();
		}

		if (expandoTableService == null) {
			expandoTableService = ExpandoTableServiceFactory.getImpl();
		}

		if (expandoTablePersistence == null) {
			expandoTablePersistence = ExpandoTableUtil.getPersistence();
		}

		if (expandoValueLocalService == null) {
			expandoValueLocalService = ExpandoValueLocalServiceFactory.getImpl();
		}

		if (expandoValueService == null) {
			expandoValueService = ExpandoValueServiceFactory.getImpl();
		}

		if (expandoValuePersistence == null) {
			expandoValuePersistence = ExpandoValueUtil.getPersistence();
		}

		if (expandoValueFinder == null) {
			expandoValueFinder = ExpandoValueFinderUtil.getFinder();
		}

		if (counterLocalService == null) {
			counterLocalService = CounterLocalServiceFactory.getImpl();
		}

		if (counterService == null) {
			counterService = CounterServiceFactory.getImpl();
		}
	}

	protected ExpandoColumnLocalService expandoColumnLocalService;
	protected ExpandoColumnService expandoColumnService;
	protected ExpandoColumnPersistence expandoColumnPersistence;
	protected ExpandoColumnFinder expandoColumnFinder;
	protected ExpandoRowLocalService expandoRowLocalService;
	protected ExpandoRowPersistence expandoRowPersistence;
	protected ExpandoRowFinder expandoRowFinder;
	protected ExpandoTableLocalService expandoTableLocalService;
	protected ExpandoTableService expandoTableService;
	protected ExpandoTablePersistence expandoTablePersistence;
	protected ExpandoValueLocalService expandoValueLocalService;
	protected ExpandoValueService expandoValueService;
	protected ExpandoValuePersistence expandoValuePersistence;
	protected ExpandoValueFinder expandoValueFinder;
	protected CounterLocalService counterLocalService;
	protected CounterService counterService;
}