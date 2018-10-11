package idm.test.com.internetdownloadmanager.controller;

import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import idm.test.com.internetdownloadmanager.Constant;
import idm.test.com.internetdownloadmanager.InternetDownloadManagerActivity;
import idm.test.com.internetdownloadmanager.model.TasksManagerModel;
import idm.test.com.internetdownloadmanager.view.TaskItemViewHolder;

/**
 * Created by erfan on 11/11/2017.
 */

public class TasksManager {
    private final static class HolderClass {
        private final static TasksManager INSTANCE = new TasksManager();
    }

    public static TasksManager getImpl() {
        return HolderClass.INSTANCE;
    }

    private TasksManagerDBController dbController;
    private List<TasksManagerModel> modelList;

    private TasksManager() {
        dbController = new TasksManagerDBController();
        modelList = dbController.getAllTasks();

        initDemo();
    }

    private void initDemo() {
        if (modelList.size() <= 0) {
            final int demoSize = Constant.BIG_FILE_URLS.length;
//            for (int i = 0; i < demoSize; i++) {
//                final String url = Constant.BIG_FILE_URLS[i];
//                addTask(url);
//            }
        }
    }

    private SparseArray<BaseDownloadTask> taskSparseArray = new SparseArray<>();

    public void addTaskForViewHolder(final BaseDownloadTask task) {
        taskSparseArray.put(task.getId(), task);
    }

    public void removeTaskForViewHolder(final int id) {
        taskSparseArray.remove(id);
    }

    public void updateViewHolder(final int id, final TaskItemViewHolder holder) {
        final BaseDownloadTask task = taskSparseArray.get(id);
        if (task == null) {
            return;
        }

        task.setTag(holder);
    }

    private void releaseTask() {
        taskSparseArray.clear();
    }

    private FileDownloadConnectListener listener;

    private void registerServiceConnectionListener(final WeakReference<InternetDownloadManagerActivity>
                                                           activityWeakReference) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
        }

        listener = new FileDownloadConnectListener() {

            @Override
            public void connected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }

            @Override
            public void disconnected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }
        };

        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    private void unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
        listener = null;
    }

    public void onCreate(final WeakReference<InternetDownloadManagerActivity> activityWeakReference) {
        if (!FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().bindService();
            registerServiceConnectionListener(activityWeakReference);
        }
    }

    public void onDestroy() {
        unregisterServiceConnectionListener();
        releaseTask();
    }

    public boolean isReady() {
        return FileDownloader.getImpl().isServiceConnected();
    }

    public TasksManagerModel get(final int position) {
        return modelList.get(position);
    }

    public void removeItemFromModelList(final int position, final int id) {
        modelList.remove(position);
        dbController.deleteTask(id);
    }

    public void clearAllModelList() {
        modelList.clear();
        dbController.deleteAll();
    }

    public List<TasksManagerModel> getModelList() {
        return modelList;
    }

    private TasksManagerModel getById(final int id) {
        for (TasksManagerModel model : modelList) {
            if (model.getId() == id) {
                return model;
            }
        }

        return null;
    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    public boolean isDownloaded(final int status) {
        return status == FileDownloadStatus.completed;
    }

    public int getStatus(final int id, String path) {
        return FileDownloader.getImpl().getStatus(id, path);
    }

    public long getTotal(final int id) {
        return FileDownloader.getImpl().getTotal(id);
    }

    public long getSoFar(final int id) {
        return FileDownloader.getImpl().getSoFar(id);
    }

    public int getTaskCounts() {
        return modelList.size();
    }

    public TasksManagerModel addTask(final String url) {
        return addTask(url, createPath(url));
    }

    private TasksManagerModel addTask(final String url, final String path) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null;
        }

        final int id = FileDownloadUtils.generateId(url, path);
        TasksManagerModel model = getById(id);
        if (model != null) {
            return model;
        }
        final TasksManagerModel newModel = dbController.addTask(url, path);
        if (newModel != null) {
            modelList.add(newModel);
        }

        return newModel;
    }

    private String createPath(final String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(url);
        return FileDownloadUtils.generateFilePath(FileDownloadUtils.getDefaultSaveRootPath(), URLUtil.guessFileName(url, null, fileExtenstion));
    }
}
