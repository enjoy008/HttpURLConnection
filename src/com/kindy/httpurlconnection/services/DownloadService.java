package com.kindy.httpurlconnection.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.kindy.httpurlconnection.models.FileInfo;
import com.kindy.httpurlconnection.utils.DBBaseHelper;
import com.kindy.httpurlconnection.utils.DBDownloadHelper;
import com.kindy.httpurlconnection.utils.Debug;
import com.kindy.httpurlconnection.utils.SQLiteUtils;

public class DownloadService extends Service {

    //路径标识
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";

    public static final String NOTICE_FILE_ID = "notice_file_id";
	public static final String NOTICE_PROGRESS = "notice_progress";
	public static final String NOTICE_DOWNLOAD_FINISH = "notice_download_finish";
	
	public static final String FILE_INFO = "file_info";

    //开始标识
    public static final String ACTION_START = "action_start";
    //结束标识
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_UPDATE = "action_update";

    //handler传递标识
    public static final int MSG_INIT = 0;
    
    //定义下载任务
    private List<DownloadTask> mDownloadTaskList;

    private DownloadTask getDownloadTask(int fileId) {
    	synchronized (mDownloadTaskList) {
    		for(DownloadTask t : mDownloadTaskList) {
        		if(t.fileInfo.id == fileId) {
        			return t;
        		}
        	}
        	return null;
    	}
    }
    private boolean addDownloadTask(DownloadTask task) {
    	synchronized (mDownloadTaskList) { 
    		if(!mDownloadTaskList.contains(task)) {
        		return mDownloadTaskList.add(task);
        	}
        	return false;
    	}
    }
    private boolean removeDownloadTask(DownloadTask task) {
    	synchronized (mDownloadTaskList) {  
    		return mDownloadTaskList.remove(task);
    	}
    }
    private boolean removeDownloadTask(int fileId) {
    	DownloadTask task = getDownloadTask(fileId);
    	if(task != null) {
    		task.cancel();
    	}
    	return removeDownloadTask(task);
    }

    @Override
	public void onCreate() {
		super.onCreate();
		mDownloadTaskList = Collections.synchronizedList(new ArrayList<DownloadTask>());
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	if(intent != null) {
            if (ACTION_START.equals(intent.getAction())) {
            	FileInfo downloadFile = (FileInfo) intent.getSerializableExtra(FILE_INFO);
            	start(downloadFile);
            } else if (ACTION_STOP.equals(intent.getAction())) {
            	FileInfo downloadFile = (FileInfo) intent.getSerializableExtra(FILE_INFO);
            	stop(downloadFile);
            } else if (ACTION_UPDATE.equals(intent.getAction())) {
            	FileInfo downloadFile = (FileInfo) intent.getSerializableExtra(FILE_INFO);
            	update(downloadFile);
            }
    	}
       
        return super.onStartCommand(intent, flags, startId);
    }
	
	private void start(FileInfo downloadFile) {
		DownloadTask task = getDownloadTask(downloadFile.id);
    	if(task == null) {
    		task = new DownloadTask(getApplicationContext(), downloadFile);
    		task.download();
    		
    		addDownloadTask(task);
    	}
	}
	
	private void stop(FileInfo downloadFile) {
		removeDownloadTask(downloadFile.id);
	}
	
	private void update(FileInfo downloadFile) {
		stop(downloadFile);
		
		SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
    	if(downloadFile.progress == downloadFile.length) {
    		File tempFile = new File(DOWNLOAD_PATH + downloadFile.getFileName(true));
    		boolean succeed = tempFile.renameTo(new File(DOWNLOAD_PATH + downloadFile.getFileName()));
    		Debug.o(this, " succeed : " + succeed);
    		
    		SQLiteUtils.getInstance().delete(db, DBDownloadHelper.TABLE_FILE, "id = ?", new String[]{downloadFile.id +""});
    		Toast.makeText(getApplication(), "下载完成", Toast.LENGTH_SHORT).show();
    	} else {
    		SQLiteUtils.getInstance().update(db, DBDownloadHelper.TABLE_FILE, downloadFile.toContentValues(), "id = ?", new String[]{downloadFile.id +""});
    	}
    	db.close();
    	
    	// 结束后再通知一次
    	Intent intent2 = new Intent(DownloadService.ACTION_UPDATE);
    	intent2.putExtra(NOTICE_FILE_ID, downloadFile.id);
    	intent2.putExtra(NOTICE_PROGRESS, downloadFile.progress * 100 / downloadFile.length);
        sendBroadcast(intent2);
	}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
}
