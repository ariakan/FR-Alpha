package jrc.ipsc.surcit;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class UploadPics extends Activity implements OnClickListener, OnEditorActionListener
{
	private static String TAG = "ComparePicsActivity"; 
	private static String wservice = "uploadForDb";
	private ImageButton imgViewFromCam;
	private Bitmap camPic;
	private boolean camData;
	private String camPicPath;

	private TextView camLabel;	
	private UploadPics self ;
	private Button remote;
	private EditText nameText;
	private EditText surnameText;
	private EditText idText;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;
		setContentView(R.layout.comparepics);
		imgViewFromCam = (ImageButton)findViewById(R.id.fromCamera);
		remote = (Button)findViewById(R.id.remotebutton);
		camLabel = (TextView)findViewById(R.id.camLabel);
		remote.setOnClickListener(this);
		imgViewFromCam.setOnClickListener(this);

		nameText = (EditText)findViewById(R.id.nameText);
		surnameText = (EditText)findViewById(R.id.surnameText);
		idText = (EditText)findViewById(R.id.idText);

		nameText.setOnEditorActionListener((OnEditorActionListener) this);
		surnameText.setOnEditorActionListener((OnEditorActionListener) this);
		idText.setOnEditorActionListener((OnEditorActionListener) this);

		nameText.setText(FaceDBMain.getName());
		surnameText.setText(FaceDBMain.getSurname());
		idText.setText(FaceDBMain.getId());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.v(TAG,"onStart");
		camPicPath = FaceDBMain.getCamPath();
		camPic = setCamInfo(camPicPath); 

		imgViewFromCam.setImageBitmap(camPic);

		enableComparisonFeatures();

	}


	private Bitmap setCamInfo(String path)
	{
		Bitmap returnValue = BitmapFactory.decodeFile(path);
		camLabel.setVisibility(View.INVISIBLE);
		camData = true;
		//camLabel.setText("");
		if (returnValue == null)
		{
			//camLabel.setText(R.string.taptoload);
			camLabel.setVisibility(View.VISIBLE);
			returnValue = BitmapFactory.decodeResource(getResources(),R.drawable.camdeficon);
			camData = false;
		} 
		return returnValue;
	}

	private void enableComparisonFeatures()
	{

		if (camData && nameText.getText().toString() != "" && surnameText.getText().toString() != "" && idText.getText().toString() != "")
		{

			remote.setEnabled(true);
		}
		else
		{			
			remote.setEnabled(false);
		}
	}

	private void changePicOrDie()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Take Pic from Camera?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id)
			{
				Log.v(TAG,"camYES");
				Intent intent = new Intent(self,SnapFaceActivity.class);   
				startActivity(intent);
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.v(TAG,"camNO");
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void uploadFace() 
	{
		try
		{


			JSONObject picInfo = new JSONObject();
			picInfo.put("name", nameText.getText().toString());
			picInfo.put("surname", surnameText.getText().toString());
			picInfo.put("personid", idText.getText().toString());
			picInfo.put("jpegname", new File(camPicPath).getName());
			JSONObject wsInfo = new JSONObject();
			wsInfo.put(wservice, picInfo);		
			JSONObject json = FaceUploader.uploadFaceWithInfo(camPicPath, wsInfo);
			Log.v(TAG,json.toString());
			displayResults(json);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}


	public void onClick(View v)
	{
		Log.v(TAG,"onClick");
		switch (v.getId())		
		{
		case R.id.fromCamera:			  
			Log.v(TAG, "fromCamera");
			changePicOrDie();
			break;

		case R.id.remotebutton:			  
			Log.v(TAG, "Remote");
			ProgressDialog dialogRemote = ProgressDialog.show(UploadPics.this, "", "Loading. Please wait...", true);
			uploadFace();
			dialogRemote.dismiss();
			break;

		}

	}

	private void displayResults(JSONObject results)
	{
		try {
			Context mContext = getApplicationContext();
			Dialog dialog = new Dialog(this);

			dialog.setContentView(R.layout.result_dialog);


			TextView distance = (TextView) dialog.findViewById(R.id.distance);
			TextView name = (TextView) dialog.findViewById(R.id.name);
			dialog.setTitle(results.getJSONObject("error").getString("ERRORMessage"));

			distance.setText(R.string.frmatch + results.getJSONObject("error").getString("ERRORMessage"));
			name.setText(R.string.frmatch );
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.icon);
			dialog.show();
		} catch (JSONException e) {

			e.printStackTrace();
		}

	}

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
	{
		Log.v(TAG,"onEditorAction");
		switch (v.getId())		
		{
		case R.id.nameText:			  
			Log.v(TAG, v.getText().toString());
			FaceDBMain.setName(v.getText().toString());
			break;

		case R.id.surnameText:			  
			Log.v(TAG, v.getText().toString());
			FaceDBMain.setSurname(v.getText().toString());
			break;
		case R.id.idText:			  
			Log.v(TAG, v.getText().toString());
			FaceDBMain.setId(v.getText().toString());
			break;
		}
		//		if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||( event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
		//		{
		//			Log.v(TAG,"onEditorAction - Finito");
		//			if (!event.isShiftPressed()) 
		//			{
		//				// the user is done typing. 
		//
		//				return true; // consume.
		//			}                
		//		}
		return false; // pass on to other listeners. 	
	}

}
