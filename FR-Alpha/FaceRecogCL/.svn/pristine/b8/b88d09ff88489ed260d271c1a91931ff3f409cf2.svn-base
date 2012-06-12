package jrc.ipsc.surcit.facerecogcl;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;



public class SnapFaceActivity extends Activity {

    private static final String TAG = "SnapFace";

    private PreviewView camPreview_;
	private int fdetLevel_;
	private int appMode_;

	private boolean calledACTION_GET_CONTENT_ = false;
	private long startTimeMills_ = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
      
        
		Log.v(TAG,"onCreate");


		/* set Full Screen */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		/* set window with no title bar */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Restore preferences */
		SharedPreferences settings = getSharedPreferences(getString(R.string.SnapFacePreference), 0);
		appMode_ = 0;
		fdetLevel_ = 1;
//		appMode_ = settings.getInt(getString(R.string.menu_AppMode), 0);
//		fdetLevel_ = settings.getInt(getString(R.string.menu_Preferences), 1);

		//implicit intent receiver(GET_CONTENT will be invoked to add contact thumbnail)
        if(Intent.ACTION_GET_CONTENT.equals(getIntent().getAction())) {
            Log.i(TAG,"implicit intent:ACTION_GET_CONTENT");
            calledACTION_GET_CONTENT_ = true;
            appMode_ = 0;
        }
		
		/* create camera view */
		camPreview_ = new PreviewView(this,calledACTION_GET_CONTENT_);
		camPreview_.setAppMode(appMode_);
//		camPreview_.setfdetLevel(fdetLevel_,true);
		setContentView(camPreview_);
		/* append Overlay */
		addContentView(camPreview_.getOverlay(), new LayoutParams 
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	/* onDestroy */
	@Override
	protected void onDestroy() {
		Log.v(TAG,"onDestroy");
		// Stop the tracker when it is no longer needed.
		long curTimeMills = System.currentTimeMillis();
		Date date = new Date(curTimeMills);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		super.onDestroy();
	}

	/* onStart */
	@Override
	protected void onStart() {
		Log.v(TAG,"onStart");
		startTimeMills_ = System.currentTimeMillis();
		Date date = new Date(startTimeMills_);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		super.onStart();
	}

	/* onPause */
	@Override
	protected void onPause() {
		Log.v(TAG,"onPause");
		long durationTimeMills = System.currentTimeMillis()-startTimeMills_;
		Date date = new Date(durationTimeMills);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");


		super.onPause();
	}

	/* create Menu */
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		Log.v(TAG,"onCreateOptionsMenu");
//		super.onCreateOptionsMenu(menu);
//		MenuItem menu_Preference = menu.add(0,R.id.menu_Preferences,0,R.string.menu_Preferences);
//		menu_Preference.setIcon(android.R.drawable.ic_menu_preferences);
//		MenuItem menu_AppMode = menu.add(0,R.id.menu_AppMode,0,R.string.menu_AppMode);
//		menu_AppMode.setIcon(android.R.drawable.ic_menu_manage);
//		return true;
//	}

	/* Menu handling */
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.v(TAG,"onOptionsItemSelected");
//		switch (item.getItemId()) {
//			case R.id.menu_Preferences:{
//				showDialog(R.id.PreferencesDlg);
//				break;
//			}
//			case R.id.menu_AppMode:{
//				showDialog(R.id.AppModeDlg);
//				break;
//			}
//		}
//		return true;
//	}

	/* onCreateDialog */
//	protected Dialog onCreateDialog(int id) {
//		Log.v(TAG,"onCreateDialog");
//		switch (id) {
//			// create progress dialog for search
//			// create map selection dialog and change modes
//			case R.id.PreferencesDlg: {
//				return new AlertDialog.Builder(this)
//				.setTitle(R.string.PreferencesDlgTitle)
//				.setSingleChoiceItems(R.array.select_fdetlevel, fdetLevel_, new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						fdetLevel_ = whichButton;
//						camPreview_.setfdetLevel(fdetLevel_,false);
//						SharedPreferences settings = getSharedPreferences(getString(R.string.SnapFacePreference), 0);
//						SharedPreferences.Editor editor = settings.edit();
//						editor.putInt(getString(R.string.menu_Preferences), whichButton);
//						editor.commit();
//						dismissDialog(R.id.PreferencesDlg);
//
//					}
//				})
//				.create();
//			}
//			case R.id.AppModeDlg: {
//				return new AlertDialog.Builder(this)
//				.setTitle(R.string.PreferencesDlgTitle)
//				.setSingleChoiceItems(R.array.select_appmode, appMode_, new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						camPreview_.setAppMode(whichButton);
//						SharedPreferences settings = getSharedPreferences(getString(R.string.SnapFacePreference), 0);
//						SharedPreferences.Editor editor = settings.edit();
//						editor.putInt(getString(R.string.menu_AppMode), whichButton);
//						editor.commit();
//						dismissDialog(R.id.AppModeDlg);
//
//					}
//				})
//				.create();
//			}
//		}
//		return null;
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG,"onKeyDown");
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
	        if(calledACTION_GET_CONTENT_){
	        	setResult(RESULT_CANCELED);

	        	finish();
	        }
		} 
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG,"onActivityResult");
		// TODO Auto-generated method stub
		if(calledACTION_GET_CONTENT_){
			if(resultCode == RESULT_OK && data != null ){
				Uri uri = data.getData();
				Intent intent = new Intent();
				intent.setData(uri);
				setResult(RESULT_OK,intent);
	
			}
        	setResult(resultCode);
        	finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

}

