/**
 * 
 */
package org.websitegenerator.core.model;

import java.io.InputStream;

/**
 * @author thobach
 * 
 */
public abstract class AbstractArtifact implements Artifact {
	/*
	 * fields
	 */

	/**
	 * 
	 */
	private String name;

	/**
	 * 
	 */
	private String payload;

	/**
	 * 
	 */
	private String fileName;

	/**
	 * 
	 */
	private String contentType;

	/**
	 * 
	 */
	private InputStream inputStream;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String getPayload() {
		return payload;
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
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}
}
