package com.kindy.httpurlconnection.utils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.ContentValues;

public class SQLiteUtils {

	private static SQLiteUtils instance;
	
	private SQLiteUtils() {
		
	}
	
	public static SQLiteUtils getInstance() {
		if(instance == null) {
			instance = new SQLiteUtils();
		}
		
		return instance;
	}
	
	/**
	 *  查询  
	 * @param db
	 * @param sql
	 * @param selectionArgs
	 * @return
	 * 	Cursor 使用后请关闭
	 */
	public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs) {
		Cursor cursor = null;
		
		try {
			cursor = db.rawQuery(sql, selectionArgs);
		} catch(Exception e) {
			Debug.e(this, " rawQuery : \n" + e.toString());
		}
		
		return cursor;
	}
	
	/** 插入 */
	public long insert(SQLiteDatabase db, String tableName, ContentValues values) {
		long rowId = -1;
		
		try {
			rowId = db.insert(tableName, null, values);
		} catch(Exception e) {
			Debug.e(this, " insert : \n" + e.toString());
		}
		
		return rowId;
	}
	
	/** 修改 */
	public int update(SQLiteDatabase db, String tableName, ContentValues values, String whereClause, String[] whereArgs) {
		int num = 0;
		
		try {
			num = db.update(tableName, values, whereClause, whereArgs);
		} catch(Exception e) {
			Debug.e(this, " update : \n" + e.toString());
		}
		
		return num;
	}
	
	/** 删除 */
	public int delete(SQLiteDatabase db, String tableName, String whereClause, String[] whereArgs) {
		int num = 0;
		
		try {
			num = db.delete(tableName, whereClause, whereArgs);
		} catch(Exception e) {
			Debug.e(this, " delete : \n" + e.toString());
		}
		
		return num;
	}
	
	
	//addColumn(db, tableName, "newColumn text not null default ('Kindy')")
	/** 增加列 */
	public void addColumn(SQLiteDatabase db, String tableName, String sql) {
		Cursor cursor  = rawQuery(db, "alter table " + tableName + " add column " + sql, null);
		if(cursor != null) {
			cursor.close();
		}
	}
	
	
	
	
	public void showTables(SQLiteDatabase db) {
		try {
			Cursor cursor = rawQuery(db, "select * from sqlite_master where type in('table', 'view') order by name", null);
//			Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
//			Cursor cursor = db.rawQuery("select * from sqlite_master where type='table'", null);
			if(cursor!= null) {
				int num = 0;
				while(cursor.moveToNext()) {
					StringBuilder sb = new StringBuilder(" ============================ " + ++num);
					String[] names = cursor.getColumnNames();
					for(String name : names) {
						sb.append("\n").append(name).append(" : ").append(cursor.getString(cursor.getColumnIndex(name)));
					}
					Debug.o(this, sb.toString());
				}
				cursor.close();
			}
		} catch(Exception e) {
			Debug.e(this, " showTables : \n" + e.toString());
		}
	}
	
	public void showTableInfo(SQLiteDatabase db, String table) {
		try {
			Cursor cursor = rawQuery(db, "select * from " + table, null);
			if(cursor!= null) {
				int num = 0;
				while(cursor.moveToNext()) {
					StringBuilder sb = new StringBuilder(" ============================ " + table + " " + ++num);
					String[] names = cursor.getColumnNames();
					for(String name : names) {
						sb.append("\n").append(name).append(" : ").append(cursor.getString(cursor.getColumnIndex(name)));
					}
					Debug.o(this, sb.toString());
				}
				cursor.close();
			}
		} catch(Exception e) {
			Debug.e(this, " showTableInfo : \n" + e.toString());
		}
	}
	
	
	/**
	 * 

		创建表  CREATE TABLE 表名 （列名 数据类型 限定符...）
		CREATE TABLE tTable (ID INTEGER,NAME TEXT);
		
		修改表  ALTER TABLE ...(命令允许用户重命名或添加新的字段在已有表中，不能从表中删除字段。并且只能在表的末尾添加字段)
		修改表名：ALTER TABLE 旧表名  RENAME TO 新表名
		ALTER TABLE tTable RENAME TO MyTable;
		
		添加一列：ALTER TABLE 表名 ADD COLUMN 列名 数据类型 限定符 default （默认值）
		ALTER TABLE MyTable ADD COLUMN AGE INTEGER default (0);
		
		删除表 DROP TABLE 表名
		DROP TABLE MyTable;
	  	
	  	
	  	db.execSQL("create table if not exists stutb("
				+ "_id integer primary key autoincrement,"
				+ "name text not null,"
				+ "sex text not null,"
				+ "age integer not null)");
		db.execSQL("insert into stutb(name,sex,age)values('张三','女',999)");
		
		for(int i=0; i<10; i++) {
			ContentValues values = new ContentValues();
			values.put("name", "张三");
			values.put("sex", "男");
			values.put("age", i);
			long rowId = db.insert("stutb", null, values);
			Debug.o(this, " rowId : " + rowId);
		}
	  	
	  	
	  	
	  	Cursor c = db.rawQuery("select * from stutb", null);
			
		db.update("stutb", values, "_id>?", new String[]{"3"});//将全部id>3的人的性别改成女
		db.delete("stutb", "name like ?", new String[]{"%丰%"});//删除所有名字中带有丰的人
		
		Cursor c = db.query("stutb", null, "_id>?", new String[]{"0"}, null, 
		null, //DESC/ASC:降序/升序(格式是String orderBy = "_id desc")
		"name");
	 
	  
		"select * from stutb limit " + 跳过多少条记录 +", " + 要显示多少条记录
		"select * from stutb limit 0, 3"     
		       
		分页查询
		Cursor c = db.rawQuery("select * from stutb where age > 3 order by age asc limit 2 offset 0", null);
		通用公试: sql = "select * from FlyCrocodile where "+条件+" order by "+排序+" limit "+要显示多少条记录+" offset "+跳过多少条记录 
	 */
}