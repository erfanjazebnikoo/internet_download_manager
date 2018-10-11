package idm.test.com.internetdownloadmanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.liulishuo.filedownloader.FileDownloadMonitor;
import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by Jacksgong on 12/17/15.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


//        FileDownloadUtils.setDefaultSaveRootPath(getCacheDir().getPath());

        // 这是只是为了全局监控。如果你有需求需要全局监控（比如用于打点/统计）可以使用这个方式，如果没有类似需求就不需要
        // 如果你有这个需求，实现FileDownloadMonitor.IMonitor接口，也使用FileDownloadMonitor.setGlobalMonitor
        // 注册进去即可
        // You do not have to add below code to your project only if you need monitor the global
        // FileDownloader Engine for statistic or others
        // If you have such requirement, just implement FileDownloadMonitor.IMonitor, and register it
        // use FileDownloadDownloader.setGlobalMonitor the same as below code.
        FileDownloadMonitor.setGlobalMonitor(GlobalMonitor.getImpl());
    }

    public void onClickMultitask(final View view) {
        startActivity(new Intent(this, MultitaskTestActivity.class));
    }

    public void onClickSingle(final View view) {
        startActivity(new Intent(this, SingleTaskTestActivity.class));
    }

    public void onClickHybridTest(final View view) {
        startActivity(new Intent(this, HybridTestActivity.class));
    }

    public void onClickTasksManager(final View view) {
        startActivity(new Intent(this, InternetDownloadManagerActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileDownloader.getImpl().unBindServiceIfIdle();

        FileDownloadMonitor.releaseGlobalMonitor();
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
}
