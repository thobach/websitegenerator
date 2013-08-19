<%@page import="java.util.Comparator"%>
<%@page import="java.util.Arrays"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page
	import="org.websitegenerator.contentstore.simpledbs3.SimpleDBS3ContentStore"%>
<%@ page import="org.websitegenerator.core.model.Content"%>
<%@ page import="org.websitegenerator.core.model.ContentStore"%>
<%
	ContentStore contentStore = new SimpleDBS3ContentStore();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WebsiteGenerator (beta)</title>
<script type="text/javascript" src="js/tiny_mce/tiny_mce.js"></script>
<script type="text/javascript">
	var iFrame = null;
	var currentType = null;
	function init(){
		iFrame = document.getElementById('iframe');
	}
	function updateForm(title, fileName, content, type, templateName) {
		currentType = type;
		if(type == 'File'){
			document.getElementById('fileNameFile').value = fileName;
		} else if(type == 'HTML'){
			document.getElementById('titleHtml').value = title;
			document.getElementById('fileNameHtml').value = fileName;
			document.getElementById('templateNameHtml').value = templateName;
			iFrame.src='/RetrieveContent?id=' + fileName + '&type=html';
		} else if(type == 'Template'){
			document.getElementById('titleTemplate').value = title;
			document.getElementById('fileNameTemplate').value = fileName;
			document.getElementById('templateNameTemplate').value = templateName;
			iFrame.src='/RetrieveContent?id=' + fileName + '&type=template';
		}		
	}
	function setContent(){
		if(iFrame != null) {
			if(currentType == 'HTML'){
				tinyMCE.activeEditor.setContent(iFrame.contentWindow.document.body.innerHTML);
			} else if (currentType == 'Template'){
				document.getElementById('contentTemplate').value = iFrame.contentWindow.document.body.innerText; //iFrame.contentWindow.document.documentElement.innerHTML;
			}
		}
	}
	tinyMCE.init({
		// General options
		mode : "exact",
	    elements : "content",
		theme : "advanced",
		plugins : "autolink,lists,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template",
		verify_html : false,
		document_base_url : "http://websitegenerator-templates.s3-website-eu-west-1.amazonaws.com/",
        relative_urls : false,

		// Theme options
		theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
		theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
		theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_statusbar_location : "bottom",
		theme_advanced_resizing : true,

		// Skin options
		skin : "o2k7",
		skin_variant : "silver",

		// Example content CSS (should be your site CSS)
		content_css : "css/bootstrap.css",

		// Drop lists for link/image/media/template dialogs
		template_external_list_url : "js/template_list.js",
		external_link_list_url : "js/link_list.js",
		external_image_list_url : "js/image_list.js",
		media_external_list_url : "js/media_list.js"
	});
</script>
<link href="css/bootstrap.min.css" rel="stylesheet" />
<style type="text/css">
body {
	padding-bottom: 40px;
}

