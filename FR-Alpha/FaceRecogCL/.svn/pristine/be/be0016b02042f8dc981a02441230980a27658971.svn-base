package jrc.ipsc.surcit.facerecogcl;


import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.nfc.NfcAdapter;
//import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ComparePics extends Activity implements OnClickListener, Runnable
{
	private static String TAG = "ComparePicsActivity"; 
	private static String wservice = "uploadForCompare";
	private ImageButton imgViewFromCam;
	private ImageButton imgViewFromNFC;
	private Bitmap camPic;
	private Bitmap nfcPic;
	private boolean nfcData;
	private boolean camData;
	private String nfcPicPath;
	private String camPicPath;
	private TextView nfcLabel;
	private TextView camLabel;	
	private ComparePics self ;
	private JSONObject results;
	private Button remote;
	private Button local;
	private ProgressDialog pd;
	private final int NFC_DIALOG = 1;
	private final int RESULT_DIALOG = 2;
	private final int WAITING_DIALOG = 3;
	private final int PIC_DIALOG = 4;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		//		Bundle b = getIntent().getExtras();
		//		
		//		Log.v("TAG","Prova");
		self = this;



		//		camPic = BitmapFactory.decodeResource(getResources(),R.drawable.camdeficon);
		//		nfcPic = BitmapFactory.decodeResource(getResources(),R.drawable.nfcdeficon);
		//		camPic = savedInstanceState.get("camPath").equals("")  ? 
		//				BitmapFactory.decodeResource(getResources(),R.drawable.camdeficon) : 
		//				BitmapFactory.decodeFile((String) savedInstanceState.get("camPath")) ;
		//				
		//		nfcPic = savedInstanceState.get("nfcPath") == "" ? 
		//				BitmapFactory.decodeResource(getResources(),R.drawable.nfcdeficon) : 
		//				BitmapFactory.decodeFile((String) savedInstanceState.get("nfcPath")) ;

		setContentView(R.layout.comparepics);

		imgViewFromCam = (ImageButton)findViewById(R.id.fromCamera);
		imgViewFromNFC = (ImageButton)findViewById(R.id.fromNFC);
		remote = (Button)findViewById(R.id.remotebutton);
		local = (Button)findViewById(R.id.localbutton);
		nfcLabel = (TextView)findViewById(R.id.nfcLabel);
		camLabel = (TextView)findViewById(R.id.camLabel);
		remote.setOnClickListener(this);
		local.setOnClickListener(this);
		imgViewFromCam.setOnClickListener(this);
		imgViewFromNFC.setOnClickListener(this);

		//		camPic = BitmapFactory.decodeFile(camPicPath);
		//		nfcPic = BitmapFactory.decodeFile(nfcPicPath);


	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.v(TAG,"onStart");
		camPicPath = IdApp.getCamPath();
		nfcPicPath = IdApp.getNFCPath();
		camPic = setCamInfo(camPicPath); 
		nfcPic = setNFCInfo(nfcPicPath);
		imgViewFromCam.setImageBitmap(camPic);
		imgViewFromNFC.setImageBitmap(nfcPic);

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

	private Bitmap setNFCInfo(String path)
	{
		Bitmap returnValue = BitmapFactory.decodeFile(path);
		nfcLabel.setVisibility(View.INVISIBLE);
		nfcData = true;
		//nfcLabel.setText("");
		if (returnValue == null)
		{
			nfcLabel.setVisibility(View.VISIBLE);
			nfcData = false;
			//nfcLabel.setText(R.string.taptoload);
			returnValue = BitmapFactory.decodeResource(getResources(),R.drawable.nfcdeficon);
		} 
		return returnValue;
	}

	private void enableComparisonFeatures()
	{

		if (nfcData && camData)
		{
			local.setEnabled(true);
			remote.setEnabled(true);
		}
		else if (nfcData || camData)
		{
			local.setEnabled(false);
			remote.setEnabled(true);
		}
		else
		{
			local.setEnabled(false);
			remote.setEnabled(false);
		}
	}

	private void changePicOrDie()
	{
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage("Take Pic from Camera?")
//		.setCancelable(false)
//		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id)
//			{
//				Log.v(TAG,"camYES");
//				Intent intent = new Intent(self,SnapFaceActivity.class);   
//				startActivity(intent);
//			}
//		})
//		.setNegativeButton("No", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				Log.v(TAG,"camNO");
//				dialog.cancel();
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.show();
		showDialog(PIC_DIALOG);
	}

	private void performRemoteMatching() 
	{


		try
		{
			JSONObject picInfo = new JSONObject();
			picInfo.put("jpegname", camPicPath);
			JSONObject wsInfo = new JSONObject();
			wsInfo.put(wservice, picInfo);
			
			results = FaceUploader.upLoad2Server(wsInfo);
			//displayResults(FaceUploader.upLoad2Server(wsInfo));
			dismissDialog(WAITING_DIALOG);
			showDialog(RESULT_DIALOG);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}


	public void run() {
		Looper.prepare();
		performRemoteMatching();
		handler.sendEmptyMessage(0);
		Looper.loop();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// pd.dismiss();


		}
	};

	private void displayResults(JSONObject results)
	{
		dismissDialog(WAITING_DIALOG);
		showDialog(RESULT_DIALOG);
		//		Context mContext = getApplicationContext();
		//		Dialog dialog = new Dialog(mContext);
		//
		//		dialog.setContentView(R.layout.result_dialog);
		//		dialog.setTitle(R.string.frresult);
		//
		//		TextView distance = (TextView) dialog.findViewById(R.id.distance);
		//		TextView name = (TextView) dialog.findViewById(R.id.name);
		//		try {
		//			distance.setText(R.string.frmatch + results.getJSONObject("result").getString("distance"));
		//			name.setText(R.string.frmatch + results.getJSONObject("result").getString("name"));
		//		} catch (JSONException e) {
		//			
		//			e.printStackTrace();
		//		}
		//		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		//		image.setImageResource(R.drawable.icon);
		//		dialog.show();

	}

	private void performLocalMatching() 
	{


	}

	private void scanNFCOrDie()
	{

	}
	@Override
	protected Dialog onCreateDialog(int id) {
		//		private int NFC_DIALOG = 1;
		//		private int RESULT_DIALOG = 2;
		//		private int WAITING_DIALOG  = 3;
		Dialog dialog = new Dialog(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);



		switch(id) {
		case PIC_DIALOG:
			
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
			dialog = builder.create();
			
			break;
		case NFC_DIALOG:
			//			AlertDialog nfcErrorDialog = new AlertDialog.Builder(ComparePics.this).create();
			//			nfcErrorDialog.setTitle(R.string.nfcerror);
			//			nfcErrorDialog.setMessage(getResources().getString(R.string.nfcerrormessage));
			//			nfcErrorDialog.setCancelable(true);
			//			nfcErrorDialog.show();
			builder.setMessage("R.string.nfcerror");
			builder.setMessage(getResources().getString(R.string.nfcerrormessage));
			builder.setCancelable(true);
			dialog = builder.create();
			break;

		case RESULT_DIALOG:
			//		Context mContext = getApplicationContext();
			//		Dialog dialog = new Dialog(mContext);
			//
					dialog.setContentView(R.layout.result_dialog);
					builder.setTitle(R.string.frresult);
			//
					TextView distance = (TextView) dialog.findViewById(R.id.distance);
					TextView name = (TextView) dialog.findViewById(R.id.name);
					try {
						distance.setText(R.string.frmatch + results.getJSONObject("result").getString("distance"));
						name.setText(R.string.frmatch + results.getJSONObject("result").getString("name"));
					} catch (JSONException e) {
						
						e.printStackTrace();
					}
					ImageView image = (ImageView) dialog.findViewById(R.id.image);
					image.setImageResource(R.drawable.icon);
					dialog = builder.create();
			break;

		case 0:

			//builder.setMessage(res.getString(R.string.raderror))
			builder.setMessage("prova")

			.setCancelable(false)

			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {

					dialog.dismiss();

				}

			});

			dialog = builder.create();

			break;
		case WAITING_DIALOG:

			dialog = ProgressDialog.show(this, "", 
					"Loading. Please wait...", true);

			break;


			//altri case con altre dialog qui



		default:

			dialog = null;

		}

		return dialog;

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
		case R.id.fromNFC:			  
			Log.v(TAG, "fromNFC");
			showDialog(NFC_DIALOG);
			//			scanNFCOrDie();
			//			AlertDialog nfcErrorDialog = new AlertDialog.Builder(ComparePics.this).create();
			//			nfcErrorDialog.setTitle(R.string.nfcerror);
			//			nfcErrorDialog.setMessage(getResources().getString(R.string.nfcerrormessage));
			//			nfcErrorDialog.setCancelable(true);
			//			nfcErrorDialog.show();
			break;
		case R.id.remotebutton:			  
			Log.v(TAG, "Remote");
			/*       pd = ProgressDialog.show(this, "Working..", "Calculating", true,
	                false);*/
			showDialog(WAITING_DIALOG);
			Thread thread = new Thread(this);
			thread.start();

			break;
		case R.id.localbutton:			  
			Log.v(TAG, "Local");
			performLocalMatching();
			break;
		}

	}



}
