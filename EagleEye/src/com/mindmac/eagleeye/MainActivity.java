package com.mindmac.eagleeye;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity {

	private static ExecutorService mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
			new PriorityThreadFactory());

	private static class PriorityThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Environment Initialization
     	EnvironmentInitTask envInitTask = new EnvironmentInitTask();
     	envInitTask.executeOnExecutor(mExecutor, (Object) null);

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
    
    // Copy fd2path.dex to /sdcard && copy libfd2path.so to the /system/lib
    private boolean initEnvironment(){
    	// Copy fd2path.dex 
    	if(Util.isExternalStorageWritable()){
    		File dstDexFile = new File(Environment.getExternalStorageDirectory(), Util.DEXFD2PATH);
    		Util.copyAsset(this, Util.DEXFD2PATH, dstDexFile.getAbsolutePath());
    	}
    		
    	File dstLibDir = new File(String.format("%s/resources", this.getFilesDir().getPath()));
    	if(!dstLibDir.exists())
    		dstLibDir.mkdirs();
    	
    	File dstLibFile = new File(dstLibDir, Util.LIBFD2PATH);
    	// Copy libfd2path.so to the destination path
    	Util.copyAsset(this, Util.LIBFD2PATH, dstLibFile.getAbsolutePath());
    	
    	// Check if copied
    	if(dstLibFile.exists()){
    		// Copy to /system/lib
    		String cmd = "mount -o rw,remount /system";
    		Util.execSuCmd(cmd);
    		
    		cmd = String.format("dd if=%s of=/system/lib/%s", dstLibFile.getAbsolutePath(), Util.LIBFD2PATH);
    		Util.execSuCmd(cmd);
    		
    		cmd = String.format("chmod 777 /system/lib/%s", Util.LIBFD2PATH);
    		Util.execSuCmd(cmd);
    		
    		cmd = "mount -o ro,remount /system";
    		Util.execSuCmd(cmd);
    	}
    		
    	return this.isEnvironmentInited();
    }
    
    // Check environment initialization
    private boolean isEnvironmentInited(){
    	File dstDexFile = new File(Environment.getExternalStorageDirectory(), Util.DEXFD2PATH);
    	File systemLibPath = new File("/system/lib/libfd2path.so");
    	if(dstDexFile.exists() && systemLibPath.exists())
    		return true;
    	else
    		return false;
    	
    }
    
    private class EnvironmentInitTask extends AsyncTask<Object, Integer, Boolean> {
		private ProgressDialog mProgressDialog;

		@Override
		protected Boolean doInBackground(Object... params) {
			if(isEnvironmentInited())
				return true;
			else
				return initEnvironment();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// Show progress dialog
			mProgressDialog = new ProgressDialog(MainActivity.this);
			mProgressDialog.setMessage(getString(R.string.msg_init));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setProgressNumberFormat(null);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(Boolean inited) {
			if (!MainActivity.this.isFinishing()) {
				// Dismiss progress dialog
				if (mProgressDialog.isShowing())
					try {
						mProgressDialog.dismiss();
					} catch (IllegalArgumentException ignored) {
					}

				if(inited)
					Toast.makeText(MainActivity.this, R.string.msg_init_success, 
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, R.string.msg_init_fail, 
							Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(inited);
		}
	}

}
