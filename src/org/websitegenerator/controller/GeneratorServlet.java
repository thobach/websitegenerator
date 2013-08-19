package org.websitegenerator.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.websitegenerator.content.file.FileContent;
import org.websitegenerator.content.staticwebpage.StaticWebPageContent;
import org.websitegenerator.contentstore.simpledbs3.SimpleDBS3ContentStore;
import org.websitegenerator.core.model.Content;
import org.websitegenerator.core.model.ContentStore;
import org.websitegenerator.core.model.Target;
import org.websitegenerator.core.model.Template;
import org.websitegenerator.core.model.WebsiteGenerator;
import org.websitegenerator.target.disk.DiskTarget;
import org.websitegenerator.target.s3.AWSS3Target;
import org.websitegenerator.template.staticwebpage.StaticWebPageTemplate;

/**
 * Servlet implementation class GeneratorServlet
 */
@WebServlet("/generator")
@MultipartConfig
public class GeneratorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GeneratorServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		Target target = null;
		if (request.getParameter("target") != null && request.getParameter("target").equals("s3")) {
			target = new AWSS3Target();
		} else if (request.getParameter("target") != null && request.getParameter("target").equals("disk")) {
			target = new DiskTarget();
		}
		String contentToPublish = null;
		if (request.getParameter("content") == null || request.getParameter("content").isEmpty()) {
			contentToPublish = "";
		} else if (request.getParameter("content") != null && !request.getParameter("content").isEmpty()) {
			contentToPublish = request.getParameter("content");
		}

		ContentStore contentStore = new SimpleDBS3ContentStore();
		WebsiteGenerator websiteGenerator = new WebsiteGenerator(target);

		if (request.getParameter("publish") != null) {
			// generate new artifacts from content and publish them
			Content[] contents = contentStore.getContents();
			if (contentToPublish != null && !contentToPublish.isEmpty()) {
				for (Content content : contents) {
					if (content.getFileName().equals(contentToPublish)) {
						contents = new Content[] { content };
						break;
					}
				}
			}
			websiteGenerator.generate(contents);
		} else if (request.getParameter("clean") != null) {
			target.cleanup();
		} else if (request.getParameter("invalidate") != null) {
			target.invalidateCDN();
		} else if (request.getPart("save") != null) {

			String type = IOUtils.toString(request.getPart("type").getInputStream());
			if (type.equals("File")) {
				for (Part part : request.getParts()) {
					if (part.getName().equals("file")) {
						Content content = new FileContent();
						content.setContent(part.getInputStream());
						content.setMimeType(part.getContentType());
						content.setFileName(IOUtils.toString(request.getPart("folderName").getInputStream(), "UTF-8")
								+ getFileName(part));
						content.setType(type);
						// store new content
						contentStore.addContent(content);
					}
				}
			} else if (type.equals("HTML")) {
				Content content = new StaticWebPageContent();
				content.setTitle(IOUtils.toString(request.getPart("title").getInputStream(), "UTF-8"));
				content.setContent(IOUtils.toString(request.getPart("content").getInputStream(), "UTF-8"));
				content.setFileName(IOUtils.toString(request.getPart("fileName").getInputStream(), "UTF-8"));
				content.setType(type);
				content.setMimeType(IOUtils.toString(request.getPart("mimeType").getInputStream(), "UTF-8"));
				content.setTemplateName(IOUtils.toString(request.getPart("templateName").getInputStream(), "UTF-8"));
				// store new content
				contentStore.addContent(content);
			} else if (type.equals("Template")) {
				Template template = new StaticWebPageTemplate();
				template.setTitle(IOUtils.toString(request.getPart("title").getInputStream(), "UTF-8"));
				template.setContent(IOUtils.toString(request.getPart("content").getInputStream(), "UTF-8"));
				template.setFileName(IOUtils.toString(request.getPart("fileName").getInputStream(), "UTF-8"));
				template.setType(type);
				template.setMimeType(IOUtils.toString(request.getPart("mimeType").getInputStream(), "UTF-8"));
				template.setTemplateName(IOUtils.toString(request.getPart("templateName").getInputStream(), "UTF-8"));
				// store new template
				contentStore.addContent(template);
			}

		} else if (request.getPart("delete") != null) {
			contentStore.deleteContent(IOUtils.toString(request.getPart("fileName").getInputStream(), "UTF-8"));
		}

		response.sendRedirect("index.jsp");
	}

	private String getFileName(Part part) {
		for (String contentDisposition : part.getHeader("content-disposition").split(";")) {
			if (contentDisposition.trim().startsWith("filename")) {
				return contentDisposition.substring(contentDisposition.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;

	}
}
