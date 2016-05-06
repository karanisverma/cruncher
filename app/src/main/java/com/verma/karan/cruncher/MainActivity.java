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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mUsageStatsManager = (UsageStatsManager) this
                .getSystemService("usagestats");
//        getNetworkStats();
//        getSms();
//        getCallDetails();
//        getBrowserHist();

        StatsUsageInterval statsUsageInterval = StatsUsageInterval.DAILY;
        if (statsUsageInterval != null) {
            List<UsageStats> usageStatsList =
                    getUsageStatistics(statsUsageInterval.mInterval);
//                    Collections.sort(usageStatsList, new LastTimeLaunchedComparatorDesc());
            try {
                updateAppsList(usageStatsList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



    private void getCallDetails() {
        StringBuffer sb = new StringBuffer();
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Log :");
        while (managedCursor.moveToNext()) {
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
            Log.d("Number => ", phNumber);
            Log.d("TYPE => ", dir);
            Log.d("DATE =>", callDate);
            Log.d("Durstion =>",callDuration);
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
        } //managedCursor.close(); textView.setText(sb); }


    }

    public void getSms(){
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = this.getContentResolver().query(uriSms, null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    Log.v("SMS => ", msgData);
                }
                // use msgData
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
    }

    public void getNetworkStats(){
        long ts = TrafficStats.getTotalRxBytes();
        Log.v("TOTAL BYTE RECEIVED => ",String.valueOf(ts));

    }


    public void getBrowserHist()  {
        Uri uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");
        Cursor mCur = this.getContentResolver().query(uriCustom,
                Browser.HISTORY_PROJECTION, null, null, null);
        mCur.moveToFirst();
        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            while (mCur.isAfterLast() == false) {
                Log.v("TITLE => ", mCur
                        .getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
                Log.v("URL => ", mCur
                        .getString(Browser.HISTORY_PROJECTION_URL_INDEX));
                Log.v("DATE => ", mCur
                        .getString(Browser.HISTORY_PROJECTION_DATE_INDEX));
                Log.v("VISITS => ", mCur
                        .getString(Browser.HISTORY_PROJECTION_VISITS_INDEX));
                mCur.moveToNext();
            }
        }
    }




    public List<UsageStats> getUsageStatistics(int intervalType) {
        List<String> nameNtime = new ArrayList<String>();
        DateFormat mDateFormat = new SimpleDateFormat();
        long now = System.currentTimeMillis();
//         Get the app statistics since epoch till current date.
        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, 0,now);
        Log.d("SIZE => ", String.valueOf(queryUsageStats.size()));
        for (int i = 0; i < queryUsageStats.size(); i++) {

            CustomUsageStats cUsageStats = new CustomUsageStats();
            cUsageStats.usageStats = queryUsageStats.get(i);
            int val = 0;
            PackageManager packageManager= this.getApplicationContext().getPackageManager();
            String appName="";
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(cUsageStats.usageStats.getPackageName(), PackageManager.GET_META_DATA));
            }
            catch (PackageManager.NameNotFoundException e){
                appName = "";
            }

            Log.d("PACKAGE NAME => ",appName);
            Log.d("TOTAL TIME => ",String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground()/60000));
//            Log.d("TYPE => ",);
//            Log.d("CONTENT => ",String.valueOf(cUsageStats.usageStats.describeContents()));
//            Log.d("FIRST_TIME STAMP => ",String.valueOf(cUsageStats.usageStats.getFirstTimeStamp()));
            Log.d("LAST_TIME STAMP => ",String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeStamp()))));
            Log.d("LAST_TIME USED => ",String.valueOf(mDateFormat.format(new Date(cUsageStats.usageStats.getLastTimeUsed()))));
//            Log.d("------------------",String.valueOf(val));
            nameNtime.add(String.valueOf(cUsageStats.usageStats.getPackageName()) +" -> "+String.valueOf(cUsageStats.usageStats.getTotalTimeInForeground()/60000));
        }
// }
//        Log.d("List", String.valueOf(nameNtime));
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
        }
        return queryUsageStats;
    }
    void updateAppsList(List<UsageStats> usageStatsList) throws IOException {
        //writing stuff in mobile

        File sdCard = Environment.getExternalStorageDirectory();
        DateFormat mDateFormat = new SimpleDateFormat();
        File directory = new File (sdCard.getAbsolutePath() + "/MyFiles");
        directory.mkdirs();

        //Now create the file in the above directory and write the contents into it
        File file = new File(directory, "mysdfile.txt");
        FileOutputStream fOut = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();
        List<String> app_names = new ArrayList<String>();
        for (int i = 0; i < usageStatsList.size(); i++) {
            CustomUsageStats customUsageStats = new CustomUsageStats();
            customUsageStats.usageStats = usageStatsList.get(i);
            try {
                Drawable appIcon = this.getPackageManager()
                        .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                customUsageStats.appIcon = appIcon;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("ERROR => ", String.format("App Icon is not found for %s",
                        customUsageStats.usageStats.getPackageName()));
                customUsageStats.appIcon = this
                        .getDrawable(R.drawable.ic_default_app_launcher);
            }
            customUsageStatsList.add(customUsageStats);
            customUsageStats.use_time = customUsageStats.usageStats.getTotalTimeInForeground() / 60000;

            app_names.add(customUsageStats.usageStats.getPackageName()+ " Total Time => "+customUsageStats.use_time+ " First Time => "+mDateFormat.format(new Date(customUsageStats.usageStats.getFirstTimeStamp())));
        }


        osw.write(String.valueOf(app_names));
        osw.flush();
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
