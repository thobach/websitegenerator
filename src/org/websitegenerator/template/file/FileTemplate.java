/**
 * 
 */
package org.websitegenerator.template.file;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.websitegenerator.artifact.file.FileArtifact;
import org.websitegenerator.core.model.AbstractTemplate;
import org.websitegenerator.core.model.Artifact;
import org.websitegenerator.core.model.Content;

/**
 * @author thobach
 * 
 */
public class FileTemplate extends AbstractTemplate {

	@Override
	public Artifact getArtifact(Content content) {
		Artifact artifact = new FileArtifact();
		artifact.setName(content.getTitle());
		artifact.setFileName(content.getFileName());
		try {
			artifact.setInputStream(new URL(content.getSourceLocation())
					.openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		artifact.setContentType(content.getMimeType());
		
		Logger.getLogger("WebsiteGenerator").info("Created artifact for '" + content.getFileName() + "'.");
		
		return artifact;
	}

}
