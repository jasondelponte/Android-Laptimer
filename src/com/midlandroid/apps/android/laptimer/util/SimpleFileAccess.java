package com.midlandroid.apps.android.laptimer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class SimpleFileAccess {
	private static final String LOG_TAG = SimpleFileAccess.class.getSimpleName();
	
	private Context context;
	private String outFilePath;
	private String outText;
	
	
	public void showOutFileAlertPromptAndWriteTo(Context context, final String fileName, final String outText) {	
		Log.d(LOG_TAG, "showOutFileAlertPromptAndWriteTo");
    	
    	// Save off the output text
    	this.context = context;
    	this.outText = outText;
		this.outFilePath = fileName;
    	
    	// Build the full path
    	final  EditText input = new EditText(context);   
    	    	
    	// Create the alert that will prompt the user
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	alert.setTitle("File Selection");
    	alert.setMessage("Path to write to:");
    	input.setText(outFilePath);
    	alert.setView(input);

    	// Create the click listeners for user response
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				_writeTextToFlle(false);
			}
		});
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Canceled, do nothing.
			}
		});

    	// Display the alert
    	alert.show();
    }
	

    private void _showPromptToOverwriteFile() {
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	
    	alert.setTitle("File Exists");
    	alert.setMessage("File exists overwrite it?");
    	
    	final  TextView input = new TextView(context);    	
    	input.setText(outFilePath);
    	alert.setView(input);
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				_writeTextToFlle(true);
			}
		});
    	
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Canceled, do nothing.
			}
		});
    	
    	alert.show();
    }

    private void _writeTextToFlle(boolean overwrite) {
		final File file = new File(outFilePath);
		if (file.exists()==true && !overwrite) {
			_showPromptToOverwriteFile();
		} else {
			// Make sure we can write the file first
			try {
				file.createNewFile();
			} catch (IOException e) {
				_showErrorCreatingOutfileAlert(e);
			}
			
			// create a background thread to write the file to
	    	final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						FileOutputStream os = new FileOutputStream(file);
						os.write(SimpleFileAccess.this.outText.getBytes());
						
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
	    	});
	    	thread.start();
		}
	}
    
    private void _showErrorCreatingOutfileAlert(IOException e) {
    	final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    	
    	alert.setTitle("Error!");
    	alert.setMessage("Failed to save file because "+ e.getMessage());
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
    	
    	alert.show();
    }
}