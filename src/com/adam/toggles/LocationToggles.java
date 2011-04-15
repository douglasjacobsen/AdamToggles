package com.adam.toggles;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;

public class LocationToggles extends Activity {
	Context context;
	final int duration = Toast.LENGTH_LONG;
	Button bsave;
	private SharedPreferences sPrefs;
	private static final String prefs_name = "toggle-prefs";
	NativeTasks m = new NativeTasks();
	String gps_tag = "gps";
	String gps_server;
	int gps_selected;
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.location_toggles);

		sPrefs = getSharedPreferences(prefs_name,0);
		context = this;
		final SharedPreferences.Editor editor = sPrefs.edit();
		this.bsave = (Button)this.findViewById(R.id.button_save_location);
		Spinner sgps = (Spinner)findViewById(R.id.spinner_gps);

        final MyData items[] = new MyData[73];
        items[0] = new MyData( "Angola","ao" );
        items[1] = new MyData( "Argentina","ar" );
        items[2] = new MyData( "Australia","au" );
        items[3] = new MyData( "Austria","at" );
        items[4] = new MyData( "Bahamas","bs" );
        items[5] = new MyData( "Belarus","by" );
        items[6] = new MyData( "Belgium","be" );
        items[7] = new MyData( "Bosnia and Herzegovina","ba" );
        items[8] = new MyData( "Brazil","br" );
        items[9] = new MyData( "Bulgaria","bg" );
        items[10] = new MyData( "Cambodia","kh" );
        items[11] = new MyData( "Canada","ca" );
        items[12] = new MyData( "Chile","cl" );
        items[13] = new MyData( "China","cn" );
        items[14] = new MyData( "Costa Rica", "cr" );
        items[15] = new MyData( "Croatia","hr" );
        items[16] = new MyData( "Czech Republic","cz" );
        items[17] = new MyData( "Denmark","dk" );
        items[18] = new MyData( "El Salvador","sv" );
        items[19] = new MyData( "Estonia","ee" );
        items[20] = new MyData( "Finland","fi" );
        items[21] = new MyData( "France","fr" );
        items[22] = new MyData( "Germany","de" );
        items[23] = new MyData( "Greece","gr" );
        items[24] = new MyData( "Hungary","hu" );
        items[25] = new MyData( "Hong Kong","hk" );
        items[26] = new MyData( "India","in" );
        items[27] = new MyData( "Indonesia","id" );
        items[28] = new MyData( "Iran","ir" );
        items[29] = new MyData( "Ireland","ie" );
        items[30] = new MyData( "Italy","it" );
        items[31] = new MyData( "Japan","jp" );
        items[32] = new MyData( "Korea","kr" );
        items[33] = new MyData( "Kyrgyzstan","kg" );
        items[34] = new MyData( "Latvia","lv" );
        items[35] = new MyData( "Lithuania","lt" );
        items[36] = new MyData( "Luxembourg","lu" );
        items[37] = new MyData( "Madagascar","mg" );
        items[38] = new MyData( "Macedonia","mk" );
        items[39] = new MyData( "Malaysia","my" );
        items[40] = new MyData( "Mexico","mx" );
        items[41] = new MyData( "Moldova","md" );
        items[42] = new MyData( "Netherlands","nl" );
        items[43] = new MyData( "New Caledonia","nc" );
        items[44] = new MyData( "New Zeland","nz" );
        items[45] = new MyData( "Norway","no" );
        items[46] = new MyData( "Oman","om" );
        items[47] = new MyData( "Pakistan","pk" );
        items[48] = new MyData( "Panama","pa" );
        items[49] = new MyData( "Philippines","ph" );
        items[50] = new MyData( "Poland", "pl" );
        items[51] = new MyData( "Portugal","pt" );
        items[52] = new MyData( "Qatar","qa" );
        items[53] = new MyData( "Republic of Serbia","rs" );
        items[54] = new MyData( "Romania","ro" );
        items[55] = new MyData( "Russian Federation","ru" );
        items[56] = new MyData( "Singapore","sg" );
        items[57] = new MyData( "Slovakia","sk" );
        items[58] = new MyData( "Slovenia","si" );
        items[59] = new MyData( "South Africa","tz" );
        items[60] = new MyData( "Spain","es" );
        items[61] = new MyData( "Sri Lanka","lk" );
        items[62] = new MyData( "Sweden","se" );
        items[63] = new MyData( "Switzerland","ch" );
        items[64] = new MyData( "Taiwan","tw" );
        items[65] = new MyData( "Thailand","th" );
        items[66] = new MyData( "Turkey","tr" );
        items[67] = new MyData( "Ukraine","ua" );
        items[68] = new MyData( "United Kingdom","uk" );
        items[69] = new MyData( "United States","us" );
        items[70] = new MyData( "Uzbekistan","uz" );
        items[71] = new MyData( "Vietnam","vn" );
        items[72] = new MyData( "Yugoslavia","yu" );
        
        ArrayAdapter<MyData> adapter = new ArrayAdapter<MyData>( this, android.R.layout.simple_spinner_item, items );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        sgps.setAdapter(adapter);
        sgps.setSelection(sPrefs.getInt("spinnerSelection",69));
        
        sgps.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
        	
                public void onItemSelected( AdapterView<?> parent, View view, int position, long id) {
    				Spinner sgps = (Spinner)findViewById(R.id.spinner_gps);

                	MyData d = items[position];
                	gps_server = d.getValue();
                    gps_selected = sgps.getSelectedItemPosition();
                }
                
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        
		this.bsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				Toast toast;
				
				m.suCom("echo '# GPS configuration - Autogenerated from Notion Ink Toggles' > /data/gps.conf");
				m.suCom("echo 'NTP_SERVER=" + gps_server + ".pool.ntp.org' >> /data/gps.conf");
				m.suCom("echo 'XTRA_SERVER_1=http://xtra1.gpsonextra.net/xtra.bin' >> /data/gps.conf");
				m.suCom("echo 'XTRA_SERVER_2=http://xtra2.gpsonextra.net/xtra.bin' >> /data/gps.conf");
				m.suCom("echo 'XTRA_SERVER_3=http://xtra3.gpsonextra.net/xtra.bin' >> /data/gps.conf");
				m.suCom("echo 'XTRA_SERVER_4=http://xtra4.gpsonextra.net/xtra.bin' >> /data/gps.conf");
				m.suCom("echo 'SUPL_HOST=supl.google.com' >> /data/gps.conf");
				m.suCom("echo 'SUPL_PORT=7276' >> /data/gps.conf");
				m.suCom("echo 'C2K_HOST=default' >> /data/gps.conf");
				m.suCom("echo 'C2K_PORT=0' >> /data/gps.conf");
                m.suCom("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system");
                m.suCom("cat /data/gps.conf > /system/etc/gps.conf");
                m.suCom("mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system");
				
                editor.putInt("spinnerSelection", gps_selected);
				editor.putString(gps_tag, gps_server);
				editor.commit();
				
				toast = Toast.makeText(context,"You still need to reboot at some point for location changes to take effects.",duration);
				toast.show(); 
			}});
    }

    class MyData {
        public MyData( String spinnerText, String value ) {
            this.spinnerText = spinnerText;
            this.value = value;}

        public String getSpinnerText() {
            return spinnerText;}

        public String getValue() {
            return value;}

        public String toString() {
            return spinnerText;}

        String spinnerText;
        String value;
}}
