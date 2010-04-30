package com.midlandroid.apps.android.timerwithsetcounter;

import com.midlandroid.apps.android.timerwithsetcounter.timerservice.LapData;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.TimerService;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.DelayTimerUpdateUIListener;
import com.midlandroid.apps.android.timerwithsetcounter.timerservice.uilistener.TimerUpdateUIListener;
import com.midlandroid.apps.android.timerwithsetcounter.util.MessageId;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.KeyEvent;
import android.widget.TextView;

public class DelayTimeCountDown extends Activity implements DelayTimerUpdateUIListener {
	private TextView currTimeTxt;
	private Messenger myMessenger;
	private boolean notifyTimerServiceOfExit;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delay_timer_count_down);
        
        currTimeTxt = (TextView)findViewById(R.id.timer_delay_countdown);
        myMessenger = new Messenger(myHandler);
        notifyTimerServiceOfExit = false;

        _connectToService();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	_connectToService();
    }
    
    @Override
    public void onPause() {
    	super.onPause();

    	_disconnectFromService();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (notifyTimerServiceOfExit) {
	    	TimerService srvc = TimerService.getService();
	    	if (srvc!=null) {
	    		Handler handler = srvc.getMessageHandler();
		    	Message msg = Message.obtain();
		    	msg.arg1 = MessageId.SRC_DELAYTIMECOUNTDOWN;
		    	msg.arg2 = MessageId.DelayTimerCountDownCmd.CMD_STOP_TIMER;
		    	handler.sendMessage(msg);
	    	}
    	}
    	
    	_disconnectFromService();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
    		notifyTimerServiceOfExit = true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }

	@Override
	public void updateDelayTimerUI(final long time) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currTimeTxt.setText(Long.valueOf(time/1000).toString());
			}
		});
	}
	
	private Handler myHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.arg1) {
    		case MessageId.SRC_TIMERSERVICE:
    			switch(msg.arg2) {
    			case MessageId.TimerServiceCmd.CMD_FINISH_TIMER_DELAY_UI:
    				notifyTimerServiceOfExit = false;
    				finish();
    			}
    			break;
    		}
    	}
	};
    
	/////////////////////////////////////
	// Service Connection management
	/////////////////////////////////////
    private void _connectToService() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
    		srvc.setDelayTimerUIListener(this);
			srvc.setDelayTimerMessenger(myMessenger);
    	}
    }
    
    private void _disconnectFromService() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
    		srvc.setDelayTimerUIListener(null);
    		srvc.setDelayTimerMessenger(null);
    	}
    }
}
