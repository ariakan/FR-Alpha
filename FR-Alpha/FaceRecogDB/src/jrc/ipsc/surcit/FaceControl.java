package jrc.ipsc.surcit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;

public class FaceControl extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);

		ScrollView cameraButton =  (ScrollView) findViewById(R.id.scrollView);
		
	}
}
