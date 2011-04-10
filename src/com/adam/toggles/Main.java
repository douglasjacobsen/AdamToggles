package com.adam.toggles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;

public class Main extends TabActivity {
	
	final String TAG = "Toggles-Main";
	
	final String reboot_tag = "need_reboot";
	final String phone_tag = "phoneButton";
	final String sdcard_tag = "sdcardSwitch";
	final String led_tag = "ledSwitch";
	final String physical_button_tag = "physicalButton";
	
	final int duration = Toast.LENGTH_SHORT;
	final int DIALOG_SELECT_PHYSICAL_BUTTON = 0;
	final int DIALOG_REBOOT = 1;
	final int DIALOG_REBOOT_REQUESTED = 2;
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.main);

	    Resources res = this.getResources();
	    TabHost tabHost = this.getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, SystemToggles.class);
	    spec = tabHost.newTabSpec("system").setIndicator(" ",
	    		res.getDrawable(R.drawable.ic_tab_system))
                .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MountToggles.class);
	    spec = tabHost.newTabSpec("mount").setIndicator(" ",
	    		res.getDrawable(R.drawable.ic_tab_media))
                .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, ButtonToggles.class);
	    spec = tabHost.newTabSpec("button").setIndicator(" ",
	    		   res.getDrawable(R.drawable.ic_tab_key_layout))
                   .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(3);
	    
        final Button reboot = (Button) findViewById(R.id.button_reboot);
        reboot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	showDialog(DIALOG_REBOOT);
            }	
        });
        
        final Button close = (Button) findViewById(R.id.button_reboot);
        close.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }	
        });    
	}

	@Override
    protected Dialog onCreateDialog(int id, Bundle args){//{{{
        super.onCreateDialog(id,args);
        Dialog dialog = null;
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            
        switch(id){
            case DIALOG_REBOOT:
            	builder.setTitle("Reboot:");
            	builder.setMessage("Are you sure you want to reboot now?");
            	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which){
            			reboot();
            		}
            	});
            	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which){
            		}
            	});
            	alert = builder.create();
            	dialog = alert;
            	break;
            case DIALOG_REBOOT_REQUESTED:
            	builder.setTitle("Reboot:");
            	builder.setMessage("Some of the actions you performed require a reboot before they will take effect."
            			+"Would you like to reboot now?");
            	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which){
            			reboot();
            		}
            	});
            	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which){
            			back();
            		}
            	});
            	alert = builder.create();
            	dialog = alert;
            	break;
        }
        return dialog;
    }//}}}
	
    @Override
    protected void onDestroy(){//{{{
        Log.d(TAG,"onDestroy");
    	super.onDestroy();
    }//}}}
    
    @Override
    public void onBackPressed(){
    	SharedPreferences.Editor editor = sPrefs.edit();
    	boolean need_reboot;
    	
    	need_reboot = sPrefs.getBoolean(reboot_tag, false);
    	
    	if(need_reboot){
    		editor.putBoolean(reboot_tag, true);
    		editor.commit();
    		showDialog(DIALOG_REBOOT_REQUESTED);
    	} else {
    		super.onBackPressed();
    	}
    }
    
    public void back(){
    	super.onBackPressed();
    }
    
    public void reboot(){
    	run.Com("reboot");
    }
}