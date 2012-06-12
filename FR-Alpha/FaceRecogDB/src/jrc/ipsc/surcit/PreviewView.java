package jrc.ipsc.surcit;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;




/* Class PreviewView for camera surface
 */
class PreviewView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback, AutoFocusCallback {

	private static final String TAG = "SnapFace";

	private final static int PREVIEW_WIDTH_FINE		= 320;
	private final static int PREVIEW_WIDTH_NORMAL	= 240;

	// mag ratio for eye-distant to face region
	private final static float MAG_EYEDIST_FACE		= 3.0f;
	// pixel len for contact icon
	private final static int CONTACT_ICON_PIXLEN	= 60;

	/*Cropped image fixed dimension*/
	private int dimX = 150;
	private int dimY = 125;
	
	private int MAX_CAMERA_WIDTH = 1920;
	private int maxCameraHeight = 1088;
	private int minCameraWidth = 640;
	private int minCameraHeight = 480;

	/* local members */
	private Context context_;
	private SurfaceHolder surfacehldr_;
	private Camera camera_;
	private boolean lockPreview_ = true;
	private int prevSettingWidth_;
	private int prevSettingHeight_;
	private int previewWidth_;
	private int previewHeight_;
	private int captureWidth_ = 0;
	private int captureHeight_ = 0;
	private boolean takingPicture_ = false;
	private Bitmap fdtmodeBitmap_ = null;
	private boolean isSDCardPresent_ = false;
	private int fdetLevel_ = 1;
	private int appMode_ = 0;

	/* Overlay Layer for additional graphics overlay */
	private Bitmap overlayBitmap_;
	private OverlayLayer overlayLayer_;

	/* Face Detection */
	private FaceResult faces_[];
	private static final int MAX_FACE = 5;
	private FaceDetector fdet_;
	private PointF selFacePt_ = null;

	/* Face Detection Threads */
	private boolean isThreadWorking_ = false;
	private Handler handler_;
	private FaceDetectThread detectThread_ = null;

	/* buffers for vision analysis */
	private byte[] grayBuff_;
	private int bufflen_;
	private int[] rgbs_;


	private boolean calledACTION_GET_CONTENT_ = false;

	/* Constructor */
	public PreviewView(Context context, boolean calledACTION_GET_CONTENT) {
		super(context);
		context_ = context;
		previewWidth_ = previewHeight_ = 1;
		faces_ = new FaceResult[MAX_FACE];
		for(int i=0;i<MAX_FACE;i++)
			faces_[i] = new FaceResult();
		overlayLayer_ = new OverlayLayer(context);
		surfacehldr_ = getHolder();
		surfacehldr_.addCallback(this);
		surfacehldr_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		handler_ = new Handler();
		//		System.loadLibrary("snapface-jni");
		isSDCardPresent_ = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

		calledACTION_GET_CONTENT_ = calledACTION_GET_CONTENT;
	}

	/* Jni entry point */
	//	private native int grayToRgb(byte src[],int dst[]);

	/* Overlay instance access method for Activity */
	public OverlayLayer getOverlay(){
		return overlayLayer_;
	}

	/* surfaceCreated */
	public void surfaceCreated(SurfaceHolder holder) {
		setKeepScreenOn(true);
		setupCamera();
		if(!isSDCardPresent_)
			Toast.makeText(context_, R.string.SDCardNotPresentAlert, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context_, R.string.TapInstructionAlert, Toast.LENGTH_LONG).show();
	}

	/* surfaceDestroyed */
	public void surfaceDestroyed(SurfaceHolder holder) {
		setKeepScreenOn(false);
		releaseCamera();
	}

	/* onAutoFocus */
	public void onAutoFocus(boolean success, Camera camera){
		if(takingPicture_)//&&success)
			takePicture();
	}

