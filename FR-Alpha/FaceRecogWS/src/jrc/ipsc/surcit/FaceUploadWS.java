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

public class FaceUploadWS extends HttpServlet 
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
//		super.init(config);
//		tmpDir = new File(TMP_DIR_PATH);
//		if(!tmpDir.isDirectory()) {
//			throw new ServletException(TMP_DIR_PATH + " is not a directory");
//		}
		//String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
//		String realPath = DESTINATION_DIR_PATH;
//		destinationDir = new File(realPath);
//		if(!destinationDir.isDirectory()) {
//			throw new ServletException(DESTINATION_DIR_PATH+" is not a directory");
//		}
		
//		String trainingPath = TRAINING_DIR_PATH;
//		trainingDir = new File(trainingPath);
//		if(!trainingDir.isDirectory()) {
//			throw new ServletException(TRAINING_DIR_PATH+" is not a directory");
//		}
		initJSONHeader();
	}
	
	  public void initJSONHeader()
	  {
		  jsonHEADER = new JSONObject();
		  jsonHEADER.put("header", "header");
	  }
	
	  public JSONObject getJSONHeader ()
	  {
		  return jsonHEADER;
	  }
	  private static File makeDirectory(String dir)
	  // create a new directory or delete the contents of an existing one
	  {
	    File dirF = new File(dir);
	    if (dirF.isDirectory()) {
	      System.out.println("Directory: " + dir + " already exists;");
//	      for (File f : dirF.listFiles())
//	        deleteFile(f);
	    }
	    else {
	      dirF.mkdir();
	      System.out.println("Created new directory: " + dir);
	    }
	    
	   return dirF;
	  }  // end of makeDirectory()

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		tmpDir = makeDirectory(TMP_DIR_PATH);
		trainingDir = makeDirectory(TRAINING_DIR_PATH);
		destinationDir = makeDirectory(DESTINATION_DIR_PATH);
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		//out.println("<h1>Servlet File Upload Example using Commons File Upload</h1>");
		//out.println();

		DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
		/*
		 *Set the size threshold, above which content will be stored on disk.
		 */
		fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
		/*
		 * Set the temporary directory to store the uploaded files of size above threshold.
		 */
		fileItemFactory.setRepository(tmpDir);
		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try {
			/*
			 * Parse the request
			 */
			List items = uploadHandler.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();


				String filename = new File(item.getName()).getName();
				/*
				 * Handle Form Fields.
				 */
				if(item.isFormField()) {
					out.println("File Name = "+item.getFieldName()+", Value = "+item.getString());
				} else {
					//Handle Uploaded files.

					/*
					 * Write file to the ultimate location.
					 */
					//String jsonString = new String();
					//System.out.println(item.getFieldName());
					//jsonString = item.getFieldName().replaceAll("\\\"", "\"");
					//System.out.println(jsonString);

					JSONObject jsonObj = (JSONObject) JSONValue.parse(item.getFieldName());
//					jsonObj.put("StoredFileName", filename);
					//jsonObj.put("JSON",jsonString);
					//System.out.println(jsonObj.toString());

					File file = new File(tmpDir,filename);					  
					item.write(file);
					ResizeImage.doResizeImage(TEST_DIR_PATH+"\\"+filename,DESTINATION_DIR_PATH+"\\"+filename);
					out.println(isTrainingOrTest(jsonObj).toJSONString());
				}
//				out.close();
			}
		}catch(FileUploadException ex) {
			log("Error encountered while parsing the request",ex);
		} catch(Exception ex) {
			log("Error encountered while uploading file",ex);
		}
		
		
		out.close();
	}


	@SuppressWarnings("unchecked")
	private JSONObject isTrainingOrTest(JSONObject json)
	{
		JSONObject returnValue = new JSONObject();
		JSONObject jsonERROR = new JSONObject(); 
		JSONObject jsonResponse = new JSONObject(); 
		JSONParser parser = new JSONParser();
		 
		try
		{
			Map<String,Object> jsonMap = new HashMap<String,Object>();
			jsonMap = (Map<String, Object>) parser.parse(json.toString());
			json.putAll(jsonMap);
			if (jsonMap.containsKey("uploadForDb"))
			{
				
				java.util.Date dNow = new java.util.Date();
				java.text.SimpleDateFormat ft = new java.text.SimpleDateFormat ("yyyy_MM_dd'-'hh_mm_ss");
				HashMap queryMap = (HashMap) jsonMap.get("uploadForDb");
				String jpegname = queryMap.get("name").toString()+ queryMap.get("surname").toString()+ft.format(dNow)+FILE_EXT;
				
				copyFileToTrainingDir(queryMap.get("jpegname").toString(),jpegname);	
				
				queryMap.put("StoredFileName", jpegname);
				System.out.println(queryMap.toString());
				
				addFileIntoDB(queryMap);
				
				refreshTrainingSet();
				jsonResponse.put("data",queryMap);
				jsonResponse.put("status","add");
				returnValue.put("result",jsonResponse);				
				jsonERROR.put("ERROR", "1");
			}
			else
			{
				//{"uploadForCompare":{"jpegname":"\/sdcard\/DCIM\/snapface\/2012-05-14 11.38.57.jpg"}}
				Map map = (Map) jsonMap.get("uploadForCompare");
				map.get("StoredFileName");
				File f = new File ((String) map.get("jpegname"));
				String[] recogs = {f.getName()};
				JSONObject jsonFace = FaceRecognition.mainRecog(recogs);
				returnValue.put("result",jsonFace.get("result"));				
				returnValue.put("error",jsonFace.get("error"));
				
			}
		}
		catch (ParseException pe)
		{	
			jsonERROR.put("ERROR", "-4");
			jsonERROR.put("ERRORMessage", pe.toString());
			System.out.println(pe);
		}
		catch (Exception e)
		{
			jsonERROR.put("ERROR", "-3");
			jsonERROR.put("ERRORMessage", e.toString());
			
			e.printStackTrace();
		}
		returnValue.put("error", jsonERROR);
		returnValue.put("header", getJSONHeader());
		
		return returnValue;

	}

	private int copyFileToTrainingDir(String fileName, String destinationName)
	{
		int returnValue = 0;
						  
		try {
			File file = new File(DESTINATION_DIR_PATH+"\\"+fileName);
			File dest = new File(TRAINING_DIR_PATH+"\\"+destinationName);	
			FileUtils.copyFile(file, dest);
//			if (!file.renameTo(new File(dest, fileName))) {
//			    System.out.println("Not moved");
//			}
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return returnValue;
	}
	
	private int addFileIntoDB(HashMap<String,String> queryMap) throws Exception
	{
		int returnValue = 0;
		DBUtils.addNewFace(queryMap);
		return returnValue;
	}
	
	private int refreshTrainingSet()
	{
		int returnValue = 0;
		String[] args = {"build"};
		//BuildEigenFaces.mainBuild(args);
	
		return returnValue;
	}
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {

		String[] args = {"build"};
		BuildEigenFaces.mainBuild(args);
		PrintWriter out = response.getWriter();
		out.println("<h1>eigenfaces created</h1>");
		System.out.println("eigenfaces created");
	} 

}