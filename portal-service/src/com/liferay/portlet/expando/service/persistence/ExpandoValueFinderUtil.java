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

package com.liferay.portlet.expando.service.persistence;

/**
 * <a href="ExpandoValueFinderUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class ExpandoValueFinderUtil {
	public static int countByTC_TN_CN(long classNameId,
		java.lang.String tableName, java.lang.String columnName)
		throws com.liferay.portal.SystemException {
		return getFinder().countByTC_TN_CN(classNameId, tableName, columnName);
	}

	public static java.util.List<com.liferay.portlet.expando.model.ExpandoValue> findByTC_TN_CN(
		long classNameId, java.lang.String tableName,
		java.lang.String columnName, int begin, int end)
		throws com.liferay.portal.SystemException {
		return getFinder()
				   .findByTC_TN_CN(classNameId, tableName, columnName, begin,
			end);
	}

	public static com.liferay.portlet.expando.model.ExpandoValue findByTC_TN_N(
		long classNameId, java.lang.String tableName,
		java.lang.String columnName, long rowId)
		throws com.liferay.portal.SystemException {
		return getFinder()
				   .findByTC_TN_N(classNameId, tableName, columnName, rowId);
	}

	public static ExpandoValueFinder getFinder() {
		return _getUtil()._finder;
	}

	public void setFinder(ExpandoValueFinder finder) {
		_finder = finder;
	}

	private static ExpandoValueFinderUtil _getUtil() {
		if (_util == null) {
			_util = (ExpandoValueFinderUtil)com.liferay.portal.kernel.bean.BeanLocatorUtil.locate(_UTIL);
		}

		return _util;
	}

	private static final String _UTIL = ExpandoValueFinderUtil.class.getName();
	private static ExpandoValueFinderUtil _util;
	private ExpandoValueFinder _finder;
}