/**
 * 
 */
package org.websitegenerator.contentstore.simpledbs3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.websitegenerator.content.file.FileContent;
import org.websitegenerator.core.model.AbstractContentStore;
import org.websitegenerator.core.model.Content;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.util.StringInputStream;

/**
 * @author thobach
 * 
 */
public class SimpleDBS3ContentStore extends AbstractContentStore {

	/*
	 * fields
	 */

	/**
	 * 
	 */
	private AmazonSimpleDBClient simpleDbClient;

	/**
	 * 
	 */
	private AmazonS3 s3Client;

	/**
	 * 
	 */
	private String s3BucketName;

	/**
	 * 
	 */
	private String simpleDbDomain;

	/**
	 * 
	 */
	private String s3Location;

	public SimpleDBS3ContentStore() {
		AWSCredentials credentials = null;
		try {
			InputStream propertiesStream = SimpleDBS3ContentStore.class
					.getResourceAsStream("/websitegenerator.properties");
			credentials = new PropertiesCredentials(propertiesStream);
			Properties properties = new Properties();
			propertiesStream = SimpleDBS3ContentStore.class.getResourceAsStream("/websitegenerator.properties");
			properties.load(propertiesStream);
			s3BucketName = properties.getProperty("s3BucketTemplate");
			simpleDbDomain = properties.getProperty("simpleDbDomain");
			s3Location = properties.getProperty("s3Location");
		} catch (IOException e) {
			System.out.println("Credentials were not properly entered into websitegenerator.properties.");
			e.printStackTrace();
			System.exit(-1);
		}

		simpleDbClient = new AmazonSimpleDBClient(credentials);

		// idempotent (will not replace if already exists)
		simpleDbClient.createDomain(new CreateDomainRequest(simpleDbDomain));

		s3Client = new AmazonS3Client(credentials);
	}

	@Override
	public void addContent(Content content) {
		String sourceLocation = addToS3(content);
		addToSimpleDB(content, sourceLocation);
	}

	/**
	 * @param content
	 * @param contentData
	 * @param sourceLocation
	 */
	private void addToSimpleDB(Content content, String sourceLocation) {
		List<ReplaceableItem> contentData = new ArrayList<ReplaceableItem>();
		Collection<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(new ReplaceableAttribute("Source Location", sourceLocation, true));
		attributes.add(new ReplaceableAttribute("Type", content.getType(), true));
		attributes.add(new ReplaceableAttribute("Mime Type", content.getMimeType(), true));
		if (content.getTemplateName() != null) {
			attributes.add(new ReplaceableAttribute("Template", content.getTemplateName(), true));
		}
		if (content.getTitle() != null) {
			attributes.add(new ReplaceableAttribute("Title", content.getTitle(), true));
		}
		for (String attributeName : content.getCustomAttributeNames()) {
			attributes.add(new ReplaceableAttribute(attributeName, content.getCustomAttribute(attributeName), true));
		}
		contentData.add(new ReplaceableItem(content.getFileName()).withAttributes(attributes));
		simpleDbClient.batchPutAttributes(new BatchPutAttributesRequest(simpleDbDomain, contentData));
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	private String addToS3(Content content) {
		String artifactS3Key = content.getFileName();
		ObjectMetadata metaData = new ObjectMetadata();
		metaData.setContentType(content.getMimeType());
		String sourceLocation = "https://" + s3Location + ".amazonaws.com/" + s3BucketName + "/" + artifactS3Key;
		try {
			InputStream artifactContent = null;
			if (content.getInputStream() == null) {
				artifactContent = new StringInputStream(content.getContent());
			} else {
				artifactContent = content.getInputStream();
			}
			s3Client.putObject(s3BucketName, artifactS3Key, artifactContent, metaData);
			s3Client.setObjectAcl(s3BucketName, artifactS3Key, CannedAccessControlList.PublicRead);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sourceLocation;
	}

	@Override
	public Content[] getContents() {
		Logger.getLogger("WebsiteGenerator").info("Retrieving all contents.");

		ArrayList<Content> contents = new ArrayList<Content>();
		String selectExpression = "SELECT * FROM `" + simpleDbDomain
				+ "` WHERE `Source Location` is not null ORDER BY `Source Location` ASC LIMIT 2500";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setConsistentRead(true);
		for (Item item : simpleDbClient.select(selectRequest).getItems()) {
			Content content = new FileContent();
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Title")) {
					content.setTitle(attribute.getValue());
				} else if (attribute.getName().equals("Source Location")) {
					content.setSourceLocation(attribute.getValue());
				} else if (attribute.getName().equals("Type")) {
					content.setType(attribute.getValue());
				} else if (attribute.getName().equals("Mime Type")) {
					content.setMimeType(attribute.getValue());
				} else if (attribute.getName().equals("Template")) {
					content.setTemplateName(attribute.getValue());
				} else {
					content.addCustomAttribute(attribute.getName(), attribute.getValue());
				}
			}
			content.setFileName(item.getName());
			contents.add(content);
		}

		Logger.getLogger("WebsiteGenerator").info("Retrieved " + contents.size() + " content items.");

		return contents.toArray(new Content[contents.size()]);
	}

	@Override
	public void deleteContent(String key) {
		simpleDbClient.deleteAttributes(new DeleteAttributesRequest(simpleDbDomain, key));
		s3Client.deleteObject(s3BucketName, key);
	}
}
