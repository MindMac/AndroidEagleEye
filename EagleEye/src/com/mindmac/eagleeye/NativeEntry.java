package com.mindmac.eagleeye;

public class NativeEntry {
	static{
		System.loadLibrary(Util.NATIVE_LIB);
	}
	
	public native static void initSystemNativeHook();
	public native static void initCustomNativeHook(String libName);
	public native static boolean logFilePathFromFd(int uid, int pid, int fd, int id);
}
