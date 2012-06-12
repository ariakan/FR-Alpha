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

public class DBWS extends HttpServlet {


	public void init(ServletConfig config) throws ServletException {
		super.init(config);


	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		

	}


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException 
	{
		PrintWriter out = response.getWriter();
		try {
			out.print(DBUtils.doFaceQuery("zio.pg"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} 

}