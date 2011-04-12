package com.adam.toggles;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MountToggles extends Activity {
	
	final String TAG = "MountToggles"; 
	final String main_sdcard_tag = "mainSdcard";
	final String sec_sdcard_tag = "secondarySdcard";
	
	Context context;
	
	final int duration = Toast.LENGTH_LONG;
	
	private SharedPreferences sPrefs;
    private static final String prefs_name = "toggle-prefs";
	
    NativeTasks run = new NativeTasks();
	
	String command;
	Button swapSdbutton, refreshButton;
	Button mainUsbbutton;
	TextView mainSDPath, secSDPath;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.main);
        this.setContentView(R.layout.mount_toggles);
        sPrefs = getSharedPreferences(prefs_name,0);
        context = this;
    
        this.swapSdbutton = (Button)this.findViewById(R.id.button_swap_sd);
        this.mainSDPath = (TextView)this.findViewById(R.id.text_main_sd_path);
        this.secSDPath = (TextView)this.findViewById(R.id.text_sec_sd_path);
        this.refreshButton = (Button)this.findViewById(R.id.button_refresh_mounts);
        this.mainUsbbutton = (Button)this.findViewById(R.id.button_mount_main_usb);
        
		refreshStates();
       
        this.refreshButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		refreshStates();
        	}
        });
        
        this.swapSdbutton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		swapSdCards();
        	}
        });
        
        this.mainUsbbutton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		manageMainUsb();
        	}
        });
    }
    
    public void refreshStates(){
    	File secSdCard0 = new File("/dev/block/mmcblk0p1");
    	File secSdCard1 = new File("/dev/block/mmcblk1p1");
    	File secSdCard2 = new File("/dev/block/mmcblk2p1");
    	boolean main_state, sec_state, sec_plugged;
    	
    	main_state = sPrefs.getBoolean(main_sdcard_tag, false);
    	sec_state = sPrefs.getBoolean(sec_sdcard_tag, false);
    	
    	sec_plugged = secSdCard0.exists() || secSdCard1.exists() || secSdCard2.exists();
    	
    	if(main_state){
    		this.mainSDPath.setText("/mnt/sdcard2");
    	} else {
    		this.mainSDPath.setText("/mnt/sdcard");
    	}
    
    	if(sec_plugged){
    		if(sec_state){
    			this.secSDPath.setText("/mnt/sdcard");
    		} else {
    			this.secSDPath.setText("/mnt/sdcard2");
    		}
    	} else {
    		this.secSDPath.setText("Not Plugged In.");
    	}
    }
    
    public void manageMainUsb(){
    	String command;
    	
    	command = "umount /mnt/usbdisk";
    	run.suCom(command);
    }
    
    public void writeVold(){
    	boolean main_state;
    	String command;
    	Toast toast;
    	
    	main_state = sPrefs.getBoolean(main_sdcard_tag, false);
    	
    	if(main_state){
    		try{
    			command = "echo '## Vold 2.0 NVIDIA Harmony fstab' > /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '#dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc0 /devices/platform/tegra-sdhci.3/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount sdcard2 /mnt/sdcard2 auto /devices/platform/tegra-sdhci.3/mmc_host/mmc2' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '# todo: the secondary sdcard seems to confuse vold badly' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.2/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount usbdisk /mnt/usbdisk auto /devices/platform/tegra-ehci' >> /system/etc/vold.fstab";
    			run.suCom(command);
    						
    			Log.w(TAG,"Sdcard toggle is on");
    			toast = Toast.makeText(context,"SDCard mount points are now swapped.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	} else {
    		try {
    			command = "echo '## Vold 2.0 NVIDIA Harmony fstab' > /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '#dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc0 /devices/platform/tegra-sdhci.3/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount sdcard /mnt/sdcard auto /devices/platform/tegra-sdhci.3/mmc_host/mmc2' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '# todo: the secondary sdcard seems to confuse vold badly' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount sdcard2 /mnt/sdcard2 auto /devices/platform/tegra-sdhci.2/mmc_host/mmc1' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo 'dev_mount usbdisk /mnt/usbdisk auto /devices/platform/tegra-ehci' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			
    			Log.w(TAG,"Sdcard toggle is off");
    			toast = Toast.makeText(context,"SDCard mount points are now restored to normal.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	}
    }
    
    public void swapSdCards(){
    	String command;
    	boolean main_state, sec_state, sec_plugged;
    	File secSdCard0 = new File("/dev/block/mmcblk0p1");
    	File secSdCard1 = new File("/dev/block/mmcblk1p1");
    	File secSdCard2 = new File("/dev/block/mmcblk2p1");
    	SharedPreferences.Editor editor = sPrefs.edit();
    	
    	main_state = sPrefs.getBoolean(main_sdcard_tag, false);
    	sec_state = sPrefs.getBoolean(sec_sdcard_tag, false);
    	sec_plugged = secSdCard0.exists() || secSdCard1.exists() || secSdCard2.exists();
    	
    	if(main_state){
    		command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard";
    		run.suCom(command);
    		this.mainSDPath.setText("/mnt/sdcard");
    		editor.putBoolean(main_sdcard_tag,!main_state);
    	} else {
    		command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard2";
    		run.suCom(command);
    		this.mainSDPath.setText("/mnt/sdcard2");
    		editor.putBoolean(main_sdcard_tag,!main_state);
    	}
    	
    	if(sec_plugged){
    		if(sec_state){
    			command = "mount -t vfat /dev/block/mmcblk0p1 /mnt/sdcard2";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk1p1 /mnt/sdcard2";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard2";
    			run.suCom(command);
    			this.secSDPath.setText("/mnt/sdcard2");
    			editor.putBoolean(sec_sdcard_tag,!sec_state);
    		} else {
    			command = "mount -t vfat /dev/block/mmcblk0p1 /mnt/sdcard";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk1p1 /mnt/sdcard";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard";
    			run.suCom(command);
    			this.secSDPath.setText("/mnt/sdcard");
    			editor.putBoolean(sec_sdcard_tag,!sec_state);
    		}
    	} else {
    		this.secSDPath.setText("Not Plugged In.");
    	}
    
    	editor.commit();
    	writeVold();
    }
}
