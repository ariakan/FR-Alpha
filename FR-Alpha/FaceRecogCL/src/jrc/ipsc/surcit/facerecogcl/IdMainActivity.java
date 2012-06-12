package jrc.ipsc.surcit.facerecogcl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class IdMainActivity extends Activity implements OnClickListener {
	private static String TAG = "IdMainActivity";
	Dialog loginDialog;
	String username = "user";
	String password = "user";
	TextView textUser;
	TextView textPass;
	private String EDIT_TEXT_WSPREF = "wsurl"; 
//	private static final int STOPSPLASH = 0;
//	//time in milliseconds
//	private static final long SPLASHTIME = 3000;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		loadPreferences();
		
		setContentView(R.layout.main);


		Button faceButton =  (Button) findViewById(R.id.face);
		faceButton.setOnClickListener(this);
		Button recogButton = (Button) findViewById(R.id.comparebutton);
		recogButton.setOnClickListener(this);

		
		
		
	}
	private void loadPreferences()
	{
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		IdApp.setWSUrl(PreferenceManager.getDefaultSharedPreferences(this).getString(EDIT_TEXT_WSPREF, ""));
		Log.v(TAG,IdApp.getWSUrl());
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
		case R.id.face:
			Intent intent1 = new Intent(this, SnapFaceActivity.class);  
			Log.v(TAG, "face");
			startActivity(intent1);
			break;
//		case R.id.finger:
//			AlertDialog fingerErrorDialog = new AlertDialog.Builder(IdMainActivity.this).create();
//			fingerErrorDialog.setTitle(R.string.fingererror);
//			fingerErrorDialog.setMessage(getResources().getString(R.string.fingererrormessage));
//			fingerErrorDialog.setCancelable(true);
//			fingerErrorDialog.show();
//			Intent intent2 = new Intent(this, FaceCapture.class); 
//			Log.v(TAG, "finger");
//			startActivity(intent2);
//			break;
		case R.id.comparebutton:
			Intent intent3 = new Intent(this, ComparePics.class);   
			startActivity(intent3);
			break;
//		case R.id.nfcbutton:
//			Intent intent4 = new Intent(this, ComparePics.class);   
//			Log.v(TAG, "Button4");
//			startActivity(intent4);
//			AlertDialog nfcErrorDialog = new AlertDialog.Builder(IdMainActivity.this).create();
//			nfcErrorDialog.setTitle(R.string.nfcerror);
//			nfcErrorDialog.setMessage(getResources().getString(R.string.nfcerrormessage));
//			nfcErrorDialog.setCancelable(true);
//			nfcErrorDialog.show();
//			break;
//		case R.id.loginbutton:
//			Log.v(TAG, "Login");
//			if (checkUser(textUser.getText().toString(),textPass.getText().toString()))
//			{
//				loginDialog.dismiss();
//			}
//			else
//			{
//				AlertDialog loginErrorDialog = new AlertDialog.Builder(IdMainActivity.this).create();
//				loginErrorDialog.setTitle(R.string.loginerror);
//				loginErrorDialog.setMessage(getResources().getString(R.string.loginerrormessage));
//				loginErrorDialog.setCancelable(true);
//				loginErrorDialog.show();
//			}
//			break;
		}
		
	}
	
	private boolean checkUser(String user, String pass)
	{
		if (user.compareTo(username) == 0 &&  pass.compareTo(password) == 0)
			return true;
		else
			return true;
	}
	
	public void onClicked(View v) 
	{
		

	}
}