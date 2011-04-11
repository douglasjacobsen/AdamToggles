package com.adam.toggles;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

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

	final String[] physical_types = {"Wake", "Back", "Power", "Home", "Menu", "Search", "Camera", "Call", "Endcall"};

	final int[] button_codes = {217, 102, 139, 158, 115, 114, 158}; 
	final int type_wake = 0;
	final int type_back = 1;
	final int type_power = 2;
	final int type_home = 3;
	final int type_menu = 4;
	final int type_search = 5;
	final int type_camera = 6;
	final int type_call = 7;
	final int type_endcall = 8;
	
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	int curButton;

    ByteArrayOutputStream inpbuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream errbuffer = new ByteArrayOutputStream();
	
	String command;
	final String [] search_tags = {"search_wake","search_back","seach_power","search_home","search_menu","search_search","search_camera"
		,"search_call","search_endcall"};
	final String[] home_tags = {"home_wake","home_back","seach_power","home_home","home_menu","home_search","home_camera","home_call","home_endcall"};
	final String[] menu_tags = {"menu_wake","menu_back","seach_power","menu_home","menu_menu","menu_search","menu_camera","menu_call","menu_endcall"};
	final String[] capback_tags = 	{"capback_wake","capback_back","seach_power","capback_home","capback_menu","capback_search"
		,"capback_camera","capback_call","capback_endcall"};
	final String[] volup_tags = 	{"volup_wake","volup_back","seach_power","volup_home","volup_menu","volup_search"
		,"volup_camera","volup_call","volup_endcall"};
	final String[] voldown_tags = 	{"voldown_wake","voldown_back","seach_power","voldown_home","voldown_menu","voldown_search"
		,"voldown_camera","voldown_call","voldown_endcall"};
	final String[] physback_tags = 	{"physback_wake","physback_back","seach_power","physback_home","physback_menu","physback_search"
		,"physback_camera","physback_call","physback_endcall"};
	ToggleButton[] search, home, menu, capback, volup, voldown, physback; 
	boolean[] search_toggles, home_toggles, menu_toggles, capback_toggles, volup_toggles, voldown_toggles, physback_toggles;

	Button commit_maps;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {//{{{
		int i;
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.button_toggles);
        sPrefs = getSharedPreferences(prefs_name,0);
        context = this;
		
		search = new ToggleButton[nTypes];
		home = new ToggleButton[nTypes];
		menu = new ToggleButton[nTypes];
		capback = new ToggleButton[nTypes];
		volup = new ToggleButton[nTypes];
		voldown = new ToggleButton[nTypes];
		physback = new ToggleButton[nTypes];
		search_toggles = new boolean[nTypes];
		home_toggles = new boolean[nTypes];
		menu_toggles = new boolean[nTypes];
		capback_toggles = new boolean[nTypes];
		volup_toggles = new boolean[nTypes];
		voldown_toggles = new boolean[nTypes];
		physback_toggles = new boolean[nTypes];
    
		this.search[type_wake] = (ToggleButton)this.findViewById(R.id.search_wake);
		this.search[type_back] = (ToggleButton)this.findViewById(R.id.search_back);
		this.search[type_power] = (ToggleButton)this.findViewById(R.id.search_power);
		this.search[type_home] = (ToggleButton)this.findViewById(R.id.search_home);
		this.search[type_menu] = (ToggleButton)this.findViewById(R.id.search_menu);
		this.search[type_search] = (ToggleButton)this.findViewById(R.id.search_search);
		this.search[type_camera] = (ToggleButton)this.findViewById(R.id.search_camera);
		this.search[type_call] = (ToggleButton)this.findViewById(R.id.search_call);
		this.search[type_endcall] = (ToggleButton)this.findViewById(R.id.search_endcall);

		this.home[type_wake] = (ToggleButton)this.findViewById(R.id.home_wake);
		this.home[type_back] = (ToggleButton)this.findViewById(R.id.home_back);
		this.home[type_power] = (ToggleButton)this.findViewById(R.id.home_power);
		this.home[type_home] = (ToggleButton)this.findViewById(R.id.home_home);
		this.home[type_menu] = (ToggleButton)this.findViewById(R.id.home_menu);
		this.home[type_search] = (ToggleButton)this.findViewById(R.id.home_search);
		this.home[type_camera] = (ToggleButton)this.findViewById(R.id.home_camera);
		this.home[type_call] = (ToggleButton)this.findViewById(R.id.home_call);
		this.home[type_endcall] = (ToggleButton)this.findViewById(R.id.home_endcall);

		this.menu[type_wake] = (ToggleButton)this.findViewById(R.id.menu_wake);
		this.menu[type_back] = (ToggleButton)this.findViewById(R.id.menu_back);
		this.menu[type_power] = (ToggleButton)this.findViewById(R.id.menu_power);
		this.menu[type_home] = (ToggleButton)this.findViewById(R.id.menu_home);
		this.menu[type_menu] = (ToggleButton)this.findViewById(R.id.menu_menu);
		this.menu[type_search] = (ToggleButton)this.findViewById(R.id.menu_search);
		this.menu[type_camera] = (ToggleButton)this.findViewById(R.id.menu_camera);
		this.menu[type_call] = (ToggleButton)this.findViewById(R.id.menu_call);
		this.menu[type_endcall] = (ToggleButton)this.findViewById(R.id.menu_endcall);

		this.capback[type_wake] = (ToggleButton)this.findViewById(R.id.capback_wake);
		this.capback[type_back] = (ToggleButton)this.findViewById(R.id.capback_back);
		this.capback[type_power] = (ToggleButton)this.findViewById(R.id.capback_power);
		this.capback[type_home] = (ToggleButton)this.findViewById(R.id.capback_home);
		this.capback[type_menu] = (ToggleButton)this.findViewById(R.id.capback_menu);
		this.capback[type_search] = (ToggleButton)this.findViewById(R.id.capback_search);
		this.capback[type_camera] = (ToggleButton)this.findViewById(R.id.capback_camera);
		this.capback[type_call] = (ToggleButton)this.findViewById(R.id.capback_call);
		this.capback[type_endcall] = (ToggleButton)this.findViewById(R.id.capback_endcall);

		this.volup[type_wake] = (ToggleButton)this.findViewById(R.id.volup_wake);
		this.volup[type_back] = (ToggleButton)this.findViewById(R.id.volup_back);
		this.volup[type_power] = (ToggleButton)this.findViewById(R.id.volup_power);
		this.volup[type_home] = (ToggleButton)this.findViewById(R.id.volup_home);
		this.volup[type_menu] = (ToggleButton)this.findViewById(R.id.volup_menu);
		this.volup[type_search] = (ToggleButton)this.findViewById(R.id.volup_search);
		this.volup[type_camera] = (ToggleButton)this.findViewById(R.id.volup_camera);
		this.volup[type_call] = (ToggleButton)this.findViewById(R.id.volup_call);
		this.volup[type_endcall] = (ToggleButton)this.findViewById(R.id.volup_endcall);

		this.voldown[type_wake] = (ToggleButton)this.findViewById(R.id.voldown_wake);
		this.voldown[type_back] = (ToggleButton)this.findViewById(R.id.voldown_back);
		this.voldown[type_power] = (ToggleButton)this.findViewById(R.id.voldown_power);
		this.voldown[type_home] = (ToggleButton)this.findViewById(R.id.voldown_home);
		this.voldown[type_menu] = (ToggleButton)this.findViewById(R.id.voldown_menu);
		this.voldown[type_search] = (ToggleButton)this.findViewById(R.id.voldown_search);
		this.voldown[type_camera] = (ToggleButton)this.findViewById(R.id.voldown_camera);
		this.voldown[type_call] = (ToggleButton)this.findViewById(R.id.voldown_call);
		this.voldown[type_endcall] = (ToggleButton)this.findViewById(R.id.voldown_endcall);

		this.physback[type_wake] = (ToggleButton)this.findViewById(R.id.physback_wake);
		this.physback[type_back] = (ToggleButton)this.findViewById(R.id.physback_back);
		this.physback[type_power] = (ToggleButton)this.findViewById(R.id.physback_power);
		this.physback[type_home] = (ToggleButton)this.findViewById(R.id.physback_home);
		this.physback[type_menu] = (ToggleButton)this.findViewById(R.id.physback_menu);
		this.physback[type_search] = (ToggleButton)this.findViewById(R.id.physback_search);
		this.physback[type_camera] = (ToggleButton)this.findViewById(R.id.physback_camera);
		this.physback[type_call] = (ToggleButton)this.findViewById(R.id.physback_call);
		this.physback[type_endcall] = (ToggleButton)this.findViewById(R.id.physback_endcall);

		for(i = 0; i < nTypes; i++){
			search_toggles[i] = sPrefs.getBoolean(search_tags[i],false);
			this.search[i].setChecked(search_toggles[i]);
			home_toggles[i] = sPrefs.getBoolean(home_tags[i], false);
			this.home[i].setChecked(home_toggles[i]);
			menu_toggles[i] = sPrefs.getBoolean(menu_tags[i], false);
			this.menu[i].setChecked(menu_toggles[i]);
			capback_toggles[i] = sPrefs.getBoolean(capback_tags[i], false);
			this.capback[i].setChecked(capback_toggles[i]);
			volup_toggles[i] = sPrefs.getBoolean(volup_tags[i], false);
			this.volup[i].setChecked(volup_toggles[i]);
			voldown_toggles[i] = sPrefs.getBoolean(voldown_tags[i], false);
			this.voldown[i].setChecked(voldown_toggles[i]);
			physback_toggles[i] = sPrefs.getBoolean(physback_tags[i], false);
			this.physback[i].setChecked(physback_toggles[i]);
		}

		this.search[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_wake] = search[type_wake].isChecked();
			}
		});
		this.search[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_back] = search[type_back].isChecked();
			}
		});
		this.search[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_power] = search[type_power].isChecked();
			}
		});
		this.search[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_home] = search[type_home].isChecked();
			}
		});
		this.search[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_menu] = search[type_menu].isChecked();
			}
		});
		this.search[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_search] = search[type_search].isChecked();
			}
		});
		this.search[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_camera] = search[type_camera].isChecked();
			}
		});
		this.search[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_call] = search[type_call].isChecked();
			}
		});
		this.search[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				search_toggles[type_endcall] = search[type_endcall].isChecked();
			}
		});

		this.home[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_wake] = home[type_wake].isChecked();
			}
		});
		this.home[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_back] = home[type_back].isChecked();
			}
		});
		this.home[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_power] = home[type_power].isChecked();
			}
		});
		this.home[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_home] = home[type_home].isChecked();
			}
		});
		this.home[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_menu] = home[type_menu].isChecked();
			}
		});
		this.home[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_search] = home[type_search].isChecked();
			}
		});
		this.home[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_camera] = home[type_camera].isChecked();
			}
		});
		this.home[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_call] = home[type_call].isChecked();
			}
		});
		this.home[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				home_toggles[type_endcall] = home[type_endcall].isChecked();
			}
		});

		this.menu[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_wake] = menu[type_wake].isChecked();
			}
		});
		this.menu[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_back] = menu[type_back].isChecked();
			}
		});
		this.menu[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_power] = menu[type_power].isChecked();
			}
		});
		this.menu[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_home] = menu[type_home].isChecked();
			}
		});
		this.menu[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_menu] = menu[type_menu].isChecked();
			}
		});
		this.menu[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_search] = menu[type_search].isChecked();
			}
		});
		this.menu[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_camera] = menu[type_camera].isChecked();
			}
		});
		this.menu[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_call] = menu[type_call].isChecked();
			}
		});
		this.menu[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				menu_toggles[type_endcall] = menu[type_endcall].isChecked();
			}
		});

		this.capback[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_wake] = capback[type_wake].isChecked();
			}
		});
		this.capback[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_back] = capback[type_back].isChecked();
			}
		});
		this.capback[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_power] = capback[type_power].isChecked();
			}
		});
		this.capback[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_home] = capback[type_home].isChecked();
			}
		});
		this.capback[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_menu] = capback[type_menu].isChecked();
			}
		});
		this.capback[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_search] = capback[type_search].isChecked();
			}
		});
		this.capback[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_camera] = capback[type_camera].isChecked();
			}
		});
		this.capback[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_call] = capback[type_call].isChecked();
			}
		});
		this.capback[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				capback_toggles[type_endcall] = capback[type_endcall].isChecked();
			}
		});

		this.volup[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_wake] = volup[type_wake].isChecked();
			}
		});
		this.volup[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_back] = volup[type_back].isChecked();
			}
		});
		this.volup[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_power] = volup[type_power].isChecked();
			}
		});
		this.volup[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_home] = volup[type_home].isChecked();
			}
		});
		this.volup[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_menu] = volup[type_menu].isChecked();
			}
		});
		this.volup[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_search] = volup[type_search].isChecked();
			}
		});
		this.volup[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_camera] = volup[type_camera].isChecked();
			}
		});
		this.volup[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_call] = volup[type_call].isChecked();
			}
		});
		this.volup[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				volup_toggles[type_endcall] = volup[type_endcall].isChecked();
			}
		});

		this.voldown[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_wake] = voldown[type_wake].isChecked();
			}
		});
		this.voldown[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_back] = voldown[type_back].isChecked();
			}
		});
		this.voldown[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_power] = voldown[type_power].isChecked();
			}
		});
		this.voldown[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_home] = voldown[type_home].isChecked();
			}
		});
		this.voldown[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_menu] = voldown[type_menu].isChecked();
			}
		});
		this.voldown[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_search] = voldown[type_search].isChecked();
			}
		});
		this.voldown[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_camera] = voldown[type_camera].isChecked();
			}
		});
		this.voldown[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_call] = voldown[type_call].isChecked();
			}
		});
		this.voldown[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				voldown_toggles[type_endcall] = voldown[type_endcall].isChecked();
			}
		});

		this.physback[type_wake].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_wake] = physback[type_wake].isChecked();
			}
		});
		this.physback[type_back].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_back] = physback[type_back].isChecked();
			}
		});
		this.physback[type_power].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_power] = physback[type_power].isChecked();
			}
		});
		this.physback[type_home].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_home] = physback[type_home].isChecked();
			}
		});
		this.physback[type_menu].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_menu] = physback[type_menu].isChecked();
			}
		});
		this.physback[type_search].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_search] = physback[type_search].isChecked();
			}
		});
		this.physback[type_camera].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_camera] = physback[type_camera].isChecked();
			}
		});
		this.physback[type_call].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_call] = physback[type_call].isChecked();
			}
		});
		this.physback[type_endcall].setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				physback_toggles[type_endcall] = physback[type_endcall].isChecked();
			}
		});

		this.commit_maps = (Button)this.findViewById(R.id.button_save_mapping);
		this.commit_maps.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				togglePhysicalButtons();
			}
		});

    }//}}}

    public void togglePhysicalButtons(){//{{{
		int i;
    	String command;
		String search_com;
		String home_com;
		String menu_com;
		String capback_com;
		String volup_com;
		String voldown_com;
		String physback_com;
		String gpio_com, qwerty_com;
    	SharedPreferences.Editor editor = sPrefs.edit();

    	try{
    		command = "echo 'key 62 POWER WAKE' > /system/usr/keylayout/gpio-keys.kl";
    		run.suCom(command);
    		command = "echo 'key 62 POWER WAKE' > /system/usr/keylayout/qwerty.kl";
    		run.suCom(command);

			search_com = "echo 'key "+button_codes[SEARCH_BUTTON]+" ";
			home_com = "echo 'key "+button_codes[HOME_BUTTON]+" ";
			menu_com = "echo 'key "+button_codes[MENU_BUTTON]+" ";
			capback_com = "echo 'key "+button_codes[CAPBACK_BUTTON]+" ";
			volup_com = "echo 'key "+button_codes[VOLUP_BUTTON]+" ";
			voldown_com = "echo 'key "+button_codes[VOLDOWN_BUTTON]+" ";
			physback_com = "echo 'key "+button_codes[PHYSBACK_BUTTON]+" ";

			for(i = 0; i< nTypes; i++){
				if(search_toggles[i]){
					search_com = search_com + physical_types[i].toUpperCase() + " ";
				}
				if(home_toggles[i]){
					home_com = home_com + physical_types[i].toUpperCase() + " ";
				}
				if(menu_toggles[i]){
					menu_com = menu_com + physical_types[i].toUpperCase() + " ";
				}
				if(capback_toggles[i]){
					capback_com = capback_com + physical_types[i].toUpperCase() + " ";
				}
				if(volup_toggles[i]){
					volup_com = volup_com + physical_types[i].toUpperCase() + " ";
				}
				if(voldown_toggles[i]){
					voldown_com = voldown_com + physical_types[i].toUpperCase() + " ";
				}
				if(physback_toggles[i]){
					physback_com = physback_com + physical_types[i].toUpperCase() + " ";
				}
				editor.putBoolean(search_tags[i], search_toggles[i]);
				editor.putBoolean(home_tags[i], home_toggles[i]);
				editor.putBoolean(menu_tags[i], menu_toggles[i]);
				editor.putBoolean(capback_tags[i], capback_toggles[i]);
				editor.putBoolean(volup_tags[i], volup_toggles[i]);
				editor.putBoolean(voldown_tags[i], voldown_toggles[i]);
				editor.putBoolean(physback_tags[i], physback_toggles[i]);
			}
			gpio_com = search_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = search_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = home_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = home_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = menu_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = menu_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = capback_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = capback_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = volup_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = volup_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = voldown_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = voldown_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);
			gpio_com = physback_com + "' >> /system/usr/keylayout/gpio-keys.kl";
			qwerty_com = physback_com + "' >> /system/usr/keylayout/qwerty.kl";
			run.suCom(gpio_com);
			run.suCom(qwerty_com);

			editor.putBoolean(reboot_tag,true);
			editor.commit();
			Log.w(TAG,"Buttons are now setup.");
    	} catch (Exception e){
			Log.e(TAG,"Button Exception.");
    	}
    }//}}}
}//}}}
