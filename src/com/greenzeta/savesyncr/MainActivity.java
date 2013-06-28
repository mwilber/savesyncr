package com.greenzeta.savesyncr;

import android.view.Menu;

import java.io.IOException;
import java.util.List;
import java.io.File;

import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import java.io.*;
import java.util.*;
import android.widget.AdapterView.*;
import android.widget.*;

public class MainActivity extends Activity {

	private static final String appKey = "908lm07bru67cqc";
	private static final String appSecret = "e77781n0tunfpqb";

	private static final int REQUEST_LINK_TO_DBX = 0;

	private TextView mTestOutput;
	private Button mLinkButton;
	private DbxAccountManager mDbxAcctMgr;
	
	private String fsRoot;
	private PathStore pStore;
	
	public List list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTestOutput = (TextView) findViewById(R.id.test_output);
		mLinkButton = (Button) findViewById(R.id.link_button);
		mLinkButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickLinkToDropbox();
				}
			});

		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);
		
		fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
		pStore = new PathStore();
		
		// TODO: load this data from external storage
		pStore.Add("savesyncr.txt",File.separator);
		pStore.Add("monkey2.s00",File.separator+"ScummVM"+File.separator+"Saves"+File.separator);
		
		try{
			// Adapter for the file selector spinner
			list = new ArrayList<String>();
			for( String key : pStore.filePaths.keySet()) {
				list.add(key);
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			Spinner fileSpin = (Spinner)findViewById(R.id.filespin);
			
			fileSpin.setOnItemSelectedListener(new OnItemSelectedListener(){
				public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 ){
					mTestOutput.append(list.get(arg2).toString()+"\n");
				}
				public void onNothingSelected(AdapterView<?> arg0){
					
				}
			});
			
			fileSpin.setAdapter(adapter);
		}catch(Exception e){
			Log.e("SPINNER ADAPTER", e.getMessage());
		}
	}
	
//		public Object getItem(int position){
//			HashMap<String, String> tblItem = listItems.get(position);
//			return tblItem.get("NAME");
//		}
//	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mDbxAcctMgr.hasLinkedAccount()) {
			showLinkedView();
		} else {
			showUnlinkedView();
		}
	}

	private void showLinkedView() {
		mLinkButton.setVisibility(View.GONE);
		mTestOutput.setVisibility(View.VISIBLE);
	}

	private void showUnlinkedView() {
		mLinkButton.setVisibility(View.VISIBLE);
		mTestOutput.setVisibility(View.GONE);
	}

	private void onClickLinkToDropbox() {
		mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				//TODO: handle dropbox auth result in gui
			} else {
				mTestOutput.setText("Link to Dropbox failed or was cancelled.");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	
	public void DoUpload(View view){
		Log.d("testing","step1");
		boolean doUpdate = false;
		String fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
		File tf = new File(fsRoot+File.separator+"savesyncr.txt");
		if( tf != null ){
			//Log.d("directory",getExternalFilesDir(null).toString());
			Log.d("upload",tf.getName());
			Log.d("filesize",String.valueOf(tf.length()));
			
			try{
				DbxPath testPath = new DbxPath(DbxPath.ROOT, tf.getName());
	
				// Create DbxFileSystem for synchronized file access.
				DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
	
				// Print the contents of the root folder.  This will block until we can
				// sync metadata the first time.
				List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
				mTestOutput.setText("\nContents of app folder:\n");
				for (DbxFileInfo info : infos) {
					mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
				}
				
				DbxFile testFile;
				
				
				if( dbxFs.exists(testPath) ){
					Log.d("FILE CHECK3", "OPEN");
					testFile = dbxFs.open(testPath);
				}else{
					Log.d("FILE CHECK3", "CREATE");
					testFile = dbxFs.create(testPath);
				}
				try {
					while( !testFile.getSyncStatus().isCached ){
						Log.d("Loading file to cache",String.valueOf(testFile.getSyncStatus().bytesTransferred));
						//TODO: add check to break this loop
					}
					testFile.writeFromExistingFile(tf,false);
					while( testFile.getSyncStatus().pending.toString() != "NONE" )
						Log.d("SYNC STATUS", testFile.getSyncStatus().pending.toString());
					
					doUpdate = true;

				} finally {
					testFile.close();
				}
				mTestOutput.append("\nPosted file to DropBox: " + testPath + ".\n");

			}catch(Exception e){
				Log.e("ERROR POSTING FILE:",e.getMessage());
			}
			
			if( doUpdate ){
				DoDownload( view );
			}
		}else{
			Log.d("upload","file not found. error. ready_");
		}	
	}
	
	
	public void DoDownload( View view ){
		
		try{
			DbxPath testPath = new DbxPath(DbxPath.ROOT, "savesyncr.txt");
			String fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
			File tf = new File(fsRoot+File.separator+"savesyncr.txt");
	
			// Create DbxFileSystem for synchronized file access.
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			
			if (dbxFs.isFile(testPath)) {
				FileInputStream resultData;
				DbxFile testFile = dbxFs.open(testPath);
				try {
					resultData = testFile.getReadStream();
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = resultData.read(b)) != -1) {
						bos.write(b, 0, bytesRead);
					}
					byte[] bytes = bos.toByteArray();
					FileOutputStream fos = new FileOutputStream(tf);
					fos.write(bytes);
					fos.close();
					Log.d("filewrite","File Write Complete!!!");
				} finally {
					testFile.close();
				}
				mTestOutput.append("\nRead file '" + testPath + "' and got data:\n    " + resultData);
			} else if (dbxFs.isFolder(testPath)) {
				mTestOutput.append("'" + testPath.toString() + "' is a folder.\n");
			}
			
		}catch(Exception e){
			Log.e("ERROR RETRIEVING FILE:",e.getMessage());
		}
	}

}
