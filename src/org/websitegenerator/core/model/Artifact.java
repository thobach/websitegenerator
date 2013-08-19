/**
 * 
 */
package org.websitegenerator.core.model;

import java.io.InputStream;

/**
 * @author thobach
 * 
 */
public interface Artifact {

	/**
	 * 
	 * @param string
	 */
	public void setName(String string);

	/**
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 
	 * @param payload
	 */
	public void setPayload(String payload);

	/**
	 * 
	 * @return
	 */
	public String getPayload();

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
	 * @param contentType
	 */
	public void setContentType(String contentType);

	/**
	 * 
	 * @return
	 */
	public String getContentType();

	/**
	 * 
	 * @param inputStream
	 */
	void setInputStream(InputStream inputStream);

	/**
	 * 
	 * @return
	 */
	public InputStream getInputStream();

}
