package com.mindmac.eagleeyetest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {
	
	public static final String TAG = "EagleEyeTest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // System api
        String phoneNum = "15555215556";
        String message = "hello world";
        sendSms(phoneNum, message);
        Log.i(TAG, "send sms " + message + " to " + phoneNum);
        isFileExists(getFilesDir().getPath());
        
        // Customize system api
        String pkgName = getPackageName();
        Log.i(TAG, "Customized system api: getPackageName " + pkgName);
        
        sendSmsViaIntent(phoneNum, message);
        
        sendTestBroadcast();
        Log.i(TAG, "send broadcast");
        
        // Customize app api
        add(5, 7);
        
        // Dynamic load
        dynamicLoad();
        
        // Native Entry
        Native.nativeEntry();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void sendSms(String phoneNumber, String message){
    	 SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
    
    private boolean isFileExists(String inputFile){
    	File file = new File(inputFile);
    	return file.exists();
    }
    
    private void sendSmsViaIntent(String phoneNumber, String message){
    	Uri uri = Uri.parse("smsto:" + phoneNumber);   
    	Intent intent = new Intent(Intent.ACTION_SENDTO, uri);   
    	intent.putExtra("sms_body", message);   
    	startActivity(intent);  
    }
    
    private void sendTestBroadcast(){
    	Intent intent = new Intent();
    	intent.setClassName(this, String.format("%s.Receiver", getPackageName()));
    	sendBroadcast(intent);
    	
    	sendBroadcast(intent, "android.test.Permission");
    }
    
    private int add(int a, int b){
    	Log.i(TAG, "Customized app apis: add method: " + a + "+" + b);
    	return a + b;
    }
    
    private void dynamicLoad(){
    	String dynamicApk = "Dynamic.apk";
    	this.copyAsset(this, dynamicApk, String.format("%s/%s",Environment.getExternalStorageDirectory(), dynamicApk));
    	
    	ApplicationInfo appInfo = this.getApplicationInfo();
		File dynamic = new File(Environment.getExternalStorageDirectory().toString()
                		  + File.separator + dynamicApk);
        DexClassLoader dexClassLoader = new DexClassLoader(dynamic.getAbsolutePath(), 
        		appInfo.dataDir, null, this.getClassLoader());
        try {
        	Class<?> dynamicTestClass = dexClassLoader.loadClass("com.example.dynamic.DynamicTest");
        	for(Method method : dynamicTestClass.getDeclaredMethods()){
        		if(method.getName().equals("add")){
        			int addRes = (Integer) method.invoke(dynamicTestClass, 6, 8);
        			Log.i(TAG, "DynamicLoading: com.example.dynamic.DynamicTest.add, res " + addRes);
        		}else if(method.getName().equals("concat")){
        			String concatRes = (String) method.invoke(dynamicTestClass, "hello", "world");
        			Log.i(TAG, "DynamicLoading: com.example.dynamic.DynamicTest.concat, res " + concatRes);
        		}
        	}
        	} catch (Exception exception) {
                exception.printStackTrace();
            }
    }
    
    private void copyAsset(Context context, String assetFileName, String dstFile) {
		AssetManager assetManager = null;
		assetManager = context.getAssets();
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;

		try {
			inputStream = assetManager.open(assetFileName);
			fileOutputStream = new FileOutputStream(dstFile);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			fileOutputStream.write(buffer);
			fileOutputStream.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				inputStream.close();
				fileOutputStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
