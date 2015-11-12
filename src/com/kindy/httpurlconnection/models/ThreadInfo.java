package com.kindy.httpurlconnection.models;

import android.content.ContentValues;
import android.database.Cursor;

public class ThreadInfo {
	public int id;
	public int fileId;
	public long position;
	public long end;
	
	public ThreadInfo(int id, int fileId, long position, long end) {
		this.id = id;
		this.fileId = fileId;
		this.position = position;
		this.end = end;
	}

	public ThreadInfo(Cursor cursor) {
		id   = cursor.getInt(cursor.getColumnIndex("id"));
		fileId   = cursor.getInt(cursor.getColumnIndex("fileId"));
		position = cursor.getLong(cursor.getColumnIndex("position"));
		end = cursor.getLong(cursor.getColumnIndex("end"));
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		
		values.put("id", id);
		values.put("fileId", fileId);
		values.put("position", position +"");
		values.put("end", end +"");
		
		return values;
	}
	
}
