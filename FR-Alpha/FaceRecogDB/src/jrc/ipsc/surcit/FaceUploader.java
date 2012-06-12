package jrc.ipsc.surcit;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
public class FaceUploader 
{
	private static String TAG = "FaceUploader";
	private static String wservice = "upload";

	public static JSONObject uploadFaceWithInfo(String sourceFileUri, JSONObject picInfo)
	{
		int serverResponseCode = 0;
		JSONObject serverResponseJson = new JSONObject();
		JSONObject serverResponseJsonERROR = new JSONObject();
		String upLoadServerUri = FaceDBMain.getWSUrl()+"/"+wservice;
		String fileName = new File(sourceFileUri).getName();

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		String responseFromServer = "";

		File sourceFile = new File(sourceFileUri);
		if (!sourceFile.isFile()) {
			try {
				Log.e(TAG, "Source File Does not exist");
				serverResponseJsonERROR.put("ERROR", "-1");
				serverResponseJsonERROR.put("ERRORmessage", "Source File Does not exist");
				serverResponseJson.put("error",serverResponseJsonERROR);
			} 

			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return serverResponseJson;
		}
		try { 
			// open a URL connection to the Servlet
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			//			conn.setRequestProperty("uploaded_file", fileName);
			//			conn.setRequestProperty(wservice, picInfo.toString());	

			dos = new DataOutputStream(conn.getOutputStream());

			Log.v(TAG,picInfo.toString());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\""+picInfo.toString()+"\";filename=\""+ fileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);


			bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
			Log.i(TAG, "Initial .available : " + bytesAvailable);

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(wservice+"="+picInfo.toString());
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.i("Upload file to server", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
			// close streams
			Log.i("Upload file to server", fileName + " File is written");
			fileInputStream.close();

			dos.flush();
			dos.close();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//this block will give the response of upload link
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				Log.i(TAG, "RES Message: " + line);		
				serverResponseJson.put(wservice, new JSONObject(line));
				serverResponseJsonERROR.put("ERROR", "0");
				serverResponseJsonERROR.put("ERRORMessage", "Face Stored");
				serverResponseJson.put("error",serverResponseJsonERROR);
			}
			rd.close();
		} catch (IOException ioex) {
			Log.e(TAG, "error: " + ioex.getMessage(), ioex);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serverResponseJson;  // like 200 (Ok)
	}



	//	public static JSONObject postData(String URL, JSONObject jsonObjSend)  
	//	{
	//		String resultString = "";
	//		// Create a new HttpClient and Post Header
	//		HttpClient httpclient = new DefaultHttpClient();
	//		HttpPost httppost = new HttpPost(URL);
	//		JSONObject jsonObjRecv = null;
	//		try {
	//			// Add your data
	//			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	//			nameValuePairs.add(new BasicNameValuePair("json", jsonObjSend.toString()));
	//
	//			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	//
	//			// Execute HTTP Post Request
	//			HttpResponse response = httpclient.execute(httppost);
	//			HttpEntity entity = response.getEntity();
	//			InputStream instream = entity.getContent();
	//			resultString= convertStreamToString(instream);
	//			instream.close();
	//
	//			Log.v(TAG, resultString);
	//
	//			jsonObjRecv = new JSONObject(resultString);
	//
	//		} catch (ClientProtocolException e) {
	//			e.printStackTrace();
	//
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//
	//		}
	//		catch (JSONException e) {			
	//			e.printStackTrace();
	//		}
	//		return  jsonObjRecv;
	//
	//	}

	public static int upLoad2Server(String sourceFileUri) 
	{
		int serverResponseCode = 0;
		String upLoadServerUri = "http://127.0.0.1:8080/FaceRecogWS/upload";
		//String upLoadServerUri = "http://139.191.141.132/upload.php";
		// String [] string = sourceFileUri;
		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		String responseFromServer = "";

		File sourceFile = new File(sourceFileUri);
		if (!sourceFile.isFile()) {
			Log.e(TAG, "Source File Does not exist");
			return 0;
		}
		try { // open a URL connection to the Servlet
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);
			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
			Log.i(TAG, "Initial .available : " + bytesAvailable);

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.i("Upload file to server", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
			// close streams
			Log.i("Upload file to server", fileName + " File is written");
			fileInputStream.close();
			dos.flush();
			dos.close();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//this block will give the response of upload link
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				Log.i(TAG, "RES Message: " + line);
			}
			rd.close();
		} catch (IOException ioex) {
			Log.e(TAG, "error: " + ioex.getMessage(), ioex);
		}
		return serverResponseCode;  // like 200 (Ok)

	} // end upLoad2Server

}










