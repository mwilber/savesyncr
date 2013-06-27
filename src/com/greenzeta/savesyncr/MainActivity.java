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
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import java.io.*;

public class MainActivity extends Activity {

	private static final String appKey = "908lm07bru67cqc";
	private static final String appSecret = "e77781n0tunfpqb";

	private static final int REQUEST_LINK_TO_DBX = 0;

	private TextView mTestOutput;
	private Button mLinkButton;
	private DbxAccountManager mDbxAcctMgr;

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
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mDbxAcctMgr.hasLinkedAccount()) {
			showLinkedView();
			doDropboxTest();
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
				doDropboxTest();
			} else {
				mTestOutput.setText("Link to Dropbox failed or was cancelled.");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void doDropboxTest() {
		try {
			final String TEST_DATA = "Hello Dropbox";
			final String TEST_FILE_NAME = "hello_dropbox.txt";
			DbxPath testPath = new DbxPath(DbxPath.ROOT, TEST_FILE_NAME);

			// Create DbxFileSystem for synchronized file access.
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());

			// Print the contents of the root folder.  This will block until we can
			// sync metadata the first time.
			List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
			mTestOutput.setText("\nContents of app folder:\n");
			for (DbxFileInfo info : infos) {
				mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
			}

			// Create a test file only if it doesn't already exist.
			if (!dbxFs.exists(testPath)) {
				DbxFile testFile = dbxFs.create(testPath);
				try {
					testFile.writeString(TEST_DATA);
				} finally {
					testFile.close();
				}
				mTestOutput.append("\nCreated new file '" + testPath + "'.\n");
			}

			// Read and print the contents of test file.  Since we're not making
			// any attempt to wait for the latest version, this may print an
			// older cached version.  Use getSyncStatus() and/or a listener to
			// check for a new version.
			if (dbxFs.isFile(testPath)) {
				String resultData;
				DbxFile testFile = dbxFs.open(testPath);
				try {
					resultData = testFile.readString();
				} finally {
					testFile.close();
				}
				mTestOutput.append("\nRead file '" + testPath + "' and got data:\n    " + resultData);
			} else if (dbxFs.isFolder(testPath)) {
				mTestOutput.append("'" + testPath.toString() + "' is a folder.\n");
			}
		} catch (IOException e) {
			mTestOutput.setText("Dropbox test failed: " + e);
		}
	}
	
	public void DoUpload(View view){
		Log.d("testing","step1");
		String fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
		File tf = new File(fsRoot+File.separator+"ScummVM"+File.separator+"Saves"+File.separator+"monkey2.s00");
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
	
				// Create a test file only if it doesn't already exist.
				if (!dbxFs.exists(testPath)) {
					DbxFile testFile = dbxFs.create(testPath);
					try {
						testFile.writeFromExistingFile(tf,false);
					} finally {
						testFile.close();
					}
					mTestOutput.append("\nCreated new file '" + testPath + "'.\n");
				}
			}catch(Exception e){
				Log.e("ERROR POSTING FILE:",e.getMessage());
			}
		}else{
			Log.d("upload","file not found. error. ready_");
		}	
	}
	
	
	public void DoDownload( View view ){
		
		try{
			DbxPath testPath = new DbxPath(DbxPath.ROOT, "monkey2.s00");
			String fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
			File tf = new File(fsRoot+File.separator+"ScummVM"+File.separator+"Saves"+File.separator+"monkey2b.s00");
	
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
