package com.adam.toggles;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SystemToggles extends Activity {//{{{

	final String TAG = "SystemToggles"; 
	final String pq_tag = "pqSwitch";
	final String phone_tag = "phoneButton";
	final String led_tag = "ledSwitch";
	final String data2loop_tag = "data2loopSwitch";
	final String reboot_tag = "need_reboot";

	Context context;

	final int duration = Toast.LENGTH_SHORT;
	final int DIALOG_DATA2LOOP = 0;

	private SharedPreferences sPrefs;
	private static final String prefs_name = "toggle-prefs";

	NativeTasks run = new NativeTasks();

	ToggleButton phoneButton, ledButton, pqButton, data2loopbutton;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {//{{{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_toggles);
		boolean state;

		context = this;

		this.phoneButton = (ToggleButton)this.findViewById(R.id.button_phone_toggle);
		this.ledButton = (ToggleButton)this.findViewById(R.id.button_led_toggle);
		this.pqButton = (ToggleButton)this.findViewById(R.id.button_pq_toggle);
		this.data2loopbutton = (ToggleButton)this.findViewById(R.id.button_data2loop_toggle);

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

		this.data2loopbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				showDialog(DIALOG_DATA2LOOP);
			}
		});
		state = sPrefs.getBoolean(data2loop_tag, false);
		this.data2loopbutton.setChecked(state);

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
	}//}}}

	@Override
		protected Dialog onCreateDialog(int id, Bundle args){//{{{
			super.onCreateDialog(id,args);
			boolean state;
			Dialog dialog = null;
			AlertDialog alert;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			switch(id){
				case DIALOG_DATA2LOOP:
					state = sPrefs.getBoolean(data2loop_tag,false);
					if(state){
						Toast toast = Toast.makeText(context,"Data2Loop is already toggled. You can only un-toggle this by wiping data and cache in recovery.",duration);
						toast.show();
						data2loopbutton.setChecked(true);
						return null;
					} else {
						builder.setTitle("Toggle Data2Loop:");
						builder.setMessage("Are you sure you want to toggle data2loop now? This is only un-toggleable by wiping data and cache in recovery.");
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which){
								toggleData2Loop();
							}
						});
						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which){
								clearData2Loop();
							}
						});
						alert = builder.create();
						dialog = alert;
					}
					break;
			}
			return dialog;
		}//}}}

	public void toggleLed(){//{{{
		boolean temp;
		String command;
		SharedPreferences.Editor editor = sPrefs.edit();
		Toast toast;

		temp = sPrefs.getBoolean(led_tag,false);

		if(temp){
			try{
				command = "echo heartbeat > /sys/class/leds/cpu/trigger";
				run.suCom(command);
				editor.putBoolean(led_tag,false);
				Log.w(TAG,"Led toggle is off");
				toast = Toast.makeText(context,"LED Heartbeat is now on.",duration);
				toast.show();
			} catch (Exception e){

			}
		} else {
			try{
				command = "echo none > /sys/class/leds/cpu/trigger";
				run.suCom(command);
				editor.putBoolean(led_tag,true);
				Log.w(TAG,"Led toggle is on");
				toast = Toast.makeText(context,"LED Heartbeat is now off.",duration);
				toast.show();    			
			} catch (Exception e){

			}
		}
		editor.commit();
	}//}}}

	public void togglePhone(){//{{{
		boolean temp;
		String command;
		SharedPreferences.Editor editor = sPrefs.edit();
		Toast toast;

		temp = sPrefs.getBoolean(phone_tag, false);

		if(!temp){
			try{
				command = "mv /system/app/Phone.apk /system/app/Phone.bak";
				run.suCom(command);
				command = "mv /system/app/TelephonyProvider.apk /system/app/TelephonyProvider.bak";
				run.suCom(command);
				command = "mv /system/etc/permissions/android.hardware.telephony.gsm.xml /system/etc/permissions/android.hardware.telephony.gsm.bak";
				run.suCom(command);
				command = "if [ -a /system/etc/Scripts/handheld_core_hardware.off ]; then cat /system/etc/Scripts/handheld_core_hardware.off > /system/etc/permissions/handheld_core_hardware.xml; fi";
				run.suCom(command);
				editor.putBoolean(phone_tag, true);
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
				editor.putBoolean(phone_tag, false);
				Log.w(TAG,"Phone toggle is off");
				toast = Toast.makeText(context,"Phone services are now enabled.",duration);
				toast.show();
			} catch (Exception e){
			}
		}
		editor.commit();
	}//}}}

	public void toggleData2Loop(){//{{{
		boolean state;
		SharedPreferences.Editor editor = sPrefs.edit();
		Toast toast;

		state = sPrefs.getBoolean(data2loop_tag,false);

		if(!state){
			toast = Toast.makeText(context,"Data2Loop is now toggled. Please reboot to finish turning this on.",duration);
			editor.putBoolean(data2loop_tag,true);
			editor.putBoolean(reboot_tag,true);
			editor.commit();
			toast.show();
		}
	}//}}}
	
	public void clearData2Loop(){
		boolean state;
		
		state = sPrefs.getBoolean(data2loop_tag, false);
		
		if(state==false){
			data2loopbutton.setChecked(false);
		}
	}
}//}}}
