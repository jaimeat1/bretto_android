package com.provauto.bretto.wcontrol;

import com.provauto.bretto.wcontrol.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsHandler {
		
	public Context mContext = null;
	
	// Type of new password form
	static public final int PASSWORD_APP_FIRST = 0;		// first execution, set new app password
	static public final int PASSWORD_APP_NOFIRST = 1;	// change app password
	static public final int PASSWORD_ALARM = 2;			// change alarm password
	
	/**
	 * Asks for a password through an AlertDialog. The password can be for the application or the alarm
	 * @param passType type pass to ask
	 * @retunr alert dialog built
	 */
	public AlertDialog askNewPassword(final int passType) {
	
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final View view;
		
		// Load different layout
		if (passType == PASSWORD_APP_FIRST){
    		view = LayoutInflater.from(mContext).inflate(R.layout.first_password_layout, null);
    		builder.setView(view);
    		builder.setTitle(R.string.wizard_first_title);
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.settings_pass_layout, null);
			builder.setView(view);
			builder.setTitle(R.string.sett_app_pass_title);
		}
		
		final AlertDialog alertDialog;
		
		// Steps only for no PASSWORD_APP_FIRST types
		if (passType != PASSWORD_APP_FIRST) {
			String currentPassword = null;
			
			currentPassword = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDAPP, null);
			
			if (passType != PASSWORD_ALARM){
				builder.setTitle(R.string.sett_app_pass_title);
				currentPassword = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDAPP, null);
			}else{
				
				// Show error, a telephone number for the alarm must be set before
				String currentAlarmNumber = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_NUMBERALARM, null);
				if (currentAlarmNumber == null) {
					showError(mContext.getResources().getString(R.string.error_settings_number_before));
					return null;
				}
				
				builder.setTitle(R.string.sett_vehicle_pass_title);
				currentPassword = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDALARM, null);
			}
			
			// If there is no previous password, remove that field from form, if it exists
			EditText editOld = (EditText)view.findViewById(R.id.edit_old_pass);
			if ((currentPassword == null) && (editOld != null)) {
				ViewGroup parent = (ViewGroup)editOld.getParent();
				parent.removeView(editOld);
				
				TextView titleOld = (TextView)view.findViewById(R.id.title_old_pass);
				parent.removeView(titleOld);
			}

			builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int whichButton) {
	
					// Get all editable fields to check them
					
					EditText editOld = (EditText)view.findViewById(R.id.edit_old_pass);
					String oldPassword;
					if (editOld != null) {
						oldPassword = editOld.getText().toString();
					} else {
						oldPassword = null;
					}
										
					EditText editNew = (EditText)view.findViewById(R.id.edit_new_pass);
					String newPassword = editNew.getText().toString();
					
					EditText editNewAgain = (EditText)view.findViewById(R.id.edit_new_pass_again);
					String newPasswordAgain = editNewAgain.getText().toString();
					
					String currentAppPassword = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDAPP, null);
					String currentVehiclePassword = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDALARM, null);
	
					// Wrong old password
					if ((oldPassword != null) && 
						(((passType != PASSWORD_ALARM) && (oldPassword.compareTo(currentAppPassword) != 0)) ||
						((passType == PASSWORD_ALARM) && (oldPassword.compareTo(currentVehiclePassword) != 0))) 
						){
													
						showError(mContext.getResources().getString(R.string.error_wrong_password));
					
					// New password doesn't match
					}else if (newPassword.compareTo(newPasswordAgain) != 0){
						
						showError(mContext.getResources().getString(R.string.error_password_mismatch));
			
					// All fields are right, continue
					}else{
	
						// App password changed, save it in preferences
						if (passType != PASSWORD_ALARM){
						
					        SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
					        SharedPreferences.Editor editor = preferences.edit();
					        editor.putString(MainActivity.PREFERENCES_PASSWORDAPP, newPassword);
					        editor.commit();
							
						// Alarm password changed, send SMS
					    // New password will be saved in preferences when SMS is successfully sent
						}else{
							
							Intent sentIntent = new Intent(MainActivity.SMS_SENT);
							Intent deliveryIntent = new Intent(MainActivity.SMS_DELIVERY);
							sentIntent.putExtra(MainActivity.COMMAND, Communicator.COMMAND_PASSWORD);
							sentIntent.putExtra(MainActivity.NEW_PASSWORD, newPassword);
							
							PendingIntent pendingSent = PendingIntent.getBroadcast(mContext, 0, sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
							PendingIntent pendingDelivery = PendingIntent.getBroadcast(mContext, 0, deliveryIntent, PendingIntent.FLAG_CANCEL_CURRENT);
							
							String[] args = new String[3];
							args[0] = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_PASSWORDALARM, null);
							args[1] = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE).getString(MainActivity.PREFERENCES_NUMBERALARM, null);
							args[2] = newPassword;
							
							Communicator.sendCommand(Communicator.COMMAND_PASSWORD, args, pendingSent, pendingDelivery);
							
							// Password only must be saved after SMS is successfully sent
							// Debugging version without SMS sending
							/*
			        		SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
			        		SharedPreferences.Editor editor = preferences.edit();
			        		editor.putString(MainActivity.PREFERENCES_PASSWORDALARM, newPassword);
			        		editor.commit();
			        		*/
						}						
					}
				}
			});
			
			builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			});
			
			alertDialog = builder.create();
		
		// Steps only for PASSWORD_APP_FIRST types
		} else {
			
	   		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	   	    	
    			public void onClick(DialogInterface dialog, int whichButton) {
    				// Do nothing, override onClick() in setOnShowListener() to prevent to dismiss alert dialog
    			}
    			
    		});
    		
    		// User can't be able to dismiss alert dialog
    		builder.setCancelable(false);
    		
    		alertDialog = builder.create();
    		
    		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

    		    @Override
    		    public void onShow(DialogInterface dialog) {

    		        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    		        b.setOnClickListener(new View.OnClickListener() {

    		            @Override
    		            public void onClick(View view) {
    	    				EditText editNew = (EditText)alertDialog.findViewById(R.id.edit_new_pass);
    	    				String newPassword = editNew.getText().toString();
    	    				
    	    				EditText editNewAgain = (EditText)alertDialog.findViewById(R.id.edit_new_pass_again);
    	    				String newPasswordAgain = editNewAgain.getText().toString();
    	    				
    	    				// Fields are wrong
    	    				if (newPassword.compareTo(newPasswordAgain) != 0){
    	    					showError(mContext.getResources().getString(R.string.error_password_mismatch));
    	    					
    	    				// Fields are right, continue
    	    				}else{
    	    					// Save it in preferences
    	    					SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
    	    				    SharedPreferences.Editor editor = preferences.edit();
    	    				    editor.putString(MainActivity.PREFERENCES_PASSWORDAPP, newPassword);
    	    				    editor.commit();
    	    				    
    	    				    //Dismiss once everything is OK.
    	    				    alertDialog.dismiss();
    	    				}
    		            }
    		        });
    		    }
    		});
		}

		// Instantiate all the EditText
		final EditText editOldPass = (EditText)view.findViewById(R.id.edit_old_pass);
		final EditText editNew = (EditText)view.findViewById(R.id.edit_new_pass);
		final EditText editNewAgain = (EditText)view.findViewById(R.id.edit_new_pass_again);
		
		// Create lister for each EditText, in order to enable/disable positive button
		final TextWatcher mTextEditorWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence s,
					int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {

				// All valid fields are fill, enable positive button
				if (
						((editOldPass != null) && 
						(editOldPass.getText().toString().compareTo("") != 0) && 
						(editNew.getText().toString().compareTo("") != 0) &&
						(editNewAgain.getText().toString().compareTo("") != 0)) 
						||
						((editOldPass == null) && 
						(editNew.getText().toString().compareTo("") != 0) &&
						(editNewAgain.getText().toString().compareTo("") != 0))
					)
				{
					alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
					
				// Some field is empty, disable positive button
				}else{
					alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}
				
			}
		};
		
		// Add listener to EditText fields
		if (editOldPass != null){
			editOldPass.addTextChangedListener(mTextEditorWatcher);
		}
		editNew.addTextChangedListener(mTextEditorWatcher);
		editNewAgain.addTextChangedListener(mTextEditorWatcher);
		
		alertDialog.show();
		alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		return alertDialog;
	}
	
	/**
	 * 
	 * @return
	 */
	public AlertDialog askNewNumber(boolean firstNumber) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final View view;
		if (firstNumber) {
			view = LayoutInflater.from(mContext).inflate(R.layout.first_number_layout, null);
			builder.setTitle(R.string.wizard_second_title);
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.setting_number_layout, null);
			builder.setTitle(R.string.sett_new_number);
		}
		builder.setView(view);
		
		// Load current alarm's phone number
		final EditText editNumber = (EditText)view.findViewById(R.id.edit_new_number);
        SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
		editNumber.setText(preferences.getString(MainActivity.PREFERENCES_NUMBERALARM, null));
		
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				EditText editNumber = (EditText)view.findViewById(R.id.edit_new_number);
				String newNumber = editNumber.getText().toString();
				
				// Save new number in preferences
		        SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
		        SharedPreferences.Editor editor = preferences.edit();
		        editor.putString(MainActivity.PREFERENCES_NUMBERALARM, newNumber);
		        editor.commit();
			}
		});

		if (firstNumber) {
			
    		// User can't be able to dismiss alert dialog
    		builder.setCancelable(false);
			
		} else {
			builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			});
		}
		
		final AlertDialog alertDialog = builder.create();
		
		// Create lister for each EditText, in order to enable/disable positive button
		final TextWatcher mTextEditorWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence s,
					int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {

				// All valid fields are fill, enable positive button
				if ((editNumber != null) && (editNumber.getText().toString().compareTo("") != 0))
				{
					alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
					
				// Some field is empty, disable positive button
				}else{
					alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				}
				
			}
		};

		editNumber.addTextChangedListener(mTextEditorWatcher);
		alertDialog.show();
		alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		return alertDialog;
	}
	
	
	/**
	 * Handles a selected option in a preferences tree
	 * @param preferenceScreen
	 * @param preference
	 */
	public void onHandlePreferenceTreeClick (PreferenceScreen preferenceScreen, final Preference preference) {
		
	final String appKey = mContext.getResources().getString(R.string.sett_key_app_pass);
	final String switchKey = mContext.getResources().getString(R.string.sett_key_app_pass_switch);
	final String vehicleKey = mContext.getResources().getString(R.string.sett_key_vehicle_pass);
	final String numberKey = mContext.getResources().getString(R.string.sett_key_vehicle_number);
		
	final boolean isAppPassword = (preference.getKey().compareTo(appKey) == 0);
	final boolean isSwitch = (preference.getKey().compareTo(switchKey) == 0);
	final boolean isVehiclePassword = (preference.getKey().compareTo(vehicleKey) == 0);
	final boolean isVehicleNumber = (preference.getKey().compareTo(numberKey) == 0);
	
	// Change app password or alarm password
	if (isAppPassword || isVehiclePassword){
		
		int type = isAppPassword ? PASSWORD_APP_NOFIRST : PASSWORD_ALARM;
		askNewPassword(type);
		
	// Change alarm's phone number
	} else if (isVehicleNumber) {

		askNewNumber(false);
		
	// Switch to ask or not for app password
	} else if (isSwitch) {

		CheckBoxPreference checkbox = (CheckBoxPreference) preference;
		
		// Save it in preferences
		SharedPreferences preferences = mContext.getSharedPreferences(MainActivity.APP_TAG, mContext.MODE_PRIVATE);
	    SharedPreferences.Editor editor = preferences.edit();
	    editor.putBoolean(MainActivity.PREFERENCES_ASKPASSWORD, checkbox.isChecked());
	    editor.commit();
	}
}
	
	private void showError(String errorMsg){
		
		if (mContext!=null){
		
		    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		    
		    builder.setTitle(R.string.error_title);
		    builder.setMessage(errorMsg);
		    builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		        }
		    });
		    
		    builder.create().show();
		}
	}

}
