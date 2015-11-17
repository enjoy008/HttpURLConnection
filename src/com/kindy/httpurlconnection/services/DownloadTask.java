package com.kindy.httpurlconnection.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.kindy.httpurlconnection.models.FileInfo;
import com.kindy.httpurlconnection.models.ThreadInfo;
import com.kindy.httpurlconnection.utils.DBBaseHelper;
import com.kindy.httpurlconnection.utils.DBDownloadHelper;
import com.kindy.httpurlconnection.utils.Debug;
import com.kindy.httpurlconnection.utils.HttpUtils;
import com.kindy.httpurlconnection.utils.SQLiteUtils;

public class DownloadTask extends Thread {
	/** 开启下载 */
	private static final int OPEN_DOWNLOAD   = 1;
	/** 更新进度 */
	private static final int UPDATE_PROGRESS = 2;
	/** 更新数据库中的线程信息 */
	private static final int UPDATE_SUBTASK  = 3;
	
	private Context context;
	public FileInfo fileInfo;
	private boolean isRunning;
	
	private static final int mThreadCount = 2;
	private int mCurrentThreadCount = 0;
	
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
		
		if(fileInfo.length > 0) {
			mHandler.sendEmptyMessage(OPEN_DOWNLOAD);
			return;
		}
		
		HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        try {
            //连接网络文件
            conn = HttpUtils.getInstance().createConnection(fileInfo.url);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
//            conn.setRequestProperty("Accept-Encoding", "identity");//不采用gzip压缩
        	
          //获得文件长度
            int length = -1;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                length = conn.getContentLength();
            }
            
            Debug.o(this, " responseCode : " + responseCode + " length = " + length);
            
