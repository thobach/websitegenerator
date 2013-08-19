package org.websitegenerator.target.s3;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Servlet implementation class RetrieveContent
 */
@WebServlet("/RetrieveContent")
public class RetrieveContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AmazonS3Client s3Client;
	private String s3BucketName;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RetrieveContent() {
		AWSCredentials credentials = null;
		try {
			credentials = new PropertiesCredentials(
					AWSS3Target.class
							.getResourceAsStream("/websitegenerator.properties"));
			Properties properties = new Properties();
			properties.load(AWSS3Target.class
					.getResourceAsStream("/websitegenerator.properties"));
			s3BucketName = properties.getProperty("s3BucketTemplate");
		} catch (IOException e) {
			System.out
					.println("Credentials were not properly entered into websitegenerator.properties.");
			e.printStackTrace();
			System.exit(-1);
		}
		s3Client = new AmazonS3Client(credentials);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String id = request.getParameter("id");
		String type = request.getParameter("type");

		// set header
		if (type.equals("template")) {
			response.setHeader("Content-Type", "text/plain; charset=UTF-8");
		} else if (type.equals("html")) {
			response.setHeader("Content-Type", "text/html; charset=UTF-8");
		}

		S3Object s3Object = s3Client.getObject(s3BucketName, id);
		byte[] buffer = new byte[10240];
		int bytesRead;
		while ((bytesRead = s3Object.getObjectContent().read(buffer)) > -1) {
			response.getOutputStream().write(buffer, 0, bytesRead);
		}
		response.getOutputStream().close();
		s3Object.getObjectContent().close();
	}

}
