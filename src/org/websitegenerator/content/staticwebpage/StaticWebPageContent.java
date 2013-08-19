/**
 * 
 */
package org.websitegenerator.content.staticwebpage;

import java.io.InputStream;

import org.websitegenerator.core.model.AbstractContent;

/**
 * @author thobach
 * 
 */
public class StaticWebPageContent extends AbstractContent {
	
	public StaticWebPageContent() {
		setType("HTML");
		setMimeType("text/html");
	}

	@Override
	public InputStream getInputStream() {
		// ignore
		return null;
	}

	@Override
	public void setContent(InputStream inputStream) {
		// ignore
	}

}