            if(length > 0) {
                
            	File fileDir = new File(DownloadService.DOWNLOAD_PATH);
                if (!fileDir.exists()) {
                	fileDir.mkdir();
                }
                
                File tempFile = new File(fileDir, fileInfo.getFileName(true));
                raf = new RandomAccessFile(tempFile, "rwd");
                // 设置文件长度
                raf.setLength(length);
                
                fileInfo.length = length;
        		SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
            	SQLiteUtils.getInstance().insert(db, DBDownloadHelper.TABLE_FILE, fileInfo.toContentValues());
            	db.close();
            	
            	mHandler.sendEmptyMessage(OPEN_DOWNLOAD);
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
        	
        }
	}

	private long mTime = 0;
	private Handler mHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			if(OPEN_DOWNLOAD == msg.what) {
				if(fileInfo.length > 0) {
					ArrayList<ThreadInfo> threadInfoList = new ArrayList<ThreadInfo>();
					SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
	            	Cursor cursor = SQLiteUtils.getInstance().rawQuery(db, "select * from " + DBDownloadHelper.TABLE_THREAD + " where fileId = ?", new String[]{fileInfo.id +""});
	            	if(cursor != null) {
	            		while(cursor.moveToNext()) {
	            			ThreadInfo threadInfo = new ThreadInfo(cursor);
	            			threadInfoList.add(threadInfo);
	            		}
	            		cursor.close();
	            	}
	            	
	            	if(threadInfoList.size() == 0) {
	            		long len = fileInfo.length / mThreadCount;
	            		for(int i=0; i<mThreadCount; i++) {
	            			long position = len*i;
	            			long end = position + len;
	            			if(i == mThreadCount - 1) {
	            				end = fileInfo.length;
	            			}
	            			ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.id, position, end-1);
	            			threadInfoList.add(threadInfo);
	                    	SQLiteUtils.getInstance().insert(db, DBDownloadHelper.TABLE_THREAD, threadInfo.toContentValues());
	            		}
	            	}
	            	db.close();
	            	
	            	mCurrentThreadCount = threadInfoList.size();
	            	for(ThreadInfo threadInfo : threadInfoList) {
	            		new SubTask(threadInfo).start();
	            	}
				}
				return true;
			} else if(UPDATE_PROGRESS == msg.what) {
				//下载进度发送广播给activity
                fileInfo.progress += msg.arg1;
                if (System.currentTimeMillis() - mTime > 500) {
                	mTime = System.currentTimeMillis();
                    
                    Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                    intent.putExtra(DownloadService.NOTICE_FILE_ID, fileInfo.id);
                    intent.putExtra(DownloadService.NOTICE_PROGRESS, fileInfo.progress * 100 / fileInfo.length);
                    context.sendBroadcast(intent);
                }
                return true;
			} else if(UPDATE_SUBTASK == msg.what) {
				ThreadInfo threadInfo = (ThreadInfo) msg.obj;

				SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
				if(threadInfo.position == threadInfo.end + 1) {
					SQLiteUtils.getInstance().delete(db, DBDownloadHelper.TABLE_THREAD, "id = ? and fileId = ?", new String[]{threadInfo.id +"", threadInfo.fileId +""});
				} else {
					SQLiteUtils.getInstance().update(db, DBDownloadHelper.TABLE_THREAD, threadInfo.toContentValues(), "id = ? and fileId = ?", new String[]{threadInfo.id +"", threadInfo.fileId +""});
				}
				db.close();
				
				Debug.o(this, " fileId = " + threadInfo.fileId + " id = " + threadInfo.id
						+ " position = " + threadInfo.position + " end = " + threadInfo.end);
				mCurrentThreadCount --;
				if(mCurrentThreadCount == 0) {
					isRunning = false;
					
					Intent intent = new Intent(context, DownloadService.class);
		            intent.setAction(DownloadService.ACTION_UPDATE);
		            intent.putExtra(DownloadService.FILE_INFO, fileInfo);
					context.startService(intent);
				}
				
				return true;
			}
			
			return false;
		}
	});
	
	class SubTask extends Thread {
		private ThreadInfo threadInfo;
		public SubTask(ThreadInfo threadInfo) {
			this.threadInfo = threadInfo;
		}
		
		@Override
		public void run() {
			if(isRunning) {
				HttpURLConnection conn = null;
		        RandomAccessFile raf = null;
		        try {
		            //连接网络文件
		            conn = HttpUtils.getInstance().createConnection(fileInfo.url);
		            conn.setRequestMethod("GET");
		            conn.setUseCaches(false);
//		            conn.setRequestProperty("Accept-Encoding", "identity");//不采用gzip压缩
//		            conn.setRequestProperty("Range", "bytes = " + threadInfo.position + "-" + threadInfo.end); // 坑爹，首先得服务端支持才行啊
//		            Debug.o(this, " Range = " + conn.getRequestProperty("Range"));
		            Debug.o(this, " Range = " + threadInfo.position + "-" + threadInfo.end);
		        	
		          //获得文件长度
		            int length = -1;
		            int responseCode = conn.getResponseCode();
		            if (/*responseCode == HttpURLConnection.HTTP_PARTIAL || */responseCode == HttpURLConnection.HTTP_OK) {
		                length = conn.getContentLength();
		            }
		            
//		            Debug.o(this, " " + Thread.currentThread().getName() + " responseCode : " + responseCode + " length = " + length);
		            
		            if(length > 0) {
		                
		                File tempFile = new File(DownloadService.DOWNLOAD_PATH + fileInfo.getFileName(true));
		                raf = new RandomAccessFile(tempFile, "rwd");
		                raf.seek(threadInfo.position);  //seek()方法，在读写的时候跳过设置好的字节数，从下一个字节数开始读写

		            	InputStream is = conn.getInputStream();
		                byte[] buffer = new byte[1024 * 4];
		                int len = -1;
		                boolean finish = false;
		                long skip = threadInfo.position;
		                long temp;
		                while(isRunning && skip > 0) {
		                	temp = is.skip(skip);
		                	skip -= temp;
		                }
		                Debug.o(this, " skip : " + skip);
		                while (isRunning && !finish && (len = is.read(buffer)) != -1) {
		                    //写入文件
		                    raf.write(buffer, 0, len);
		                    
		                    if(threadInfo.position + len > threadInfo.end) {
		                    	len = (int) (threadInfo.end + 1 - threadInfo.position);
		                    	finish = true;
		                    }
		                    threadInfo.position += len;
		                    Message msg = Message.obtain();
		                    msg.what = UPDATE_PROGRESS;
		                    msg.arg1 = len;
		                    mHandler.sendMessage(msg);
		                }
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
		            
					Message msg = Message.obtain();
                    msg.what = UPDATE_SUBTASK;
                    msg.obj = threadInfo;
                    mHandler.sendMessage(msg);
		        }
			}
		}
	}
}
