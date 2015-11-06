package com.kindy.httpurlconnection.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.kindy.httpurlconnection.models.FileInfo;
import com.kindy.httpurlconnection.utils.DBDownloadHelper;
import com.kindy.httpurlconnection.utils.Debug;
import com.kindy.httpurlconnection.utils.HttpUtils;
import com.kindy.httpurlconnection.utils.SQLiteUtils;

public class DownloadTask extends Thread {
	
	private Context context;
	public FileInfo fileInfo;
	private boolean isRunning;
	
	public DownloadTask(Context context, FileInfo fileInfo) {
		this.context = context;
		this.fileInfo = fileInfo;
		isRunning = false;
	}
	
	/**
	 * 开始下载
	 */
	public void download() {
		if(isRunning) {
			return;
		}
		
		start();
	}

	/**
	 * 取消下载
	 */
	public void cancel() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	@Override
	public void run() {
		isRunning = true;
		
		HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        File FileDir = null;
        File tempFile = null;
        try {
            //连接网络文件
            conn = HttpUtils.getInstance().createConnection(fileInfo.url);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            if(fileInfo.length > 0) {
            	conn.setRequestProperty("Range", "bytes = " + fileInfo.progress + "-" + fileInfo.length);
            }
            Debug.o(this, " Range = " + conn.getRequestProperty("Range"));
        	
          //获得文件长度
            int length = -1;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                length = conn.getContentLength();
            }
            
            Debug.o(this, " responseCode : " + conn.getResponseCode() + " length = " + length);
            
            if(length > 0) {
                
            	FileDir = new File(DownloadService.DOWNLOAD_PATH);
                if (!FileDir.exists()) {
                	FileDir.mkdir();
                }
                
                tempFile = new File(FileDir, fileInfo.getFilename(true));
                raf = new RandomAccessFile(tempFile, "rwd");
                if(responseCode == HttpURLConnection.HTTP_OK) { // 第一次连接
                	// 设置文件长度
                    raf.setLength(length);
                    
                    fileInfo.length = length;
            		SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase();
                	SQLiteUtils.getInstance().insert(db, DBDownloadHelper.TABLE_FILE, fileInfo.toContentValues());
                	db.close();
                }
                raf.seek(fileInfo.progress);  //seek()方法，在读写的时候跳过设置好的字节数，从下一个字节数开始读写

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
            	InputStream is = conn.getInputStream();
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                long time = System.currentTimeMillis();
                while (isRunning && (len = is.read(buffer)) != -1) {
                    //写入文件
                    raf.write(buffer, 0, len);
                    //下载进度发送广播给activity
                    fileInfo.progress += len;
                    if (System.currentTimeMillis() - time > 500) {
                        time = System.currentTimeMillis();
                        intent.putExtra(DownloadService.NOTICE_FILE_ID, fileInfo.id);
                        intent.putExtra(DownloadService.NOTICE_PROGRESS, fileInfo.progress * 100 / fileInfo.length);
                        context.sendBroadcast(intent);
                    }
                }
                // 结束后再通知一次
                intent.putExtra(DownloadService.NOTICE_FILE_ID, fileInfo.id);
                intent.putExtra(DownloadService.NOTICE_PROGRESS, fileInfo.progress * 100 / fileInfo.length);
                context.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(raf != null) {
        		try {
        			raf.close();
        		} catch (Exception e) {}
        	}
        	if(conn != null) {
        		conn.disconnect();
        	}

    		SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase();
        	if(fileInfo.progress == fileInfo.length) {
        		boolean succeed = tempFile.renameTo(new File(FileDir, fileInfo.getFilename()));
        		Debug.o(this, " succeed : " + succeed);
        		if(succeed) {
        			Intent intent = new Intent(DownloadService.NOTICE_DOWNLOAD_FINISH);
                    intent.putExtra(DownloadService.NOTICE_FILE_ID, fileInfo.id);
        			context.sendBroadcast(intent);
        			
        			// 删除下载任务
        			Intent intent2 = new Intent(context, DownloadService.class);
        			intent2.setAction(DownloadService.NOTICE_DOWNLOAD_FINISH);
        			intent2.putExtra(DownloadService.NOTICE_FILE_ID, fileInfo.id);
        			context.startService(intent2);
        		}
        		SQLiteUtils.getInstance().delete(db, DBDownloadHelper.TABLE_FILE, "id = ?", new String[]{fileInfo.id +""});
        	} else {
            	SQLiteUtils.getInstance().update(db, DBDownloadHelper.TABLE_FILE, fileInfo.toContentValues(), "id = ?", new String[]{fileInfo.id +""});
        	}
        	db.close();
        	
        	isRunning = false;
        }
	}

	
}
