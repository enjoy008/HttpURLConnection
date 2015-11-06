package com.kindy.httpurlconnection.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBBaseHelper extends SQLiteOpenHelper {
	protected static Context mContext;
	public static void init(Context context) {
		mContext = context;
	}
	
	protected DBBaseHelper(String name, int version) {
		super(mContext, name, null, version);
	}
	
	/**
	 * 首次创建数据库的时候调用 一般可以把建库 建表的操作
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Debug.o(this, " ================== onCreate");
	}

	/**
	 * 当数据库的版本发生变化的时候 会自动执行
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Debug.o(this, " ================== onUpgrade");
	}
}
