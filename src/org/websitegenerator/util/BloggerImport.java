package org.websitegenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.websitegenerator.content.staticwebpage.StaticWebPageContent;
import org.websitegenerator.contentstore.simpledbs3.SimpleDBS3ContentStore;
import org.websitegenerator.core.model.Content;
import org.websitegenerator.core.model.ContentStore;
import org.xml.sax.SAXException;

/**
 * Import utility for blogger.com Atom-XML export files.
 * 
 * @author thobach
 * 
 */
public class BloggerImport {

	/**
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		ContentStore contentStore = new SimpleDBS3ContentStore();

		// parse document
		String exportFileURI = "/Users/thobach/Downloads/blog-02-08-2013-2.xml";
		String blogUrl = "http://fotoblog.thobach.de";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(exportFileURI));

		// iterate over blog entries and comments
		ArrayList<Content> entiresAndComments = new ArrayList<Content>();
		NodeList entries = document.getElementsByTagName("entry");
		for (int entryNumber = 0; entryNumber < entries.getLength(); entryNumber++) {
			// save entries and comments as content objects
			Content content = new StaticWebPageContent();
			Node entry = entries.item(entryNumber);
			NodeList entryNodes = entry.getChildNodes();
			for (int entryNodeNumber = 0; entryNodeNumber < entryNodes.getLength(); entryNodeNumber++) {
				Node entryNode = entryNodes.item(entryNodeNumber);
				String nodeName = entryNode.getNodeName();
				// skip templates and settings
				if (nodeName.equals("category")
						&& (entryNode.getAttributes().getNamedItem("term").getTextContent()
								.equals("http://schemas.google.com/blogger/2008/kind#template") || entryNode
								.getAttributes().getNamedItem("term").getTextContent()
								.equals("http://schemas.google.com/blogger/2008/kind#settings"))) {
					// ignore settings and templates
					content = null;
					break;
				} else if (nodeName.equals("category")
						&& entryNode.getAttributes().getNamedItem("term").getTextContent()
								.equals("http://schemas.google.com/blogger/2008/kind#post")) {
					content.setTemplateName("fotoblog-post-template");
				} else if (nodeName.equals("category")
						&& entryNode.getAttributes().getNamedItem("term").getTextContent()
								.equals("http://schemas.google.com/blogger/2008/kind#comment")) {
					// ignore comments for now
					content.setTemplateName("fotoblog-post-comment-template");
					content = null;
					break;
				} else if (nodeName.equals("id")) {
					content.addCustomAttribute("id", entryNode.getTextContent());
				} else if (nodeName.equals("published")) {
					content.addCustomAttribute("published", entryNode.getTextContent());
				} else if (nodeName.equals("updated")) {
					content.addCustomAttribute("updated", entryNode.getTextContent());
				} else if (nodeName.equals("category")
						&& entryNode.getAttributes().getNamedItem("scheme").getTextContent()
								.equals("http://www.blogger.com/atom/ns#")) {
					String newTag = entryNode.getAttributes().getNamedItem("term").getTextContent();
					if (content.getCustomAttribute("category") == null) {
						content.addCustomAttribute("category", newTag);
					} else {
						content.addCustomAttribute("category", content.getCustomAttribute("category") + ", " + newTag);
					}
				} else if (nodeName.equals("title")) {
					content.setTitle(entryNode.getTextContent());
				} else if (nodeName.equals("content")) {
					content.setContent(entryNode.getTextContent());
				} else if (nodeName.equals("link")
						&& entryNode.getAttributes().getNamedItem("rel").getTextContent().equals("alternate")) {
					String href = entryNode.getAttributes().getNamedItem("href").getTextContent();
					String fileName = href.replaceAll("^" + blogUrl + "/(.*?)$", "$1");
					content.setFileName(fileName);
				} else if (nodeName.equals("thr:in-reply-to")) {
					content.addCustomAttribute("inReplyToUrl", entryNode.getAttributes().getNamedItem("href")
							.getTextContent());
					content.addCustomAttribute("inReplyToId", entryNode.getAttributes().getNamedItem("ref")
							.getTextContent());
				} else if (nodeName.equals("author")) {
					NodeList authorNodes = entryNode.getChildNodes();
					for (int authorNodeNumber = 0; authorNodeNumber < authorNodes.getLength(); authorNodeNumber++) {
						Node authorNode = authorNodes.item(authorNodeNumber);
						if (authorNode.getNodeName().equals("name")) {
							content.addCustomAttribute("authorName", authorNode.getTextContent());
						} else if (authorNode.getNodeName().equals("uri")) {
							content.addCustomAttribute("authorUri", authorNode.getTextContent());
						} else if (authorNode.getNodeName().equals("email")) {
							content.addCustomAttribute("authorEmail", authorNode.getTextContent());
						}
					}
				}
				content.addCustomAttribute("blogUrl", blogUrl);
			}
			if (content != null && content.getFileName() != null) {
				contentStore.addContent(content);
				entiresAndComments.add(content);
			}
		}

		System.out.println(entiresAndComments);
	}
}
