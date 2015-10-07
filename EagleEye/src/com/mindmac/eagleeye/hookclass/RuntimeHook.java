package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.NativeEntry;
import com.mindmac.eagleeye.Util;
import android.os.Binder;
import android.util.Log;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class RuntimeHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "java.lang.Runtime";

	private RuntimeHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public Process exec(String[] progArray, String[] envp, File directory)
	// void load(String filename, ClassLoader loader)
	// void loadLibrary(String libraryName, ClassLoader loader)
	// libcore/luni/src/main/java/java/lang/Runtime.java
	// http://developer.android.com/reference/java/lang/Runtime.html

	private enum Methods {
		exec, load, loadLibrary
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new RuntimeHook(method));
		
		return methodHookList;
	}
	
	private String unifyLibName(String libName){
		String unifiedLibName = libName;
		if(!libName.startsWith("lib"))
			unifiedLibName = "lib" + libName;
		if(unifiedLibName.endsWith(".so"))
			unifiedLibName = unifiedLibName.substring(0, unifiedLibName.length() - 3);
		return unifiedLibName;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.load && param.args.length == 2){
			argNames = "filename|loader";
			log(uid, param, argNames);
			
			if(!Util.isAppNeedNtLog(uid))
				return;
			String libPath = (String) param.args[0];
			if(libPath != null && libPath.equals("")){
				int slashIndex = libPath.lastIndexOf("/");
				if(slashIndex != -1 ){
					String libName = libPath.substring(slashIndex+1);
					libName = unifyLibName(libName);
					if(Util.CUSTOM_NATIVE_LIB_NAMES_MAP.containsKey(libName))
						NativeEntry.initCustomNativeHook(libName);
				}
			}
		}else if(mMethod == Methods.loadLibrary && param.args.length == 2){
			argNames = "library|loader";
			log(uid, param, argNames);
			if(!Util.isAppNeedNtLog(uid))
				return;
			String libName = (String) param.args[0];
			if(libName != null && !libName.equals("")){
				libName = unifyLibName(libName);
				if(Util.CUSTOM_NATIVE_LIB_NAMES_MAP.containsKey(libName)){
					NativeEntry.initCustomNativeHook(libName);
				}
			}
		}else if(mMethod == Methods.exec){
			if(param.args[0] instanceof String[] && param.args.length == 3){
				log(uid, param, argNames);
			}
		}	
	}
}
