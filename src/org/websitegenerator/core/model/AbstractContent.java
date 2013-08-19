/**
 * 
 */
package org.websitegenerator.core.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author thobach
 * 
 */
public abstract class AbstractContent implements Content {
	/*
	 * fields
	 */

	/**
	 * 
	 */
	private String title;

	/**
	 * 
	 */
	private String content;

	/**
	 * 
	 */
	private String fileName;

	/**
	 * 
	 */
	private String type;

	/**
	 * 
	 */
	private String sourceLocation;

	/**
	 * 
	 */
	private String mimeType;

	/**
	 * 
	 */
	private String templateName;

	private HashMap<String, String> customAttributes = new HashMap<String, String>();

	private InputStream inputStream;

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setSourceLocation(String sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	@Override
	public String getSourceLocation() {
		return sourceLocation;
	}

	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public void setContent(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public void addCustomAttribute(String name, String value) {
		customAttributes.put(name, value);
	}

	@Override
	public String getCustomAttribute(String attributeName) {
		return customAttributes.get(attributeName);
	}

	@Override
	public Collection<String> getCustomAttributeNames() {
		return customAttributes.keySet();
	}

}
