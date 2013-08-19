/**
 * 
 */
package org.websitegenerator.target.s3;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.websitegenerator.core.model.AbstractTarget;
import org.websitegenerator.core.model.Artifact;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;

/**
 * @author thobach
 * 
 */
public class AWSS3Target extends AbstractTarget {

	private AmazonS3 s3Client;
	private String s3BucketName;
	private String cloudFrontDistributionId;

	private AmazonCloudFrontClient cloudFrontClient;

	public AWSS3Target() {

		AWSCredentials credentials = null;
		try {
			InputStream propertiesStream = AWSS3Target.class
					.getResourceAsStream("/websitegenerator.properties");
			credentials = new PropertiesCredentials(propertiesStream);
			propertiesStream.reset();
			Properties properties = new Properties();
			properties.load(propertiesStream);
			propertiesStream.close();
			s3BucketName = properties.getProperty("s3BucketProduction");
			cloudFrontDistributionId = properties
					.getProperty("cloudFrontDistributionId");
		} catch (IOException e) {
			System.out
					.println("Credentials were not properly entered into websitegenerator.properties.");
			e.printStackTrace();
			System.exit(-1);
		}
		s3Client = new AmazonS3Client(credentials);

		cloudFrontClient = new AmazonCloudFrontClient(credentials);
	}

	@Override
	public void add(Artifact artifact) {
		String artifactS3Key = artifact.getFileName();
		ObjectMetadata metaData = new ObjectMetadata();
		metaData.setContentType(artifact.getContentType());
		if (artifact.getFileName().endsWith(".html")
				|| artifact.getFileName().endsWith(".css")
				|| artifact.getFileName().endsWith(".js")) {
			metaData.setContentEncoding("gzip");
		}
		try {
			InputStream artifactContent = null;
			if (artifact.getPayload() != null) {
				if (artifact.getFileName().endsWith(".html")
						|| artifact.getFileName().endsWith(".css")
						|| artifact.getFileName().endsWith(".js")) {
					// gzip first
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					GZIPOutputStream gzipStream = new GZIPOutputStream(
							byteArrayOutputStream);
					byte[] bytes = artifact.getPayload().getBytes("UTF-8");
					gzipStream.write(bytes, 0, bytes.length);
					gzipStream.finish();
					gzipStream.close();

					byte[] gzipBytes = byteArrayOutputStream.toByteArray();
					byteArrayOutputStream.close();
					artifactContent = new ByteArrayInputStream(gzipBytes);
				} else {
					artifactContent = new StringInputStream(
							artifact.getPayload());
				}
			} else {
				if (artifact.getFileName().endsWith(".html")
						|| artifact.getFileName().endsWith(".css")
						|| artifact.getFileName().endsWith(".js")) {

					// gzip first
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					GZIPOutputStream gzipStream = new GZIPOutputStream(
							byteArrayOutputStream);

					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(artifact.getInputStream()));
					StringBuilder stringBuilder = new StringBuilder();
					String line = null;
					
					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line + "\n");
					}

					bufferedReader.close();

					byte[] bytes = stringBuilder.toString().getBytes("UTF-8");
					gzipStream.write(bytes, 0, bytes.length);
					gzipStream.finish();
					gzipStream.close();

					byte[] gzipBytes = byteArrayOutputStream.toByteArray();
					byteArrayOutputStream.close();
					artifactContent = new ByteArrayInputStream(gzipBytes);

				} else {
					artifactContent = artifact.getInputStream();
				}
			}
			s3Client.putObject(s3BucketName, artifactS3Key, artifactContent,
					metaData);
			s3Client.setObjectAcl(s3BucketName, artifactS3Key,
					CannedAccessControlList.PublicRead);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Logger.getLogger("WebsiteGenerator").info(
				"Published " + artifact.getFileName() + " to disk.");
	}

	@Override
	public void cleanup() {

		DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(
				s3BucketName);
		List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<DeleteObjectsRequest.KeyVersion>();

		// delete from S3 bucket
		ObjectListing objects = s3Client.listObjects(s3BucketName);
		if (objects.getObjectSummaries().size() > 0) {
			boolean moreResults = true;

			while (moreResults) {
				for (S3ObjectSummary summary : objects.getObjectSummaries()) {
					keys.add(new DeleteObjectsRequest.KeyVersion(summary
							.getKey()));
				}
				if (objects.isTruncated()) {
					objects = s3Client.listNextBatchOfObjects(objects);
				} else {
					moreResults = false;
				}
			}
			deleteObjectsRequest.setKeys(keys);
			s3Client.deleteObjects(deleteObjectsRequest);
		}

	}

	@Override
	public void invalidateCDN() {

		List<String> paths = new ArrayList<String>();

		// get files from from S3 bucket
		ObjectListing objects = s3Client.listObjects(s3BucketName);
		if (objects.getObjectSummaries().size() > 0) {
			boolean moreResults = true;
			while (moreResults) {
				for (S3ObjectSummary summary : objects.getObjectSummaries()) {
					paths.add("/" + summary.getKey());
				}
				if (objects.isTruncated()) {
					objects = s3Client.listNextBatchOfObjects(objects);
				} else {
					moreResults = false;
				}
			}
		}

		// delete from edge locations
		if (paths.size() > 0) {
			InvalidationBatch invalidationBatch = new InvalidationBatch(paths,
					"ref" + System.currentTimeMillis());
			CreateInvalidationRequest createInvalidationRequest = new CreateInvalidationRequest(
					cloudFrontDistributionId, invalidationBatch);
			try {
				cloudFrontClient.createInvalidation(createInvalidationRequest);
			} catch (AmazonServiceException e) {
				e.printStackTrace();
			}
		}
	}
}
