package com.kindy.httpurlconnection.utils;

import android.database.sqlite.SQLiteDatabase;

public class DBDownloadHelper extends DBBaseHelper {
	
	/** 默认数据库版本 */
	public static final int DB_VERSION = 1;
	
	/** 默认数据库名字 */
	public static final String DB_DOWNLOAD = "downlod.db";
	
	/** 表名 */
	public static final String TABLE_FILE = "t_file";
	
	/** 创建表 */
	public static final String SQL_FILE = "create table if not exists " + TABLE_FILE + "("
			+ "_id integer primary key autoincrement, "
			
			+ "id integer not null, "
			+ "url text not null, "
			+ "length text not null, "
			+ "progress text not null, "
			
			+ "_extra1 text default(''), "
			+ "_extra2 text default('')"
			+ ")";
	
	
	private static DBDownloadHelper instance;
	
	public static DBDownloadHelper getInstance() {
		if(instance == null) {
			synchronized (DBDownloadHelper.class) {
				if(instance == null) {
					instance = new DBDownloadHelper(DB_DOWNLOAD, DB_VERSION);
				}
			}
		}
		return instance;
	}
	
	private DBDownloadHelper(String name, int version) {
		super(name, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		
		db.execSQL(SQL_FILE);
	}

}
