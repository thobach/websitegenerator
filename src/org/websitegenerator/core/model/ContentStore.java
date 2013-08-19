/**
 * 
 */
package org.websitegenerator.core.model;

/**
 * 
 * @author thobach
 * 
 */
public interface ContentStore {

	/**
	 * 
	 * @return
	 */
	public Content[] getContents();

	/**
	 * 
	 * @param content
	 */
	public void addContent(Content content);

	/**
	 * 
	 * @param parameter
	 */
	public void deleteContent(String parameter);

}
