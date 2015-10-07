package com.mindmac.eagleeyetest;

public class Native {
	static{
		System.loadLibrary("eagleeyetest");
	}
	
	public static native void nativeEntry();
}
