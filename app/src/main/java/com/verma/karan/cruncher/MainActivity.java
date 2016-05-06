package com.verma.karan.cruncher;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    UsageStatsManager mUsageStatsManager;
    Button mOpenUsageSettingButton;
    Button mCrunchingButton;
    long now = System.currentTimeMillis();
    TextView mText;
    String data = "";
    List<String> data3 = new ArrayList<String>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOpenUsageSettingButton = (Button) this.findViewById(R.id.button_open_usage_setting);
        mCrunchingButton = (Button) this.findViewById(R.id.curnching_button);
        mText = (TextView) this.findViewById(R.id.textView);
        mUsageStatsManager = (UsageStatsManager) this
                .getSystemService("usagestats");
        File sdCard = Environment.getExternalStorageDirectory();
        final File directory = new File (sdCard.getAbsolutePath() + "/Cruncher1");
        directory.mkdirs();
        checkPermission();
        mCrunchingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                      getNetworkStats();
                      storeIt(data,"Network_stats.txt",directory);
                      data = getSms();
                      storeIt(data,"SMS_Log.txt",directory);
//                    data = getCallDetails();
//                    storeIt(data,"callLog.txt",directory);
//                    data= getUseageStats();
//                    storeIt(data,"appUseageStats.txt",directory);
//                    data = getBrowserHist();
//                    storeIt(data,"History.txt",directory);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mCrunchingButton.setText("Done!!!");
                mText.setVisibility(View.VISIBLE);
                mText.setText("Files have been stored in SD card under /Myfiles Directory");

            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    @Override
    public void onResume(){
        super.onResume();
        checkPermission();
    }

    private String getUseageStats() throws JSONException {

        StatsUsageInterval statsUsageInterval = StatsUsageInterval.YEARLY;
        if (statsUsageInterval != null) {

            try {
                String usageStatsList =
                        getUsageStatistics(statsUsageInterval.mInterval);
                return usageStatsList;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    //Function to store json into external storage.
    private  void storeIt(String data,String filename,File directory) throws IOException {


        //Now create the file in the above directory and write the contents into it
        File file = new File(directory, filename);
        FileOutputStream fOut = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        osw.write(String.valueOf(this.data));
        osw.flush();
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String getCallDetails() throws JSONException {
        StringBuffer sb = new StringBuffer();

        JSONObject call_json = new JSONObject();
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Log :");
        while (managedCursor.moveToNext()) {
            JSONObject call_details_jsonObj = new JSONObject();
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            call_details_jsonObj.put("Number", phNumber);
            call_details_jsonObj.put("Type",dir);
            call_details_jsonObj.put("Date", callDayTime);
            call_details_jsonObj.put("Duration",callDuration);
            call_json.put(callDate,call_details_jsonObj);
        }
        ;
        return call_json.toString();


    }
    public  void checkPermission(){
        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, now);

        if (queryUsageStats.size() == 0) {
            Log.i("ERROR", "The user may not allow the access to apps usage. ");
            Toast.makeText(this,
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mCrunchingButton.setVisibility(View.INVISIBLE);
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }else{
            mCrunchingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setVisibility(View.GONE);
        }
    }
    public String getSms() throws JSONException {
        Uri uriSms = Uri.parse("content://sms/inbox");
        JSONObject sms_json = new JSONObject();
        Cursor cursor = this.getContentResolver().query(uriSms, null, null, null, null);
        int i =0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do { i =i+1;
                String msgData = "";
                JSONObject text_json = new JSONObject();
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {

                    text_json.put(cursor.getColumnName(idx),cursor.getString(idx));

                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
//                    Log.v("SMS => ", msgData);
                }
                sms_json.put(String.valueOf(i),text_json);
                // use msgData
            } while (cursor.moveToNext());
            String test = sms_json.toString();
            Log.d("Sms json =>", test);
            return sms_json.toString();
        } else {
            // empty box, no SMS
        }
        return null;
    }

    public void getNetworkStats() {
        long ts = TrafficStats.getTotalRxBytes();
        Log.v("TOTAL BYTE RECEIVED => ", String.valueOf(ts));

    }


    public String getBrowserHist() throws JSONException {
        JSONObject history_json = new JSONObject();
        Uri uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");
        Cursor mCur = this.getContentResolver().query(uriCustom,
                Browser.HISTORY_PROJECTION, null, null, null);
        mCur.moveToFirst();
        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            while (mCur.isAfterLast() == false) {
                JSONObject website_details_jsonObj = new JSONObject();

                website_details_jsonObj.put("Title",mCur.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
                website_details_jsonObj.put("Url",mCur.getString(Browser.HISTORY_PROJECTION_URL_INDEX));
                website_details_jsonObj.put("Date",mCur.getString(Browser.HISTORY_PROJECTION_DATE_INDEX));
                website_details_jsonObj.put("Visit",mCur.getString(Browser.HISTORY_PROJECTION_VISITS_INDEX));
                history_json.put(String.valueOf(mCur.getString(Browser.HISTORY_PROJECTION_DATE_INDEX)),website_details_jsonObj);

//                Log.v("TITLE => ", mCur
//                        .getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
//                Log.v("URL => ", mCur
//                        .getString(Browser.HISTORY_PROJECTION_URL_INDEX));
//                Log.v("DATE => ", mCur
//                        .getString(Browser.HISTORY_PROJECTION_DATE_INDEX));
//                Log.v("VISITS => ", mCur
//                        .getString(Browser.HISTORY_PROJECTION_VISITS_INDEX));

                mCur.moveToNext();
            }
        }


        return history_json.toString();
    }


    public String getUsageStatistics(int intervalType) throws JSONException {
//        List<String> nameNtime = new ArrayList<String>();
        String val1 ="";
        DateFormat mDateFormat = new SimpleDateFormat();


        JSONObject App_useage_json = new JSONObject();


        long now = System.currentTimeMillis();
//         Get the app statistics since epoch till current date.
        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, 0, now);
        Log.d("SIZE => ", String.valueOf(queryUsageStats.size()));
        for (int i = 0; i < queryUsageStats.size(); i++) {
            JSONObject App_deatils_json = new JSONObject();
            CustomUsageStats cUsageStats = new CustomUsageStats();
            cUsageStats.usageStats = queryUsageStats.get(i);;
            PackageManager packageManager = this.getApplicationContext().getPackageManager();
            String appName = "";
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(cUsageStats.usageStats.getPackageName(), PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                appName = "";
            }
            String f_time = String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000);
            String first_used =  String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getFirstTimeStamp())));
            String last_used =  String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeUsed())));

            App_deatils_json.put("Name",appName);
            App_deatils_json.put("Foreground",f_time);
            App_deatils_json.put("Frist_time",first_used);
            App_deatils_json.put("Last_time",last_used );
            if (appName!=""){
            App_useage_json.put(last_used,App_deatils_json);}
