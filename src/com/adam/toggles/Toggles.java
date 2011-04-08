package com.adam.toggles;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Toggles extends Activity {
	
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
	final String[] physical_wakes = {"WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED", "WAKE_DROPPED", "WAKE", "WAKE","WAKE"};
	final String[] physical_button_tags = {"Search","Home","Menu","Capacitive Back","Volume up","Volume down", "Hardware Back"};
	final int[] physical_codes = {217, 102, 139, 158, 115, 114, 158};
	final CharSequence[] physical_buttons = (CharSequence[])physical_button_tags;
	boolean [] physical_checked = {false, false, false, false, false, false, false};
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
	Runtime rt;
	Process process;
	DataOutputStream toProcess;
    final Object ReadLock = new Object();
    final Object WriteLock = new Object();
    boolean need_reboot;

    ByteArrayOutputStream inpbuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream errbuffer = new ByteArrayOutputStream();

    InputReader inpreader;
    InputReader errreader;
	
	String command;
	Button phoneButton, ledButton, sdcardButton, physicalButton, rebootButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        
        context = this;
        
        this.phoneButton = (Button)this.findViewById(R.id.button_phone_toggle);
        this.ledButton = (Button)this.findViewById(R.id.button_led_toggle);
        this.sdcardButton = (Button)this.findViewById(R.id.button_sdcard);
        this.rebootButton = (Button)this.findViewById(R.id.button_reboot);
        this.physicalButton = (Button)this.findViewById(R.id.button_physical_button);
        need_reboot = false;
        
        try{
        	process = Runtime.getRuntime().exec("su");
        	toProcess = new DataOutputStream(process.getOutputStream());
        	
            inpreader = new InputReader(process.getInputStream(), inpbuffer);
            errreader = new InputReader(process.getErrorStream(), errbuffer);

            Thread.sleep(10);

            inpreader.start();
            errreader.start();
        } catch (Exception err){
		}
        
        sPrefs = getSharedPreferences(prefs_name,0);
        
        for(int i = 0; i < n_physical_buttons; i++){
        	physical_checked[i] = sPrefs.getBoolean(physical_button_tags[i], false);
        }
        
        this.phoneButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		togglePhone();
        	}
        });
        
        this.ledButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		toggleLed();
        	}
        });
        
        this.sdcardButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		toggleSdcard();
        		need_reboot=true;
        	}
        });
        
        this.physicalButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		showDialog(DIALOG_SELECT_PHYSICAL_BUTTON);
        	}
        });
        
        this.rebootButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		showDialog(DIALOG_REBOOT);
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
    		runCommand(command);
    	} catch (Exception e){	
    	}
    }
    
    public void togglePhysicalButtons(){
    	String new_button;
    	String command;
    	SharedPreferences.Editor editor = sPrefs.edit();

    	try{
    		command = "echo 'key 62 POWER WAKE' > /system/usr/keylayout/gpio-keys.kl";
    		runCommand(command);
    		command = "echo 'key 102 MENU' > /system/usr/keylayout/gpio-keys.kl";
    		runCommand(command);
    		command = "echo -n '' > /system/usr/keylayout/qwerty.kl";
    		runCommand(command);
    		for(int i = 0; i < n_physical_buttons; i++){
				new_button = physical_names[i];
    			if(physical_checked[i]){
    				command = "echo 'key "+physical_codes[i]+" "+new_button+
    				" "+physical_wakes[i]+"' >> /system/usr/keylayout/"+physical_files[i]+".kl";
    			} else {
    				command = "echo 'key "+physical_codes[i]+" "+new_button+
    				" ' >> /system/usr/keylayout/"+physical_files[i]+".kl";
    			}
    			runCommand(command);
    			editor.putBoolean(physical_button_tags[i], physical_checked[i]);
    			Log.w(TAG,physical_buttons[i]+" is a wake button: "+physical_checked[i]);
    		}
    	} catch (Exception e){
    		
    	}
    	editor.commit();
    }
    
    public void toggleSdcard(){
    	int temp;
    	String command;
    	SharedPreferences.Editor editor = sPrefs.edit();
    	Toast toast;
    	
    	temp = sPrefs.getInt(sdcard_tag, 0);
    	
    	if(temp == 0){
    		try{
    			command = "echo '## Vold 2.0 NVIDIA Harmony fstab' > /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '#######################' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## Regular device mount' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '##' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## label        - Label for the volume' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## mount_point  - Where the volume will be mounted' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## part         - Partition # (1 based), or 'auto' for first usable partition.' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## <sysfs_path> - List of sysfs paths to source devices' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '######################' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '#dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc0 /devices/platform/tegra-sdhci.3/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount sdcard2 /mnt/sdcard2 auto /devices/platform/tegra-sdhci.3/mmc_host/mmc2' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '# todo: the secondary sdcard seems to confuse vold badly' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.2/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount usbdisk /mnt/usbdisk auto /devices/platform/tegra-ehci' >> /system/etc/vold.fstab";
    			runCommand(command);
    			
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard";
    			runCommand(command);
    			command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard2";
    			runCommand(command);
    			
    			editor.putInt(sdcard_tag, 1);
    			Log.w(TAG,"Sdcard toggle is on");
    			toast = Toast.makeText(context,"SDCard mount points are now swapped. Reboot for them to take effect.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	} else {
    		try {
    			command = "echo '## Vold 2.0 NVIDIA Harmony fstab' > /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '#######################' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## Regular device mount' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '##' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## label        - Label for the volume' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## mount_point  - Where the volume will be mounted' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## part         - Partition # (1 based), or 'auto' for first usable partition.' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '## <sysfs_path> - List of sysfs paths to source devices' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '######################' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '#dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc0 /devices/platform/tegra-sdhci.3/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc2' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo '# todo: the secondary sdcard seems to confuse vold badly' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount sdcard2 /mnt/sdcard2 auto /devices/platform/tegra-sdhci.2/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			runCommand(command);
    			command = "echo 'dev_mount usbdisk /mnt/usbdisk auto /devices/platform/tegra-ehci' >> /system/etc/vold.fstab";
    			runCommand(command);
    			
    			command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard";
    			runCommand(command);
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard2";
    			runCommand(command);
    			
    			editor.putInt(sdcard_tag, 0);
    			Log.w(TAG,"Sdcard toggle is off");
    			toast = Toast.makeText(context,"SDCard mount points are now restored to normal. Reboot for them to take effect.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	}
    	editor.commit();
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
    			runCommand(command);
    			editor.putString(led_tag,"heartbeat");
    			Log.w(TAG,"Led toggle is off");
    			toast = Toast.makeText(context,"LED Heartbeat is now on.",duration);
    			toast.show();
    		} catch (Exception e){
    			
    		}
    	} else {
    		try{
    			command = "echo none > /sys/class/leds/cpu/trigger";
    			runCommand(command);
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
				runCommand(command);
				command = "mv /system/app/TelephonyProvider.apk /system/app/TelephonyProvider.bak";
				runCommand(command);
				command = "mv /system/etc/permissions/android.hardware.telephony.gsm.xml /system/etc/permissions/android.hardware.telephony.gsm.bak";
				runCommand(command);
				command = "if [ -a /system/etc/Scripts/handheld_core_hardware.off ]; then cat /system/etc/Scripts/handheld_core_hardware.off > /system/etc/permissions/handheld_core_hardware.xml; fi";
				runCommand(command);
				editor.putInt(phone_tag, 1);
				Log.w(TAG,"Phone toggle is on");
				toast = Toast.makeText(context,"Phone services are now disabled.",duration);
    			toast.show();    			
			} catch (Exception e){
			}
		} else {
			try{
				command = "mv /system/app/Phone.bak /system/app/Phone.apk";
				runCommand(command);
				command = "mv /system/app/TelephonyProvider.bak /system/app/TelephonyProvider.apk";
				runCommand(command);
				command = "mv /system/etc/permissions/android.hardware.telephony.gsm.bak /system/etc/permissions/android.hardware.telephony.gsm.xml";
				runCommand(command);
				command = "if [ -a /system/etc/Scripts/handheld_core_hardware.on ]; then cat /system/etc/Scripts/handheld_core_hardware.on > /system/etc/permissions/handheld_core_hardware.xml; fi";
				runCommand(command);
				editor.putInt(phone_tag, 0);
				Log.w(TAG,"Phone toggle is off");
				toast = Toast.makeText(context,"Phone services are now enabled.",duration);
    			toast.show();
			} catch (Exception e){
			}
		}
		editor.commit();
    }
    
    /*********** The following class structures are copied from z4root ***************/
    public VTCommandResult runCommand(String command) throws Exception {
        Log.i("oclf", command);
        synchronized (WriteLock) {
            inpbuffer.reset();
            errbuffer.reset();
        }
    
        toProcess.writeBytes(command + "\necho :RET=$?\n");
        toProcess.flush();
        while (true) {
            synchronized (ReadLock) {
                boolean doWait;
                synchronized (WriteLock) {
                    byte[] inpbyte = inpbuffer.toByteArray();
                    String inp = new String(inpbyte);
                    doWait = !inp.contains(":RET=");
                }
                if (doWait) {
                    ReadLock.wait();
                }
            }
            synchronized (WriteLock) {
                byte[] inpbyte = inpbuffer.toByteArray();
                byte[] errbyte = errbuffer.toByteArray();
    
                String inp = new String(inpbyte);
                String err = new String(errbyte);
    
                if (inp.contains(":RET=")) {
                    if (inp.contains(":RET=EOF") || err.contains(":RET=EOF"))
                        throw new Exception();
                    if (inp.contains(":RET=0")) {
                        //Log.i("oclf success", inp);
                        return new VTCommandResult(0, inp, err);
                    } else {
                        //Log.i("oclf error", err);
                        return new VTCommandResult(1, inp, err);
                    }
                }
            }
        }
    } 
    
    public class InputReader extends Thread {

        InputStream is;
        ByteArrayOutputStream baos;

        public InputReader(InputStream is, ByteArrayOutputStream baos) {
            this.is = is;
            this.baos = baos;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = is.read(buffer);
                    if (read < 0) {
                        synchronized(WriteLock) {
                            buffer = ":RET=EOF".getBytes();
                            baos.write(buffer);
                        }
                        synchronized (ReadLock) {
                            ReadLock.notifyAll();
                        }
                        return;
                    }
                    if (read > 0) {
                        synchronized(WriteLock) {
                            baos.write(buffer, 0, read);
                        }
                        synchronized (ReadLock) {
                            ReadLock.notifyAll();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public class VTCommandResult {
        public final String stdout;
        public final String stderr;
        public final Integer exit_value;

        VTCommandResult(Integer exit_value_in, String stdout_in, String stderr_in) {
            exit_value = exit_value_in;
            stdout = stdout_in;
            stderr = stderr_in;
        }

        VTCommandResult(Integer exit_value_in) {
            this(exit_value_in, null, null);
        }

        public boolean success() {
            return exit_value != null && exit_value == 0;
        }
    }

}