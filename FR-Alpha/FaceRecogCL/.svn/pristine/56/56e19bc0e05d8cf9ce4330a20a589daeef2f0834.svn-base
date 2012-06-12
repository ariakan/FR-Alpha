package jrc.ipsc.surcit.facerecogcl;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;

public class IdApp extends Application 
{
	private JSONObject jsonHEADER;
	private static String wsUrl = "http://139.191.141.132:8080/FaceRecogWS";
	//private String nfcPicPath ="/mnt/sdcard/idtest/nfcFake.jpg";
	private static String camPicPath ="/mnt/sdcard/trainingImage/andrew0.jpg";
	private static String nfcPicPath ="";
	//private static String camPicPath ="";

	static public String getWSUrl()
	{
		return wsUrl;
	}
	static public void setWSUrl(String s)
	{
		wsUrl = s;
	}
	static public String getNFCPath()
	{
		return nfcPicPath;
	}
	static public void setNFCPath(String s)
	{
		nfcPicPath = s;
	}
	static public String getCamPath()
	{
		return camPicPath;
	}
	static public void setCamPath(String s)
	{
		camPicPath = s;
	}

	public void setJSONHeader ()
	{
		try {
			jsonHEADER.put("header", "header");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONObject getJSONHeader ()
	{
		return jsonHEADER;
	}
}