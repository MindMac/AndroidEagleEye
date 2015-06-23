package com.mindmac.eagleeye.hookclass;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.Util;


import android.os.Binder;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class IoBridgeHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "libcore.io.IoBridge";
	
	private IoBridgeHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public static FileDescriptor open(String path, int flags)
	// public static int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
	// public static void write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
	// libcore/luni/src/main/java/libcore/io/IoBridge.java
	
	private enum Methods {
		open, read, write
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new IoBridgeHook(method));
	
		return methodHookList;
	}
	
	private ArrayList<byte[]> extractData(MethodHookParam param){
		ArrayList<byte[]> dataSlices = new ArrayList<byte[]>();
		
		byte[] bytes = (byte[]) param.args[1];
		int byteOffset =  (Integer) param.args[2];
		int byteCount = (Integer) param.args[3];
		
		while(byteCount>0){
			int targetDataLen = byteCount>Util.DATA_BYTES_TO_LOG ? Util.DATA_BYTES_TO_LOG : byteCount;
			byte[] targetData = new byte[targetDataLen];
			for(int i=0; i<targetDataLen; i++)
				targetData[i] = bytes[byteOffset+i];
			byteOffset += targetDataLen;
			byteCount -= targetDataLen;
			
			dataSlices.add(targetData);
			
		}
		
		return dataSlices;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {	
		int uid = Binder.getCallingUid();
		int pid = Binder.getCallingPid();
		
		if(uid <= 1000)
			return;
		
		if(!Util.isAppNeedLog(uid))
			return;
		
		if(mMethod == Methods.open){
			if(param.args.length >= 1){
				
				String argNames = null;
				if(mMethod == Methods.open)
					argNames = "path|flags";
				log(uid, param, argNames);
			}
		}else if(mMethod == Methods.read){			
			if(param.args.length >= 4){
				if(Util.pathConvertorClass == null || Util.logFilePathMethod == null)
					return;
				
				FileDescriptor fileDescriptor = (FileDescriptor) param.args[0];
				int fdInt = Util.getFd(fileDescriptor);
				int fdId = Util.getTimeId();
				
		        if((Boolean) Util.logFilePathMethod.invoke(Util.pathConvertorClass, 
		        		uid, pid, fdInt, fdId) == false)
		        	return;
		        
		        ArrayList<byte[]> dataSlices = extractData(param);
		        for(int i=0; i<dataSlices.size(); i++){
			        String logMsg = String.format("{\"Uid\":\"%d\",\"HookType\":\"system_api\", \"Customized\":\"false\"," +
			        		"\"FileRW\":{ \"operation\": \"read\",\"data\": \"%s\", \"id\": \"%d\"}}",
			        		uid, Util.toHex(dataSlices.get(i)), fdId);
					Log.i(Util.LOG_TAG, logMsg);
		        }
		        		        
			}
		}else if(mMethod == Methods.write){			
			if(param.args.length >= 4){
				if(Util.pathConvertorClass == null || Util.logFilePathMethod == null)
					return;
					
				FileDescriptor fileDescriptor = (FileDescriptor) param.args[0];
				int fdInt = Util.getFd(fileDescriptor);
				int fdId = Util.getTimeId();
				
				if((Boolean) Util.logFilePathMethod.invoke(Util.pathConvertorClass, 
		        		uid, pid, fdInt, fdId) == false)
		        	return;
		        ArrayList<byte[]> dataSlices = extractData(param);
		        for(int i=0; i<dataSlices.size(); i++){
		        	String logMsg = String.format("{\"Uid\":\"%d\", \"HookType\":\"system_api\", \"Customized\":\"false\"," +
			        		"\"FileRW\":{ \"operation\": \"write\", \"data\": \"%s\", \"id\": \"%d\"}}",
			        		uid, Util.toHex(dataSlices.get(i)), fdId);
					Log.i(Util.LOG_TAG, logMsg);
		        }
			}
		}

	}
}
