package com.midlandroid.apps.android.laptimer;

import com.midlandroid.apps.android.laptimer.R;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class Preferences extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        
        Resources res = getResources();
        EditTextPreference myEditTextPreference = (EditTextPreference) findPreference(
        		res.getString(R.string.pref_timer_start_delay_key)); 
        EditText myEditText = (EditText)myEditTextPreference.getEditText();
        myEditText.setKeyListener(DigitsKeyListener.getInstance(false,false)); 
    }
}
