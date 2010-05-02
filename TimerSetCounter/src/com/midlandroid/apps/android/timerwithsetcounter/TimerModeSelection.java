package com.midlandroid.apps.android.timerwithsetcounter;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

public class TimerModeSelection {
	private SelectionListener listener;
	private PopupWindow popup;
	private ListView listView;
	private View parentLayout;
	
	public TimerModeSelection(Activity parent, int parentLayoutId) {
		_init();
		
		LayoutInflater inflater = parent.getLayoutInflater();
		View mView = inflater.inflate(R.layout.timer_mode_selection,
				(ViewGroup)parent.findViewById(R.id.timer_mode_selc_layout));
		
		popup = new PopupWindow(mView,
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, false);
		
		parentLayout = (View)parent.findViewById(parentLayoutId);
		
		
		// TESTING
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent,
				R.id.list_item_textview);
		listView.setAdapter(adapter);
		adapter.add("SimpleCountUp");
		// TESTING
	}
	
	public void showPopup() {
		popup.showAtLocation(parentLayout, Gravity.CENTER, 0, 0);
	}
	
	public void setSelectionListener(SelectionListener listener) {
		this.listener = listener;
	}

	///////////////////////////////
	// Private methods
	///////////////////////////////
	private void _init() {
		listener = null;
	}
	
	/**
	 * Timer mode selection listener.
	 * @author Jason Del Ponte
	 */
	public interface SelectionListener {
		public void selectionMade(final String selection);
	}
}
