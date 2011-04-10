package com.adam.toggles;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MountToggles extends Activity {
	
	final String TAG = "MountToggles"; 
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
        this.setContentView(R.layout.mount_toggles);
        
        context = this;
        
        this.sdcardButton = (Button)this.findViewById(R.id.button_sdcard);
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
        
        
        this.sdcardButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		toggleSdcard();
        		need_reboot=true;
        	}
        });
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
    			run.suCom(command);
    			/*command = "echo '#######################' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## Regular device mount' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '##' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## label        - Label for the volume' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## mount_point  - Where the volume will be mounted' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## part         - Partition # (1 based), or 'auto' for first usable partition.' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## <sysfs_path> - List of sysfs paths to source devices' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '######################' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '' >> /system/etc/vold.fstab";*/
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
    			
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard2";
    			run.suCom(command);
    			
    			editor.putInt(sdcard_tag, 1);
    			Log.w(TAG,"Sdcard toggle is on");
    			toast = Toast.makeText(context,"SDCard mount points are now swapped. Reboot for them to take effect.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	} else {
    		try {
    			command = "echo '## Vold 2.0 NVIDIA Harmony fstab' > /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '#######################' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## Regular device mount' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '##' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## label        - Label for the volume' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## mount_point  - Where the volume will be mounted' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## part         - Partition # (1 based), or 'auto' for first usable partition.' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '## <sysfs_path> - List of sysfs paths to source devices' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '######################' >> /system/etc/vold.fstab";
    			run.suCom(command);
    			command = "echo '' >> /system/etc/vold.fstab";
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
    			
    			command = "mount -t vfat /dev/block/mmcblk3p1 /mnt/sdcard";
    			run.suCom(command);
    			command = "mount -t vfat /dev/block/mmcblk2p1 /mnt/sdcard2";
    			run.suCom(command);
    			
    			editor.putInt(sdcard_tag, 0);
    			Log.w(TAG,"Sdcard toggle is off");
    			toast = Toast.makeText(context,"SDCard mount points are now restored to normal. Reboot for them to take effect.",duration);
    			toast.show();
    		} catch (Exception e){
    		}
    	}
    	editor.commit();
    }
}