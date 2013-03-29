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
	static final int COMMAND_CLIMATEON = 2;
	static final int COMMAND_CLIMATEOFF = 3;
	static final int COMMAND_ENGINEON = 4;
	static final int COMMAND_ENGINEOFF = 5;
	static final int COMMAND_IGNITIONON = 6;
	static final int COMMAND_IGNITIONOFF = 7;
	static final int COMMAND_LOCATIONWEB = 8;
	static final int COMMAND_LOCATIONGPRMC = 9;
	static final int COMMAND_LOCATIONGPSD = 10;
	static final int COMMAND_LOCATIONGPSM = 11;
	static final int COMMAND_LOCATIONPARKING = 12;
	static final int COMMAND_SENSORON = 13;
	static final int COMMAND_SENSOROFF = 14;
	static final int COMMAND_AUTOMATICON = 15;
	static final int COMMAND_AUTOMATICOFF = 16;
	static final int COMMAND_SPEEDON = 17;
	static final int COMMAND_SPEEDOFF = 18;
	static final int COMMAND_CALL = 19;
	static final int COMMAND_IMEI = 20;
	static final int COMMAND_RESET = 21;
	static final int COMMAND_HARDRESET = 22;
	static final int COMMAND_SETDEVICES = 23;
	static final int COMMAND_GETDEVICES = 24;
	static final int COMMAND_PASSWORD = 25;
	static final int COMMAND_SENSIBILITY = 26;
	static final int COMMAND_STATE = 27;
	
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
		case COMMAND_CLIMATEON:
			text = text + String.format("air start %s#", args[2]);
			break;
		case COMMAND_CLIMATEOFF:
			text = text + "air off#";
			break;
		case COMMAND_ENGINEON:
			text = text + String.format("engine start %s#", args[2]);
			break;
		case COMMAND_ENGINEOFF:
			text = text + "engine off#";
			break;
		case COMMAND_IGNITIONON:
			text = text + "K#";
			break;
		case COMMAND_IGNITIONOFF:
			text = text + "STOP#";
			break;
		case COMMAND_LOCATIONWEB:
			text = text + "GPSW#";
			break;
		case COMMAND_LOCATIONGPRMC:
			text = text + "GPS#";
			break;
		case COMMAND_LOCATIONGPSD:
			text = text + "GPSD#";
			break;
		case COMMAND_LOCATIONGPSM:
			text = text + "GPSM#";
			break;
		case COMMAND_LOCATIONPARKING:
			text = text + "T#";
			break;
		case COMMAND_SENSORON:
			text = text + "H#";
			break;
		case COMMAND_SENSOROFF:
			text = text + "N#";
			break;
		case COMMAND_AUTOMATICON:
			text = text + "Aon#";
			break;
		case COMMAND_AUTOMATICOFF:
			text = text + "Aoff#";
			break;
		case COMMAND_SPEEDON:
			text = text + String.format("SPD%s,%s#", args[2], args[3]);
			break;
		case COMMAND_SPEEDOFF:
			text = text + String.format("SPD000#");
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
		//SmsManager.getDefault().sendTextMessage(args[1], null, text, sentIntent, deliveryIntent);
		

		// Log message, debugging version
		//Log.d(MainActivity.APP_TAG,"Sent SMS to "+args[1]+" with message "+text);
		// Message to show, debugging version
		MainActivity.mMessage = "Sent SMS to number "+args[1]+" with message \""+text+"\"";

		// Send intent, debugging version
		try {
			sentIntent.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}

	}
}
