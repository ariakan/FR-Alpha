package jrc.ipsc.surcit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class DBUtils {


	private static Connection conn;
	private static Statement stmt;
	private static ResultSet rset;
	private static String tableFACE = "namefaces";
	private static String connectionDriver = "jdbc:mysql://localhost:3306/faceinfo";
	private static String dbUser = "faceinfo";
	private static String dbPass = "faceinfo";

	public static void doConnect() throws Exception
	{
		try
		{
			Class.forName("org.gjt.mm.mysql.Driver");
		}
		catch (Exception exc)
		{
			System.out.println("Errore - Driver jdbc non presente: "+
					exc.getMessage());
		}

		try
		{

			conn = DriverManager.getConnection(
					connectionDriver,
					dbUser,
					dbPass
					);

		}	 
		//			 stmt = conn.createStatement();
		//			 rset = stmt.executeQuery("SELECT now();");
		//			while (rset.next())
		//			{
		//				System.out.println(rset.getString(1));
		//			}
		//
		//			rset.close();
		//			stmt.close();
		//			conn.close();
		//		}
		catch (Exception exc)
		{
			System.out.println("Errore: "+ exc.getMessage());
		}
	}

	//This method return the information about the person on "photoName" 
	//Query: "SELECT * FROM namefaces WHERE jpegname LIKE photoName"

	public static HashMap<String, ArrayList> getFaceForControl() throws Exception
	{
		HashMap<String, ArrayList> returnHashMap = new HashMap<String,ArrayList>();

		String query = "SELECT name, surname, date, personid, jpegname FROM "+tableFACE+" ORDER BY personid, date;";

		if (conn == null)
		{
			doConnect();
		}
		stmt = conn.createStatement();
		rset = stmt.executeQuery(query);
		while (rset.next())
		{
			if (returnHashMap.containsKey(rset.getString(4)))
			{			
				ArrayList<HashMap<String, String>> row = returnHashMap.get(rset.getString(4));
				//ArrayList <HashMap> row = new ArrayList<HashMap>();
				HashMap<String, String> entry = new HashMap<String,String>();

				entry.put("name", rset.getString(1));
				entry.put("surname", rset.getString(2));
				entry.put("date", rset.getString(3));
				entry.put("personid", rset.getString(4));
				entry.put("jpegname", rset.getString(5));

				row.add(entry);

				returnHashMap.put(rset.getString(4), row);
			}
			else
			{
				ArrayList<HashMap<String, String>> row = new ArrayList<HashMap<String, String>>();
				//ArrayList <HashMap> row = new ArrayList<HashMap>();
				HashMap<String, String> entry = new HashMap<String,String>();

				entry.put("name", rset.getString(1));
				entry.put("surname", rset.getString(2));
				entry.put("date", rset.getString(3));
				entry.put("personid", rset.getString(4));
				entry.put("jpegname", rset.getString(5));

				row.add(entry);

				returnHashMap.put(rset.getString(4), row);
			}
		}	
		return returnHashMap;
	}


	public static String doFaceQuery(String photoName) throws Exception
	{
		String returnString =""; 
		String query = "SELECT * FROM "+tableFACE+" WHERE jpegname LIKE '"+photoName+"';";
		ArrayList<String> keys = getColumns("namefaces");
		//		ArrayList<String> values = new ArrayList<String>();

		if (conn == null)
		{
			doConnect();
		}
		stmt = conn.createStatement();
		rset = stmt.executeQuery(query);
		JSONObject json = new JSONObject();
		while (rset.next())
		{
			for (int i = 1 ; i <= keys.size() ; i++)
			{
				json.put(keys.get(i-1), rset.getString(i));
			}
		}		
		returnString = json.toJSONString();
		return returnString;
	}

	private static ArrayList<String> getColumns(String tableName) throws Exception
	{
		ArrayList<String> columns = new ArrayList<String>();
		String query = "SHOW FIELDS FROM "+tableName+";";

		if (conn == null)
		{
			doConnect();
		}
		stmt = conn.createStatement();
		rset = stmt.executeQuery(query);
		while (rset.next())
		{
			columns.add(rset.getString(1));
		}		
		return columns;
	}

	public static String doQuery(String query) throws Exception
	{
		String returnString =""; 

		if (conn == null)
		{
			doConnect();
		}
		stmt = conn.createStatement();
		rset = stmt.executeQuery(query);
		JSONObject obj = new JSONObject();
		while (rset.next())
		{
			obj.put(rset.getString(1), rset.getString(1));
			//System.out.println(rset.getString(1));
		}
		//System.out.println(obj);
		returnString = obj.toString();
		closeConnection();
		return returnString;
	}

	public static int addNewFace(HashMap<String,String> queryMap) throws Exception
	{
		int returnValue =0; 

		if (conn == null)
		{
			doConnect();
		}
		stmt = conn.createStatement();

		String query = new String();
		System.out.println(queryMap.toString());		
		query = "INSERT INTO "+tableFACE+"(name, surname, personid, jpegname) ";
		query +="VALUES ('"+queryMap.get("name") +"', '"+ queryMap.get("surname") +"', '" +queryMap.get("personid")+"', '"+queryMap.get("StoredFileName")+"' )";
		System.out.println(query);
		returnValue = stmt.executeUpdate(query);
		rset = null;
		closeConnection();
		return returnValue;
	}

	private static void closeConnection() throws SQLException
	{
		//rset.close();
		stmt.close();
		conn.close();
		conn = null;
	}
}

