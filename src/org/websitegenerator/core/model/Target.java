/**
 * 
 */
package org.websitegenerator.core.model;

/**
 * @author thobach
 * 
 */
public interface Target {

	/**
	 * Add artifact to target, e.g. add file to S3 or file system
	 * 
	 * @param artifact
	 */
	public void add(Artifact artifact);

	/**
	 * Clean up target, e.g. delete all files
	 */
	public void cleanup();

	/**
	 * Invalidate resources that are located at a CDN
	 */
	public void invalidateCDN();

	/**
	 * Initialize the target, e.g. add configuration files
	 */
	public void init();

}
