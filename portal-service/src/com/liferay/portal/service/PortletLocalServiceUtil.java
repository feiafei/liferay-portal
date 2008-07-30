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

package com.liferay.portal.service;


/**
 * <a href="PortletLocalServiceUtil.java.html"><b><i>View Source</i></b></a>
 *
 * <p>
 * ServiceBuilder generated this class. Modifications in this class will be
 * overwritten the next time is generated.
 * </p>
 *
 * <p>
 * This class provides static methods for the
 * <code>com.liferay.portal.service.PortletLocalService</code>
 * bean. The static methods of this class calls the same methods of the bean
 * instance. It's convenient to be able to just write one line to call a method
 * on a bean instead of writing a lookup call and a method call.
 * </p>
 *
 * @author Brian Wing Shun Chan
 *
 * @see com.liferay.portal.service.PortletLocalService
 *
 */
public class PortletLocalServiceUtil {
	public static com.liferay.portal.model.Portlet addPortlet(
		com.liferay.portal.model.Portlet portlet)
		throws com.liferay.portal.SystemException {
		return _service.addPortlet(portlet);
	}

	public static void deletePortlet(long id)
		throws com.liferay.portal.PortalException,
			com.liferay.portal.SystemException {
		_service.deletePortlet(id);
	}

	public static void deletePortlet(com.liferay.portal.model.Portlet portlet)
		throws com.liferay.portal.SystemException {
		_service.deletePortlet(portlet);
	}

	public static com.liferay.portal.model.Portlet deployRemotePortlet(
			com.liferay.portal.model.Portlet remotePortlet) {
		return _service.deployRemotePortlet(remotePortlet);
	}

	public static java.util.List<Object> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery)
		throws com.liferay.portal.SystemException {
		return _service.dynamicQuery(dynamicQuery);
	}

	public static java.util.List<Object> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) throws com.liferay.portal.SystemException {
		return _service.dynamicQuery(dynamicQuery, start, end);
	}

	public static com.liferay.portal.model.Portlet getPortlet(long id)
		throws com.liferay.portal.PortalException,
			com.liferay.portal.SystemException {
		return _service.getPortlet(id);
	}

	public static java.util.List<com.liferay.portal.model.Portlet> getPortlets(
		int start, int end) throws com.liferay.portal.SystemException {
		return _service.getPortlets(start, end);
	}

	public static int getPortletsCount()
		throws com.liferay.portal.SystemException {
		return _service.getPortletsCount();
	}

	public static com.liferay.portal.model.Portlet updatePortlet(
		com.liferay.portal.model.Portlet portlet)
		throws com.liferay.portal.SystemException {
		return _service.updatePortlet(portlet);
	}

	public static void destroyPortlet(com.liferay.portal.model.Portlet portlet) {
		_service.destroyPortlet(portlet);
	}

	public static com.liferay.portal.model.PortletCategory getEARDisplay(
		java.lang.String xml) throws com.liferay.portal.SystemException {
		return _service.getEARDisplay(xml);
	}

	public static com.liferay.portal.model.PortletCategory getWARDisplay(
		java.lang.String servletContextName, java.lang.String xml)
		throws com.liferay.portal.SystemException {
		return _service.getWARDisplay(servletContextName, xml);
	}

	public static java.util.List<com.liferay.portal.kernel.portlet.FriendlyURLMapper> getFriendlyURLMappers() {
		return _service.getFriendlyURLMappers();
	}

	public static com.liferay.portal.model.Portlet getPortletById(
		long companyId, java.lang.String portletId)
		throws com.liferay.portal.SystemException {
		return _service.getPortletById(companyId, portletId);
	}

	public static com.liferay.portal.model.Portlet getPortletByStrutsPath(
		long companyId, java.lang.String strutsPath)
		throws com.liferay.portal.SystemException {
		return _service.getPortletByStrutsPath(companyId, strutsPath);
	}

	public static java.util.List<com.liferay.portal.model.Portlet> getPortlets() {
		return _service.getPortlets();
	}

	public static java.util.List<com.liferay.portal.model.Portlet> getPortlets(
		long companyId) throws com.liferay.portal.SystemException {
		return _service.getPortlets(companyId);
	}

	public static java.util.List<com.liferay.portal.model.Portlet> getPortlets(
		long companyId, boolean showSystem, boolean showPortal)
		throws com.liferay.portal.SystemException {
		return _service.getPortlets(companyId, showSystem, showPortal);
	}

	public static boolean hasPortlet(long companyId, java.lang.String portletId)
		throws com.liferay.portal.SystemException {
		return _service.hasPortlet(companyId, portletId);
	}

	public static void initEAR(java.lang.String[] xmls,
		com.liferay.portal.kernel.plugin.PluginPackage pluginPackage) {
		_service.initEAR(xmls, pluginPackage);
	}

	public static java.util.List<com.liferay.portal.model.Portlet> initWAR(
		java.lang.String servletContextName, java.lang.String[] xmls,
		com.liferay.portal.kernel.plugin.PluginPackage pluginPackage) {
		return _service.initWAR(servletContextName, xmls, pluginPackage);
	}

	public static com.liferay.portal.model.Portlet updatePortlet(
		long companyId, java.lang.String portletId, java.lang.String roles,
		boolean active) throws com.liferay.portal.SystemException {
		return _service.updatePortlet(companyId, portletId, roles, active);
	}

	public static PortletLocalService getService() {
		return _service;
	}

	public void setService(PortletLocalService service) {
		_service = service;
	}

	private static PortletLocalService _service;
}