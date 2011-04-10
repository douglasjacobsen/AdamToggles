package com.adam.toggles;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

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
import android.widget.Toast;

public class ButtonToggles extends Activity {
	
	final String TAG = "Toggles"; 
	final String phone_tag = "phoneButton";
	final String sdcard_tag = "sdcardSwitch";
	final String led_tag = "ledSwitch";
	final String physical_button_tag = "physicalButton";
	
	Context context;
	
	final int duration = Toast.LENGTH_SHORT;
	final int DIALOG_SELECT_PHYSICAL_BUTTON = 0;
	final int DIALOG_REBOOT = 1;
	final int DIALOG_REBOOT_REQUESTED = 2;
	
	final int n_physical_buttons = 7;
	final String[] physical_files = {"qwerty","qwerty","qwerty","qwerty","gpio-keys","gpio-keys","gpio-keys"};
	final String[] physical_names = {"SEARCH","HOME","MENU","BACK","VOLUME_UP","VOLUME_DOWN","BACK"};
	final String[] physical_wakes = {"WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED","WAKE"};
	final String[] physical_button_tags = {"Search","Home","Menu","Capacitive Back","Volume up","Volume down", "Hardware Back"};
	final int[] physical_codes = {217, 102, 139, 158, 115, 114, 158};
	final CharSequence[] physical_buttons = (CharSequence[])physical_button_tags;
	boolean [] physical_checked = {false, false, false, false, false, false, false};
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	Runtime rt;
	Process process;
	DataOutputStream toProcess;
    final Object ReadLock = new Object();
    final Object WriteLock = new Object();
    boolean need_reboot;

    ByteArrayOutputStream inpbuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream errbuffer = new ByteArrayOutputStream();
	
	String command;
	Button phoneButton, ledButton, sdcardButton, physicalButton, rebootButton;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.main);
        setContentView(R.layout.button_toggles);
        
        context = this;
    
        this.physicalButton = (Button)this.findViewById(R.id.button_physical_button);
        need_reboot = false;
        
        try{
        	process = Runtime.getRuntime().exec("su");
        	toProcess = new DataOutputStream(process.getOutputStream());

        } catch (Exception err){
		}
        
        sPrefs = getSharedPreferences(prefs_name,0);
        
        for(int i = 0; i < n_physical_buttons; i++){
        	physical_checked[i] = sPrefs.getBoolean(physical_button_tags[i], false);
        }
        
        this.physicalButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
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
            case DIALOG_SELECT_PHYSICAL_BUTTON:
            	
                builder.setTitle("Select Button:");
                builder.setMultiChoiceItems(physical_buttons, physical_checked, new DialogInterface.OnMultiChoiceClickListener() {
                //builder.setItems(physical_buttons, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    	physical_checked[which] = isChecked;
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						togglePhysicalButtons();
                        need_reboot=true;
                    	Toast toast = Toast.makeText(context,"Wake buttons are set up now. Reboot for them to take effect.",duration);
                    	toast.show();
					}
				});
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which){
                		/*for(int i = 0; i < n_physical_buttons; i++){
                			physical_checked[i] = false;
                		}
                		togglePhysicalButtons();
                		Toast toast = Toast.makeText(context,"Wake buttons are cleared now. Reboot for them to take effect.",duration);
                		toast.show();*/
                	}
                });
                alert = builder.create();
                dialog = alert;
                break;
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
    	if(need_reboot){
    		showDialog(DIALOG_REBOOT_REQUESTED);
    	} else {
    		super.onBackPressed();
    	}
    }
    
    public void back(){
    	super.onBackPressed();
    }
    
    public void reboot(){
    	String command;
    	try{
    		command = "reboot";
    		run.suCom(command);
    	} catch (Exception e){	
    	}
    }
    
    public void togglePhysicalButtons(){
    	String new_button;
    	String command;
    	SharedPreferences.Editor editor = sPrefs.edit();

    	try{
    		command = "echo 'key 62 POWER WAKE' > /system/usr/keylayout/gpio-keys.kl";
    		run.suCom(command);
    		command = "echo 'key 102 MENU' > /system/usr/keylayout/gpio-keys.kl";
    		run.suCom(command);
    		command = "echo -n '' > /system/usr/keylayout/qwerty.kl";
    		run.suCom(command);
    		for(int i = 0; i < n_physical_buttons; i++){
				new_button = physical_names[i];
    			if(physical_checked[i]){
    				command = "echo 'key "+physical_codes[i]+" "+new_button+
    				" "+physical_wakes[i]+"' >> /system/usr/keylayout/"+physical_files[i]+".kl";
    			} else {
    				command = "echo 'key "+physical_codes[i]+" "+new_button+
    				" ' >> /system/usr/keylayout/"+physical_files[i]+".kl";
    			}
    			run.suCom(command);
    			editor.putBoolean(physical_button_tags[i], physical_checked[i]);
    			Log.w(TAG,physical_buttons[i]+" is a wake button: "+physical_checked[i]);
    		}
    	} catch (Exception e){
    		
    	}
    	editor.commit();
    }
}