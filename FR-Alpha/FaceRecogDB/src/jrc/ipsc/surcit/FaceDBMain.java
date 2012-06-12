package jrc.ipsc.surcit;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class FaceDBMain extends Activity implements OnClickListener
{

	private static final String TAG = "FaceDBMain";
	private static String camPicPath ="";
	private static String dbWS = "";
	private static String name = "";
	private static String surname = "";
	private static String personid = "";
	private static String wsUrl = "http://139.191.141.132:8080/FaceRecogWS";
	//  private static String wsUrl = "http://192.168.43.53:8080/FaceRecogWS/";
	//private static String camPicPath ="/mnt/sdcard/trainingImage/andrew0.jpg";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button cameraButton =  (Button) findViewById(R.id.camera);
		cameraButton.setOnClickListener(this);

		Button nfcButton = (Button) findViewById(R.id.comparebutton);
		nfcButton.setOnClickListener(this);
		
		Button controlButton =  (Button) findViewById(R.id.control);
		controlButton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
			// More items go here (if any) ...
		}
		return false;
	}

	public void onClick(View v) 
	{
		Log.v(TAG,"onClick");
		switch (v.getId())
		{
		case R.id.camera:
			Log.v(TAG, "Start SnapFace");
			Intent SnapFaceIntent = new Intent(this, SnapFaceActivity.class);
			startActivity(SnapFaceIntent);
			break;
		case R.id.comparebutton:
			Log.v(TAG, "Start ComparePics");
			Intent ComparePicsIntent = new Intent(this, UploadPics.class);
			startActivity(ComparePicsIntent);
			break;
		case R.id.control:
			Log.v(TAG, "Start ComparePics");
			Intent ControlIntent = new Intent(this, FaceControl.class);
			startActivity(ControlIntent);
			break;
		}
	}

	static public String getWSUrl()
	{
		return wsUrl;
	}
	static public void setWSUrl(String s)
	{
		wsUrl = s;
	}

	static public String getName()
	{
		return name;
	}
	static public void setName(String s)
	{
		name = s;
	}

	static public String getSurname()
	{
		return surname;
	}
	static public void setSurname(String s)
	{
		surname = s;
	}
	static public String getId()
	{
		return personid;
	}
	static public void setId(String s)
	{
		personid = s;
	}

	static public String getCamPath()
	{
		return camPicPath;
	}
	static public void setCamPath(String s)
	{
		camPicPath = s;
	}
}