package com.provauto.bretto.wcontrol;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Class to manage SMS sending and its result
 */
public class Communicator {
	
	// List of command identifiers
	static final int COMMAND_ASSEMBLEON = 0;
	static final int COMMAND_ASSEMBLEOFF = 1;
	static final int COMMAND_IMMOBILIZEON = 2;
	static final int COMMAND_IMMOBILIZEOFF = 3;
	static final int COMMAND_SENSORON = 4;
	static final int COMMAND_SENSOROFF = 5;
	static final int COMMAND_LOCATIONWEB = 6;
	static final int COMMAND_LOCATIONGPRMC = 7;
	static final int COMMAND_LOCATIONGPSD = 8;
	static final int COMMAND_SIRENON = 9;
	static final int COMMAND_SIRENOFF = 10;
	static final int COMMAND_SAVEON = 11;
	static final int COMMAND_SAVEOFF = 12;
	static final int COMMAND_CALL = 13;
	static final int COMMAND_IMEI = 14;
	static final int COMMAND_RESET = 15;
	static final int COMMAND_HARDRESET = 16;
	static final int COMMAND_SETDEVICES = 17;
	static final int COMMAND_GETDEVICES = 18;
	static final int COMMAND_PASSWORD = 19;
	static final int COMMAND_SENSIBILITY = 20;
	static final int COMMAND_STATE = 21;
	

	/**
	 * Sends a SMS with a command and its parameters
	 * @param command command identifier
	 * @param args list of arguments. Fixed positions: 0 alarm password; 1 alarm number; 2 and following, command parameters.
	 * @param sentIntent intent to notify sending result
	 * @param deliveryIntent intent to notify delivering result
	 */
	static public void sendCommand(int command, String[] args, PendingIntent sentIntent, PendingIntent deliveryIntent) {
		String text = String.format("*%s*", args[0]);
		
		switch (command){
		case COMMAND_ASSEMBLEON:
			text = text + "S#";
			break;
		case COMMAND_ASSEMBLEOFF:
			text = text + "C#";
			break;
		case COMMAND_IMMOBILIZEOFF:
			text = text + "STOP#";
			break;
		case COMMAND_IMMOBILIZEON:
			text = text + "K#";
			break;
		case COMMAND_SENSORON:
			text = text + "H#";
			break;
		case COMMAND_SENSOROFF:
			text = text + "N#";
			break;
		case COMMAND_LOCATIONWEB:
			text = text + "P#";
			break;
		case COMMAND_LOCATIONGPRMC:
			text = text + "GPS#";
			break;
		case COMMAND_LOCATIONGPSD:
			text = text + "GPSD#";
			break;
		case COMMAND_SIRENON:
			text = text + "SIRENON#";
			break;
		case COMMAND_SIRENOFF:
			text = text + "SIRENOFF#";
			break;
		case COMMAND_SAVEON:
			text = text + "SL*O#";
			break;
		case COMMAND_SAVEOFF:
			text = text + "SL*C#";
			break;
		case COMMAND_CALL:
			text = text + String.format("VM%s#", args[2]);
			break;
		case COMMAND_IMEI:
			text = text + "IMEI#";
			break;
		case COMMAND_RESET:
			text = text + "Z#";
			break;
		case COMMAND_HARDRESET:
			text = text + "V#";
			break;
		case COMMAND_SETDEVICES:						
			switch(args.length){
				case 3:
					text = text + String.format("A%s#", args[2]);
					break;
				case 4:
					text = text + String.format("A%s*B%s#", args[2], args[3]);
					break;
				case 5:
					text = text + String.format("A%s*B%s*C%s#", args[2], args[3], args[4]);
					break;
			}
			break;
		case COMMAND_GETDEVICES:
			text = text + "YY#";
			break;
		case COMMAND_PASSWORD:
			text = text + String.format("E%s#", args[2]);
			break;
		case COMMAND_SENSIBILITY:
			text = text + String.format("VS*%s#", args[2]);
			break;
		case COMMAND_STATE:
			text = text + "X#";
			break;
		default:
		}

		// Send SMS
		SmsManager.getDefault().sendTextMessage(args[1], null, text, sentIntent, deliveryIntent);
		

		// Log message, debugging version
		//Log.d(MainActivity.APP_TAG,"Sent SMS to "+args[1]+" with message "+text);
		// Message to show, debugging version
		//MainActivity.mMessage = "Sent SMS to number "+args[1]+" with message \""+text+"\"";
/*
		// Send intent, debugging version
		try {
			sentIntent.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
*/
	}
}