@media ( min-width : 768px) {
	body {
		padding-top: 40px;
	}
}
</style>
</head>
<body onload="init();">
	<%
		Content[] contents = contentStore.getContents();
	%>
	<header>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="index.jsp">Website Generator</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li id="homepageLink"><a href="index.jsp"><i
								class="icon-home icon-white"></i></a></li>
						<li class="divider-vertical"></li>
						<li><form action="/generator" method="post"
								class="navbar-form pull-right form-inline">
								<select name="content" class="span2">
									<option value="">- everything -</option>
									<%
										Content[] sortedContents = contents;
										Arrays.sort(sortedContents, new Comparator<Content>() {
											@Override
											public int compare(Content o1, Content o2) {
												// descending
												return o2.getFileName().compareTo(o1.getFileName());
											}
										});
										for (Content content : sortedContents) {
											if (content.getType().equals("HTML")) {
									%>
									<option value="<%=content.getFileName()%>"><%=content.getFileName()%></option>
									<%
										}
										}
									%>
								</select> <select name="target" class="span1">
									<option value="disk">Disk</option>
									<option value="s3">S3</option>
								</select> <input type="submit" name="publish" value="Publish"
									class="btn btn-primary" /> <input type="submit" name="clean"
									value="Clean" class="btn btn-danger" /> <input type="submit"
									name="invalidate" value="Invalidate" class="btn" />
							</form></li>
					</ul>
					<form class="navbar-search pull-right" action="#">
						<input type="text" class="search-query" placeholder="Search"
							name="q">
					</form>
				</div>
				<!--/.nav-collapse -->
			</div>
			<!--/.container -->
		</div>
		<!--/.navbar-inner -->
	</div>
	<!--/.navbar --> </header>
	<div class="container">
		<section style="float: left; margin: 1em;"> Templates:
		<ul>
			<%
				for (Content content : contents) {
					if (content.getType().equals("Template")) {
			%>
			<li style="float: left; margin-right: 2em;"><a
				href="#<%=content.getType()%>"
				onclick="updateForm('<%=content.getTitle() != null ? content.getTitle()
							.replace("'", "\\\'").replace("\"", "&quot;") : ""%>', '<%=content.getFileName()%>', '<%=content.getSourceLocation()%>','<%=content.getType()%>','<%=content.getTemplateName()%>');"><%=content.getFileName()%></a>
				- <a
				href="https://s3-eu-west-1.amazonaws.com/websitegenerator/<%=content.getFileName()%>">view</a></li>
			<%
				}
				}
			%>
		</ul>
		</section>
		<section style="float: left; margin: 1em;"> HTML Contents:
		<ul>
			<%
				for (Content content : contents) {
					if (content.getType().equals("HTML")) {
			%>
			<li style="float: left; margin-right: 2em;"><a
				href="#<%=content.getType()%>"
				onclick="updateForm('<%=content.getTitle() != null ? content.getTitle()
							.replace("'", "\\\'").replace("\"", "&quot;") : ""%>', '<%=content.getFileName()%>', '<%=content.getSourceLocation()%>','<%=content.getType()%>','<%=content.getTemplateName()%>');"><%=content.getFileName()%></a>
				- <a
				href="https://s3-eu-west-1.amazonaws.com/websitegenerator/<%=content.getFileName()%>">view</a></li>
			<%
				}
				}
			%>
		</ul>
		</section>
		<section style="float: left; margin: 1em;"> File Contents:
		<ul>
			<li style="clear: both;">Images
				<ul>
					<%
						for (Content content : contents) {
							if (content.getType().equals("File")
									&& content.getFileName().startsWith("img/")) {
					%>
					<li style="float: left; margin-right: 2em;"><a
						href="#<%=content.getType()%>"
						onclick="updateForm('<%=content.getTitle() != null ? content.getTitle()
							.replace("'", "\\\'").replace("\"", "&quot;") : ""%>', '<%=content.getFileName()%>', 'see file','<%=content.getType()%>','<%=content.getTemplateName()%>');"><%=content.getFileName()%></a>
						- <a
						href="https://s3-eu-west-1.amazonaws.com/websitegenerator/<%=content.getFileName()%>">view</a></li>
					<%
						}
						}
					%>
				</ul>
			</li>
			<li style="clear: both;">CSS
				<ul>
					<%
						for (Content content : contents) {
							if (content.getType().equals("File")
									&& content.getFileName().startsWith("css/")) {
					%>
					<li style="float: left; margin-right: 2em;"><a
						href="#<%=content.getType()%>"
						onclick="updateForm('<%=content.getTitle() != null ? content.getTitle()
							.replace("'", "\\\'").replace("\"", "&quot;") : ""%>', '<%=content.getFileName()%>', '<%if (content.getType().equals("File")) {
						out.print("see file");
					} else {
						out.print(content.getSourceLocation());
					}%>','<%=content.getType()%>','<%=content.getTemplateName()%>');"><%=content.getFileName()%></a>
						- <a
						href="https://s3-eu-west-1.amazonaws.com/websitegenerator/<%=content.getFileName()%>">view</a></li>
					<%
						}
						}
					%>
				</ul>
			</li>
			<li style="clear: both;">JS
				<ul>
					<%
						for (Content content : contents) {
							if (content.getType().equals("File")
									&& content.getFileName().startsWith("js/")) {
					%>
					<li style="float: left; margin-right: 2em;"><a
						href="#<%=content.getType()%>"
						onclick="updateForm('<%=content.getTitle() != null ? content.getTitle()
							.replace("'", "\\\'").replace("\"", "&quot;") : ""%>', '<%=content.getFileName()%>', '<%if (content.getType().equals("File")) {
						out.print("see file");
					} else {
						out.print(content.getSourceLocation());
					}%>','<%=content.getType()%>');"><%=content.getFileName()%></a>
						- <a
						href="https://s3-eu-west-1.amazonaws.com/websitegenerator/<%=content.getFileName()%>">view</a></li>
					<%
						}
						}
					%>
				</ul>
			</li>
		</ul>
		</section>
		<section style="float: left; margin: 1em;">
		<form action="/generator" method="post" enctype="multipart/form-data"
			class="form-horizontal">
			<fieldset>
				<legend id="HTML">Content (HTML Site)</legend>
				<div class="control-group">
					<label class="control-label" for="titleHtml">Title: </label>
					<div class="controls">
						<input name="title" id="titleHtml" class="input-xlarge" />
						<p class="help-block">The title of your page.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="fileNameHtml">File Name:
					</label>
					<div class="controls">
						<input name="fileName" id="fileNameHtml" class="input-xlarge" />
						<p class="help-block">The target file name of your page or
							uploaded file.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="templateNameHtml">Template
						File Name: </label>
					<div class="controls">
						<input name="templateName" id="templateNameHtml"
							class="input-xlarge" />
						<p class="help-block">The file name of the template in which
							this page should be rendered in.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="content">Content: </label>
					<div class="controls">
						<textarea name="content" id="content"
							style="width: 800px; height: 1200px;"></textarea>
					</div>
				</div>
				<input type="hidden" value="HTML" name="type" /> <input
					type="hidden" value="text/html" name="mimeType" />
				<div class="form-actions">
					<input type="submit" name="save" value="Save"
						class="btn btn-primary" /> <input type="submit" name="delete"
						value="Delete" class="btn btn-danger" />
				</div>
			</fieldset>
		</form>
		</section>
		<section style="float: left; margin: 1em;">
		<form action="/generator" method="post" enctype="multipart/form-data"
			class="form-horizontal">
			<fieldset>
				<legend id="Template">Template</legend>
				<div class="control-group">
					<label class="control-label" for="titleTemplate">Title: </label>
					<div class="controls">
						<input name="title" id="titleTemplate" class="input-xlarge" />
						<p class="help-block">The title of your template.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="fileNameTemplate">File
						Name: </label>
					<div class="controls">
						<input name="fileName" id="fileNameTemplate" class="input-xlarge" />
						<p class="help-block">The target file name of your template.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="templateNameTemplate">Parent
						Template File Name: </label>
					<div class="controls">
						<input name="templateName" id="templateNameTemplate"
							class="input-xlarge" />
						<p class="help-block">The file name of the template in which
							this template should be rendered in.</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="contentTemplate">Content:
					</label>
					<div class="controls">
						<textarea name="content" id="contentTemplate"
							style="width: 800px; height: 600px;"></textarea>
					</div>
				</div>
				<input type="hidden" value="Template" name="type" /> <input
					type="hidden" value="text/html" name="mimeType" />
				<div class="form-actions">
					<input type="submit" name="save" value="Save"
						class="btn btn-primary" /> <input type="submit" name="delete"
						value="Delete" class="btn btn-danger" />
				</div>
			</fieldset>
		</form>
		</section>
		<section style="float: left; margin: 1em;">
		<form action="/generator" method="post" enctype="multipart/form-data"
			class="form-horizontal">
			<fieldset>
				<legend id="File">File</legend>
				<div class="control-group">
					<label class="control-label" for="folderNameFile">Folder
						Name: </label>
					<div class="controls">
						<input name="folderName" id="folderNameFile" class="input-xlarge" />
						<p class="help-block">The folder for the uploaded file(s), the
							name stays the same, e.g. "img/" or "css/".</p>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="fileFile">File: </label>
					<div class="controls">
						<input name="file" id="fileFile" type="file" multiple="multiple" />
					</div>
				</div>
				<input type="hidden" name="type" value="File" /> <input
					type="hidden" name="fileName" id="fileNameFile" />
				<div class="form-actions">
					<input type="submit" name="save" value="Save"
						class="btn btn-primary" /> <input type="submit" name="delete"
						value="Delete" class="btn btn-danger" />
				</div>
			</fieldset>
		</form>
		</section>
		<iframe id="iframe" src="" style="display: none;"
			onload="setContent();"></iframe>
	</div>
	<script src="js/jquery-1.7.2.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
</body>
</html>