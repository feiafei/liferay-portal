/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.source.formatter;

import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.nio.charset.CharsetDecoderUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.tools.ToolsUtil;
import com.liferay.portal.xml.SAXReaderFactory;
import com.liferay.source.formatter.checks.FileCheck;
import com.liferay.source.formatter.checks.IncorrectFileLocationCheck;
import com.liferay.source.formatter.checks.JavaTermCheck;
import com.liferay.source.formatter.checks.ReturnCharacterCheck;
import com.liferay.source.formatter.checks.SourceCheck;
import com.liferay.source.formatter.parser.JavaClass;
import com.liferay.source.formatter.parser.JavaClassParser;
import com.liferay.source.formatter.util.FileUtil;

import java.awt.Desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * @author Brian Wing Shun Chan
 * @author Igor Spasic
 * @author Wesley Gong
 * @author Hugo Huijser
 */
public abstract class BaseSourceProcessor implements SourceProcessor {

	public static final int PLUGINS_MAX_DIR_LEVEL =
		ToolsUtil.PLUGINS_MAX_DIR_LEVEL;

	public static final int PORTAL_MAX_DIR_LEVEL =
		ToolsUtil.PORTAL_MAX_DIR_LEVEL;

	@Override
	public final void format() throws Exception {
		if (sourceFormatterArgs.isShowDocumentation()) {
			System.setProperty("java.awt.headless", "false");
		}

		List<String> fileNames = getFileNames();

		if (fileNames.isEmpty()) {
			return;
		}

		preFormat();

		_populateGenericSourceChecks();

		populateSourceChecks();

		if ((portalSource || subrepository) && _containsModuleFile(fileNames)) {
			populateModuleSourceChecks();
		}

		ExecutorService executorService = Executors.newFixedThreadPool(
			sourceFormatterArgs.getProcessorThreadCount());

		List<Future<Void>> futures = new ArrayList<>(fileNames.size());

		for (final String fileName : fileNames) {
			Future<Void> future = executorService.submit(
				new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						try {
							format(fileName);

							return null;
						}
						catch (Exception e) {
							throw new RuntimeException(
								"Unable to format " + fileName, e);
						}
					}

				});

