/**
 * 
 */
package org.websitegenerator.core.model;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author thobach
 * 
 */
public interface Content {

	/**
	 * 
	 * @return
	 */
	public String getTitle();

	/**
	 * 
	 * @return
	 */
	public String getContent();

	/**
	 * 
	 * @param content
	 */
	public void setContent(String content);

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title);

	/**
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName);

	/**
	 * 
	 * @return
	 */
	public String getFileName();

	/**
	 * 
	 * @param parameter
	 */
	public void setType(String parameter);

	/**
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * 
	 * @param sourceLocation
	 */
	public void setSourceLocation(String sourceLocation);

	/**
	 * 
	 * @return
	 */
	public String getSourceLocation();

	/**
	 * 
	 * @param mimeType
	 */
	public void setMimeType(String mimeType);

	/**
	 * 
	 * @return
	 */
	public String getMimeType();

	/**
	 * 
	 * @param inputStream
	 */
	public void setContent(InputStream inputStream);

	/**
	 * 
	 * @return
	 */
	public InputStream getInputStream();

	/**
	 * 
	 * @param templateName
	 */
	public void setTemplateName(String templateName);

	/**
	 * 
	 * @return
	 */
	public String getTemplateName();

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addCustomAttribute(String name, String value);

	/**
	 * 
	 * @return
	 */
	public Collection<String> getCustomAttributeNames();

	/**
	 * 
	 * @param attributeName
	 * @return
	 */
	public String getCustomAttribute(String attributeName);

}
