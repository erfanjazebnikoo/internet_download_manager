/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package idm.test.com.internetdownloadmanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import idm.test.com.internetdownloadmanager.controller.TasksManager;
import idm.test.com.internetdownloadmanager.model.Time;
import idm.test.com.internetdownloadmanager.receiver.CancelDownload;
import idm.test.com.internetdownloadmanager.receiver.StartDownload;
import idm.test.com.internetdownloadmanager.view.TaskItemAdapter;
import idm.test.com.internetdownloadmanager.view.TaskItemViewHolder;

/**
 * Created by Jacksgong on 1/9/16.
 */
public class InternetDownloadManagerActivity extends AppCompatActivity implements FileSizeInterface {

    Button addUrlButton;
    Button downloadAllButton;
    Button clearHistoryButton;
    TaskItemAdapter taskItemAdapter;
    RecyclerView recyclerView;
    Context context;
    int currentPosition;

    EstimateFileSize estimateFileSize;
    FileSizeInterface fileSizeInterface;

    Dialog dialog;
    int okButtonCounter;
    DatePicker datePicker;
    TimePicker timePicker;
    TextView dialogeTitle;
    Time startTime;
    Time finishTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_download_manager);

        context = getApplicationContext();
        currentPosition = 0;

        FileDownloadUtils.setDefaultSaveRootPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "IDM");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskItemAdapter = new TaskItemAdapter(TasksManager.getImpl().getModelList());
        taskItemAdapter.setSpeed(-1);
        recyclerView.setAdapter(taskItemAdapter);

        checkForPermissionsMAndAboveBlocking(this);

        ////////////////////////////////////////////////// Download Scheduler

        okButtonCounter = 0;

        dialog = new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Download Scheduler");

        datePicker = dialog.findViewById(R.id.datePicker);
        timePicker = dialog.findViewById(R.id.timePicker);
        dialogeTitle = dialog.findViewById(R.id.dialog_title);

        startTime = new Time();
        finishTime = new Time();

        ////////////////////////////////////////////////////////

        addUrlButton = (Button) findViewById(R.id.add_url_btn);
        downloadAllButton = (Button) findViewById(R.id.download_all_btn);
        clearHistoryButton = (Button) findViewById(R.id.clear_history_btn);

        addUrlButton.setOnClickListener(addUrlOnClickListener);

        downloadAllButton.setOnClickListener(downloadAllOnClickListener);
        clearHistoryButton.setOnClickListener(clearHistoryOnClickListener);

        TasksManager.getImpl().onCreate(new WeakReference<>(this));
        fileSizeInterface = this;

        registerReceiver(broadcastReceiver, new IntentFilter("START_DOWNLOAD"));
        registerReceiver(broadcastReceiver2, new IntentFilter("CANCEL_DOWNLOAD"));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            downloadAll();
            Toast.makeText(context, "Download Started Automatically...!", Toast.LENGTH_LONG).show();
        }
    };

    BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FileDownloader.getImpl().pauseAll();
            for (int i = 0; i < TasksManager.getImpl().getModelList().size(); i++) {
                taskItemAdapter.notifyItemChanged(i);
            }
            Toast.makeText(context, "All Downloads Paused Automatically...!", Toast.LENGTH_LONG).show();
        }
    };

    public void postNotifyDataChanged() {
        if (taskItemAdapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskItemAdapter != null) {
                        taskItemAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        TasksManager.getImpl().onDestroy();
        taskItemAdapter = null;
        FileDownloader.getImpl().pauseAll();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(broadcastReceiver2);
        super.onDestroy();
    }

    View.OnClickListener addUrlOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("URL:");

            estimateFileSize = new EstimateFileSize();
            estimateFileSize.delegate = fileSizeInterface;

            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!input.getText().toString().isEmpty()) {
                        currentPosition = TasksManager.getImpl().getTaskCounts();
                        estimateFileSize.execute(input.getText().toString());
                        TasksManager.getImpl().addTask(input.getText().toString());
                        taskItemAdapter.notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    };

    @Override
    public void postResult(double asyncresult) {
        taskItemAdapter.updateFileSize(asyncresult, currentPosition);
    }

    private class EstimateFileSize extends AsyncTask<String, String, Double> {

        FileSizeInterface delegate = null;

        protected Double doInBackground(String... urls) {
            HttpURLConnection ucon = null;
            try {
                if (android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();
                final URL url = new URL(urls[0]);
                ucon = (HttpURLConnection) url.openConnection();
                ucon.connect();
                return (double) ucon.getContentLength() / 1024.0;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ucon.disconnect();
            }
            return 0.0;
        }

        @Override
        protected void onPostExecute(Double result) {
            if (delegate != null) {
                delegate.postResult(result);
            } else {
                Log.e("ApiAccess", "You have not assigned IApiAccessResponse delegate");
            }
        }
    }

    View.OnClickListener downloadAllOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            downloadAll();
        }
    };


    View.OnClickListener clearHistoryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TasksManager.getImpl().clearAllModelList();
            taskItemAdapter.removeAll();
            taskItemAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.other_demo:
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                return true;
            case R.id.schedule:
                okButtonCounter = 0;
                dialog.show();
                Button okButton = dialog.findViewById(R.id.dilaog_ok);
                Button cancelButton = dialog.findViewById(R.id.dilaog_cancel);
                okButton.setOnClickListener(okButtonOnClickListener);
                cancelButton.setOnClickListener(cancelButtonButtonOnClickListener);
                return true;
            case R.id.speedLimit:
                speedLimiter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(23)
    public static void checkForPermissionsMAndAboveBlocking(Activity act) {
        Log.i("Permission", "checkForPermissions() called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Here, thisActivity is the current activity
            if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                act.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                while (true) {
                    if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission", "Got permissions, exiting block loop");
                        break;
                    }
                    Log.i("Permission", "Sleeping, waiting for permissions");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (act.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                act.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                while (true) {
                    if (act.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission", "Got permissions, exiting block loop");
                        break;
                    }
                    Log.i("Permission", "Sleeping, waiting for permissions");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (act.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                act.requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
                while (true) {
                    if (act.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission", "Got permissions, exiting block loop");
                        break;
                    }
                    Log.i("Permission", "Sleeping, waiting for permissions");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (act.checkSelfPermission(Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                act.requestPermissions(new String[]{Manifest.permission.SET_ALARM}, 0);
                while (true) {
                    if (act.checkSelfPermission(Manifest.permission.SET_ALARM) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission", "Got permissions, exiting block loop");
                        break;
                    }
                    Log.i("Permission", "Sleeping, waiting for permissions");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (act.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
                act.requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 0);
                while (true) {
                    if (act.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission", "Got permissions, exiting block loop");
                        break;
                    }
                    Log.i("Permission", "Sleeping, waiting for permissions");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // permission already granted
            else {
                Log.i("Permission", "permission already granted");
            }
        } else {
            Log.i("Permission", "Below M, permissions not via code");
        }
    }

    public void downloadAll() {
        for (TaskItemViewHolder holder : taskItemAdapter.taskItemViewHolderList) {
            holder.taskActionBtn.performClick();
        }
    }

    void speedLimiter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Limit Connection's Speed:");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);

        linearLayout2.setPadding(10, 0, 10, 0);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        final EditText input = new EditText(this);

        final TextView textView = new TextView(this);
        textView.setText("KB/s");

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setLayoutParams(p);

        final CheckBox isUnlimit = new CheckBox(this);
        isUnlimit.setText("Unlimited");

        isUnlimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    input.setEnabled(false);
                } else {
                    input.setEnabled(true);
                }

            }
        });

        linearLayout.addView(isUnlimit);

        linearLayout2.addView(input);
        linearLayout2.addView(textView);

        linearLayout.addView(linearLayout2);
        builder.setView(linearLayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isUnlimit.isChecked()) {
                    taskItemAdapter.setSpeed(-1);
                    Toast.makeText(context, "-1", Toast.LENGTH_LONG).show();
                } else {
                    if (!input.getText().toString().isEmpty()) {
                        taskItemAdapter.setSpeed(Integer.valueOf(input.getText().toString()));
                        Toast.makeText(context, input.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    View.OnClickListener okButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (okButtonCounter) {
                case 0:
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.VISIBLE);
                    dialogeTitle.setText("Choose Start Time");
                    readStartDate();
                    break;
                case 1:
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.GONE);
                    dialogeTitle.setText("Choose Finish Date");
                    readStartTime();
                    break;
                case 2:
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.VISIBLE);
                    dialogeTitle.setText("Choose Finish Time");
                    readFinishDate();
                    break;
                case 3:
                    readFinishTime();
                    dialog.cancel();
                    Toast.makeText(context, "Set Schedule Completed...!", Toast.LENGTH_LONG).show();
                    setAlarmForDownload();
                    break;
            }
            okButtonCounter++;
        }
    };

    View.OnClickListener cancelButtonButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.cancel();
        }
    };

    void readStartDate() {
        startTime.setYear(datePicker.getYear());
        startTime.setMonth(datePicker.getMonth());
        startTime.setDay(datePicker.getDayOfMonth());
    }

    void readStartTime() {
        if (Build.VERSION.SDK_INT < 23) {
            startTime.setHour(timePicker.getCurrentHour());
            startTime.setMinute(timePicker.getCurrentMinute());
        } else {
            startTime.setHour(timePicker.getHour());
            startTime.setMinute(timePicker.getMinute());
        }
    }

    void readFinishDate() {
        finishTime.setYear(datePicker.getYear());
        finishTime.setMonth(datePicker.getMonth());
        finishTime.setDay(datePicker.getDayOfMonth());
    }

    void readFinishTime() {
        if (Build.VERSION.SDK_INT < 23) {
            finishTime.setHour(timePicker.getCurrentHour());
            finishTime.setMinute(timePicker.getCurrentMinute());
        } else {
            finishTime.setHour(timePicker.getHour());
            finishTime.setMinute(timePicker.getMinute());
        }
    }

    void setAlarmForDownload() {
        Calendar startAlarm = Calendar.getInstance();
        startAlarm.setTimeInMillis(System.currentTimeMillis());

        startAlarm.set(Calendar.YEAR, startTime.getYear());
        startAlarm.set(Calendar.MONTH, startTime.getMonth());
        startAlarm.set(Calendar.DAY_OF_MONTH, startTime.getDay());
        startAlarm.set(Calendar.HOUR_OF_DAY, startTime.getHour());
        startAlarm.set(Calendar.MINUTE, startTime.getMinute());
        startAlarm.set(Calendar.SECOND, 0);

        StartDownload alarm = new StartDownload();

        alarm.setAlarm(this.getApplicationContext(), startAlarm);


        Calendar stopAlarm = Calendar.getInstance();
        stopAlarm.setTimeInMillis(System.currentTimeMillis());
        stopAlarm.set(Calendar.YEAR, finishTime.getYear());
        stopAlarm.set(Calendar.MONTH, finishTime.getMonth());
        stopAlarm.set(Calendar.DAY_OF_MONTH, finishTime.getDay());
        stopAlarm.set(Calendar.HOUR_OF_DAY, finishTime.getHour());
        stopAlarm.set(Calendar.MINUTE, finishTime.getMinute());
        stopAlarm.set(Calendar.SECOND, 0);

        CancelDownload cancelAlarm = new CancelDownload();
        cancelAlarm.setAlarm(getApplicationContext(), stopAlarm);
    }

}
