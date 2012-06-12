package jrc.ipsc.surcit.facerecogcl;

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
	private String WSUrl = IdApp.getWSUrl();
	EditTextPreference wsEditText;
	public static final String EDIT_TEXT_WSPREF = "wsurl";
	private HashMap<String, Integer> mPrefMap = new HashMap<String, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		wsEditText = (EditTextPreference) getPreferenceScreen().findPreference(EDIT_TEXT_WSPREF);
		if (wsEditText.getText() == null  || wsEditText.getText().equals("") || wsEditText.getText().isEmpty())
		{
			wsEditText.setSummary(WSUrl);
			wsEditText.setText(WSUrl);
		}

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
			IdApp.setWSUrl(sharedPreferences.getString(EDIT_TEXT_WSPREF,""));
			
		}
		else
		{ 
		}

	}

}