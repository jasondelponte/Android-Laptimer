package com.midlandroid.apps.android.timerwithsetcounter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.TextView;

public class DelayTimeCountDown extends Activity implements TimerServiceUpdateUIListener {
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
        notifyTimerServiceOfExit = true;

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
	public void addLapToUI(LapData lapData) {
	}

	@Override
	public void clearLapList() {
	}

	@Override
	public void updateTimerUI(final long currTime, final long lapTime, final int setCount) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currTimeTxt.setText(Long.valueOf(currTime/1000).toString());
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
    
    private void _connectToService() {
    	TimerService srvc = TimerService.getService();
    	if (srvc!=null) {
    		if (srvc.getDelayTimerUIListner()==null)
        		srvc.setDelayTimerUIListener(this);
    		if (srvc.getDelayTimerMessenger()==null)
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
