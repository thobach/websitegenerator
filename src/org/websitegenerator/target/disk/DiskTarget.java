/**
 * 
 */
package org.websitegenerator.target.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.websitegenerator.core.model.AbstractTarget;
import org.websitegenerator.core.model.Artifact;

/**
 * @author thobach
 * 
 */
public class DiskTarget extends AbstractTarget {

	private String absoluteLocation;

	public DiskTarget() {

		try {
			Properties properties = new Properties();
			InputStream propertiesStream = DiskTarget.class
					.getResourceAsStream("/websitegenerator.properties");
			properties.load(propertiesStream);
			propertiesStream.close();
			absoluteLocation = properties.getProperty("diskLocation");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void add(Artifact artifact) {
		String fileName = absoluteLocation + "/" + artifact.getFileName();
		File file = new File(fileName);
		String folderName = file.getParent();
		File folder = new File(folderName);
		try {
			folder.mkdirs();
			file.createNewFile();
			OutputStream fileOutputStream = null;
			if (fileName.endsWith(".html") || fileName.endsWith(".css")
					|| fileName.endsWith(".js")) {
				fileOutputStream = new GZIPOutputStream(new FileOutputStream(
						file));
			} else {
				fileOutputStream = new FileOutputStream(file);
			}
			if (artifact.getInputStream() != null) {
				byte buf[] = new byte[1024];
				int len;
				while ((len = artifact.getInputStream().read(buf)) > 0) {
					fileOutputStream.write(buf, 0, len);
				}
				artifact.getInputStream().close();
			} else {
				byte[] bytes = artifact.getPayload().getBytes("UTF-8");
				fileOutputStream.write(bytes, 0, bytes.length);
			}
			if (fileOutputStream instanceof GZIPOutputStream) {
				((GZIPOutputStream) fileOutputStream).finish();
			}
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Logger.getLogger("WebsiteGenerator").info(
				"Published " + artifact.getFileName() + " to disk.");

	}

	@Override
	public void cleanup() {
		File dir = new File(absoluteLocation);
		String[] info = dir.list();
		for (int i = 0; i < info.length; i++) {
			File file = new File(absoluteLocation + File.separator + info[i]);
			file.delete();
		}
	}

	@Override
	public void init() {
		String htaccessFileName = absoluteLocation + "/" + ".htaccess";
		File htaccessFile = new File(htaccessFileName);
		String folderName = htaccessFile.getParent();
		File folder = new File(folderName);
		try {
			folder.mkdirs();
			htaccessFile.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(
					htaccessFile);
			fileOutputStream
					.write("AddEncoding x-gzip .html\nAddEncoding x-gzip .css\nAddEncoding x-gzip .js"
							.getBytes());
			fileOutputStream.close();
			Logger.getLogger("WebsiteGenerator")
					.info("Added "
							+ htaccessFileName
							+ " to disk to set encoding for html, css and js files.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
