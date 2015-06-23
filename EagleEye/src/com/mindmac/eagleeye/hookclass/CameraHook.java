package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class CameraHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "android.hardware.Camera";

	private CameraHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}



	// @formatter:off

	// public static Camera open(int cameraId)
	// public final void setPreviewCallback(PreviewCallback cb)
	// public final void setPreviewCallbackWithBuffer(PreviewCallback cb)
	// public final void setOneShotPreviewCallback(PreviewCallback cb)
	// public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg)
	// public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback postview, PictureCallback jpeg)
	// frameworks/base/core/java/android/hardware/Camera.java
	// http://developer.android.com/reference/android/hardware/Camera.html

	// @formatter:on

	private enum Methods {
		open, setPreviewCallback, setPreviewCallbackWithBuffer, 
		setOneShotPreviewCallback, takePicture
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();

		for (Methods method : Methods.values())
			methodHookList.add(new CameraHook(method));
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		if(mMethod == Methods.open)
			argNames = "cameraId";
		else if(mMethod == Methods.setPreviewCallback || mMethod == Methods.setPreviewCallbackWithBuffer)
			argNames = "cb";
		else if(mMethod == Methods.takePicture){
			if(param.args.length == 3)
				argNames = "shutter|raw|jpeg";
			else if(param.args.length == 4)
				argNames = "shutter|raw|postview|jpeg";
		}
		
		log(uid, param, argNames);
	}
}
