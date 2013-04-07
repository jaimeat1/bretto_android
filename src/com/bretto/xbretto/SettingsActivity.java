package com.bretto.xbretto;

import com.bretto.xbretto.fragments.SettingsFragment;
import com.bretto.xbretto.R;

import android.app.Activity;
import android.os.Bundle;


public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_layout);

		SettingsFragment myFragment = new SettingsFragment();
		getFragmentManager().beginTransaction()
		.add(R.id.settings_fragment, myFragment)
		.commit();
	}
	
}
