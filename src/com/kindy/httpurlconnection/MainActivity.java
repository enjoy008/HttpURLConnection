package com.kindy.httpurlconnection;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kindy.httpurlconnection.utils.DBBaseHelper;
import com.kindy.httpurlconnection.utils.HttpUtils;

public class MainActivity extends Activity {
	
	private ImageView img;
	private Bitmap bmp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		img = (ImageView) findViewById(R.id.img);
		
		DBBaseHelper.init(this.getApplicationContext());
	}
	
	public void click(View view) {
		System.out.println(this.getClass().getName() + " ================= click");
		
		new Thread() {
			public void run() {
				
				String  url = "https://www.baidu.com/img/bd_logo1.png";
				InputStream is = HttpUtils.getInstance().getInputStream(url);
				if(is != null) {
					bmp = BitmapFactory.decodeStream(is);
					try {
						is.close();
					} catch (IOException e) { }
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						if(bmp == null) {
							img.setImageResource(R.drawable.ic_launcher);
						} else {
							img.setImageBitmap(bmp);
						}
						
						bmp = null;
					}
				});
				
			}
		}.start();
	}
	
	public void download(View view) {
		startActivity(new Intent(this, DownloadActivity.class));
	}
}
