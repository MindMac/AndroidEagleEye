package com.mindmac.eagleeye.service;

import static de.robv.android.xposed.XposedHelpers.findClass;

import com.mindmac.eagleeye.MethodParser;
import com.mindmac.eagleeye.NativeEntry;
import com.mindmac.eagleeye.Util;
import com.mindmac.eagleeye.hookclass.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

// This class will be called by Xposed
@SuppressLint("DefaultLocale")
public class Launcher implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	
	private static XC_MethodHook xcMethodHookApp = null;
	private static XC_MethodHook xcMethodHookSystem = null;
	
	// System services
	private static boolean mAccountManagerHooked = false;
	private static boolean mActivityManagerHooked = false;
	private static boolean mConnectivityManagerHooked = false;
	private static boolean mLocationManagerHooked = false;
	private static boolean mPackageManagerHooked = false;
	private static boolean mPowerManagerHooked = false;
	private static boolean mTelephonyManagerHooked = false;
	private static boolean mNotificationManagerHooked = false;
	private static boolean mWifiManagerHooked = false;
	
	// Api config pattern, validated one is like Lcom/example/class;->Fun(Ljava/lang/String;)Ljava/lang/String;
	private static Pattern apiConfigPattern = Pattern.compile("^L.*(;->.*\\(.*\\).*)?$");
	
	// Called when zygote initialize
	public void initZygote(StartupParam startupParam) throws Throwable {
		// Create hook method
		xcMethodHookApp = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (!param.hasThrowable())
					try {
						if (Process.myUid() <= 0)
								return;
							after(param, Util.FRAMEWORK_HOOK_APP_API);
						} catch (Throwable ignore) {
						}
			}
		};
		
		xcMethodHookSystem = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (!param.hasThrowable())
					try {
						if (Process.myUid() <= 0)
								return;
							after(param, Util.FRAMEWORK_HOOK_SYSTEM_API);
						} catch (Throwable ignore) {
						}
			}
		};

		// Hook system APIs
		hookSystemApis();
	}


	// Hook System APIs
	private void hookSystemApis(){		
		// AbstractHttpClient
		hookAll(AbstractHttpClientHook.getMethodHookList());
		
		// Activity
		hookAll(ActivityHook.getMethodHookList());
		
		// ActivityThread
		hookAll(ActivityThreadHook.getMethodHookList());
		
		// ApplicationManager
		hookAll(ApplicationPackageManagerHook.getMethodHookList());
		
		// AudioRecord
		hookAll(AudioRecordHook.getMethodHookList());
		
		// BluetoothAdapter
		hookAll(BluetoothAdapterHook.getMethodHookList());
		
		// BluetoothSocket
		hookAll(BluetoothSocketHook.getMethodHookList());
		
		// BroadcastReceiver
		hookAll(BroadcastReceiverHook.getMethodHookList());
				
		// Cipher
		hookAll(CipherHook.getMethodHookList());
				
		// Camera - Camera
		hookAll(CameraHook.getMethodHookList());
				
		// ContentResolver
		hookAll(ContentResolverHook.getMethodHookList());
		
		// ContextImpl
		hookAll(ContextImplHook.getMethodHookList());
		
		// File
		hookAll(FileHook.getMethodHookList());
				
		// InetAddress
		hookAll(InetAddressHook.getMethodHookList());
		
		// Instrumentation - StartActivity
		hookAll(InstrumentationHook.getMethodHookList());
		
		// IoBridge
		// hookAll(IoBridgeHook.getMethodHookList());
		
		// MediaRecord
		hookAll(MediaRecorderHook.getMethodHookList());
		
		// NetworkInterface
		hookAll(NetworkInterfaceHook.getMethodHookList());
				
		// ProcessBuilder
		hookAll(ProcessBuilderHook.getMethodHookList());
		
		// Process
		hookAll(ProcessHook.getMethodHookList());
						
		//SystemProperties
		hookAll(SystemPropertiesHook.getMethodHookList());
		
		// SecretKeySpec
		hookAll(SecretKeySpecHook.getMethodHookList());
		
		// Settings.Secure
		hookAll(SettingsSecureHook.getMethodHookList());
		
		// SmsManager
		hookAll(SmsManagerHook.getMethodHookList());
		
		// URL
		hookAll(URLHook.getMethodHookList());
				
		// WebView
		hookAll(WebViewHook.getMethodHookList());
		
		
	}
		
	public static void hookSystemServices(MethodHook methodHook, String serviceName, Object instance) {	
		if (serviceName.equals(Context.ACCOUNT_SERVICE)) {
			// Account manager
			if (!mAccountManagerHooked) {
				hookAll(AccountManagerHook.getMethodHookList(instance));
				mAccountManagerHooked = true;
			}
		} else if (serviceName.equals(Context.ACTIVITY_SERVICE)) {
			// Activity manager
			if (!mActivityManagerHooked) {
				hookAll(ActivityManagerHook.getMethodHookList(instance));
				mActivityManagerHooked = true;
			}
		}else if(serviceName.equals(Context.CONNECTIVITY_SERVICE)){
			// Connectivity manager
			if(!mConnectivityManagerHooked){
				hookAll(ConnectivityManagerHook.getMethodHookList(instance));
				mConnectivityManagerHooked = true;
			}
		}else if (serviceName.equals(Context.LOCATION_SERVICE)) {
			// Location manager
			if (!mLocationManagerHooked) {
				hookAll(LocationManagerHook.getMethodHookList(instance));
				mLocationManagerHooked = true;
			}
		}else if (serviceName.equals(Context.NOTIFICATION_SERVICE)) {
			// Notification manager
			if (!mNotificationManagerHooked) {
				hookAll(NotificationManagerHook.getMethodHookList(instance));
				mNotificationManagerHooked = true;
			}
		}else if (serviceName.equals("PackageManager")) {
			// Package manager
			if (!mPackageManagerHooked) {
				hookAll(PackageManagerHook.getMethodHookList(instance));
				mPackageManagerHooked = true;
			}
		}else if (serviceName.equals(Context.POWER_SERVICE)) {
			// Power manager
			if (!mPowerManagerHooked) {
				hookAll(PowerManagerHook.getMethodHookList(instance));
				mPackageManagerHooked = true;
			}
		} else if (serviceName.equals(Context.TELEPHONY_SERVICE)) {
			// Telephony manager
			if (!mTelephonyManagerHooked) {
				hookAll(TelephonyManagerHook.getMethodHookList(instance));
				mTelephonyManagerHooked = true;
			}
		}else if (serviceName.equals(Context.WIFI_SERVICE)) {
			// Wifi manager
			if (!mWifiManagerHooked) {
				hookAll(WifiManagerHook.getMethodHookList(instance));
				mWifiManagerHooked = true;
			}
		}
	}
	
	
	// Call when package loaded
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		ApplicationInfo appInfo = lpparam.appInfo;
		if(appInfo == null)
			return;
		if((appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) !=0)
			return;
		if(appInfo.packageName.equals("de.robv.android.xposed.installer") || 
			appInfo.packageName.equals(Util.SELF_PACKAGE_NAME))
			return;
		
		// Get the uids for the applications which need log
		Util.storeNativeLogAppUids();
		Util.storeFrameworkLogAppUids();
		
		if(Util.isAppNeedNtLog(appInfo.uid)){
			if(lpparam.isFirstApplication){
				// Set customized native hook lib names
				setCustomNativeHookLibNames(appInfo.packageName);

				// Hook findLibrary method to invoke the native lib
				hookAll(BaseDexClassLoaderHook.getMethodHookList());
				
				// Hook Application to hook system native lib
				hookAll(ApplicationHook.getMethodHookList());

				// Hook Runtime to hook custom native lib
				hookAll(RuntimeHook.getMethodHookList());
			}
		}
		
		if(!Util.isAppNeedFrLog(appInfo.uid))
			return;
			
		// Anti anti emulator
		hookBuildFields();
		
		// Hook classloader
		hookAll(ClassLoaderHook.getMethodHookList());
		
		// Hook IO
		// Waring: do not hook this class at the beginning of zygote init, 
		// or it will cause UnsatisfiedLinkException when load libnative.so
		hookAll(IoBridgeHook.getMethodHookList());
		
		// Hook customized apis
		hookCustomizedSystemApis();
		hookCustomizedAppApis(appInfo.packageName, lpparam.classLoader);
		
	}
	
	// Hook customized system apis
	private static void hookCustomizedSystemApis(){
		File systemApiConfigFile = new File(String.format("/data/local/tmp/%s", 
				Util.FRAMEWORK_SYSTEM_API_HOOK_CONFIG));
		if(!systemApiConfigFile.exists())
			return;
				
		Util.FRAMEWORK_SYSTEM_API_NUM = getSystemApiNumLimit();
		Util.FRAMEWORK_SYSTEM_UN_HOOKED_APIS = readApiConfig(systemApiConfigFile.getAbsolutePath(), 
				Util.FRAMEWORK_SYSTEM_API_NUM);
		ArrayList<String> tmpUnHookedApis = Util.copyArrayList(Util.FRAMEWORK_SYSTEM_UN_HOOKED_APIS);
		
		for(String methodInfo : tmpUnHookedApis){
			Log.d(Util.LOG_TAG, "hook customized system apis: " + methodInfo);
			if(hookCustomize(methodInfo, null, Util.FRAMEWORK_HOOK_SYSTEM_API))
				Util.FRAMEWORK_SYSTEM_UN_HOOKED_APIS.remove(methodInfo);
		}
	}
	
	// Hook customized app apis
	private static void setCustomNativeHookLibNames(String packageName){
		File nativeLibNamesConfig = new File(String.format("/data/data/%s/%s", 
				packageName, Util.CUSTOM_NATIVE_LIB_NAMES_CONFIG));
		if(!nativeLibNamesConfig.exists())
			return;
		storeNativeLibNames(nativeLibNamesConfig.getAbsolutePath());
	}
	
	private static void hookCustomizedAppApis(String packageName, ClassLoader classLoader){
		File appApiConfigFile = new File(String.format("/data/data/%s/%s", 
				packageName, Util.FRAMEWORK_APP_API_HOOK_CONFIG));
		if(!appApiConfigFile.exists())
			return;
				
		Util.FRAMEWORK_APP_API_NUM = getAppApiNumLimit();
		Util.FRAMEWORK_APP_UN_HOOKED_APIS = readApiConfig(appApiConfigFile.getAbsolutePath(), 
				Util.FRAMEWORK_APP_API_NUM);
		ArrayList<String> tmpUnHookedApis = Util.copyArrayList(Util.FRAMEWORK_APP_UN_HOOKED_APIS);
		
		for(String methodInfo : tmpUnHookedApis){
			if(hookCustomize(methodInfo, classLoader, Util.FRAMEWORK_HOOK_APP_API))
				Util.FRAMEWORK_APP_UN_HOOKED_APIS.remove(methodInfo);
		}
	}
	
	private static int getSystemApiNumLimit(){
		int systemApiNumLimit = Util.FRAMEWORK_SYSTEM_API_NUM;
		String systemApiNumLimitVal = Util.getSystemProperty(Util.FRAMEWORK_APP_API_NUM_PROP_KEY);
		if(systemApiNumLimitVal != null){
			try{
				systemApiNumLimit = Integer.parseInt(systemApiNumLimitVal.trim());
			}catch(Exception ex){
				
			}
		}
		
		return systemApiNumLimit;
	}
	
	private static int getAppApiNumLimit(){
		int appApiNumLimit = Util.FRAMEWORK_APP_API_NUM;
		String appApiNumLimitVal = Util.getSystemProperty(Util.FRAMEWORK_APP_API_NUM_PROP_KEY);
		if(appApiNumLimitVal != null){
			try{
				appApiNumLimit = Integer.parseInt(appApiNumLimitVal.trim());
			}catch(Exception ex){
				
			}
		}
		
		return appApiNumLimit;
	}
	
	private static void hookAll(List<MethodHook> methodHookList) {
		for (MethodHook methodHook : methodHookList)
			hook(methodHook);
	}


	private static void hook(MethodHook methodHook) {
		hook(methodHook, null);
	}


	private static void hook(final MethodHook methodHook, ClassLoader classLoader) {
		try {
			
			// Create hook method
			XC_MethodHook xcMethodHook = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						if (Process.myUid() <= 0)
							return;
						methodHook.before(param);
					} catch (Throwable ignore) {
					}
				}

				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					if (!param.hasThrowable())
						try {
							if (Process.myUid() <= 0)
								return;
							methodHook.after(param);
						} catch (Throwable ignore) {
						}
				}
			};
			
			// Find hook class
			Class<?> hookClass = null;
			try{
				hookClass = findClass(methodHook.getClassName(), classLoader);
			}catch(ClassNotFoundError ignore){
			}
			
			if (hookClass == null) {
				String message = String.format("Hook-Class not found: %s", methodHook.getClassName());
				Log.d(Util.LOG_TAG, message);
				return;
			}

			// Add hook
			if (methodHook.getMethodName() == null) {
				for (Constructor<?> constructor : hookClass.getDeclaredConstructors()){
					XposedBridge.hookMethod(constructor, xcMethodHook);
				}
			} else{
				for (Method method : hookClass.getDeclaredMethods())
					if (method.getName().equals(methodHook.getMethodName()))
						XposedBridge.hookMethod(method, xcMethodHook);
			}
			
		} catch (Throwable ex) {
			
		}
	}
	
	// Got customized hook native lib names
	private static void storeNativeLibNames(String filePath){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(filePath));
			String data = null;
			while((data = reader.readLine()) != null){
				data = data.trim();
				if(!Util.CUSTOM_NATIVE_LIB_NAMES_MAP.containsKey(data)){
					Util.CUSTOM_NATIVE_LIB_NAMES_MAP.put(data, true);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	// Hook customized methods
	private static ArrayList<String> readApiConfig(String filePath, int apiNumLimit){
		ArrayList<String> apiConfigList = new ArrayList<String>();
		BufferedReader reader = null;
		int apiCount = 0;
		try{
			reader = new BufferedReader(new FileReader(filePath));
			String data = null;
			while((data = reader.readLine()) != null){
				if(apiCount >= apiNumLimit)
					break;
				if(isApiConfigValidated(data)){
					apiConfigList.add(data);
					apiCount += 1;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return apiConfigList;
	}
	
	private static boolean isApiConfigValidated(String data){
		Matcher matcher = apiConfigPattern.matcher(data);
		return matcher.matches();
	}
	
	// Hook constructors for customized API configure
	private static void hookCustomizeConstructors(Class<?> hookClass, String[] methodInfoItems, int hookType){
		String parameterTypes = methodInfoItems[2];
		for(Constructor<?> constructor : hookClass.getDeclaredConstructors()){
			try{
				boolean match = true;
				String parsedParameterTypes = Util.parseParameterTypes(constructor);
				if(parameterTypes != null) 
					if(parameterTypes.equals(parsedParameterTypes))
						match = true;
					else
						match = false;
				if(match){
					if(hookType == Util.FRAMEWORK_HOOK_APP_API)
						XposedBridge.hookMethod(constructor, xcMethodHookApp);
					else if(hookType == Util.FRAMEWORK_HOOK_SYSTEM_API)
						XposedBridge.hookMethod(constructor, xcMethodHookSystem);
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	// Hook methods for customized API configure
	private static void hookCustomizeMethods(Class<?> hookClass,  String[] methodInfoItems, int hookType){
		String methodName = methodInfoItems[1];
		String parameterTypes = methodInfoItems[2];
		String returnType = methodInfoItems[3];

		for (Method method : hookClass.getDeclaredMethods()){
			try{
				boolean match = false;
				if (method.getName().equals(methodName)){
					match = true;
					String parsedParameterTypes = Util.parseParameterTypes(method);
					String parsedReturnType = Util.parseReturnType(method);
					if(parameterTypes != null) 
						if(parameterTypes.equals(parsedParameterTypes))
							match = true;
						else
							match = false;
					if(returnType != null)
						if(returnType.equals(parsedReturnType))
							match = true;
						else
							match = false;
						
					if(match){
						Log.d(Util.LOG_TAG, "hook customized method: " + hookClass.getName() + "." + methodName);
						if(hookType == Util.FRAMEWORK_HOOK_APP_API)
							XposedBridge.hookMethod(method, xcMethodHookApp);
						else if(hookType == Util.FRAMEWORK_HOOK_SYSTEM_API)
							XposedBridge.hookMethod(method, xcMethodHookSystem);
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	// methodInfo is in the format Lcom/example/class;->Fun(Ljava/lang/String;)Ljava/lang/String;
	public static boolean hookCustomize(String methodInfo, ClassLoader classLoader, int hookType){
		boolean methodHooked = false;
		String[] methodInfoItems = parseMethodInfo(methodInfo);
		
		String className = methodInfoItems[0];
		String methodName = methodInfoItems[1];
		Class<?> hookClass = null;
		
		// Find hook class
		if(className != null){
			try{
				hookClass = findClass(className, classLoader);
			}catch(ClassNotFoundError ignore){
			}
			
			if (hookClass != null && methodName != null) {
				String shortClassName = className;
				if(className.lastIndexOf(".") != -1){
					shortClassName = className.substring(className.lastIndexOf(".")+1);
				}
				// Hook constructors
				if(shortClassName.equals(methodName)){
					hookCustomizeConstructors(hookClass, methodInfoItems, hookType);
					methodHooked = true;
				}else{
					// Hook methods
					hookCustomizeMethods(hookClass, methodInfoItems, hookType);
					methodHooked = true;
				}
			}
		}else
			Log.d(Util.LOG_TAG, className + " not found");
		
		return methodHooked;
	}
	
	public static boolean hookCustomizeWithKnownClass(String methodInfo, Class<?> loadedClass, int hookType){
		boolean methodHooked = false;
		String[] methodInfoItems = parseMethodInfo(methodInfo);
		
		String className = methodInfoItems[0];
		String methodName = methodInfoItems[1];
		if(!loadedClass.getName().equals(className)){
			Log.d(Util.LOG_TAG, "loaded class not equal: " + className + " : " + loadedClass.getName());
			return methodHooked;
		}else
			Log.d(Util.LOG_TAG, "unhooked class: " + className + ":" + methodName);
								
		if (loadedClass != null && methodName != null) {
			String shortClassName = className;
			if(className.lastIndexOf(".") != -1){
				shortClassName = className.substring(className.lastIndexOf(".")+1);
			}
			// Hook constructors
			if(shortClassName.equals(methodName)){
				hookCustomizeConstructors(loadedClass, methodInfoItems, hookType);
				methodHooked = true;
			}else{
				// Hook methods
				hookCustomizeMethods(loadedClass, methodInfoItems, hookType);
				methodHooked = true;
			}
		}
		
		return methodHooked;
	}
	
	// Convert class name in dalvik format to java format
	private static String convertClassName(String classNameDalvik){
		String className = null;
		className = classNameDalvik.substring(1);
		className = className.replace("/", ".");
		return className;
	}
	
	// Parse method information into different parts
	private static String[] parseMethodInfo(String methodInfo){
		// {class name, method name, parameters, return type}
		String[] methodInfoItems = new String[] {null, null, null, null};
		String[] methodClassSigItems = methodInfo.split(";->");
		
		if(methodClassSigItems.length == 2){
			String className = methodClassSigItems[0];
			methodInfoItems[0] = convertClassName(className);
			
			String methodSignature = methodClassSigItems[1];
			int leftParIndex = methodSignature.indexOf("(");
			if(leftParIndex != -1){
				methodInfoItems[1] = methodSignature.substring(0, leftParIndex).trim();
				int rightParIndex = methodSignature.indexOf(")");
				if(rightParIndex != -1){
					methodInfoItems[2] = methodSignature.substring(leftParIndex+1, rightParIndex).trim();
					if(rightParIndex+1 < methodSignature.length()){
						methodInfoItems[3] = methodSignature.substring(rightParIndex+1).trim();
					}
				}
					
			}else{
				methodInfoItems[1] = methodSignature.trim();
			}
		}else if(methodClassSigItems.length == 1){
			String className = methodClassSigItems[0];
			methodInfoItems[0] = convertClassName(className);
		}
				
		return methodInfoItems;
	}
	
	private static void after(MethodHookParam param, int hookType){
		int uid = Binder.getCallingUid();
		if(Util.isAppNeedFrLog(uid))
			logMethod(param, uid, hookType);
	}
	
	private static void logMethod(MethodHookParam param, int uid, int hookType){
		String argsValue = MethodParser.parseParameters(param);
		String returnValue = MethodParser.parseReturnValue(param);
		String className = null;
		String methodName = null;
		if(param.method instanceof Constructor){
			Constructor<?> constructor = (Constructor<?>) param.method;
			className = constructor.getDeclaringClass().getName();
			methodName = constructor.getName();
		}else if(param.method instanceof Method){
			Method method = (Method) param.method;
			className = method.getDeclaringClass().getName();
			methodName = method.getName();
		}

		className = String.format("L%s", className.replace(".", "/"));
		
		String logMsg = String.format("{\"Basic\":[\"%d\",\"%s\",\"true\"], " + 
				"\"InvokeApi\":{\"%s;->%s\":[%s], \"return\":{%s}}}}", 
				uid, hookType, className, methodName, argsValue, returnValue);

		Log.i(Util.LOG_TAG, logMsg);
	}
	
	// Hook android.os.Build's fields
	private static void hookBuildFields(){
		// Init the map
		Random randomGenerator = new Random();
		HashMap<String, String> fieldsMap = new HashMap<String, String>();
		String[] fieldNames = {"PRODUCT", "DEVICE", "BOARD", "MANUFACTURER", "BRAND", 
				"MODEL", "HARDWARE", "TAGS", "HOST", "SERIAL"};
		for(String fieldName : fieldNames)
			fieldsMap.put(fieldName, Util.generateRandomStrs(randomGenerator.nextInt(5) + 5));

		Iterator<Entry<String, String>> iter = fieldsMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			XposedHelpers.setStaticObjectField(Build.class, entry.getKey(), entry.getValue());
		}
		
	}

}