			futures.add(future);
		}

		for (Future<Void> future : futures) {
			future.get();
		}

		executorService.shutdown();

		postFormat();

		_sourceFormatterHelper.close();
	}

	public final List<String> getFileNames() throws Exception {
		List<String> fileNames = sourceFormatterArgs.getFileNames();

		if (fileNames != null) {
			return fileNames;
		}

		return doGetFileNames();
	}

	@Override
	public SourceMismatchException getFirstSourceMismatchException() {
		return _firstSourceMismatchException;
	}

	@Override
	public String[] getIncludes() {
		return filterIncludes(doGetIncludes());
	}

	@Override
	public List<String> getModifiedFileNames() {
		return _modifiedFileNames;
	}

	@Override
	public Set<SourceFormatterMessage> getSourceFormatterMessages() {
		Set<SourceFormatterMessage> sourceFormatterMessages =
			new TreeSet<>();

		for (Map.Entry<String, Set<SourceFormatterMessage>> entry :
				_sourceFormatterMessagesMap.entrySet()) {

			sourceFormatterMessages.addAll(entry.getValue());
		}

		return sourceFormatterMessages;
	}

	@Override
	public void processMessage(String fileName, String message) {
		processMessage(fileName, message, -1);
	}

	@Override
	public void processMessage(String fileName, String message, int lineCount) {
		processMessage(fileName, message, null, lineCount);
	}

	@Override
	public void processMessage(
		String fileName, String message, String markdownFileName) {

		processMessage(fileName, message, markdownFileName, -1);
	}

	@Override
	public void processMessage(
		String fileName, String message, String markdownFileName,
		int lineCount) {

		processMessage(
			fileName,
			new SourceFormatterMessage(
				fileName, message, markdownFileName, lineCount));
	}

	@Override
	public void setProperties(Properties properties) {
		_properties = properties;
	}

	@Override
	public void setSourceFormatterArgs(
		SourceFormatterArgs sourceFormatterArgs) {

		this.sourceFormatterArgs = sourceFormatterArgs;

		_init();
	}

	protected int adjustLevel(int level, String text, String s, int diff) {
		String[] lines = StringUtil.splitLines(text);

		forLoop:
		for (String line : lines) {
			line = StringUtil.trim(line);

			if (line.startsWith("//")) {
				continue;
			}

			int x = -1;

			while (true) {
				x = line.indexOf(s, x + 1);

				if (x == -1) {
					continue forLoop;
				}

				if (!ToolsUtil.isInsideQuotes(line, x)) {
					level += diff;
				}
			}
		}

		return level;
	}

	protected abstract List<String> doGetFileNames() throws Exception;

	protected abstract String[] doGetIncludes();

	protected String[] filterIncludes(String[] includes) {
		List<String> fileExtensions = sourceFormatterArgs.getFileExtensions();

		if (fileExtensions.isEmpty()) {
			return includes;
		}

		String[] filteredIncludes = new String[0];

		for (String include : includes) {
			for (String fileExtension : fileExtensions) {
				if (include.endsWith(fileExtension)) {
					filteredIncludes = ArrayUtil.append(
						filteredIncludes, include);
				}
			}
		}

		return filteredIncludes;
	}

	protected final String format(
			File file, String fileName, String absolutePath, String content)
		throws Exception {

		_sourceFormatterMessagesMap.remove(fileName);

		_checkUTF8(file, fileName);

		String newContent = processSourceChecks(
			file, fileName, absolutePath, content);

		if (content.equals(newContent)) {
			return content;
		}

		return format(file, fileName, absolutePath, newContent);
	}

	protected final void format(String fileName) throws Exception {
		if (!_isMatchPath(fileName)) {
			return;
		}

		fileName = StringUtil.replace(
			fileName, CharPool.BACK_SLASH, CharPool.SLASH);

		String absolutePath = getAbsolutePath(fileName);

		File file = new File(absolutePath);

		String content = FileUtil.read(file);

		String newContent = format(file, fileName, absolutePath, content);

		processFormattedFile(file, fileName, content, newContent);
	}

	protected String getAbsolutePath(String fileName) {
		Path filePath = Paths.get(fileName);

		filePath = filePath.toAbsolutePath();

		filePath = filePath.normalize();

		return StringUtil.replace(
			filePath.toString(), CharPool.BACK_SLASH, CharPool.SLASH);
	}

	protected Map<String, String> getCompatClassNamesMap() throws Exception {
		Map<String, String> compatClassNamesMap = new HashMap<>();

		String[] includes = new String[] {
			"**/portal-compat-shared/src/com/liferay/compat/**/*.java"
		};

		String basedir = sourceFormatterArgs.getBaseDirName();

		List<String> fileNames = new ArrayList<>();

		for (int i = 0; i < PLUGINS_MAX_DIR_LEVEL; i++) {
			File sharedDir = new File(basedir + "shared");

			if (sharedDir.exists()) {
				fileNames = getFileNames(basedir, new String[0], includes);

				break;
			}

			basedir = basedir + "../";
		}

		for (String fileName : fileNames) {
			File file = new File(fileName);

			String content = FileUtil.read(file);

			fileName = StringUtil.replace(
				fileName, CharPool.BACK_SLASH, CharPool.SLASH);

			fileName = StringUtil.replace(
				fileName, CharPool.SLASH, CharPool.PERIOD);

			int pos = fileName.indexOf("com.");

			String compatClassName = fileName.substring(pos);

			compatClassName = compatClassName.substring(
				0, compatClassName.length() - 5);

			String extendedClassName = StringUtil.replace(
				compatClassName, "compat.", StringPool.BLANK);

			if (content.contains("extends " + extendedClassName)) {
				compatClassNamesMap.put(compatClassName, extendedClassName);
			}
		}

		return compatClassNamesMap;
	}

	protected String getContent(String fileName, int level) throws IOException {
		File file = getFile(fileName, level);

		if (file != null) {
			String content = FileUtil.read(file);

			if (Validator.isNotNull(content)) {
				return content;
			}
		}

		return StringPool.BLANK;
	}

	protected String getCopyright() throws Exception {
		String copyright = getContent(
			sourceFormatterArgs.getCopyrightFileName(), PORTAL_MAX_DIR_LEVEL);

		if (Validator.isNotNull(copyright)) {
			return copyright;
		}

		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		return StringUtil.read(
			classLoader.getResourceAsStream("dependencies/copyright.txt"));
	}

	protected List<String> getExcludes(String property) {
		List<String> excludes = _exclusionPropertiesMap.get(property);

		if (excludes != null) {
			return excludes;
		}

		excludes = getPropertyList(property);

		_exclusionPropertiesMap.put(property, excludes);

		return excludes;
	}

	protected File getFile(String fileName, int level) {
		return _sourceFormatterHelper.getFile(
			sourceFormatterArgs.getBaseDirName(), fileName, level);
	}

	protected List<String> getFileNames(
			String basedir, List<String> recentChangesFileNames,
			String[] excludes, String[] includes)
		throws Exception {

		return getFileNames(
			basedir, recentChangesFileNames, excludes, includes,
			sourceFormatterArgs.isIncludeSubrepositories());
	}

	protected List<String> getFileNames(
			String basedir, List<String> recentChangesFileNames,
			String[] excludes, String[] includes,
			boolean includeSubrepositories)
		throws Exception {

		if (_excludes != null) {
			excludes = ArrayUtil.append(excludes, _excludes);
		}

		return _sourceFormatterHelper.getFileNames(
			basedir, recentChangesFileNames, excludes, includes,
			includeSubrepositories);
	}

	protected List<String> getFileNames(
			String basedir, String[] excludes, String[] includes)
		throws Exception {

		return getFileNames(
			basedir, sourceFormatterArgs.getRecentChangesFileNames(), excludes,
			includes);
	}

	protected List<String> getFileNames(String[] excludes, String[] includes)
		throws Exception {

		return getFileNames(
			sourceFormatterArgs.getBaseDirName(), excludes, includes);
	}

	protected int getLeadingTabCount(String line) {
		int leadingTabCount = 0;

		while (line.startsWith(StringPool.TAB)) {
			line = line.substring(1);

			leadingTabCount++;
		}

		return leadingTabCount;
	}

	protected int getLevel(String s) {
		return getLevel(
			s, new String[] {StringPool.OPEN_PARENTHESIS},
			new String[] {StringPool.CLOSE_PARENTHESIS}, 0);
	}

	protected int getLevel(
		String s, String increaseLevelString, String decreaseLevelString) {

		return getLevel(
			s, new String[] {increaseLevelString},
			new String[] {decreaseLevelString}, 0);
	}

	protected int getLevel(
		String s, String[] increaseLevelStrings,
		String[] decreaseLevelStrings) {

		return getLevel(s, increaseLevelStrings, decreaseLevelStrings, 0);
	}

	protected int getLevel(
		String s, String[] increaseLevelStrings, String[] decreaseLevelStrings,
		int startLevel) {

		int level = startLevel;

		for (String increaseLevelString : increaseLevelStrings) {
			level = adjustLevel(level, s, increaseLevelString, 1);
		}

		for (String decreaseLevelString : decreaseLevelStrings) {
			level = adjustLevel(level, s, decreaseLevelString, -1);
		}

		return level;
	}

	protected String getLine(String content, int lineCount) {
		int nextLineStartPos = getLineStartPos(content, lineCount);

		if (nextLineStartPos == -1) {
			return null;
		}

		int nextLineEndPos = content.indexOf(
			CharPool.NEW_LINE, nextLineStartPos);

		if (nextLineEndPos == -1) {
			return content.substring(nextLineStartPos);
		}

		return content.substring(nextLineStartPos, nextLineEndPos);
	}

	protected int getLineCount(String content, int pos) {
		return StringUtil.count(content, 0, pos, CharPool.NEW_LINE) + 1;
	}

	protected int getLineLength(String line) {
		int lineLength = 0;

		int tabLength = 4;

		for (char c : line.toCharArray()) {
			if (c == CharPool.TAB) {
				for (int i = 0; i < tabLength; i++) {
					lineLength++;
				}

				tabLength = 4;
			}
			else {
				lineLength++;

				tabLength--;

				if (tabLength <= 0) {
					tabLength = 4;
				}
			}
		}

		return lineLength;
	}

	protected int getLineStartPos(String content, int lineCount) {
		int x = 0;

		for (int i = 1; i < lineCount; i++) {
			x = content.indexOf(CharPool.NEW_LINE, x + 1);

			if (x == -1) {
				return x;
			}
		}

		return x + 1;
	}

	protected List<SourceCheck> getModuleSourceChecks() {
		return null;
	}

	protected List<String> getParameterList(String methodCall) {
		String parameters = null;

		int x = -1;

		while (true) {
			x = methodCall.indexOf(StringPool.CLOSE_PARENTHESIS, x + 1);

			parameters = methodCall.substring(0, x + 1);

			if ((getLevel(parameters, "(", ")") == 0) &&
				(getLevel(parameters, "{", "}") == 0)) {

				break;
			}
		}

		x = parameters.indexOf(StringPool.OPEN_PARENTHESIS);

		parameters = parameters.substring(x + 1, parameters.length() - 1);

		return splitParameters(parameters);
	}

	protected List<String> getPluginsInsideModulesDirectoryNames()
		throws Exception {

		if (_pluginsInsideModulesDirectoryNames != null) {
			return _pluginsInsideModulesDirectoryNames;
		}

		List<String> pluginsInsideModulesDirectoryNames = new ArrayList<>();

		List<String> pluginBuildFileNames = getFileNames(
			sourceFormatterArgs.getBaseDirName(), null, new String[0],
			new String[] {
				"**/modules/apps/**/build.xml",
				"**/modules/private/apps/**/build.xml"
			});

		for (String pluginBuildFileName : pluginBuildFileNames) {
			pluginBuildFileName = StringUtil.replace(
				pluginBuildFileName, StringPool.BACK_SLASH, StringPool.SLASH);

			String absolutePath = getAbsolutePath(pluginBuildFileName);

			int x = absolutePath.indexOf("/modules/apps/");

			if (x == -1) {
				x = absolutePath.indexOf("/modules/private/apps/");
			}

			int y = absolutePath.lastIndexOf(StringPool.SLASH);

			pluginsInsideModulesDirectoryNames.add(
				absolutePath.substring(x, y + 1));
		}

		_pluginsInsideModulesDirectoryNames =
			pluginsInsideModulesDirectoryNames;

		return _pluginsInsideModulesDirectoryNames;
	}

	protected Properties getPortalLanguageProperties() throws Exception {
		Properties portalLanguageProperties = new Properties();

		File portalLanguagePropertiesFile = getFile(
			"portal-impl/src/content/Language.properties",
			PORTAL_MAX_DIR_LEVEL);

		if (portalLanguagePropertiesFile != null) {
			InputStream inputStream = new FileInputStream(
				portalLanguagePropertiesFile);

			portalLanguageProperties.load(inputStream);
		}

		return portalLanguageProperties;
	}

	protected String getProjectPathPrefix() throws Exception {
		if (!subrepository) {
			return null;
		}

		File file = getFile("gradle.properties", PORTAL_MAX_DIR_LEVEL);

		if (!file.exists()) {
			return null;
		}

		Properties properties = new Properties();

		properties.load(new FileInputStream(file));

		return properties.getProperty("project.path.prefix");
	}

	protected String getProperty(String key) {
		return _properties.getProperty(key);
	}

	protected List<String> getPropertyList(String key) {
		return ListUtil.fromString(
			GetterUtil.getString(getProperty(key)), StringPool.COMMA);
	}

	protected abstract List<SourceCheck> getSourceChecks();

	protected boolean isExcludedPath(String property, String path) {
		return isExcludedPath(property, path, -1);
	}

	protected boolean isExcludedPath(
		String property, String path, int lineCount) {

		return isExcludedPath(property, path, lineCount, null);
	}

	protected boolean isExcludedPath(
		String property, String path, int lineCount, String parameter) {

		if (property == null) {
			return false;
		}

		List<String> excludes = _exclusionPropertiesMap.get(property);

		if (excludes == null) {
			excludes = getPropertyList(property);

			_exclusionPropertiesMap.put(property, excludes);
		}

		if (ListUtil.isEmpty(excludes)) {
			return false;
		}

		String pathWithParameter = null;

		if (Validator.isNotNull(parameter)) {
			pathWithParameter = path + StringPool.AT + parameter;
		}

		String pathWithLineCount = null;

		if (lineCount > 0) {
			pathWithLineCount = path + StringPool.AT + lineCount;
		}

		for (String exclude : excludes) {
			if (Validator.isNull(exclude)) {
				continue;
			}

			if (exclude.startsWith("**")) {
				exclude = exclude.substring(2);
			}

			if (exclude.endsWith("**")) {
				exclude = exclude.substring(0, exclude.length() - 2);

				if (path.contains(exclude)) {
					return true;
				}

				continue;
			}

			if (path.endsWith(exclude) ||
				((pathWithParameter != null) &&
				 pathWithParameter.endsWith(exclude)) ||
				((pathWithLineCount != null) &&
				 pathWithLineCount.endsWith(exclude))) {

				return true;
			}
		}

		return false;
	}

	protected boolean isExcludedPath(
		String property, String path, String parameter) {

		return isExcludedPath(property, path, -1, parameter);
	}

	protected boolean isModulesApp(String absolutePath, boolean privateOnly) {
		if (absolutePath.contains("/modules/private/apps/") ||
			(!privateOnly && absolutePath.contains("/modules/apps/"))) {

			return true;
		}

		if (_projectPathPrefix == null) {
			return false;
		}

		if (_projectPathPrefix.startsWith(":private:apps") ||
			(!privateOnly && _projectPathPrefix.startsWith(":apps:"))) {

			return true;
		}

		return false;
	}

	protected boolean isModulesFile(String absolutePath) {
		return isModulesFile(absolutePath, false);
	}

	protected boolean isModulesFile(
		String absolutePath, boolean includePlugins) {

		if (subrepository) {
			return true;
		}

		if (includePlugins) {
			return absolutePath.contains("/modules/");
		}

		try {
			for (String directoryName :
					getPluginsInsideModulesDirectoryNames()) {

				if (absolutePath.contains(directoryName)) {
					return false;
				}
			}
		}
		catch (Exception e) {
		}

		return absolutePath.contains("/modules/");
	}

	protected abstract void populateSourceChecks()
		throws Exception;

	protected void populateModuleSourceChecks() throws Exception {
	}

	protected void postFormat() throws Exception {
	}

	protected void preFormat() throws Exception {
	}

	protected void printError(String fileName, String message) {
		if (sourceFormatterArgs.isPrintErrors()) {
			_sourceFormatterHelper.printError(fileName, message);
		}
	}

	protected String processSourceChecks(
			File file, String fileName, String absolutePath, String content)
		throws Exception {

		content = _processSourceChecks(
			fileName, absolutePath, content, _genericSourceChecks);

		content = _processSourceChecks(
			fileName, absolutePath, content, getSourceChecks());

		if (isModulesFile(absolutePath)) {
			content = _processSourceChecks(
				fileName, absolutePath, content, getModuleSourceChecks());
		}

		return content;
	}

	protected void processFormattedFile(
			File file, String fileName, String content, String newContent)
		throws Exception {

		if (!content.equals(newContent)) {
			if (sourceFormatterArgs.isPrintErrors()) {
				_sourceFormatterHelper.printError(fileName, file);
			}

			if (sourceFormatterArgs.isAutoFix()) {
				FileUtil.write(file, newContent);
			}
			else if (_firstSourceMismatchException == null) {
				_firstSourceMismatchException = new SourceMismatchException(
					fileName, content, newContent);
			}
		}

		if (sourceFormatterArgs.isPrintErrors()) {
			Set<SourceFormatterMessage> sourceFormatterMessages =
				_sourceFormatterMessagesMap.get(fileName);

			if (sourceFormatterMessages != null) {
				for (SourceFormatterMessage sourceFormatterMessage :
						sourceFormatterMessages) {

					_sourceFormatterHelper.printError(
						fileName, sourceFormatterMessage.toString());

					if (_browserStarted ||
						!sourceFormatterArgs.isShowDocumentation() ||
						!Desktop.isDesktopSupported()) {

						continue;
					}

					String markdownFileName =
						sourceFormatterMessage.getMarkdownFileName();

					if (Validator.isNotNull(markdownFileName)) {
						Desktop desktop = Desktop.getDesktop();

						desktop.browse(
							new URI(_DOCUMENTATION_URL + markdownFileName));

						_browserStarted = true;
					}
				}
			}
		}

		_modifiedFileNames.add(file.getAbsolutePath());
	}

	protected void processMessage(
		String fileName, SourceFormatterMessage sourceFormatterMessage) {

		Set<SourceFormatterMessage> sourceFormatterMessages =
			_sourceFormatterMessagesMap.get(fileName);

		if (sourceFormatterMessages == null) {
			sourceFormatterMessages = new TreeSet<>();
		}

		sourceFormatterMessages.add(sourceFormatterMessage);

		_sourceFormatterMessagesMap.put(fileName, sourceFormatterMessages);
	}

	protected Document readXML(String content) throws DocumentException {
		SAXReader saxReader = SAXReaderFactory.getSAXReader(null, false, false);

		return saxReader.read(new UnsyncStringReader(content));
	}

	protected List<String> splitParameters(String parameters) {
		List<String> parametersList = new ArrayList<>();

		int x = -1;

		while (true) {
			x = parameters.indexOf(StringPool.COMMA, x + 1);

			if (x == -1) {
				parametersList.add(StringUtil.trim(parameters));

				return parametersList;
			}

			if (ToolsUtil.isInsideQuotes(parameters, x)) {
				continue;
			}

			String linePart = parameters.substring(0, x);

			if ((getLevel(linePart, "(", ")") == 0) &&
				(getLevel(linePart, "{", "}") == 0)) {

				parametersList.add(StringUtil.trim(linePart));

				parameters = parameters.substring(x + 1);

				x = -1;
			}
		}
	}

	protected String stripQuotes(String s) {
		return stripQuotes(s, CharPool.APOSTROPHE, CharPool.QUOTE);
	}

	protected String stripQuotes(String s, char... delimeters) {
		List<Character> delimetersList = ListUtil.toList(delimeters);

		char delimeter = CharPool.SPACE;
		boolean insideQuotes = false;

		StringBundler sb = new StringBundler();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (insideQuotes) {
				if (c == delimeter) {
					int precedingBackSlashCount = 0;

					for (int j = (i - 1); j >= 0; j--) {
						if (s.charAt(j) == CharPool.BACK_SLASH) {
							precedingBackSlashCount += 1;
						}
						else {
							break;
						}
					}

					if ((precedingBackSlashCount == 0) ||
						((precedingBackSlashCount % 2) == 0)) {

						insideQuotes = false;
					}
				}
			}
			else if (delimetersList.contains(c)) {
				delimeter = c;
				insideQuotes = true;
			}
			else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	protected static final String LANGUAGE_KEYS_CHECK_EXCLUDES =
		"language.keys.check.excludes";

	protected static final String METHOD_CALL_SORT_EXCLUDES =
		"method.call.sort.excludes";

	protected static final String RUN_OUTSIDE_PORTAL_EXCLUDES =
		"run.outside.portal.excludes";

	protected static Pattern javaSourceInsideJSPLinePattern = Pattern.compile(
		"<%=(.+?)%>");
	protected static boolean portalSource;
	protected static boolean subrepository;

	protected SourceFormatterArgs sourceFormatterArgs;

	private void _checkUTF8(File file, String fileName) throws Exception {
		byte[] bytes = FileUtil.getBytes(file);

		try {
			CharsetDecoder charsetDecoder =
				CharsetDecoderUtil.getCharsetDecoder(
					StringPool.UTF8, CodingErrorAction.REPORT);

			charsetDecoder.decode(ByteBuffer.wrap(bytes));
		}
		catch (Exception e) {
			processMessage(
				fileName,
				new SourceFormatterMessage(fileName, "UTF-8", null, -1));
		}
	}

	private boolean _containsModuleFile(List<String> fileNames) {
		for (String fileName : fileNames) {
			if (!_isMatchPath(fileName)) {
				continue;
			}

			String absolutePath = getAbsolutePath(fileName);

			if (isModulesFile(absolutePath, true)) {
				return true;
			}
		}

		return false;
	}

	private String[] _getExcludes() {
		if (sourceFormatterArgs.getFileNames() != null) {
			return new String[0];
		}

		List<String> excludesList = ListUtil.fromString(
			GetterUtil.getString(
				System.getProperty("source.formatter.excludes")));

		excludesList.addAll(getPropertyList("source.formatter.excludes"));

		return excludesList.toArray(new String[excludesList.size()]);
	}

	private void _init() {
		try {
			_sourceFormatterHelper = new SourceFormatterHelper(
				sourceFormatterArgs.isUseProperties());

			_sourceFormatterHelper.init();

			portalSource = _isPortalSource();
			subrepository = _isSubrepository();

			_projectPathPrefix = getProjectPathPrefix();

			_sourceFormatterMessagesMap = new HashMap<>();
		}
		catch (Exception e) {
			ReflectionUtil.throwException(e);
		}

		_excludes = _getExcludes();
	}

	private boolean _isMatchPath(String fileName) {
		for (String pattern : getIncludes()) {
			if (SelectorUtils.matchPath(_normalizePattern(pattern), fileName)) {
				return true;
			}
		}

		return false;
	}

	private boolean _isPortalSource() {
		if (getFile("portal-impl", PORTAL_MAX_DIR_LEVEL) != null) {
			return true;
		}

		return false;
	}

	private boolean _isSubrepository() {
		String baseDirAbsolutePath = getAbsolutePath(
			sourceFormatterArgs.getBaseDirName());

		int x = baseDirAbsolutePath.length();

		for (int i = 0; i < 2; i++) {
			x = baseDirAbsolutePath.lastIndexOf(CharPool.FORWARD_SLASH, x - 1);

			if (x == -1) {
				return false;
			}

			String dirName = baseDirAbsolutePath.substring(x + 1);

			if (dirName.startsWith("com-liferay-")) {
				return true;
			}
		}

		return false;
	}

	private String _normalizePattern(String originalPattern) {
		String pattern = originalPattern.replace(
			CharPool.SLASH, File.separatorChar);

		pattern = pattern.replace(CharPool.BACK_SLASH, File.separatorChar);

		if (pattern.endsWith(File.separator)) {
			pattern += SelectorUtils.DEEP_TREE_MATCH;
		}

		return pattern;
	}

	private void _populateGenericSourceChecks() throws Exception {
		_genericSourceChecks.add(new IncorrectFileLocationCheck());
		_genericSourceChecks.add(new ReturnCharacterCheck());
	}

	private String _processSourceChecks(
			String fileName, String absolutePath, String content,
			List<SourceCheck> sourceChecks)
		throws Exception {

		if (ListUtil.isEmpty(sourceChecks)) {
			return content;
		}

		JavaClass javaClass = null;
		List<JavaClass> anonymousClasses = null;

		for (SourceCheck sourceCheck : sourceChecks) {
			String newContent = null;

			if (sourceCheck instanceof FileCheck) {
				FileCheck fileCheck = (FileCheck)sourceCheck;

				newContent = fileCheck.process(fileName, absolutePath, content);

				for (SourceFormatterMessage sourceFormatterMessage :
						sourceCheck.getSourceFormatterMessage(fileName)) {

					processMessage(fileName, sourceFormatterMessage);
				}
			}
			else if ((sourceCheck instanceof JavaTermCheck) &&
					 (this instanceof JavaSourceProcessor)) {

				JavaTermCheck javaTermCheck = (JavaTermCheck)sourceCheck;

				if (javaClass == null) {
					anonymousClasses = JavaClassParser.parseAnonymousClasses(
						content);
					javaClass = JavaClassParser.parseJavaClass(
						fileName, content);
				}

				newContent = javaTermCheck.process(
					fileName, absolutePath, javaClass, content);

				for (SourceFormatterMessage sourceFormatterMessage :
						sourceCheck.getSourceFormatterMessage(fileName)) {

					processMessage(fileName, sourceFormatterMessage);
				}

				for (JavaClass anonymousClass : anonymousClasses) {
					newContent = javaTermCheck.process(
						fileName, absolutePath, anonymousClass, newContent);

					for (SourceFormatterMessage sourceFormatterMessage :
							sourceCheck.getSourceFormatterMessage(fileName)) {

						processMessage(fileName, sourceFormatterMessage);
					}
				}
			}

			if (!newContent.equals(content)) {
				return newContent;
			}
		}

		return content;
	}

	private static final String _DOCUMENTATION_URL =
		"https://github.com/liferay/liferay-portal/blob/master/modules/util" +
			"/source-formatter/documentation/";

	private boolean _browserStarted;
	private String[] _excludes;
	private Map<String, List<String>> _exclusionPropertiesMap = new HashMap<>();
	private SourceMismatchException _firstSourceMismatchException;
	private final List<SourceCheck> _genericSourceChecks = new ArrayList<>();
	private final List<String> _modifiedFileNames =
		new CopyOnWriteArrayList<>();
	private List<String> _pluginsInsideModulesDirectoryNames;
	private String _projectPathPrefix;
	private Properties _properties;
	private SourceFormatterHelper _sourceFormatterHelper;
	private Map<String, Set<SourceFormatterMessage>>
		_sourceFormatterMessagesMap = new ConcurrentHashMap<>();

}