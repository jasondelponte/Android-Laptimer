package com.midlandroid.apps.android.laptimer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
	
	
	/**
	 * Writes the contents provided to the file path.  The user
	 * will be given the chance change the file path, and 
	 * if the file already exists overwrite it.
	 * @param context
	 * @param fileName
	 * @param outText
	 */
	public void showOutFileAlertPromptAndWriteTo(Context context,
			final String fileName, final String outText) {	
		Log.d(LOG_TAG, "showOutFileAlertPromptAndWriteTo");
    	
    	// Save off the output text
    	this.context = context;
    	this.outText = outText;
		this.outFilePath = fileName;
    	
    	// Build the full path
    	final  EditText input = new EditText(context);
    	input.setText(outFilePath);
    	
    	// Ask the user to confirm the file name before writings
    	UIUtil.showAlertPrompt(context,
				"File Selection",
				"Path to write to:",input,
				"OK", "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInf, int which) {
						if (DialogInterface.BUTTON_POSITIVE == which) {
							_writeTextToFlle(false);
						} else if (DialogInterface.BUTTON_NEGATIVE == which) {
							// Cancel, nothing to do.
						}
					}
				});
    }
	

	/**
	 * Displays a prompt to the user informing them that the
	 * file path provided already exists, and questions them
	 * if they want to overwrite it.
	 */
    private void _showPromptToOverwriteFile() {
    	final  TextView input = new TextView(context);    	
    	input.setText(outFilePath);
    	input.setEnabled(false);
    	
    	UIUtil.showAlertPrompt(context,
				"File Exists",
				"File exists overwrite it?",input,
				"OK", "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInf, int which) {
						if (DialogInterface.BUTTON_POSITIVE == which) {
							_writeTextToFlle(true);
						} else if (DialogInterface.BUTTON_NEGATIVE == which) {
							// Cancel, nothing to do.
						}
					}
				});
    }

    
    /**
     * Writes the file contents to the path provided.
     * If the overwrite flag is and a file by that same name
     * exist it will be overwritten if the user has permissions
     * to do so.
     * @param overwrite
     */
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
    
    
    /**
     * An error has occurred and the user needs to be informed
     * @param e
     */
    private void _showErrorCreatingOutfileAlert(IOException e) {
    	UIUtil.showAlertPrompt(context,
				"Error!",
				"Failed to save file because "+ e.getMessage(),
				"OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInf, int which) {
						if (DialogInterface.BUTTON_POSITIVE == which) {
							// Nothing to do but to allow the use to acknowledge the error.
						}
					}
				});
    }
}