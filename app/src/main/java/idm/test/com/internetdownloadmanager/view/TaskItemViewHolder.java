package idm.test.com.internetdownloadmanager.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.model.FileDownloadStatus;

import idm.test.com.internetdownloadmanager.DemoApplication;
import idm.test.com.internetdownloadmanager.R;

/**
 * Created by erfan on 11/11/2017.
 */

public class TaskItemViewHolder extends RecyclerView.ViewHolder {

    TextView taskNameTv;
    TextView taskStatusTv;
    private TextView taskSizeTv;
    private TextView taskSpeedTv;
    ProgressBar taskPb;
    public Button taskActionBtn;
    int position;
    public int id;

    TaskItemViewHolder(View itemView) {
        super(itemView);
        assignViews();
    }

    void assignViews() {
        taskNameTv = (TextView) findViewById(R.id.task_name_tv);
        taskStatusTv = (TextView) findViewById(R.id.task_status_tv);
        taskPb = (ProgressBar) findViewById(R.id.task_pb);
        taskActionBtn = (Button) findViewById(R.id.task_action_btn);
        taskSizeTv = (TextView) findViewById(R.id.size_tv);
        taskSpeedTv = (TextView) findViewById(R.id.speed_tv);
    }

    private View findViewById(final int id) {
        return itemView.findViewById(id);
    }

    void update(final int id, final int position) {
        this.id = id;
        this.position = position;
    }

    void updateDownloaded() {
        taskPb.setMax(1);
        taskPb.setProgress(1);

        taskStatusTv.setText(R.string.tasks_manager_demo_status_completed);
        taskActionBtn.setText(R.string.clear);
    }

    void updateNotDownloaded(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            final float percent = sofar
                    / (float) total;
            taskPb.setMax(100);
            taskPb.setProgress((int) (percent * 100));
        } else {
            taskPb.setMax(1);
            taskPb.setProgress(0);
        }


        switch (status) {
            case FileDownloadStatus.error:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_error);
                break;
            case FileDownloadStatus.paused:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
                break;
            default:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_not_downloaded);
                break;
        }
        taskActionBtn.setText(R.string.start);
    }

    void updateDownloading(final int status, final long sofar, final long total) {
        final float percent = sofar
                / (float) total;
        taskPb.setMax(100);
        taskPb.setProgress((int) (percent * 100));

        switch (status) {
            case FileDownloadStatus.pending:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
                break;
            case FileDownloadStatus.started:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
                break;
            case FileDownloadStatus.connected:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
                break;
            case FileDownloadStatus.progress:
                taskStatusTv.setText(R.string.tasks_manager_demo_status_progress);
                break;
            default:
                taskStatusTv.setText(DemoApplication.CONTEXT.getString(
                        R.string.tasks_manager_demo_status_downloading, status));
                break;
        }

        taskActionBtn.setText(R.string.pause);
    }

    void updateSize(double fileSize) {

        if (fileSize < 1024.0) {
            taskSizeTv.setText("Size: " + String.format("%.2f", fileSize) + " KB");
        } else {
            fileSize /= 1024.0;
            if (fileSize < 1024.0) {
                taskSizeTv.setText("Size: " + String.format("%.2f", fileSize) + " MB");
            } else {
                fileSize /= 1024.0;
                if (fileSize < 1024.0) {
                    taskSizeTv.setText("Size: " + String.format("%.2f", fileSize) + " GB");
                }
            }
        }
    }

    void updateSpeed(int speed) {
        taskSpeedTv.setText(String.format("%d KB/s", speed));
    }
}
