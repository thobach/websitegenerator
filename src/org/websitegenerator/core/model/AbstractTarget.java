/**
 * 
 */
package org.websitegenerator.core.model;

/**
 * @author thobach
 * 
 */
public abstract class AbstractTarget implements Target {

	/*
	 * does nothing, ignored by default
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.websitegenerator.core.model.Target#init()
	 */
	@Override
	public void init() {
		// ignore per default
	}

	/*
	 * does nothing, ignored by default
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.websitegenerator.core.model.Target#invalidateCDN()
	 */
	@Override
	public void invalidateCDN() {
		// ignore per default
	}

}
