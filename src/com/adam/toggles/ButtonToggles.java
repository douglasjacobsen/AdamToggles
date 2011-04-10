package com.adam.toggles;

import java.io.ByteArrayOutputStream;//{{{
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
import android.widget.Button;
import android.widget.Toast;//}}}

public class ButtonToggles extends Activity {//{{{
	
	final String TAG = "ButtonToggles"; 
	final String phone_tag = "phoneButton";
	final String sdcard_tag = "sdcardSwitch";
	final String led_tag = "ledSwitch";
	final String physical_button_tag = "physicalButton";
	final String reboot_tag = "need_reboot";
	
	Context context;
	
	final int SEARCH_BUTTON = 0;
	final int HOME_BUTTON = 1;
	final int MENU_BUTTON = 2;
	final int CAPBACK_BUTTON = 3;
	final int VOLUP_BUTTON = 4;
	final int VOLDOWN_BUTTON = 5;
	final int PHYSBACK_BUTTON = 6;

	final int duration = Toast.LENGTH_SHORT;
	final int DIALOG_SELECT_PHYSICAL_BUTTON = 0;
	final int DIALOG_REBOOT = 1;
	final int DIALOG_REBOOT_REQUESTED = 2;
	
	final int nButtons = 7;
	final int nTypes = 9;

	final String[][] button_tags = {
		{"search_wake","search_back","seach_power","search_home","search_menu","search_search","search_camera","search_call","search_endcall"},
		{"home_wake","home_back","seach_power","home_home","home_menu","home_search","home_camera","home_call","home_endcall"},
		{"menu_wake","menu_back","seach_power","menu_home","menu_menu","menu_search","menu_camera","menu_call","menu_endcall"},
		{"capback_wake","capback_back","seach_power","capback_home","capback_menu","capback_search","capback_camera","capback_call","capback_endcall"},
		{"volup_wake","volup_back","seach_power","volup_home","volup_menu","volup_search","volup_camera","volup_call","volup_endcall"},
		{"voldown_wake","voldown_back","seach_power","voldown_home","voldown_menu","voldown_search","voldown_camera","voldown_call","voldown_endcall"},
		{"physback_wake","physback_back","seach_power","physback_home","physback_menu","physback_search","physback_camera","physback_call","physback_endcall"}};

	final String[] physical_types = {"Wake", "Back", "Power", "Home", "Menu", "Search", "Camera", "Call", "Endcall"};

	boolean[][] toggled;
	boolean[] temp_toggled;
	final String[] button_files = {"qwerty","qwerty","qwerty","qwerty","gpio-keys","gpio-keys","gpio-keys"};
	final String[] button_names = {"Search","Home","Menu","Capacitive Back","Volume up","Volume down", "Hardware Back"};
	final int[] button_codes = {217, 102, 139, 158, 115, 114, 158}; 

	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	int curButton;

    ByteArrayOutputStream inpbuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream errbuffer = new ByteArrayOutputStream();
	
	String command;
	Button search, home, menu, capback, volup, voldown, physback;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {//{{{
		int i, j;
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.button_toggles);
        sPrefs = getSharedPreferences(prefs_name,0);
        context = this;

		toggled = new boolean[nButtons][nTypes];
		temp_toggled = new boolean[nTypes];
		for(i = 0; i < nButtons; i++){
			for(j = 0; j < nTypes; j++){
				toggled[i][j] = sPrefs.getBoolean(button_tags[i][j],false);
			}
		}
    
		this.search = (Button)this.findViewById(R.id.button_search);
		this.home = (Button)this.findViewById(R.id.button_home);
		this.menu = (Button)this.findViewById(R.id.button_menu);
		this.capback = (Button)this.findViewById(R.id.button_capback);
		this.volup = (Button)this.findViewById(R.id.button_volup);
		this.voldown = (Button)this.findViewById(R.id.button_voldown);
		this.physback = (Button)this.findViewById(R.id.button_physback);
        
        this.search.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = SEARCH_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.home.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = HOME_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.menu.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = MENU_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.capback.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = CAPBACK_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.volup.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = VOLUP_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.voldown.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = VOLDOWN_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

        this.physback.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
				curButton = PHYSBACK_BUTTON;
				Log.w(TAG,"curButton = "+curButton);
				for(int i = 0; i < nTypes; i++){
					temp_toggled[i] = toggled[curButton][i];
				}
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });

    }//}}}
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args){//{{{
        super.onCreateDialog(id,args);
        Dialog dialog = null;
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            
        switch(id){
            case DIALOG_SELECT_PHYSICAL_BUTTON:
            	
                builder.setTitle("Select Mappings For "+button_names[curButton]+":");
                builder.setMultiChoiceItems(physical_types, temp_toggled, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    	temp_toggled[which] = isChecked;
						toggled[curButton][which] = isChecked;
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						togglePhysicalButtons();
                        
                    	Toast toast = Toast.makeText(context,button_names[curButton]+" button maps are set up now. Reboot for them to take effect.",duration);
                    	toast.show();
					}
				});
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which){
                		for(int i = 0; i < nTypes; i++){
                			temp_toggled[i] = sPrefs.getBoolean(button_tags[curButton][i], false);
                			toggled[curButton][i] = temp_toggled[i];
                		}
                	}
                });
                alert = builder.create();
                dialog = alert;
                break;
        }
        return dialog;
    }//}}} 
   
    public void togglePhysicalButtons(){//{{{
		int i, j;
    	String command;
		String gpio_com, qwerty_com;
    	SharedPreferences.Editor editor = sPrefs.edit();

    	try{
    		command = "echo 'key 62 POWER WAKE' > /system/usr/keylayout/gpio-keys.kl";
    		run.suCom(command);
    		command = "echo -n '' > /system/usr/keylayout/qwerty.kl";
    		run.suCom(command);
			
			for(i = 0; i < nButtons; i++){
				command = "echo 'key "+button_codes[i];
				for(j = 0; j < nTypes; j++){
					if(toggled[i][j]){
						command = command+" "+physical_types[j].toUpperCase();
					}
					editor.putBoolean(button_tags[i][j], toggled[i][j]);
				}
				gpio_com = command + "' >> /system/usr/keylayout/gpio-keys.kl";
				qwerty_com = command + "' >> /system/usr/keylayout/qwerty.kl";

				run.suCom(gpio_com);
				run.suCom(qwerty_com);
			}
			editor.putBoolean(reboot_tag,true);
			Log.w(TAG,"Buttons are now setup.");
    	} catch (Exception e){
			Log.e(TAG,"Button Exception.");
    	}
    	editor.commit();
    }//}}}
}//}}}
