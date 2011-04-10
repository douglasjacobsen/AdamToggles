package com.adam.toggles;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SystemToggles extends Activity {
	
	final String TAG = "Toggles"; 
	final String pq_tag = "pqSwitch";
	final String phone_tag = "phoneButton";
	final String sdcard_tag = "sdcardSwitch";
	final String led_tag = "ledSwitch";
	final String physical_button_tag = "physicalButton";
	
	Context context;
	
	final int duration = Toast.LENGTH_SHORT;
	final int DIALOG_SELECT_PHYSICAL_BUTTON = 0;
	final int DIALOG_REBOOT = 1;
	final int DIALOG_REBOOT_REQUESTED = 2;
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	
	String command;
	ToggleButton phoneButton, ledButton, pqButton;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.main);
        setContentView(R.layout.system_toggles);
        boolean state;
        
        context = this;
        
        this.phoneButton = (ToggleButton)this.findViewById(R.id.button_phone_toggle);
        this.ledButton = (ToggleButton)this.findViewById(R.id.button_led_toggle);
        this.pqButton = (ToggleButton)this.findViewById(R.id.button_pq_toggle);
        
        sPrefs = getSharedPreferences(prefs_name,0);
        
        this.phoneButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		togglePhone();
        	}
        });
        state = sPrefs.getBoolean(phone_tag, false);
        this.phoneButton.setChecked(state);
        
        this.ledButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		toggleLed();
        	}
        });
        state = sPrefs.getBoolean(led_tag,false);
        ledButton.setChecked(state);
        
        pqButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SharedPreferences.Editor editor = sPrefs.edit();
            	boolean state = sPrefs.getBoolean(pq_tag,false);
                String PQi = "/sys/devices/platform/pixel_qi_screen_ctrl/state";
                try{
                	if(state){
                		run.Com("echo 1 > " + PQi);
                		editor.putBoolean(pq_tag,!state);
                		
                	} else {
                		run.Com("echo 0 > " + PQi);
                		editor.putBoolean(pq_tag,!state);
                	}
                	editor.commit();
                } catch (Exception e){ 
                }
            }
        });
        state = sPrefs.getBoolean(pq_tag,false);
        pqButton.setChecked(state);
    }
        
    public void toggleLed(){
    	String temp;
    	String command;
    	SharedPreferences.Editor editor = sPrefs.edit();
    	Toast toast;
    	
    	temp = sPrefs.getString(led_tag,"heartbeat");
    	
    	if(temp == "none"){
    		try{
    			command = "echo heartbeat > /sys/class/leds/cpu/trigger";
    			run.suCom(command);
    			editor.putString(led_tag,"heartbeat");
    			Log.w(TAG,"Led toggle is off");
    			toast = Toast.makeText(context,"LED Heartbeat is now on.",duration);
    			toast.show();
    		} catch (Exception e){
    			
    		}
    	} else {
    		try{
    			command = "echo none > /sys/class/leds/cpu/trigger";
    			run.suCom(command);
    			editor.putString(led_tag,"none");
    			Log.w(TAG,"Led toggle is on");
    			toast = Toast.makeText(context,"LED Heartbeat is now off.",duration);
    			toast.show();    			
    		} catch (Exception e){
    			
    		}
    	}
    	editor.commit();
    }
    
    public void togglePhone(){
    	int temp;
    	String command;
		SharedPreferences.Editor editor = sPrefs.edit();
		Toast toast;
		
		temp = sPrefs.getInt(phone_tag, 0);
		
		if(temp == 0){
			try{
				command = "mv /system/app/Phone.apk /system/app/Phone.bak";
				run.suCom(command);
				command = "mv /system/app/TelephonyProvider.apk /system/app/TelephonyProvider.bak";
				run.suCom(command);
				command = "mv /system/etc/permissions/android.hardware.telephony.gsm.xml /system/etc/permissions/android.hardware.telephony.gsm.bak";
				run.suCom(command);
				command = "if [ -a /system/etc/Scripts/handheld_core_hardware.off ]; then cat /system/etc/Scripts/handheld_core_hardware.off > /system/etc/permissions/handheld_core_hardware.xml; fi";
				run.suCom(command);
				editor.putInt(phone_tag, 1);
				Log.w(TAG,"Phone toggle is on");
				toast = Toast.makeText(context,"Phone services are now disabled.",duration);
    			toast.show();    			
			} catch (Exception e){
			}
		} else {
			try{
				command = "mv /system/app/Phone.bak /system/app/Phone.apk";
				run.suCom(command);
				command = "mv /system/app/TelephonyProvider.bak /system/app/TelephonyProvider.apk";
				run.suCom(command);
				command = "mv /system/etc/permissions/android.hardware.telephony.gsm.bak /system/etc/permissions/android.hardware.telephony.gsm.xml";
				run.suCom(command);
				command = "if [ -a /system/etc/Scripts/handheld_core_hardware.on ]; then cat /system/etc/Scripts/handheld_core_hardware.on > /system/etc/permissions/handheld_core_hardware.xml; fi";
				run.suCom(command);
				editor.putInt(phone_tag, 0);
				Log.w(TAG,"Phone toggle is off");
				toast = Toast.makeText(context,"Phone services are now enabled.",duration);
    			toast.show();
			} catch (Exception e){
			}
		}
		editor.commit();
    }
}