	/* surfaceChanged */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		resetCameraSettings();
		camera_.setPreviewCallback(this);
		camera_.startPreview();
	}

	/* onPreviewFrame */
	public void onPreviewFrame(byte[] _data, Camera _camera) {
		if(lockPreview_||takingPicture_)
			return;
		if(_data.length < bufflen_)
			return;
		// run only one analysis thread at one time
		if(!isThreadWorking_){
			isThreadWorking_ = true;
			// copy only Y buffer
			ByteBuffer bbuffer = ByteBuffer.wrap(_data);
			bbuffer.get(grayBuff_, 0, bufflen_);
			// make sure to wait for previous thread completes
			waitForFdetThreadComplete();
			// start thread
			detectThread_ = new FaceDetectThread(handler_);
			detectThread_.setBuffer(grayBuff_);
			detectThread_.start();
		}
	}

	/* onTouchEvent */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/* do not handle if already going to take picture */
		if(takingPicture_)
			return false;
		/* detect touch down */
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			/* CAUTION : touch point need to be same aspect to jpeg bitmap size */
			int w = this.getWidth();
			int h = this.getHeight();
			Point touchPt = new Point((int)event.getRawX(),(int)event.getRawY());
			float xRatio = (float)w / previewWidth_; 
			float yRatio = (float)h / previewHeight_;
			selFacePt_ = null;
			for(int i=0; i<MAX_FACE; i++){
				FaceResult face = faces_[i];
				float eyedist = face.eyesDistance()*xRatio;
				if(eyedist==0.0f)
					continue;
				PointF midEyes = new PointF();
				face.getMidPoint(midEyes);
				/* assume face rect is x3 size of eye distance each side */
				PointF lt = new PointF(midEyes.x*xRatio-eyedist*1.5f,midEyes.y*yRatio-eyedist*1.5f);
				Rect rect = new Rect((int)(lt.x),(int)(lt.y),(int)(lt.x+eyedist*3.0f),(int)(lt.y+eyedist*3.0f));
				if( rect.contains(touchPt.x,touchPt.y)){
					if(!isSDCardPresent_)
						Toast.makeText(context_, R.string.SDCardNotPresentAlert, Toast.LENGTH_LONG).show();
					else{
						takingPicture_ = true;
						Toast.makeText(context_, R.string.CapturingAlert, Toast.LENGTH_SHORT).show();
						selFacePt_ = new PointF((float)touchPt.x/w,(float)touchPt.y/h);
					}
					break;
				}
			}
			/* call autofocus. if takingPicture_ == true, take picture upon completion */
			camera_.autoFocus(this);
			return true;
		}
		return false;
	}

	/* setResolution */
	//	public void setfdetLevel(int level, boolean silent){
	//		if(fdetLevel_ == level)
	//			return;
	//		fdetLevel_ = level;
	//		if(silent)
	//			return;
	//		camera_.stopPreview();
	//		resetCameraSettings();
	//		camera_.startPreview();
	//	}

	/* setAppMode */
	public void setAppMode(int mode){
		if(appMode_ == mode)
			return;
		appMode_ = mode;
		switch(appMode_)
		{
		case 0:{
			overlayBitmap_ = null;
			break;
		}
		case 1:{
			//overlayBitmap_ = BitmapFactory.decodeResource(context_.getResources(), R.drawable.mask_vader);
			break;
		}
		//			case 2:{
		//				overlayBitmap_ = BitmapFactory.decodeResource(context_.getResources(), R.drawable.mask_laughingman);
		//				break;
		//			}
		}
	}

	/* setupCamera */
	private void setupCamera(){
		try {
			camera_ = Camera.open();
			camera_.setPreviewDisplay(surfacehldr_);
		} catch (IOException exception) {
			camera_.release();
			camera_ = null;
		}
	}

	/* releaseCamera */
	private void releaseCamera(){
		/* release camera object */
		Log.i(TAG,"releaseCamera");
		lockPreview_ = true;
		camera_.setPreviewCallback(null);
		camera_.stopPreview();
		waitForFdetThreadComplete();
		camera_.release();
		camera_ = null;
	}

	/* resetCameraSettings */
	private void resetCameraSettings(){
		Log.v(TAG, "ResetCameraSettings");
		lockPreview_ = true;
		waitForFdetThreadComplete();
		for(int i=0;i<MAX_FACE;i++)
			faces_[i].clear();
		/* set parameters for onPreviewFrame */
		Camera.Parameters params = camera_.getParameters();
		// 		Log.i(TAG,"camera params"+params.flatten());
		
		
		String strPrevSizesVals = params.get("preview-size-values");
		String strCapSizesVals = params.get("picture-size-values");
		
		
//		int previewHeightNorm = 0;
//		int	previewHeightFine = 0;
		if(strPrevSizesVals!=null){
			String tokens[] = strPrevSizesVals.split(",");
			for( int i=0; i < tokens.length; i++ ){
				String tokens2[] = tokens[i].split("x");
				Log.v(TAG, tokens2[0] + "x" + tokens2[1]);
				if ( Integer.parseInt(tokens2[0]) <= PREVIEW_WIDTH_FINE && prevSettingWidth_ <= Integer.parseInt(tokens2[0]))
				{
					prevSettingWidth_ = Integer.parseInt(tokens2[0]);
					prevSettingHeight_ = Integer.parseInt(tokens2[1]);
				}				
			}
		}
//		else{
//			previewHeightFine = 240;
//			previewHeightNorm = 160;
//		}
		//		if( fdetLevel_ == 0 ){
		//			prevSettingHeight_ = previewHeightFine;
		//			fdtmodeBitmap_ = BitmapFactory.decodeResource(context_.getResources(), R.drawable.fdt_fine);
		//		}else{
//		prevSettingWidth_ = PREVIEW_WIDTH_NORMAL;
//		prevSettingHeight_ = previewHeightNorm;
		//			fdtmodeBitmap_ = BitmapFactory.decodeResource(context_.getResources(), R.drawable.fdt_norm);
		//		}
		
//		
//		int capHeightNorm = 0;
//		int	capHeightFine = 0;
		if(strCapSizesVals!=null)
		{
			String tokens[] = strCapSizesVals.split(",");
			for( int i=0; i < tokens.length; i++ ){
				String tokens2[] = tokens[i].split("x");
				Log.v(TAG, tokens2[0] + "x" + tokens2[1]);
				if ( Integer.parseInt(tokens2[0]) <= MAX_CAMERA_WIDTH && captureWidth_ <=  Integer.parseInt(tokens2[0]))
				{
					captureWidth_ = Integer.parseInt(tokens2[0]);
					captureHeight_ = Integer.parseInt(tokens2[1]);
				}				
			}
		}
		
		
		
//		String capTokens1[] = strCapSizesVals.split(",");
//		String capTokens2[] = capTokens1[capTokens1.length-1].split("x");
//		captureWidth_ = Integer.parseInt(capTokens2[0]);
//		captureHeight_ = Integer.parseInt(capTokens2[1]);
		
		
		
		
        params.setPreviewSize(prevSettingWidth_, prevSettingHeight_);
        params.setPictureSize(captureWidth_, captureHeight_); 
		
		
		
		
		/* set preview size small for fast analysis. let say QQVGA
		 * by setting smaller image size, small faces will not be detected. */
//		if(prevSettingHeight_!=0)
//			params.setPreviewSize(prevSettingWidth_, prevSettingHeight_);
//		if(captureWidth_!=0)
//			params.setPictureSize(captureWidth_, captureHeight_);
		//params.setPreviewFrameRate(5);
//		params.setJpegQuality(80);
		
		
/*        ArrayList<Size> list = (ArrayList<Size>) params.getSupportedPictureSizes();  //recuepro le risoluzioni supportate dalla camera
        int picture_width = list.get(3).width;
        int picture_height = list.get(3).height;*/
        
//		int picture_width =  0;
//		int picture_height = 0;
//				
//        for (Camera.Size size : params.getSupportedPreviewSizes()) 
//        {
//        	Log.v(TAG,"Res: "+ size.width + "x"+size.height);
//            if (size != null) 
//            	if (size.width >= picture_width && size.height >= picture_height) 
//            {
//            	picture_width = size.width;
//            	picture_height = size.height;
//            	Log.v(TAG,"Res: "+ picture_width+"x"+picture_height);
//            }
/*            else 
            	break;*/
//          }

        
        
//        int picture_width = list.get(list.size()-1).width;
//        int picture_height = list.get(list.size()-1).height;

        
		
		
		camera_.setParameters(params);
		/* setParameters do not work well */
		params = camera_.getParameters();
		Size size = params.getPreviewSize();
		previewWidth_ = size.width;
		previewHeight_ = size.height;
		Log.i(TAG,"preview size, w:"+previewWidth_+",h:"+previewHeight_);
		size = params.getPictureSize();
		Log.i(TAG,"picture size, w:"+size.width+",h:"+size.height);
		// allocate work memory for analysis
		bufflen_ = previewWidth_*previewHeight_;
		grayBuff_ = new byte[bufflen_];
		rgbs_ = new int[bufflen_];
		float aspect = (float)previewHeight_/(float)previewWidth_;
		fdet_ = new FaceDetector( prevSettingWidth_,(int)(prevSettingWidth_*aspect), MAX_FACE ); 
		lockPreview_ = false;
	}

	/* takePicture */
	private void takePicture() {
		waitForFdetThreadComplete();
		/* implement only jpeg callback */
		camera_.takePicture(null, null, pictureCallbackJpeg); 
	}

	/* jpegCallback */
	private PictureCallback pictureCallbackJpeg = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			//			Log.i(TAG,"jpegCallback:"+_data);
			takingPicture_ = false;
			/* convert jpeg buffer to Bitmap */
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			Bitmap fullbmp = BitmapFactory.decodeByteArray(_data, 0, _data.length, opts);
			int w = opts.outWidth;
			int h = opts.outHeight;
			Log.i(TAG,"fullbitmap raw: w="+w+",h="+h);
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = (int)(w/captureWidth_);
			// resample
			fullbmp = BitmapFactory.decodeByteArray(_data, 0, _data.length,opts);
			w = fullbmp.getWidth();
			h = fullbmp.getHeight();
			Log.i(TAG,"fullbitmap mod: w="+w+",h="+h);
			/* CAUTION
			 * takepicture callback image may differs not just aspect
			 * but also the view angle. need to fix them.
			 */
			float orgRatio = (float)previewWidth_/previewHeight_;
			float offset_w = (w - h*orgRatio)/2.0f;
			float xRatio = (float)(h*orgRatio) / previewWidth_; 
			float yRatio = (float)h / previewHeight_;
			/* restore touch point to bitmap size */
			Point touchPt = new Point((int)(selFacePt_.x*w),(int)(selFacePt_.y*h));
			for(int i=0; i<MAX_FACE; i++){
				FaceResult face = faces_[i];
				float eyedist = face.eyesDistance()*xRatio;
				if(eyedist==0.0f)
					continue;
				PointF midEyes = new PointF(); 
				face.getMidPoint(midEyes);
				/* don't want region to be overlapped */
				PointF lt = new PointF(midEyes.x*xRatio-eyedist*MAG_EYEDIST_FACE*0.5f+offset_w,midEyes.y*yRatio-eyedist*MAG_EYEDIST_FACE*0.5f);
				Rect rect = new Rect((int)(lt.x),(int)(lt.y),(int)(lt.x+eyedist*MAG_EYEDIST_FACE),(int)(lt.y+eyedist*MAG_EYEDIST_FACE));
				if(!rect.contains(touchPt.x,touchPt.y))
					continue;
				/* expand region for cropping face */
				lt = new PointF(midEyes.x*xRatio-eyedist*MAG_EYEDIST_FACE*0.5f+offset_w,midEyes.y*yRatio-eyedist*MAG_EYEDIST_FACE*0.5f);
				rect = new Rect((int)(lt.x),(int)(lt.y),(int)(lt.x+eyedist*MAG_EYEDIST_FACE),(int)(lt.y+eyedist*MAG_EYEDIST_FACE));
				/* fix to fit within bitmap */
				rect.left = rect.left < 0 ? 0 : rect.left;
				rect.right = rect.right > w ? w : rect.right;
				rect.top = rect.top < 0 ? 0 : rect.top;
				rect.bottom = rect.bottom > h ? h : rect.bottom;

//				int picX = rect.right - rect.left;
//				int picY = rect.bottom - rect.top;
//				//Wrong Ratio 
//				float ratio = dimY/dimX;
//				float picRatio = picY/picX;
//				Log.v(TAG, "picRatio:"+picRatio);
//				Log.v(TAG, "ratio:"+ratio);
//				if (picRatio > ratio)
//				{
//					int newX = (int) (picY/(ratio));
//					int addX = (newX - picX);
//					if ( (addX % 2) == 0)
//					{
//						rect.right += addX/2;
//						rect.left -= addX/2;
//					}
//					else
//					{
//						rect.right += addX/2+1;
//						rect.left -= addX/2;
//					}
//				}
//				else if(picRatio < ratio)
//				{
//					int newY = (int) (picX*(ratio));
//					int addY = (newY - picY);
//					if ( (addY % 2) == 0)
//					{
//						rect.top -= addY/2;
//						rect.bottom += addY/2;
//					}
//					else
//					{
//						rect.top -= addY/2;
//						rect.bottom += addY/2+1;
//					}
//				}
//				//good Ratio (check dims)
//				else
//				{
//
//				}




				/* crop */
				Bitmap facebmp = Bitmap.createBitmap(fullbmp,rect.left,rect.top,rect.width(),rect.height());
				/*Resize bitmap*/
				facebmp = getResizedBitmap(facebmp, dimX, dimY);
				if(appMode_!=0&&overlayBitmap_!=null){
					//todo: merge bitmap
					Canvas c = new Canvas(facebmp);
					Paint p = new Paint();
					float len = eyedist*MAG_EYEDIST_FACE;
					PointF lt_mask = new PointF((facebmp.getWidth()-len)/2.0f,(facebmp.getHeight()-len)/2.0f);
					c.drawBitmap(overlayBitmap_, null , new Rect((int)lt_mask.x, (int)lt_mask.y,(int)(lt_mask.x+len),(int)(lt_mask.y+len)),p);
				}
				if(calledACTION_GET_CONTENT_){
					// scale to contact icon
					boolean scaleUp = facebmp.getWidth() < CONTACT_ICON_PIXLEN ? true : false;
					facebmp = Util.transform(new Matrix(), facebmp, CONTACT_ICON_PIXLEN, CONTACT_ICON_PIXLEN, scaleUp);
					Activity curAct = (Activity)context_;
					Bundle extras = new Bundle();
					extras.putParcelable("data", facebmp);
					curAct.setResult(Activity.RESULT_OK, (new Intent()).setAction("inline-data").putExtras(extras));
					curAct.finish();
				}
				else{
					/* Save bitmap to file */
					Uri uri = SaveBitmapToFile(facebmp);
					if(uri!=null){
						Intent intent = new Intent(context_, UploadPics.class);						
						context_.startActivity(intent);
						callWSorInfo();
						return;						
					}
				}
				break;
			}
			/* restart preview */
			camera_.startPreview();
		}
	};


	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) 
	{
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);
		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;

	}


	private static void callWSorInfo()
	{

	}

	/* Save bitmap to file */
	private Uri SaveBitmapToFile(Bitmap bmp) {
		/* get current time for file */
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		String filename = String.format("%4d-%02d-%02d %02d.%02d.%02d", year,(month + 1),day,hour,minute,second);
		/* setup for storing file */
		ContentValues values = new ContentValues();
		values.put(Media.DISPLAY_NAME, filename);
		values.put(Media.TITLE, filename);
		String absFilePath = "/sdcard/DCIM/snapface/"+filename+".jpg";
		values.put(Media.DATA, absFilePath);
		values.put(Media.MIME_TYPE, "image/jpeg");
		Uri uri = context_.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		try {
			/* save file */
			OutputStream outStream = context_.getContentResolver().openOutputStream(uri);
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
			outStream.flush();
			outStream.close();
			Toast.makeText(context_, context_.getString(R.string.SaveImageSuccessAlert)+absFilePath, Toast.LENGTH_SHORT).show();
			FaceDBMain.setCamPath(absFilePath);
			return uri;
		} catch (IOException e) {
			Toast.makeText(context_, R.string.SaveImageFailureAlert, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	/* waitForFdetThreadComplete */
	private void waitForFdetThreadComplete(){
		if(detectThread_ == null)
			return;
		if( detectThread_.isAlive() ){
			try {
				detectThread_.join();
				//				Log.i(TAG,"thread deleted.");
				detectThread_ = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* Overlay Layer class */
	public class OverlayLayer extends View { 
		private Paint paint_ = new Paint(Paint.ANTI_ALIAS_FLAG); 

		/* Constructor */
		public OverlayLayer(Context context) { 
			super(context); 
			paint_.setStyle(Paint.Style.STROKE); 
			paint_.setColor(0xFF33FF33);
			paint_.setStrokeWidth(3);
		} 

		/* onDraw - Draw Face rect */
		@Override 
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int w = canvas.getWidth();
			int h = canvas.getHeight();
			if(fdtmodeBitmap_!=null){
				int x = w-100;
				int y = 10;
				canvas.drawBitmap(fdtmodeBitmap_, null , new Rect(x,y,x+70,y+20),paint_);
			}
			float xRatio = (float)w / previewWidth_; 
			float yRatio = (float)h / previewHeight_;
			for(int i=0; i<MAX_FACE; i++){
				FaceResult face = faces_[i];
				float eyedist = face.eyesDistance()*xRatio;
				if(eyedist==0.0f)
					continue;
				PointF midEyes = new PointF();
				face.getMidPoint(midEyes);
				if(appMode_==0){
					PointF lt = new PointF(midEyes.x*xRatio-eyedist*MAG_EYEDIST_FACE*0.5f,midEyes.y*yRatio-eyedist*MAG_EYEDIST_FACE*0.5f);
					canvas.drawRect((int)(lt.x),(int)(lt.y),(int)(lt.x+eyedist*MAG_EYEDIST_FACE),(int)(lt.y+eyedist*MAG_EYEDIST_FACE), paint_); 
				}
				else if(overlayBitmap_!=null){
					PointF lt = new PointF(midEyes.x*xRatio-eyedist*MAG_EYEDIST_FACE*0.5f,midEyes.y*yRatio-eyedist*MAG_EYEDIST_FACE*0.5f);
					canvas.drawBitmap(overlayBitmap_, null , new Rect((int)lt.x, (int)lt.y,(int)(lt.x+eyedist*MAG_EYEDIST_FACE),(int)(lt.y+eyedist*MAG_EYEDIST_FACE)),paint_);
				}
			}
		}
	};

	/* Thread Class for Face Detection */
	private class FaceDetectThread extends Thread {
		/* variables */
		private Handler handler_;
		private byte[] graybuff_ = null;

		/* Constructor */
		public FaceDetectThread(Handler handler){
			handler_ = handler;
		}

		/* set buffer */
		public void setBuffer(byte[] graybuff){
			graybuff_ = graybuff;
		}

		/* run the thread */
		@Override
		public void run() {
			/* face detector only needs grayscale image */
			//			grayToRgb(graybuff_,rgbs_);										// jni method
			gray8toRGB32(graybuff_,previewWidth_,previewHeight_,rgbs_);		// java method
			float aspect = (float)previewHeight_/(float)previewWidth_;
			int w = prevSettingWidth_;
			int h = (int)(prevSettingWidth_*aspect);
			float xScale = (float)previewWidth_/(float)prevSettingWidth_;
			float yScale = (float)previewHeight_/(float)prevSettingHeight_;
			Bitmap bmp = Bitmap.createScaledBitmap(
					Bitmap.createBitmap(rgbs_,previewWidth_,previewHeight_,Bitmap.Config.RGB_565),
					w,h,false);
			//			Log.i(TAG,"downscale w="+bmp.getWidth()+",h="+bmp.getHeight());
			int prevfound=0,trackfound=0;
			for(int i=0; i<MAX_FACE; i++){
				FaceResult face = faces_[i];
				float eyedist = face.eyesDistance();
				if(eyedist==0.0f)
					continue;
				PointF midEyes = new PointF(); 
				face.getMidPoint(midEyes);
				prevfound++;
				PointF lt = new PointF(midEyes.x-eyedist*2.5f,midEyes.y-eyedist*2.5f);
				Rect rect = new Rect((int)(lt.x),(int)(lt.y),(int)(lt.x+eyedist*5.0f),(int)(lt.y+eyedist*5.0f));
				/* fix to fit */
				rect.left = rect.left < 0 ? 0 : rect.left;
				rect.right = rect.right > w ? w : rect.right;
				rect.top = rect.top < 0 ? 0 : rect.top;
				rect.bottom = rect.bottom > h ? h : rect.bottom;
				if(rect.left >= rect.right || rect.top >= rect.bottom )
					continue;
				/* crop */
				Bitmap facebmp = Bitmap.createBitmap(bmp,rect.left,rect.top,rect.width(),rect.height());
				FaceDetector.Face[] trackface = new FaceDetector.Face[1];
				FaceDetector tracker = new FaceDetector( facebmp.getWidth(),facebmp.getHeight(),1); 
				int found = tracker.findFaces(facebmp, trackface);
				if(found!=0){
					PointF ptTrack = new PointF();
					trackface[0].getMidPoint(ptTrack);
					ptTrack.x += (float)rect.left;
					ptTrack.y += (float)rect.top;
					ptTrack.x *= xScale;
					ptTrack.y *= yScale;
					float trkEyedist = trackface[0].eyesDistance()*xScale;
					faces_[i].setFace(ptTrack,trkEyedist);
					trackfound++;
				}
			}
			if(prevfound==0||prevfound!=trackfound){
				FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
				fdet_.findFaces(bmp, fullResults);
				/* copy result */
				for(int i=0; i<MAX_FACE; i++){
					if(fullResults[i]==null)
						faces_[i].clear();
					else{
						PointF mid = new PointF();
						fullResults[i].getMidPoint(mid);
						mid.x *= xScale;
						mid.y *= yScale;
						float eyedist = fullResults[i].eyesDistance()*xScale;
						faces_[i].setFace(mid,eyedist);
					}
				}
			}
			/* post message to UI */
			handler_.post(new Runnable() {
				public void run() {
					overlayLayer_.postInvalidate();
					// turn off thread lock
					isThreadWorking_ = false;
				}
			});
		}

		/* convert 8bit grayscale to RGB32bit (fill R,G,B with Y)
		 * process may take time and differs according to OS load. (100-1000ms) */
		@SuppressWarnings("unused")
		private void gray8toRGB32(byte[] gray8, int width, int height, int[] rgb_32s) {
			final int endPtr = width * height;
			int ptr = 0;
			while (true) {
				if (ptr == endPtr)
					break;
				final int Y = gray8[ptr] & 0xff; 
				rgb_32s[ptr] = 0xff000000 + (Y << 16) + (Y << 8) + Y;
				ptr++;
			}
		}

	};

	/* Face Result Class */
	private class FaceResult extends Object {
		private PointF midEye_;
		private float eyeDist_;
		public FaceResult(){
			midEye_ = new PointF(0.0f,0.0f);
			eyeDist_ = 0.0f;
		}
		public void setFace(PointF midEye, float eyeDist){
			set_(midEye,eyeDist);
		}
		public void clear(){
			set_(new PointF(0.0f,0.0f),0.0f);
		}
		private synchronized void set_(PointF midEye, float eyeDist){
			midEye_.set(midEye);
			eyeDist_ = eyeDist;
		}
		public float eyesDistance(){
			return eyeDist_;
		}
		public void getMidPoint(PointF pt){
			pt.set(midEye_);
		}
	};

	private static class Util{
		public static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, boolean scaleUp) {
			int deltaX = source.getWidth() - targetWidth;
			int deltaY = source.getHeight() - targetHeight;
			if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
				Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b2);
				int deltaXHalf = Math.max(0, deltaX / 2);
				int deltaYHalf = Math.max(0, deltaY / 2);
				Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()), deltaYHalf
						+ Math.min(targetHeight, source.getHeight()));
				int dstX = (targetWidth - src.width()) / 2;
				int dstY = (targetHeight - src.height()) / 2;
				Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
				c.drawBitmap(source, src, dst, null);
				return b2;
			}
			float bitmapWidthF = source.getWidth();
			float bitmapHeightF = source.getHeight();
			float bitmapAspect = bitmapWidthF / bitmapHeightF;
			float viewAspect = (float) targetWidth / targetHeight;
			if (bitmapAspect > viewAspect) {
				float scale = targetHeight / bitmapHeightF;
				if (scale < .9F || scale > 1F) {
					scaler.setScale(scale, scale);
				} else {
					scaler = null;
				}
			} else {
				float scale = targetWidth / bitmapWidthF;
				if (scale < .9F || scale > 1F) {
					scaler.setScale(scale, scale);
				} else {
					scaler = null;
				}
			}
			Bitmap b1;
			if (scaler != null) {
				b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
			} else {
				b1 = source;
			}
			int dx1 = Math.max(0, b1.getWidth() - targetWidth);
			int dy1 = Math.max(0, b1.getHeight() - targetHeight);
			Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);
			if (b1 != source) {
				b1.recycle();
			}
			return b2;
		}
	};
}
