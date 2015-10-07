package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.Util;


import android.os.Binder;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class SecretKeySpecHook extends MethodHook {
	private static final String mClassName = "javax.crypto.spec.SecretKeySpec";

	private SecretKeySpecHook(Methods method) {
		super(mClassName, null);
	}
	
	// public SecretKeySpec(byte[] key, int offset, int len, String algorithm)
	// public SecretKeySpec(byte[] key, String algorithm)
	// libcore/luni/src/main/java/javax/crypto/spec/SecretKeySpec.java

	private enum Methods {
		SecretKeySpec
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new SecretKeySpecHook(method));

		return methodHookList;
	}

	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		logSpecial(uid, param);
	}
	
	private void logSpecial(int uid, MethodHookParam param){
		if(!this.isNeedLog(uid))
			return;
		
		byte[] key = null;
		String k = "";
		String algorithm = "";
		
		if (param.args.length > 0 && param.args[0] != null){
			key = (byte[]) param.args[0];
		}
		
		if(param.args.length == 2 && param.args[1] != null){
			algorithm = (String) param.args[1];
		}else if(param.args.length == 4 && param.args[3] != null){
			algorithm = (String) param.args[3];
		}
		
		if(key != null){
			for (int i = 0; i < key.length; i++) {
	    		k += (int) key[i]; 
	    		k += ", ";
	    	}
	
	    	k = k.substring(0, k.length()-2);
		}
		
		String logMsg = String.format("{\"Basic\":[\"%d\",\"%d\",\"false\"], \"CryptoUsage\": " +
				"{\"operation\": \"keyalgo\", \"key\": \"%s\", \"algorithm\": \"%s\"}}", 
				uid, Util.FRAMEWORK_HOOK_SYSTEM_API, k, algorithm);
		
		Log.i(Util.LOG_TAG, logMsg);
	}
}
