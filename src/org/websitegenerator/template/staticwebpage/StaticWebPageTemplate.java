/**
 * 
 */
package org.websitegenerator.template.staticwebpage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.websitegenerator.artifact.html.HTMLArtifact;
import org.websitegenerator.contentstore.simpledbs3.SimpleDBS3ContentStore;
import org.websitegenerator.core.model.AbstractTemplate;
import org.websitegenerator.core.model.Artifact;
import org.websitegenerator.core.model.Content;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;

/**
 * @author thobach
 * 
 */
public class StaticWebPageTemplate extends AbstractTemplate {

	private String template;
	private String simpleDbDomain;
	private AmazonSimpleDBClient simpleDbClient;

	public static ArrayList<String> existingDomains = new ArrayList<String>();

	public StaticWebPageTemplate() {
		initSimpleDb();
	}

	/**
	 * 
	 */
	private void initSimpleDb() {
		AWSCredentials credentials = null;
		try {
			InputStream propertiesStream = SimpleDBS3ContentStore.class
					.getResourceAsStream("/websitegenerator.properties");
			credentials = new PropertiesCredentials(propertiesStream);
			Properties properties = new Properties();
			propertiesStream = SimpleDBS3ContentStore.class.getResourceAsStream("/websitegenerator.properties");
			properties.load(propertiesStream);
			simpleDbDomain = properties.getProperty("simpleDbDomain");
		} catch (IOException e) {
			System.out.println("Credentials were not properly entered into websitegenerator.properties.");
			e.printStackTrace();
			System.exit(-1);
		}

		simpleDbClient = new AmazonSimpleDBClient(credentials);

		if (!existingDomains.contains(simpleDbDomain)) {
			// idempotent (will not replace if already exists)
			try {
				simpleDbClient.createDomain(new CreateDomainRequest(simpleDbDomain));
				existingDomains.add(simpleDbDomain);
			} catch (AmazonServiceException e) {
				Logger.getLogger("WebsiteGenerator").warning(
						"Could not create SimpleDB domain: '" + simpleDbDomain + "' due to '" + e.getLocalizedMessage()
								+ "', trying again ...");
				try {
					simpleDbClient.createDomain(new CreateDomainRequest(simpleDbDomain));
					existingDomains.add(simpleDbDomain);
				} catch (AmazonServiceException e1) {
					Logger.getLogger("WebsiteGenerator").warning(
							"Could not create SimpleDB domain: '" + simpleDbDomain + "' (2nd try) due to '"
									+ e1.getLocalizedMessage() + "', trying again ...");
					try {
						simpleDbClient.createDomain(new CreateDomainRequest(simpleDbDomain));
						existingDomains.add(simpleDbDomain);
					} catch (AmazonServiceException e2) {
						Logger.getLogger("WebsiteGenerator").severe(
								"Could not create SimpleDB domain: '" + simpleDbDomain + "' (3rd try) due to '"
										+ e1.getLocalizedMessage() + "', aborting here ...");
						e.printStackTrace();
					}
				}
			}
		}
	}

	public StaticWebPageTemplate(String templateName) {
		initSimpleDb();

		String selectExpression = "SELECT * FROM `" + simpleDbDomain
				+ "` WHERE `Source Location` is not null AND `Type` = 'Template' AND itemName()='" + templateName
				+ "' ORDER BY `Source Location` ASC LIMIT 1";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setConsistentRead(true);
		for (Item item : simpleDbClient.select(selectRequest).getItems()) {
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Title")) {
					setTitle(attribute.getValue());
				} else if (attribute.getName().equals("Source Location")) {
					setSourceLocation(attribute.getValue());
				} else if (attribute.getName().equals("Type")) {
					setType(attribute.getValue());
				} else if (attribute.getName().equals("Mime Type")) {
					setMimeType(attribute.getValue());
				} else if (attribute.getName().equals("Template")) {
					setTemplateName(attribute.getValue());
				}
			}
			setFileName(item.getName());
			try {
				if (getTemplateName() != null && !getTemplateName().equals("") && !getTemplateName().equals("null")) {
					StaticWebPageTemplate parentTemplate = new StaticWebPageTemplate(getTemplateName());
					String parentTemplateString = IOUtils
							.toString(new URL(parentTemplate.getSourceLocation()), "UTF-8");
					template = parentTemplateString.replace("{content}",
							IOUtils.toString(new URL(getSourceLocation()), "UTF-8"));
				} else {
					template = IOUtils.toString(new URL(getSourceLocation()), "UTF-8");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public Artifact getArtifact(Content content) {

		String payload = template;
		if (content.getTitle() != null) {
			payload = payload.replace("{heading}", content.getTitle());
		}
		if (content.getContent() != null) {
			payload = payload.replace("{content}", content.getContent());
		}
		if (content.getSourceLocation() != null) {
			try {
				StringWriter writer = new StringWriter();
				IOUtils.copy(new URL(content.getSourceLocation()).openStream(), writer, "UTF-8");
				payload = payload.replace("{content}", writer.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (String customAttributeName : content.getCustomAttributeNames()) {
			payload = payload.replace("{" + customAttributeName + "}", content.getCustomAttribute(customAttributeName));
		}

		// remove placeholders which were not replaced since custom attribute was empty
		payload = payload.replaceAll("\\{.+\\}", "");

		Artifact artifact = new HTMLArtifact();
		artifact.setName(content.getTitle());
		if (content.getFileName() == null) {
			artifact.setFileName(content.getTitle().toLowerCase().replace(" ", "-") + ".html");
		} else {
			artifact.setFileName(content.getFileName());
		}
		artifact.setPayload(payload);
		artifact.setContentType(content.getMimeType());

		Logger.getLogger("WebsiteGenerator").info("Created artifact for '" + content.getFileName() + "'.");

		return artifact;
	}

}
