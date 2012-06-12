package jrc.ipsc.surcit;

import java.util.HashMap;
import java.util.prefs.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class Prefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static String TAG = "PreferenceActivity";
	final int INDEX_BACKUPTW = 0;
	private String WSUrl = FaceDBMain.getWSUrl();
	EditTextPreference wsEditText;
	public static final String EDIT_TEXT_WSPREF = "wsurl";
	private HashMap<String, Integer> mPrefMap = new HashMap<String, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		wsEditText = (EditTextPreference) getPreferenceScreen().findPreference(EDIT_TEXT_WSPREF);
		if (wsEditText.getText() == null || wsEditText.getText().isEmpty() || wsEditText.getText().equals("") )
		{
			wsEditText.setSummary(WSUrl);
			wsEditText.setText(WSUrl);
		}
//		Log.v(TAG,""+wsEditText.getText());
//		wsEditText.setSummary(WSUrl);

		//		Preference wsurl = (Preference) findPreference("wsurl"); 
		//		wsurl.setSummary(WSUrl);
		//		wsurl.setDefaultValue(WSUrl);
		//		
		//		EditTextPreference wsEditText = (EditTextPreference)findViewById(R.id.wsedittext);
		//		wsEditText.addTextChangedListener(new TextWatcher(){
		//	        public void afterTextChanged(Editable s) {
		//	            Log.v(TAG,"DOPO");
		//	        }
		//	        public void beforeTextChanged(CharSequence s, int start, int count, int after){
		//	        	Log.v(TAG,"Prima");
		//	        }
		//	        public void onTextChanged(CharSequence s, int start, int before, int count){
		//	        	Log.v(TAG,"Durante");
		//	        }
		//	    }); 
		//		
		//		mPrefMap.put("wsurl", INDEX_BACKUPTW);
		//		wsurl.setOnPreferenceClickListener(this);
	}

    @Override
    protected void onResume() 
    {
        super.onResume();
        Log.v(TAG,"onResume");
        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        wsEditText.setSummary(getPreferenceScreen().getSharedPreferences().getString(EDIT_TEXT_WSPREF,""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG,"onPause");
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
	
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(EDIT_TEXT_WSPREF)) 
		{

			Log.v(TAG,sharedPreferences.getString(EDIT_TEXT_WSPREF,""));
			wsEditText.setSummary(sharedPreferences.getString(EDIT_TEXT_WSPREF,""));
			FaceDBMain.setWSUrl(sharedPreferences.getString(EDIT_TEXT_WSPREF,""));
			
		}
		else
		{ 
		}

	}


	//	public boolean onPreferenceClick(Preference preference) {
	//		
	//		Log.v(TAG, "Click");
	//		
	//	        if (preference.hasKey()) {
	//	            int index = mPrefMap.get(preference.getKey());
	//	            switch (index)
	//	            {
	//	                case INDEX_BACKUPTW:
	//	                	Log.v(TAG, preference.getKey());
	//	                break;
	//
	//	                default:
	//	                   Log.v(TAG, "Other!");
	//	            }
	//	        }
	//	        return true;
	//	}
}