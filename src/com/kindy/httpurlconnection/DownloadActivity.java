package com.kindy.httpurlconnection;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kindy.httpurlconnection.models.FileInfo;
import com.kindy.httpurlconnection.services.DownloadService;
import com.kindy.httpurlconnection.utils.DBBaseHelper;
import com.kindy.httpurlconnection.utils.DBDownloadHelper;
import com.kindy.httpurlconnection.utils.SQLiteUtils;

public class DownloadActivity extends Activity {

    //定义组件
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private Button btnStart, btnStop;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        
        //初始化组件
        mTextView = (TextView) findViewById(R.id.textView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        //添加事件监听
        btnStart.setOnClickListener(mOnClickListener);
        btnStop.setOnClickListener(mOnClickListener);
        
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver,filter);
    }
    
    private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.btnStart: {
					String url = "http://www.imooc.com/mobile/mukewang.apk";
					FileInfo downloadFile = new FileInfo(1, url, 0, 0);
					
	            	
	            	SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
	            	Cursor cursor = SQLiteUtils.getInstance().rawQuery(db, "select * from " + DBDownloadHelper.TABLE_FILE + " where id = ?", new String[]{"1"});
	            	if(cursor != null) {
	            		if(cursor.moveToFirst()) {
	            			downloadFile = new FileInfo(cursor);
	            		}
	            		cursor.close();
	            	}
	            	db.close();
	            	
	                //通过intent传递参数给Service
	                Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
	                intent.setAction(DownloadService.ACTION_START);
	                intent.putExtra(DownloadService.FILE_INFO, downloadFile);
	                startService(intent);
	
	            	mTextView.setText(downloadFile.getFileName());
				}
				break;
			case R.id.btnStop: {
					FileInfo downloadFile = null;
	            	
	            	SQLiteDatabase db = DBDownloadHelper.getInstance().getWritableDatabase(DBBaseHelper.SECRET_KEY);
	            	Cursor cursor = SQLiteUtils.getInstance().rawQuery(db, "select * from " + DBDownloadHelper.TABLE_FILE + " where id = ?", new String[]{"1"});
	            	if(cursor != null) {
	            		if(cursor.moveToFirst()) {
	            			downloadFile = new FileInfo(cursor);
	            		}
	            		cursor.close();
	            	}
	            	db.close();
	            	
	            	if(downloadFile != null) {
	            		//通过intent传递参数给Service
	                    Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
	                    intent.setAction(DownloadService.ACTION_STOP);
	                    intent.putExtra(DownloadService.FILE_INFO, downloadFile);
	                    startService(intent);
	            	}
				}
				break;	
			}
		}
	};

    //帮助我们更新UI的广播接收器
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
            	int fileId = intent.getIntExtra(DownloadService.NOTICE_FILE_ID, 0);
            	int progress = (int) intent.getLongExtra(DownloadService.NOTICE_PROGRESS, 0);
//                int finished = intent.getIntExtra(KindyDownloadTask.NOTICE_PROGRESS, 0);
                mProgressBar.setProgress(progress);
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
