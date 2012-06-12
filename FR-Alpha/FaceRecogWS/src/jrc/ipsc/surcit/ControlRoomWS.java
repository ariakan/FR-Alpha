package jrc.ipsc.surcit;

//package us.souther.simple;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.PrintWriter;
//import java.io.IOException;
//
//public class SimpleServlet extends HttpServlet{
//
// public void doGet(HttpServletRequest request,
//                   HttpServletResponse response)
//		    throws ServletException, IOException{
//
//	response.setContentType("text/html");
//	PrintWriter out = response.getWriter();
//
//	out.println("<html>");
//	out.println("  <head>");
//	out.println("    <title>SimpleServlet</title>");
//	out.println("  </head>");
//	out.println("  <body>");
//	out.println("    Hello, World");
//	out.println("  </body>");
//	out.println("</html>");
// }
//}


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
/**
 * Servlet implementation class CommonsFileUploadServlet
 */

public class ControlRoomWS extends HttpServlet 
{
	private JSONObject jsonHEADER;
	private static final String FILE_EXT = ".jpg";
	private static final String TMP_DIR_PATH = "facerecog\\tmp";
	private File tmpDir;
	private static final String DESTINATION_DIR_PATH ="facerecog\\files";
	private File destinationDir;
	private static final String TRAINING_DIR_PATH ="facerecog\\trainingImages";
	private File trainingDir;
	private static final String TEST_DIR_PATH ="facerecog\\tmp";
	private File testDir;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

	
	}
	

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
		String jsonText = "";
		try {
			 jsonText = JSONValue.toJSONString(DBUtils.getFaceForControl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.println(jsonText);

	} 

}