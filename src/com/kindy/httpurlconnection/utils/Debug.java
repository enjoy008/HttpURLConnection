package com.kindy.httpurlconnection.utils;

import android.util.Log;


public class Debug {
	/** 测试开关，主要是LOG开关
	 * </br> 友盟LOG开关
	 */
	public static boolean debug = true;
	
	public final static byte O          = 0;  //System.out
	public final static byte V          = 1;  //verbose
	public final static byte I          = 2;  //info
	public final static byte D          = 3;  //debug
	public final static byte W          = 4;  //warning
	public final static byte E          = 5;  //error
	public final static byte LOG        = 6;  //log.txt
	public final static byte NULL       = 7;  //no log
	
	private static byte model = O;
	
//-------------------- System.out ------------------------------------------
	public static void o(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		o(tag, sb.toString());
	}
	public static void o(Object tag, String s) {
		if(model <= O) {
			System.out.println((tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName())) + s);
		}
	}
		
//-------------------- verbose ------------------------------------------
	public static void v(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		v(tag, sb.toString());
	}
	public static void v(Object tag, String s) {
		if(model <= V) {
			Log.v(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s);
		}
	}
	
//-------------------- debug --------------------------------------------
	public static void d(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		d(tag, sb.toString());
	}
	public static void d(Object tag, String s) {
		if(model <= D) {
			Log.d(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s);
		}
	}

//-------------------- info ---------------------------------------------
	public static void i(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		i(tag, sb.toString());
	}
	public static void i(Object tag, String s) {
		if(model <= I) {
			Log.i(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s);
		}
	}

//-------------------- warning ------------------------------------------
	public static void w(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		w(tag, sb.toString());
	}
	public static void w(Object tag, String s) {
		w(tag, s, null);
	}
	public static void w(Object tag, String s, Throwable tr) {
		if(model <= W) {
			if(tr == null) {
				Log.w(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s);
			} else {
				Log.w(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s, tr);
			}
		}
	}
	
//-------------------- error --------------------------------------------
	public static void e(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		e(tag, sb.toString());
	}
	public static void e(Object tag, String s) {
		e(tag, s, null);
	}
	public static void e(Object tag, String s, Throwable tr) {
		if(model <= E) {
			if(tr == null) {
				Log.e(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s);
			} else {
				Log.e(tag==null ? "" : (tag instanceof String ? (String)tag : tag.getClass().getSimpleName()), s, tr);
			}
		}
	}

//-------------------- log --------------------------------------------
	public static void log(Object tag, Object... s) {
		StringBuffer sb = new StringBuffer();
		for(Object ts:s) {
			sb.append(ts);
		}
		log(tag, sb.toString());
	}
	public static void log(Object tag, String s) {
		log(tag, s, null);
	}
	public static void log(Object tag, String s, Throwable tr) {
		e(tag, s, tr);
	}
	

	public static byte getModel() {
		return model;
	}
	public static void setModel(byte model) {
		Debug.model = model;
	}

	public static void setModel(String model) {
		if(model == null || model.trim().equalsIgnoreCase("")) {
			return;
		}
		
		byte bModel = O;
		
		if(model.equalsIgnoreCase("O")) {
			bModel = O;
		} else if(model.equalsIgnoreCase("V")) {
			bModel = V;
		} else if(model.equalsIgnoreCase("I")) {
			bModel = I;
		} else if(model.equalsIgnoreCase("D")) {
			bModel = D;
		} else if(model.equalsIgnoreCase("W")) {
			bModel = W;
		} else if(model.equalsIgnoreCase("E")) {
			bModel = E;
		} else if(model.equalsIgnoreCase("LOG")) {
			bModel = LOG;
		} else if(model.equalsIgnoreCase("NULL")) {
			bModel = NULL;
		}
		
		setModel(bModel);
	}
}
