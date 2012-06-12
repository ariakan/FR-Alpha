package jrc.ipsc.surcit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.simple.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
/**
 * Servlet implementation class CommonsFileUploadServlet
 */

public class Recog extends HttpServlet {
	private static final String TMP_DIR_PATH = "d:\\tmp";
	private File tmpDir;
	private static final String DESTINATION_DIR_PATH ="d:\\files";
	private File destinationDir;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		tmpDir = new File(TMP_DIR_PATH);
		if(!tmpDir.isDirectory()) {
			throw new ServletException(TMP_DIR_PATH + " is not a directory");
		}
		//String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
		String realPath = DESTINATION_DIR_PATH;
		destinationDir = new File(realPath);
		if(!destinationDir.isDirectory()) {
			throw new ServletException(DESTINATION_DIR_PATH+" is not a directory");
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
		fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
		fileItemFactory.setRepository(tmpDir);
		
		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try {
			List items = uploadHandler.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				String filename = new File(item.getName()).getName();
				if(item.isFormField()) {
					out.println("File Name = "+item.getFieldName()+", Value = "+item.getString());
				} 
				else 
				{
				 JSONObject obj=new JSONObject();
					 
					  obj.put("Field Name",item.getFieldName());
					  obj.put("File Name",item.getName());
					  obj.put("Content type",item.getContentType());
					  obj.put("File Size",item.getSize());
					  
					  out.println(obj);
				
					  File file = new File(destinationDir,filename);
					item.write(file);
				}
				out.close();
			}
		}catch(FileUploadException ex) {
			log("Error encountered while parsing the request",ex);
		} catch(Exception ex) {
			log("Error encountered while uploading file",ex);
		}

	}


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
		PrintWriter out = response.getWriter();
		out.println("<h1>Servlet File Upload Example using Commons File Upload</h1>");
		System.out.println("<h1>Servlet File Upload Example using Commons File Upload</h1>");
		
		String[] args = {"build"};
		BuildEigenFaces.mainBuild(args);
		String[] recogs = {"test.png"};
		FaceRecognition.mainRecog(recogs );
	} 

}