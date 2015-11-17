package com.kindy.httpurlconnection.utils;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class DBBaseHelper extends SQLiteOpenHelper {
	/** SQLCipher所依赖的key，在对数据库进行加解密的时候SQLCipher都将使用这里指定的key。 */
	public static final String SECRET_KEY = "secret_key";
	protected static Context mContext;
	public static void init(Context context) {
		mContext = context;
		SQLiteDatabase.loadLibs(mContext);
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
