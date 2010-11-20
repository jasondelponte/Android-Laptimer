package com.midlandroid.apps.android.laptimer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public final class UIUtil {
	
	/**
	 * A utility to display an alert message to the user message
	 * to the user
	 * @param context
	 * @param title
	 * @param message
	 * @param posTxt
	 * @param posHandler
	 * @param negTxt
	 * @param negHandler
	 */
	public static  void showAlertPrompt(Context context,
			final String title, final String message,
			final String posTxt, final String negTxt,
			final DialogInterface.OnClickListener clickListener) {
		// Create the alert that will prompt the user
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	alert.setTitle(title);
    	alert.setMessage(message);

    	// Create the click listeners for user response
    	alert.setPositiveButton(posTxt, clickListener);
    	alert.setNegativeButton(negTxt, clickListener);

    	// Display the alert
    	alert.show();
	}
	
	
	/**
	 * Displays an alert dialog to the use with a message.  Only a single button
	 *  is provided to acknowledge the message,
	 * @param context
	 * @param title
	 * @param message
	 * @param posTxt
	 * @param clickListener
	 */
	public static  void showAlertPrompt(Context context,
			final String title, final String message,
			final String posTxt,
			final DialogInterface.OnClickListener clickListener) {
		// Create the alert that will prompt the user
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	alert.setTitle(title);
    	alert.setMessage(message);

    	// Create the click listeners for user response
    	alert.setPositiveButton(posTxt, clickListener);

    	// Display the alert
    	alert.show();
	}
	
	/**
	 * A utility to display an alert message to the user which contains
	 * a view to be displayed as well.
	 * @param context
	 * @param title
	 * @param message
	 * @param view
	 * @param posTxt
	 * @param negTxt
	 * @param clickListener
	 */
	public static  void showAlertPrompt(Context context,
			final String title, final String message, final View view,
			final String posTxt, final String negTxt,
			final DialogInterface.OnClickListener clickListener) {
		// Create the alert that will prompt the user
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	alert.setTitle(title);
    	alert.setMessage(message);
    	alert.setView(view);

    	// Create the click listeners for user response
    	alert.setPositiveButton(posTxt, clickListener);
    	alert.setNegativeButton(negTxt, clickListener);

    	// Display the alert
    	alert.show();
	}
}
