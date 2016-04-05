package com.example.filesscanner.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filesscanner.R;
import com.example.filesscanner.common.TConstants;
import com.example.filesscanner.fragments.BigFilesFragment;
import com.example.filesscanner.fragments.MostUsedFilesFragment;
import com.example.filesscanner.tasks.AbstractScanFilesTask;
import com.example.filesscanner.utils.FileUtils;
import com.example.filesscanner.utils.SDCardFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private Button startBtn, stopBtn;
    private final int PERMISSION_REQ = 10;
    private CoordinatorLayout mainLayout;
    private ArrayList<SDCardFile> filesList = null;
    private String[] fileExtensions;
    private TreeMap<String, Integer> sortedFrequencyCountMap;
    private TreeMap<String, Integer> frequencyCountMap;
    private HashMap<String, Double> topSizedFiles;
    private double totalFileSize;
    private ArrayList<SDCardFile> sortedFiles = null;
    private double top10FileSize;
    private ProgressBar progressBar;
    private ScanFilesTask scanFilesTask;
    private ViewPager viewPager;
    private MostUsedFilesFragment mostUsedFilesFragment;
    private BigFilesFragment bigFilesFragment;
    private TabLayout tabLayout;
    private TextView statusTv, fileStatsTv;
    private String totalFilesAvgSize, fileStats;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startBtn = (Button) findViewById(R.id.startBtn);

        stopBtn = (Button) findViewById(R.id.stopBtn);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        statusTv = (TextView) findViewById(R.id.statusTv);
        fileStatsTv = (TextView) findViewById(R.id.fileStatsTv);

        startBtn.setOnClickListener(clickListener);
        stopBtn.setOnClickListener(clickListener);

        viewPager.setAdapter(new FilesPagerAdapter(getSupportFragmentManager()));

        filesList = new ArrayList<>();
        fileExtensions = FileUtils.getFormatArray();
        sortedFrequencyCountMap = new TreeMap<>();
        frequencyCountMap = new TreeMap<>();
        topSizedFiles = new HashMap<>();
        sortedFiles = new ArrayList<>();

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.most_used_string)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.frequency_string)));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.hasExtra(TConstants.INTENT_EXTRA)) {
            if (intent.getExtras().getInt(TConstants.INTENT_EXTRA) == TConstants.STOP)
                stopScanning();
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.startBtn:
                    startScanning();
                    break;

                case R.id.stopBtn:
                    stopScanning();
                    break;
            }
        }
    };

    private void startScanning() {

        if (!isPermissionCheckRequired()) {
            scanFiles();
        }
    }

    /**
     * Initiate scan process
     */
    private void scanFiles() {

        scanFilesTask = new ScanFilesTask(MainActivity.this, fileExtensions, sortedFrequencyCountMap, frequencyCountMap, filesList, topSizedFiles, totalFileSize,
                sortedFiles, top10FileSize);
        scanFilesTask.execute();
        progressBar.setVisibility(View.VISIBLE);
        statusTv.setVisibility(View.GONE);
        viewPager.setVisibility(View.INVISIBLE);

        fileStats = null;

        createNotificaton();
    }


    private class ScanFilesTask extends AbstractScanFilesTask {


        public ScanFilesTask(Context context, String[] fileExtensions, TreeMap<String, Integer> sortedFrequencyCountMap, TreeMap<String, Integer> frequencyCountMap,
                             ArrayList<SDCardFile> filesList, HashMap<String, Double> topSizedFiles, double totalFileSize, ArrayList<SDCardFile> sortedFiles, double top10FileSize) {
            super(context, fileExtensions, sortedFrequencyCountMap, frequencyCountMap, filesList, topSizedFiles, totalFileSize, sortedFiles, top10FileSize);
        }

        @Override
        protected void onSuccess(ArrayList<SDCardFile> sortedFiles, TreeMap<String, Integer> sortedFrequencyCountMap,
                                 String totalFilesAvgSize, String top10FilesAvg) {

            if (mostUsedFilesFragment != null)
                mostUsedFilesFragment.setMostUsedFiles(sortedFrequencyCountMap);

            if (bigFilesFragment != null)
                bigFilesFragment.setMostUsedFiles(sortedFiles);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            MainActivity.this.fileStats = "Total files Avg = " + totalFilesAvgSize +
                    "\n Top 10 Files Avg = " + top10FilesAvg;

            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);

            fileStatsTv.setText(fileStats);
            fileStatsTv.startAnimation(animation);
            fileStatsTv.setVisibility(View.VISIBLE);

            removeNotification();
            invalidateOptionsMenu();
        }

        @Override
        protected void onFailure() {
            statusTv.setText(getString(R.string.not_scanned_string));
            statusTv.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            removeNotification();
        }
    }

    /**
     * If VERSION > Android M, request for dynamic permission
     *
     * @return
     */
    private boolean isPermissionCheckRequired() {
        //Check for permission if API Level > 23
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Request for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    scanFiles();

                } else {

                    //Show Retry option
                    Snackbar snackbar = Snackbar
                            .make(mainLayout, getString(R.string.permission_denied_string), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.retry_string), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startScanning();
                                }
                            });

                    snackbar.setActionTextColor(Color.RED);

                    View snackbarView = snackbar.getView();
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                }
                return;
            }
        }
    }

    private void stopScanning() {
        if (scanFilesTask != null && scanFilesTask.getStatus() == AsyncTask.Status.RUNNING) {
            scanFilesTask.cancel(true);

            progressBar.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);

            Toast.makeText(this, "Scan stopped", Toast.LENGTH_LONG).show();

            removeNotification();
        }
    }


    private class FilesPagerAdapter extends FragmentStatePagerAdapter {

        int COUNT = 2;
        String[] titles = new String[]{"Frequency", "Size"};

        public FilesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mostUsedFilesFragment == null)
                        mostUsedFilesFragment = new MostUsedFilesFragment();
                    return mostUsedFilesFragment;

                case 1:
                    if (bigFilesFragment == null)
                        bigFilesFragment = new BigFilesFragment();
                    return bigFilesFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return COUNT;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Share button appear only after successful results
        if (fileStats != null && fileStats.length() > 0) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        } else
            getMenuInflater().inflate(R.menu.menu_default, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "File Stats");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, fileStats);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a notification
     * Show big notation; User can stop current scan from notification bar
     */
    public void createNotificaton() {

        Intent stopIntent = new Intent(getApplicationContext(), MainActivity.class);
        stopIntent.putExtra(TConstants.INTENT_EXTRA, TConstants.STOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, stopIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.scan_on_going))

                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                        .setContentText(getApplicationContext().getResources().getString(R.string.clickHereToOpen))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(""))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.small_circle_button_red,
                                getString(R.string.stop_string), resultPendingIntent);


        mBuilder.setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(TConstants.NOTIFICATION_ID, mBuilder.build());
    }

    private void removeNotification() {
        if (mNotificationManager != null)
            mNotificationManager.cancel(TConstants.NOTIFICATION_ID);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopScanning();
    }
}