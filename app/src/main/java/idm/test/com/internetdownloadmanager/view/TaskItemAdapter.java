package idm.test.com.internetdownloadmanager.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import idm.test.com.internetdownloadmanager.R;
import idm.test.com.internetdownloadmanager.controller.TasksManager;
import idm.test.com.internetdownloadmanager.model.TasksManagerModel;

/**
 * Created by erfan on 11/11/2017.
 */

public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemViewHolder> {

    public final Set<TaskItemViewHolder> taskItemViewHolderList;
    public List<TasksManagerModel> tasksManagerModelList;
    private int speed;

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public TaskItemAdapter(List<TasksManagerModel> tasksManagerModelList) {
        this.tasksManagerModelList = tasksManagerModelList;
        this.taskItemViewHolderList = new HashSet<>();
    }

    public FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {

        private TaskItemViewHolder checkCurrentHolder(final BaseDownloadTask task) {
            final TaskItemViewHolder tag = (TaskItemViewHolder) task.getTag();
            if (tag.id != task.getId()) {
                return null;
            }

            return tag;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.pending, soFarBytes
                    , totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.connected, soFarBytes
                    , totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }
            if (speed != -1) {
                if (task.getSpeed() <= speed) {
                    tag.updateSpeed(task.getSpeed());
                } else {
                    tag.updateSpeed(speed);
                }
            } else {
                tag.updateSpeed(task.getSpeed());
            }

            tag.updateDownloading(FileDownloadStatus.progress, soFarBytes
                    , totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }
            if (speed != -1) {
                if (task.getSpeed() <= speed) {
                    tag.updateSpeed(task.getSpeed());
                } else {
                    tag.updateSpeed(speed);
                }
            } else {
                tag.updateSpeed(task.getSpeed());
            }
            tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }
            if (speed != -1) {
                if (task.getSpeed() <= speed) {
                    tag.updateSpeed(task.getSpeed());
                } else {
                    tag.updateSpeed(speed);
                }
            } else {
                tag.updateSpeed(task.getSpeed());
            }
            tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            final TaskItemViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloaded();
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }
    };
    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }
            TaskItemViewHolder holder = (TaskItemViewHolder) v.getTag();

            CharSequence action = ((TextView) v).getText();
            if (action.equals(v.getResources().getString(R.string.pause))) {
                // to pause
                FileDownloader.getImpl().pause(holder.id);
            } else if (action.equals(v.getResources().getString(R.string.start))) {
                // to start
                // to start
                final TasksManagerModel model = TasksManager.getImpl().get(holder.position);
                BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                        .setPath(model.getPath())
                        .setCallbackProgressTimes(100)
                        .setListener(taskDownloadListener);

                TasksManager.getImpl()
                        .addTaskForViewHolder(task);

                TasksManager.getImpl()
                        .updateViewHolder(holder.id, holder);

                task.start();
            } else if (action.equals(v.getResources().getString(R.string.clear))) {
//                // to delete
//                new File(TasksManager.getImpl().get(holder.position).getPath()).delete();
//                holder.taskActionBtn.setEnabled(true);
//                holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS, 0, 0);
                removeAt(holder.position);
                TasksManager.getImpl().removeItemFromModelList(holder.position, holder.id);
                TasksManager.getImpl().removeTaskForViewHolder(holder.id);
            }
        }
    };


    @Override
    public TaskItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskItemViewHolder holder = new TaskItemViewHolder(
                LayoutInflater.from(
                        parent.getContext())
                        .inflate(R.layout.item_tasks_manager, parent, false));

        holder.taskActionBtn.setOnClickListener(taskActionOnClickListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(TaskItemViewHolder holder, int position) {
        final TasksManagerModel model = tasksManagerModelList.get(position);

        holder.update(model.getId(), position);
        holder.taskActionBtn.setTag(holder);
        holder.taskNameTv.setText(model.getName());

        TasksManager.getImpl()
                .updateViewHolder(holder.id, holder);

        holder.taskActionBtn.setEnabled(true);

        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (status == FileDownloadStatus.pending || status == FileDownloadStatus.started ||
                    status == FileDownloadStatus.connected) {
                // start task, but file not created yet
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else if (!new File(model.getPath()).exists() &&
                    !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded();
            } else if (status == FileDownloadStatus.progress) {
                // downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            }
        } else {
            holder.taskStatusTv.setText(R.string.tasks_manager_demo_status_loading);
            holder.taskActionBtn.setEnabled(false);
        }

        taskItemViewHolderList.add(holder);
    }

    @Override
    public int getItemCount() {
        return TasksManager.getImpl().getTaskCounts();
    }


    private void removeAt(int position) {
        taskItemViewHolderList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskItemViewHolderList.size());
    }

    public void removeAll() {

        int size = this.taskItemViewHolderList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.taskItemViewHolderList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void updateData(List<TasksManagerModel> viewModels) {
        tasksManagerModelList.clear();
        tasksManagerModelList.addAll(viewModels);
        notifyDataSetChanged();
    }

    public void addItem(int position, TasksManagerModel viewModel) {
        tasksManagerModelList.add(position, viewModel);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        tasksManagerModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateFileSize(double size, int position) {
        for (TaskItemViewHolder holder : taskItemViewHolderList) {
            if (holder.position == position) {
                holder.updateSize(size);
                break;
            }
        }
    }
}
