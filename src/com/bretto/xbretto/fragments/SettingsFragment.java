package com.bretto.xbretto.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.bretto.xbretto.SettingsHandler;
import com.bretto.xbretto.R;

public class SettingsFragment extends PreferenceFragment {
	
	SettingsHandler mSettingsHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.settings);	
		
		mSettingsHandler = new SettingsHandler();
		mSettingsHandler.mContext = this.getActivity();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {		
		mSettingsHandler.onHandlePreferenceTreeClick(preferenceScreen, preference);
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
