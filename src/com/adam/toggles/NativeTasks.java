package com.adam.toggles;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class NativeTasks {
	public String getProp(String prop) {
		String retVal = null;
		try {
			String line;
			java.lang.Process p = Runtime.getRuntime().exec("getprop " + prop);
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()), 8 * 1024);
			while ((line = input.readLine()) != null) {
				retVal = line;
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	public boolean suCom(String suCommand) {
		int returncode = -1;
		try {
			Process p = Runtime.getRuntime().exec("su -c sh");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(suCommand + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			returncode = p.waitFor();
			if (returncode == 0) {
				return true;
			} else {
				Log.d("***", "root command: " + suCommand
						+ " failed with returncode " + returncode);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean Com(String Command) {
		int returncode = -1;
		try {
			Process p = Runtime.getRuntime().exec("sh");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(Command + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			returncode = p.waitFor();
			if (returncode == 0) {
				return true;
			} else {
				Log.d("***", "Run command: " + Command
						+ " failed with returncode " + returncode);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
