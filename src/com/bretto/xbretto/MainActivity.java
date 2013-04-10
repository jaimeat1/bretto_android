/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bretto.xbretto;

import com.bretto.xbretto.fragments.AlarmFragment;
import com.bretto.xbretto.fragments.HomeFragment;
import com.bretto.xbretto.fragments.SystemFragment;
import com.bretto.xbretto.numberpicker.NumberPickerDialog;
import com.bretto.xbretto.tab.*;
import com.bretto.xbretto.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends TabCompatActivity implements NumberPickerDialog.OnNumberSetListener {
	
	// Intents identifiers 
	public static final String SMS_SENT = "SMS_SENT";
	public static final String SMS_DELIVERY = "SMS_DELIVERY";
	public static final String COMMAND = "COMMAND";
	public static final String NEW_PASSWORD = "NEW_PASSWORD";
	
	// Preferences access keys
	public static final String PREFERENCES_PASSWORDAPP = "PREFERENCES_PASSWORDAPP";
	public static final String PREFERENCES_ASKPASSWORD = "PREFERENCES_ASKPASSWORD";
	public static final String PREFERENCES_PASSWORDALARM = "PREFERENCES_PASSWORDALARM";
	public static final String PREFERENCES_NUMBERALARM = "PREFERENCES_NUMBERALARM";
	public static final String PREFERENCES_WIZARD = "PREFERENCES_WIZARD";
	
	// Log constant
	public static final String APP_TAG = "X-Bretto";
	
	// Alert for app password
	private AlertDialog mAlertPassword;
	
	// Flag to ask for password or not: TRUE user comes from app, FALSE user comes from out of app
	private boolean mSameActivity = false;
	
	// Message built by Communicator class, used for debugging
	public static String mMessage;
	
	// ID for the current pressed button
	public int mCurrentBtn;
	
	// Broadcast receiver to be notified when SMS is sent or delivered
	public BroadcastReceiver mBroadcastSMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {

        	// Listen to SMS sent event only
        	if (intent.getAction().compareTo(SMS_SENT) == 0) {
        	
	        	// If it is a change password command and has been successfully sent, save new password in preferences
	        	if ((intent.getIntExtra(COMMAND, 0) == Communicator.COMMAND_PASSWORD) && (getResultCode() == Activity.RESULT_OK)) {
	        		SharedPreferences preferences = getSharedPreferences(APP_TAG, MODE_PRIVATE);
	        		SharedPreferences.Editor editor = preferences.edit();
	        		editor.putString(PREFERENCES_PASSWORDALARM, intent.getStringExtra(NEW_PASSWORD));
	        		editor.commit();
	        	// If it is a hard reset command and has been successfully sent, delete preferences
	        	} else if ((intent.getIntExtra(COMMAND, 0) == Communicator.COMMAND_HARDRESET) && (getResultCode() == Activity.RESULT_OK)) {
	        		SharedPreferences preferences = getSharedPreferences(APP_TAG, MODE_PRIVATE);
	        		SharedPreferences.Editor editor = preferences.edit();
	        		editor.clear();
	        		editor.commit();
	        	}
	        	
	        	// SMS successfully sent
	        	if (getResultCode() == Activity.RESULT_OK){
	        		Toast.makeText(context, context.getResources().getString(R.string.success_sms), Toast.LENGTH_LONG).show();	
/*
	        		// Show alert with command, debugging version
	               	AlertDialog.Builder alert = new AlertDialog.Builder(context);
	            	alert.setTitle("SMS Message");
	            	alert.setMessage(MainActivity.mMessage);
	            	alert.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	        			public void onClick(DialogInterface dialog, int whichButton) {
	        			}
	        		});
	            	alert.show();
*/
	        		
	        	// Error sending SMS
	        	}else{
	        		Toast.makeText(context, context.getResources().getString(R.string.error_sms), Toast.LENGTH_LONG).show();
	        	}
        	}
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	    	
        super.onCreate(savedInstanceState);
     
        // Create right tabs depending on platform version
        setContentView(R.layout.main);
        TabHelper tabHelper = getTabHelper();

        CompatTab homeTab = tabHelper.newTab("home")
                .setText(R.string.tab_home)
                .setTabListener(new InstantiatingTabListener(this, HomeFragment.class));
        tabHelper.addTab(homeTab);

        CompatTab alarmTab = tabHelper.newTab("alarm")
                .setText(R.string.tab_alarm)
                .setTabListener(new InstantiatingTabListener(this, AlarmFragment.class));
        tabHelper.addTab(alarmTab);
        
        CompatTab systemTab = tabHelper.newTab("system")
                .setText(R.string.tab_system)
                .setTabListener(new InstantiatingTabListener(this, SystemFragment.class));
        tabHelper.addTab(systemTab);
        
    	// Register broadcast receivers to be notified about sending and delivering SMS
	    this.registerReceiver(mBroadcastSMSReceiver, new IntentFilter(MainActivity.SMS_SENT));
	    this.registerReceiver(mBroadcastSMSReceiver, new IntentFilter(MainActivity.SMS_DELIVERY));
	    
		// If there is no alarm password configured, set default alarm password
		String alarmPass = this.getSharedPreferences(APP_TAG, MODE_PRIVATE).getString(PREFERENCES_PASSWORDALARM, "");    	
		if ("".equals(alarmPass)) {
	        SharedPreferences preferences = this.getSharedPreferences(APP_TAG, MODE_PRIVATE);
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putString(PREFERENCES_PASSWORDALARM, getResources().getString(R.string.default_pass));
	        editor.commit();
		}
		
        // Set preference values, debugging version
/*
        SharedPreferences preferences = this.getSharedPreferences(APP_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_PASSWORDAPP, "1234");
        editor.putString(PREFERENCES_PASSWORDALARM, "aaaa");
        editor.putString(PREFERENCES_NUMBERALARM, "5554");
        editor.commit();
*/
    }
    
    /**
     * Shows a generic error message
     * @param errorMsg message to show
     */
	private void showError(String errorMsg){

		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    
		    builder.setTitle(R.string.error_title);
		    builder.setMessage(errorMsg);
		    builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		        }
		    });
		    
		    builder.create().show();
	}
	
	/**
	 * Asks for app password when application is open
	 * @param appPassword password to compare with
	 */
	private void askAppPassword(final String appPassword){
		
   		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view = LayoutInflater.from(this).inflate(R.layout.app_password_layout, null);
		builder.setView(view);
		builder.setTitle(R.string.ask_pass_title);
		
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {	
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing, override onClick() in setOnShowListener() to prevent to dismiss alert dialog
			}			
		});
		
		// User can't be able to dismiss alert dialog
		builder.setCancelable(false);
		
		mAlertPassword = builder.create();
		
		mAlertPassword.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(DialogInterface dialog) {

		        Button b = mAlertPassword.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		   				EditText editPass = (EditText)mAlertPassword.findViewById(R.id.edit_app_pass);
	    				String pass = editPass.getText().toString();

	    				// Fields are wrong
	    				if (appPassword.compareTo(pass) != 0){
	    					showError(getResources().getString(R.string.error_wrong_password));

	    				// Fields are right, continue
	    				} else {
	    					//Dismiss once everything is OK.
    		            	mAlertPassword.dismiss();
	    				}
		            }
		        });
		    }
		});
		
		mAlertPassword.show();
	}
	
	/**
	 * First step in wizard. Ask for app password
	 */
	private void showWizardFirstStep() {
		
		SettingsHandler settHandler = new SettingsHandler();
		settHandler.mContext = this;
		mAlertPassword = settHandler.askNewPassword(SettingsHandler.PASSWORD_APP_FIRST);
		
		mAlertPassword.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showWizardSecondStep();
		    }
		});
		
	}
	
	/**
	 * Second step in wizard. Ask for alarm number
	 */
	private void showWizardSecondStep() {
		
		SettingsHandler settHandler = new SettingsHandler();
		settHandler.mContext = this;
		mAlertPassword = settHandler.askNewNumber(true);
		
		mAlertPassword.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showWizardThirdStep();
		    }
		});
		
	}
	
	/**
	 * Third step in wizard. Ask for devices
	 */
	private void showWizardThirdStep() {
		
		mAlertPassword = this.askForDevices(R.id.setDevicesButton, true);
		
		mAlertPassword.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showWizardFourthStep();
		    }
		});

	}
	
	/**
	 * Fourth and last step in wizard. Show advice about save mode
	 */
	private void showWizardFourthStep() {

		// All steps are done, don't show wizard again
		SharedPreferences preferences = getSharedPreferences(APP_TAG, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(PREFERENCES_WIZARD, false);
		editor.commit();

	}
    
	
    protected void onResume () {
    	super.onResume();
    	
    	final String appPassword = getSharedPreferences(MainActivity.APP_TAG, MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDAPP, null);
    	boolean askPassword = getSharedPreferences(MainActivity.APP_TAG, MODE_PRIVATE).getBoolean(MainActivity.PREFERENCES_ASKPASSWORD, true);
    	boolean showWizard = getSharedPreferences(MainActivity.APP_TAG, MODE_PRIVATE).getBoolean(MainActivity.PREFERENCES_WIZARD, true);
   	
    	// First execution, ask for create new password
    	if (showWizard) {
    		
    		showWizardFirstStep();
		
		// Ask for app password
    	} else if ((appPassword != null) && (mSameActivity == false) && (askPassword)){
    		
    		askAppPassword(appPassword);
    	}
    	
    	// Use to ask for password or not
    	mSameActivity = false;
    }
    
    protected void onPause () {
    	super.onPause();
    	
    	// Dismiss alert dialog, if it exists
    	if (mAlertPassword != null){
    		
    		// Override onDismiss behaviour
    	    mAlertPassword.setOnDismissListener(new DialogInterface.OnDismissListener() {
	    		@Override
	    		public void onDismiss(DialogInterface dialog) {

	    		   }
    	    });
    	    
    		mAlertPassword.dismiss();
    	}
    }
    
    
    protected void onDestroy () {
    	this.unregisterReceiver(mBroadcastSMSReceiver);
    	super.onDestroy();
    }
    

    /**
     * Implementation of {@link CompatTabListener} to handle tab change events. This implementation
     * instantiates the specified fragment class with no arguments when its tab is selected.
     */
    public static class InstantiatingTabListener implements CompatTabListener {

        private final TabCompatActivity mActivity;
        private final Class<? extends Fragment> mClass;

        /**
         * Constructor used each time a new tab is created.
         *
         * @param activity The host Activity, used to instantiate the fragment
         * @param cls      The class representing the fragment to instantiate
         */
        public InstantiatingTabListener(TabCompatActivity activity, Class<? extends Fragment> cls) {
            mActivity = activity;
            mClass = cls;
        }

        /* The following are each of the ActionBar.TabListener callbacks */
        @Override
        public void onTabSelected(CompatTab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            Fragment fragment = tab.getFragment();
            if (fragment == null) {
                // If not, instantiate and add it to the activity
                fragment = Fragment.instantiate(mActivity, mClass.getName());
                tab.setFragment(fragment);
                ft.add(android.R.id.tabcontent, fragment, tab.getTag());
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(fragment);
            }            
        }

        @Override
        public void onTabUnselected(CompatTab tab, FragmentTransaction ft) {
            Fragment fragment = tab.getFragment();
            if (fragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(fragment);
            }
        }

        @Override
        public void onTabReselected(CompatTab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Do nothing.
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){
		case R.id.settings:			
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        	startActivity(new Intent(this, SettingsActivity.class));
	        } else {
	        	startActivity(new Intent(this, SettingsPreferencesActivity.class));
	        }
	        mSameActivity = true;
			break;
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			mSameActivity = true;
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Called when a button is pressed. It deliveries the command and its parameters to be processed
	 * @param view
	 */
    public void onCommandSelected(View view){
    	
		// Get phone number from preferences
		String number = this.getSharedPreferences(APP_TAG, MODE_PRIVATE).getString(PREFERENCES_NUMBERALARM, "");
    	
		// If there is no phone number configured, show error to user
		if ("".equals(number)) {
	    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    	alert.setTitle(R.string.error_title);
	    	alert.setMessage(R.string.error_settings_number);

	    	alert.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
	    	alert.show();
	    	
	    // There is alarm password and number configured, dispatch command
		} else {
    	
    	switch (view.getId()){

    	case R.id.assembleOffButton:
    		dispatchCommand(translateButtonToCommand(R.id.assembleOffButton, -1), null);
    		break;
    	case R.id.assembleOnButton:
    		dispatchCommand(translateButtonToCommand(R.id.assembleOnButton, -1), null);
    		break;
    	case R.id.climateButton:
    		askForOptions(getResources().getString(R.string.home_climate), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.climateButton);
    		break;
    	case R.id.engineButton:
    		askForOptions(getResources().getString(R.string.home_engine), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.engineButton);
    		break;
    	case R.id.ignitionButton:
    		askForOptions(getResources().getString(R.string.home_ignition), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.ignitionButton);
    		break;
    	case R.id.locationButton:
    		askForOptions(getResources().getString(R.string.home_location), 
    				new String[ ] {getResources().getString(R.string.location_gprmc), 
    							   getResources().getString(R.string.location_gps),
    							   getResources().getString(R.string.location_gpsm),
    							   getResources().getString(R.string.location_parking),
    							   getResources().getString(R.string.location_web)},
    				R.id.locationButton);
    		break;
    	case R.id.sensorButton:
    		askForOptions(getResources().getString(R.string.home_sensor), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.sensorButton);
    		break;
    	case R.id.callButton:
    		askForNumber(getResources().getString(R.string.alarm_call), R.id.callButton);
    		break;
    	case R.id.imeiButton:
    		dispatchCommand(translateButtonToCommand(R.id.imeiButton, -1), null);
    		break;
    	case R.id.setDevicesButton:
    		askForDevices(R.id.setDevicesButton, false);
    		break;
    	case R.id.getDevicesButton:
    		dispatchCommand(translateButtonToCommand(R.id.getDevicesButton, -1), null);
    		break;
    	case R.id.automaticButton:
    		askForOptions(getResources().getString(R.string.alarm_automatic), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.automaticButton);
    		break;
    	case R.id.speedButton:
    		askForOptions(getResources().getString(R.string.alarm_speed), 
    				new String[ ] {getResources().getString(R.string.enable), getResources().getString(R.string.disable)}, 
    				R.id.speedButton);
    		break;
    	case R.id.stateButton:
    		dispatchCommand(translateButtonToCommand(R.id.stateButton, -1), null);
    		break;
    	case R.id.resetButton:
    		askForConfirmation(getResources().getString(R.string.system_reset), R.id.resetButton, -1);
    		break;
    	case R.id.hardResetButton:
    		askForConfirmation(getResources().getString(R.string.system_hard_reset), R.id.hardResetButton, -1);
    		break;
    	case R.id.bannerButton:
    		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_banner)));
    		startActivity(browserIntent);
    		break;
    	default:
    		break;
    	}
		}
    }
    
    /**
     * Translates the button pressed into a Communicator class command
     * @param button button pressed
     * @param optionSelected option selected after pressing button
     * @return
     */
    public int translateButtonToCommand(int button, int optionSelected){
    	
    	int command = -1;
    	
		switch(button){
    	case R.id.assembleOffButton:
    		command = Communicator.COMMAND_ASSEMBLEOFF;
    		break;
    	case R.id.assembleOnButton:
    		command = Communicator.COMMAND_ASSEMBLEON;
    		break;
    	case R.id.climateButton:
    		if (optionSelected == 0){
    			command = Communicator.COMMAND_CLIMATEON;
    		} else {
    			command = Communicator.COMMAND_CLIMATEOFF;
    		}
    		break;
    	case R.id.engineButton:
    		if (optionSelected == 0){
    			command = Communicator.COMMAND_ENGINEON;
    		} else {
    			command = Communicator.COMMAND_ENGINEOFF;
    		}
    		break;
    	case R.id.ignitionButton:
    		if (optionSelected == 0){
    			command = Communicator.COMMAND_IGNITIONOFF;
    		} else {
    			command = Communicator.COMMAND_IGNITIONON;    			
    		}
    		break;
    	case R.id.sensorButton:
    		if (optionSelected == 0){
        		command = Communicator.COMMAND_SENSORON;
    		}else{
        		command = Communicator.COMMAND_SENSOROFF;
    		}
    		break;
    	case R.id.locationButton:
       		if (optionSelected == 0) {
        		command = Communicator.COMMAND_LOCATIONGPRMC;
    		}else if (optionSelected == 1) {
        		command = Communicator.COMMAND_LOCATIONGPSD;
    		} else if (optionSelected == 2) {
    			command = Communicator.COMMAND_LOCATIONGPSM;
    		} else if (optionSelected == 3) {
    			command = Communicator.COMMAND_LOCATIONPARKING;
    		} else {
    			command = Communicator.COMMAND_LOCATIONWEB;
    		}
    		break;
    	case R.id.callButton:
    		command = Communicator.COMMAND_CALL;
    		break;
    	case R.id.imeiButton:
    		command = Communicator.COMMAND_IMEI;
    		break;
    	case R.id.setDevicesButton:
    		command = Communicator.COMMAND_SETDEVICES;
    		break;
    	case R.id.getDevicesButton:
    		command = Communicator.COMMAND_GETDEVICES;
    		break;
    	case R.id.automaticButton:
    		if (optionSelected == 0){
    			command = Communicator.COMMAND_AUTOMATICON;
    		} else {
    			command = Communicator.COMMAND_AUTOMATICOFF;
    		}
    		break;
    	case R.id.speedButton:
    		if (optionSelected == 0){
    			command = Communicator.COMMAND_SPEEDON;
    		} else {
    			command = Communicator.COMMAND_SPEEDOFF;
    		}
    		break;
    	case R.id.stateButton:
    		command = Communicator.COMMAND_STATE;
    		break;
    	case R.id.resetButton:
    		command = Communicator.COMMAND_RESET;
    		break;
    	case R.id.hardResetButton:
    		command = Communicator.COMMAND_HARDRESET;
    		break;
    	default:
    		break;
		}

		return command;
    }

    /**
     * Dispatches a command through the Communicator class, that sends the SMS
     * @param command command identifier
     * @param parameters list of parameters for that command
     */
    public void dispatchCommand(int command, String[] parameters){
		
	    // Prepare Intents to deliver command
		Intent sentIntent = new Intent(MainActivity.SMS_SENT);
		Intent deliveryIntent = new Intent(MainActivity.SMS_DELIVERY);
		sentIntent.putExtra(MainActivity.COMMAND, command);
		
		PendingIntent pendingSent = PendingIntent.getBroadcast(this, 0, sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pendingDelivery = PendingIntent.getBroadcast(this, 0, deliveryIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Get alarm password and phone number for preferences
		String alarmPass = this.getSharedPreferences(APP_TAG, MODE_PRIVATE).getString(PREFERENCES_PASSWORDALARM, "");
		String number = this.getSharedPreferences(APP_TAG, MODE_PRIVATE).getString(PREFERENCES_NUMBERALARM, "");
		
		int length;
		if (parameters != null) {
			length = parameters.length;
		}else{
			length = 0;
		}

		// Add alarm password and phone number to the parameters list
		// Pass and number will be at positions 0 and 1 always
		String[] args = new String[2 + length];
		args[0] = alarmPass;
		args[1] = number;

		// Move rest of parameters
		for (int i=0; i < length; i++){
			args[2+i] = parameters[i];
		}

	    // Send command through Communicator
		Communicator.sendCommand(command, args, pendingSent, pendingDelivery);
    }
    
    /**
     * Alert popup to ask the user for one option from a list.
     * @param title title for the alert popup
     * @param options list of options to show
     * @param buttonId command button identifier
     */
    public void askForOptions(final String title, String[] options, final int buttonId){
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle(title);
    	
    	alert.setItems(options, new DialogInterface.OnClickListener() {
    		
    		public void onClick(DialogInterface d, int choice) {  			
    			if ((choice == 0) && (buttonId == R.id.climateButton)) {   				
    				askForTime(getResources().getString(R.string.home_climate), buttonId);   				
    			} else if ((choice == 0) && (buttonId == R.id.engineButton)) {   				
    				askForTime(getResources().getString(R.string.home_engine), buttonId);   				
    			} else if ((choice == 0) && (buttonId == R.id.speedButton)) {   				
    				askForTimeAndSpeed(getResources().getString(R.string.alarm_speed), buttonId);   				
    			} else {
    				String[] arg = null;  			
        			dispatchCommand(translateButtonToCommand(buttonId, choice), arg);
    			}	
    		}
    	});
    	
    	alert.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
    	});
    	alert.show();
    }
    
    /**
     * Alert popup to ask the user for a number.
     * @param title title for the alert popup
     * @param buttonId command button identifier
     */
    public void askForNumber(String title, final int buttonId){
    	
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		final View view = LayoutInflater.from(this).inflate(R.layout.setting_number_layout, null);
		builder.setView(view);

		// OK button
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText number = (EditText)view.findViewById(R.id.edit_new_number);				
    			String[] arg = {number.getText().toString()};
    			dispatchCommand(translateButtonToCommand(buttonId, -1), arg);
			}
		});

		// Cancel button
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
				
		final AlertDialog alert = builder.create();
		final EditText number = (EditText)view.findViewById(R.id.edit_new_number);

		// Create listener for EditText, in order to enable/disable positive button
		final TextWatcher textEditorWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// Field is fill, enable positive button
				if ("".equals(number.getText().toString()))
				{
					alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				// Field is empty, disable positive button
				}else{
					alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				}	
			}

			@Override
			public void afterTextChanged(Editable s) {				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}
		};
		
		number.addTextChangedListener(textEditorWatcher);

		alert.show();
		alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
    }
    
    /**
     * Alert popup to ask the user for phone numbers.
     * @param buttonId command button identifier
     * @param first TRUE it is wizard, FALSE it is a normal command invocation
     * @return alert dialog built
     */
    public AlertDialog askForDevices(final int buttonId, boolean first){
    	
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view;
		
		if (first) {
			builder.setTitle(getResources().getString(R.string.wizard_third_title));
			view = LayoutInflater.from(this).inflate(R.layout.first_set_devices_layout, null);
		} else {
			builder.setTitle(getResources().getString(R.string.alarm_set_devices));
			view = LayoutInflater.from(this).inflate(R.layout.alarm_set_devices_layout, null);
		}
		
		builder.setView(view);
		
		final EditText numberA = (EditText)view.findViewById(R.id.edit_number_a);
		final EditText numberB = (EditText)view.findViewById(R.id.edit_number_b);
		final EditText numberC = (EditText)view.findViewById(R.id.edit_number_c);
		
		// OK button
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				
				int length = 0;
    			if (!"".equals(numberA.getText().toString())){
    				length++;
    			}
    			if (!"".equals(numberB.getText().toString())){
    				length++;
    			}
    			if (!"".equals(numberC.getText().toString())){
    				length++;
    			}
				
    			String[] args = new String[length];
    			int index = 0;
    			if (!"".equals(numberA.getText().toString())){
    				args[index] = numberA.getText().toString();
    				index++;
    			}
    			if (!"".equals(numberB.getText().toString())){
    				args[index] = numberB.getText().toString();
    				index++;
    			}
    			if (!"".equals(numberC.getText().toString())){
    				args[index] = numberC.getText().toString();
    			}
    			
    			dispatchCommand(translateButtonToCommand(buttonId, -1), args);
			}
		});

		if (first) {
			// User can't be able to dismiss alert dialog
			builder.setCancelable(false);
		} else {
			// Cancel button
			builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
	
				}
			});
		}
		
		final AlertDialog alert = builder.create();
		
		// Create lister for EditText, in order to enable/disable positive button
		final TextWatcher textEditorWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// Some field is fill, enable positive button
				if ((!"".equals(numberA.toString()))||(!"".equals(numberA.toString()))||(!"".equals(numberA.toString())))
				{
					alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				// Fields are empty, disable positive button
				}else{
					alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}	
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		};
		
		numberA.addTextChangedListener(textEditorWatcher);
		numberB.addTextChangedListener(textEditorWatcher);
		numberC.addTextChangedListener(textEditorWatcher);
		
		alert.show();
		alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		return alert;
    }
    
    /**
     * Alert popup to ask the user for sensibility.
     * @param title title for the alert popup
     * @param buttonId command button identifier
     */
    public void askForSensibility(String title, final int buttonId){
    	
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		final View view = LayoutInflater.from(this).inflate(R.layout.alarm_sensibility_layout, null);
		builder.setView(view);
		
		final SeekBar seekBar = (SeekBar)view.findViewById(R.id.seek_sensibility);
		final TextView textBar = (TextView)view.findViewById(R.id.title_sensibility);
		textBar.setText(getResources().getString(R.string.sensibility)+" "+seekBar.getProgress());
				
		seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textBar.setText(getResources().getString(R.string.sensibility)+" "+progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
		
		// OK button
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String sensibility = String.valueOf(seekBar.getProgress());
				String[] arg = {sensibility};				
    			dispatchCommand(translateButtonToCommand(buttonId, -1), arg);
			}
		});

		// Cancel button
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		
		final AlertDialog alert = builder.create();
		alert.show();
    }
    
    /**
     * Alert popup to ask the user for confirmation 
     * @param title
     * @param buttonId
     * @param choice
     */
    private void askForConfirmation(String title, final int buttonId, final int choice) {
    	
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	final View view = LayoutInflater.from(this).inflate(R.layout.command_confirmation_layout, null);
		builder.setView(view);
    	
		TextView textView = (TextView)view.findViewById(R.id.command_confirmation);
		
    	switch (buttonId) {
    	case R.id.resetButton:
    		textView.setText(getResources().getString(R.string.confirmation_reset));
    		break;
    	case R.id.hardResetButton:
    		textView.setText(getResources().getString(R.string.confirmation_hard_reset));
    		break;
    	default:
    		textView.setText(getResources().getString(R.string.confirmation_default));
    		break;
    	}
		
		// OK button
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				dispatchCommand(translateButtonToCommand(buttonId, -1), null);
			}
		});

		// Cancel button
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		
		final AlertDialog alert = builder.create();
		alert.show();
    }
    
   /**
    * Alert dialog to ask for a time value. The picker depends on platform version
    * @param title
    * @param buttonId
    */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void askForTime(String title, final int buttonId) {

    	// API 11 or higher, use native NumberPicker
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			final View view = LayoutInflater.from(this).inflate(
					R.layout.alarm_time_layout, null);
			builder.setView(view);

			TextView textView = (TextView) view.findViewById(R.id.title_time);

			if (R.id.climateButton == buttonId) {
				textView.setText(this.getResources().getString(
						R.string.setClimate));
			} else if (R.id.engineButton == buttonId) {
				textView.setText(this.getResources().getString(
						R.string.setEngine));
			}

			final NumberPicker picker = (NumberPicker) view
					.findViewById(R.id.time_picker);
			picker.setMinValue(1);
			picker.setMaxValue(99);

			// OK button
			builder.setPositiveButton(R.string.button_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Integer value = picker.getValue();
							String[] arg = { value.toString() };
							dispatchCommand(
									translateButtonToCommand(buttonId, 0), arg);
						}
					});

			// Cancel button
			builder.setNegativeButton(R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			final AlertDialog alert = builder.create();
			alert.show();

		// API 10 or lower, use custom NumberPicker
		} else {
			
			String description = null;
			
			if (R.id.climateButton == buttonId) {
				mCurrentBtn = R.id.climateButton;
				description = this.getResources().getString(R.string.setClimate);
			} else if (R.id.engineButton == buttonId) {
				mCurrentBtn = R.id.engineButton;
				description = this.getResources().getString(R.string.setEngine);
			}

			NumberPickerDialog dialog = new NumberPickerDialog(this, -1, 0, R.layout.alarm_time_eclair_layout, description);
			dialog.setButton(NumberPickerDialog.BUTTON_POSITIVE, this.getText(R.string.button_ok), dialog);
			dialog.setButton(NumberPickerDialog.BUTTON_NEGATIVE, this.getText(R.string.button_cancel), (OnClickListener) null);
			dialog.setTitle(title);
			dialog.setRange(1, 1, 99);
            dialog.setOnNumberSetListener(this);

            dialog.show();
		}
    }
    
    /**
     * Alert dialog to ask for a time and speed value. The pickers depends on platform version
     * @param title
     * @param buttonId
     */
	private void askForTimeAndSpeed(String title, final int buttonId) {

		// API 11 or higher, use native NumberPicker
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			final View view = LayoutInflater.from(this).inflate(
					R.layout.alarm_time_speed_layout, null);
			builder.setView(view);

			final NumberPicker speedPicker = (NumberPicker) view
					.findViewById(R.id.speed_picker);
			speedPicker.setMinValue(20);
			speedPicker.setMaxValue(999);

			final NumberPicker timePicker = (NumberPicker) view
					.findViewById(R.id.time_speed_picker);
			timePicker.setMinValue(1);
			timePicker.setMaxValue(99);

			// OK button
			builder.setPositiveButton(R.string.button_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Integer speedValue = speedPicker.getValue();
							Integer timeValue = timePicker.getValue();
							String[] arg = { speedValue.toString(),
									timeValue.toString() };
							dispatchCommand(
									translateButtonToCommand(buttonId, 0), arg);
						}
					});

			// Cancel button
			builder.setNegativeButton(R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			final AlertDialog alert = builder.create();
			alert.show();

		// API 10 or lower, use custom NumberPicker
		} else {
			
			mCurrentBtn = buttonId;
			
			NumberPickerDialog dialog = new NumberPickerDialog(this, -1, 0, R.layout.alarm_time_speed_eclair_layout, null);
			dialog.setButton(NumberPickerDialog.BUTTON_POSITIVE, this.getText(R.string.button_ok), dialog);
			dialog.setButton(NumberPickerDialog.BUTTON_NEGATIVE, this.getText(R.string.button_cancel), (OnClickListener) null);
			dialog.setTitle(title);
			dialog.setRange(1, 1, 99);
			dialog.setRange(2, 20, 999);			
            dialog.setOnNumberSetListener(this);

            dialog.show();

		}
	}

	/**
	 * Called after user has selected a value with the custom NumberPicker, for platform versions API 10 or lower
	 * @param pickerDialog the alert dialog used to ask the value
	 * @param number1 value for picker 1
	 * @param number2 value for picker 2
	 */
	public void onNumberSet(NumberPickerDialog pickerDialog, Integer number1, Integer number2) {

		if (mCurrentBtn == R.id.climateButton) {
			String[] arg = { number1.toString() };
			dispatchCommand(translateButtonToCommand(R.id.climateButton, 0), arg);
		} else if (mCurrentBtn == R.id.engineButton) {
			String[] arg = { number1.toString() };
			dispatchCommand(translateButtonToCommand(R.id.engineButton, 0), arg);
		} else if (mCurrentBtn == R.id.speedButton) {
			String[] arg = { number2.toString(),number1.toString() };
			dispatchCommand(translateButtonToCommand(R.id.speedButton, 0), arg);
		}
		
    }

}