//            Log.d("PACKAGE NAME => ", appName);
//            Log.d("TOTAL TIME => ", String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000));
            val1 = App_useage_json.toString();
//            Log.d("#Useage_json => ",val1);


//            Log.d("TYPE => ",);
//            Log.d("CONTENT => ",String.valueOf(cUsageStats.usageStats.describeContents()));
//            Log.d("FIRST_TIME STAMP => ",String.valueOf(cUsageStats.usageStats.getFirstTimeStamp()));
//            Log.d("LAST_TIME STAMP => ", String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeStamp()))));
//            Log.d("LAST_TIME USED => ", String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeUsed()))));
//            Log.d("------------------",String.valueOf(val));
//            nameNtime.add(String.valueOf(cUsageStats.usageStats.getPackageName()) + " -> " + String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000));
        }


        if (queryUsageStats.size() == 0) {
            Log.i("ERROR", "The user may not allow the access to apps usage. ");
            Toast.makeText(this,
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }else{
            mCrunchingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setVisibility(View.GONE);
        }
        return val1;
    }

//    List<String> updateAppsList(List<UsageStats> usageStatsList) throws IOException, JSONException {
//        //writing stuff in mobile
//
//        File sdCard = Environment.getExternalStorageDirectory();
//        DateFormat mDateFormat = new SimpleDateFormat();
//        File directory = new File(sdCard.getAbsolutePath() + "/Cruncher");
//        File file = new File(directory, "mysdfile.txt");
//        FileOutputStream fOut = new FileOutputStream(file);
//        OutputStreamWriter osw = new OutputStreamWriter(fOut);
//
//        //json stuff
//        JSONObject App_deatils_json = new JSONObject();
//        JSONObject appUseage_json = new JSONObject();
//
//
//        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();
//        List<String> app_names = new ArrayList<String>();
//        for (int i = 0; i < usageStatsList.size(); i++) {
//            CustomUsageStats cUsageStats = new CustomUsageStats();
//            cUsageStats.usageStats = usageStatsList.get(i);
//            int val = 0;
//            PackageManager packageManager = this.getApplicationContext().getPackageManager();
//            String appName = "";
//            try {
//                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(cUsageStats.usageStats.getPackageName(), PackageManager.GET_META_DATA));
//            } catch (PackageManager.NameNotFoundException e) {
//                appName = "";
//            }
//            String f_time = String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000);
//            String first_used =  String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getFirstTimeStamp())));
//            String last_used =  String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeUsed())));
//
//            App_deatils_json.put("Name",appName);
//            App_deatils_json.put("Foreground",f_time);
//            App_deatils_json.put("Frist_time",first_used);
//            App_deatils_json.put("Last_time",last_used );
//
//            appUseage_json.put(String.valueOf(i),App_deatils_json);
//
////            Log.d("PACKAGE NAME => ", appName);
////            Log.d("TOTAL TIME => ", String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000));
////            Log.d("TYPE => ",);
////            Log.d("CONTENT => ",String.valueOf(cUsageStats.usageStats.describeContents()));
////            Log.d("FIRST_TIME STAMP => ",String.valueOf(cUsageStats.usageStats.getFirstTimeStamp()));
////            Log.d("LAST_TIME STAMP => ", String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeStamp()))));
////            Log.d("LAST_TIME USED => ", String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeUsed()))));
////            Log.d("------------------",String.valueOf(val));
//            app_names.add(String.valueOf(cUsageStats.usageStats.getPackageName()) + " -> " + String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground() / 60000));
//        }
//        String val1 = appUseage_json.toString();
//        Log.d("Useage_json => ",val1);
//
//        osw.write(String.valueOf(app_names));
//        osw.flush();
//        try {
//            osw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return app_names;
//    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.verma.karan.cruncher/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.verma.karan.cruncher/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //VisibleForTesting
    static enum StatsUsageInterval {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

        private int mInterval;
        private String mStringRepresentation;

        StatsUsageInterval(String stringRepresentation, int interval) {
            mStringRepresentation = stringRepresentation;
            mInterval = interval;
        }

        static StatsUsageInterval getValue(String stringRepresentation) {
            for (StatsUsageInterval statsUsageInterval : values()) {
                if (statsUsageInterval.mStringRepresentation.equals(stringRepresentation)) {
                    return statsUsageInterval;
                }
            }
            return null;
        }
    }

}
