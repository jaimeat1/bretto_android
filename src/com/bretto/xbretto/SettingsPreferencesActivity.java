package com.bretto.xbretto;

import com.bretto.xbretto.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;


public class SettingsPreferencesActivity extends PreferenceActivity {

	SettingsHandler mSettingsHandler;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   addPreferencesFromResource(R.xml.settings);
	   
	   mSettingsHandler = new SettingsHandler();
		mSettingsHandler.mContext = this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

		mSettingsHandler.onHandlePreferenceTreeClick(preferenceScreen, preference);
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
}
