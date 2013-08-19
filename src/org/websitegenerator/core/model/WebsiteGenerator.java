/**
 * 
 */
package org.websitegenerator.core.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.websitegenerator.template.file.FileTemplate;
import org.websitegenerator.template.staticwebpage.StaticWebPageTemplate;

/**
 * @author thobach
 * 
 */
public class WebsiteGenerator {

	/*
	 * fields
	 */

	/**
	 * 
	 */
	private Target target;

	/**
	 * 
	 * @param target
	 * @param template
	 */
	public WebsiteGenerator(Target target) {
		this.target = target;
	}

	/**
	 * 
	 * @param contents
	 */
	public void generate(Content[] contents) {

		Logger.getLogger("WebsiteGenerator").info("Publishing Website.");

		// regenerate artifacts for target
		Artifact[] artifacts = getArtifacts(contents);

		// update artifacts to target
		publishArtifacts(artifacts);

		Logger.getLogger("WebsiteGenerator").info("Website Published.");
	}

	/**
	 * @param artifacts
	 */
	private void publishArtifacts(Artifact[] artifacts) {

		Logger.getLogger("WebsiteGenerator").info(
				"Publishing " + artifacts.length + " artifacts.");

		target.init();

		ExecutorService exec = Executors.newFixedThreadPool(10);
		List<Future<?>> futures = new LinkedList<Future<?>>();
		try {
			for (final Artifact artifact : artifacts) {
				Future<?> future = exec.submit(new Runnable() {
					@Override
					public void run() {
						target.add(artifact);
					}
				});
				futures.add(future);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for (Future<?> f : futures) {
				try {
					f.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			exec.shutdown();
		}

		Logger.getLogger("WebsiteGenerator").info(
				"Published " + artifacts.length + " artifacts.");
	}

	/**
	 * 
	 * @param contents
	 * @return
	 */
	private Artifact[] getArtifacts(Content[] contents) {

		Logger.getLogger("WebsiteGenerator").info(
				"Retrieving artifacts for " + contents.length
						+ " content items.");

		ExecutorService exec = Executors.newFixedThreadPool(10);
		List<Future<?>> futures = new LinkedList<Future<?>>();

		final ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		final Template fileTemplate = new FileTemplate();

		try {
			for (final Content content : contents) {
				Future<?> future = exec.submit(new Runnable() {
					@Override
					public void run() {
						if (content.getType() == null
								|| content.getType().equals("HTML")) {
							artifacts.add(new StaticWebPageTemplate(content
									.getTemplateName()).getArtifact(content));
						} else if (content.getType().equals("File")) {
							artifacts.add(fileTemplate.getArtifact(content));
						}
					}
				});
				futures.add(future);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for (Future<?> f : futures) {
				try {
					f.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			exec.shutdown();
		}

		Logger.getLogger("WebsiteGenerator").info(
				"Retrieved artifacts for " + contents.length
						+ " content items.");

		return artifacts.toArray(new Artifact[artifacts.size()]);
	}
}
