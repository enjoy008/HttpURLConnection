package com.kindy.httpurlconnection.models;

import java.io.File;
import java.io.Serializable;

import android.content.ContentValues;
import android.database.Cursor;

public class FileInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int id;
	public String url;
	public long length;
	public long progress;

	public FileInfo() {
		this(0, "", 0, 0);
	}
	
	public FileInfo(int id, String url, long progress, long length) {
		this.id = id;
		this.url = url;
		this.progress = progress;
		this.length = length;
	}

	public FileInfo(Cursor cursor) {
		id   = cursor.getInt(cursor.getColumnIndex("id"));
		url = cursor.getString(cursor.getColumnIndex("url"));
		length = cursor.getLong(cursor.getColumnIndex("length"));
		progress = cursor.getLong(cursor.getColumnIndex("progress"));
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		
		values.put("id", id);
		values.put("url", url);
		values.put("length", length +"");
		values.put("progress", progress +"");
		
		return values;
	}

	public String getFileName() {
		return getFileName(false);
	}
	
	public String getFileName(boolean isTemp) {
		String name = "tmp";
		
		int start = url.lastIndexOf(File.separator);
		if(start != -1) {
			name = url.substring(start+1);
		}
		if(isTemp) {
			name += ".tmp";
		}
		
		return name;
	}
	
}
