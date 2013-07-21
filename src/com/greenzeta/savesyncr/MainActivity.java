package com.greenzeta.savesyncr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.LinearLayout;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.greenzeta.savesyncr.MESSAGE";
	private static final String appKey = "908lm07bru67cqc";
	private static final String appSecret = "e77781n0tunfpqb";
	private static final String dataStore = "savesyncr.dat";

	private static final int REQUEST_LINK_TO_DBX = 0;

	private TextView mTestOutput;
	private Button mLinkButton;
	private DbxAccountManager mDbxAcctMgr;
    private ListView mList;
	
	private String fsRoot;
	private PathStore pStore;
	
	public List list;
    public PathAdapter pathAdapter;

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
        mList = (ListView) findViewById(R.id.pathList);

		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);
		
		fsRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
		pStore = new PathStore();
		
		ObjectInputStream in = null;
		try{
			in = new ObjectInputStream( this.openFileInput(dataStore));
			pStore.filePaths = (HashMap<String,Path>)in.readObject();
			Log.d("STORED HASH", pStore.filePaths.toString());
			in.close();

		}catch(IOException ex){
			ex.printStackTrace();
		}catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}
		
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

        try{
            // Update the listview
            mList.setAdapter(new PathAdapter(this, R.layout.path_list,pStore.ToList()));
        }catch(Exception e){
            Log.e("LISTVIEW ADAPTER", e.getMessage());
        }
		
		// Get the message from the fileintent
	    Intent intent = getIntent();
	    String message = "";
	    message = intent.getStringExtra(FileBrowserActivity.FILE_MESSAGE);
	    try{
	    	if( message != null ){
	    		Log.d("INTENT", message.toString());
					AddPath(message.toString());
	    	}else{
	    		Log.d("INTENT", "NO INTENT FOUND");
				}
	    }catch(Exception e){
	    	Log.d("INTENT", "ERROR");
	    }
	    
	    // Get the message from the folderintent
	    message = "";
	    message = intent.getStringExtra(FolderBrowserActivity.FOLDER_MESSAGE);
	    try{
	    	if( message != null ){
	    		Log.d("INTENT", message.toString());
					AddPath(message.toString());
	    	}else{
	    		Log.d("INTENT", "NO INTENT FOUND");
				}
	    }catch(Exception e){
	    	Log.d("INTENT", "ERROR");
	    }
	}


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


    public void RemovePath(String pIdx){
        Log.d("MainActivity", "remove: " + pIdx);
        Log.d("RemovePath","Removing");
        pStore.Remove(pIdx);
        Log.d("RemovePath","Saving");
        SavePathStore();
    }
	
	
	public void DoAdd(View view){
		//pStore.Add("savesyncr.txt",File.separator);
		//pStore.Add("monkey2.s00",File.separator+"ScummVM"+File.separator+"Saves"+File.separator);
		//SavePathStore();
		
		//TODO: Add file browsing target action as intent
		Intent intent = new Intent(this, FileBrowserActivity.class);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		String message = "local";
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}
	
	
	public void DoAddRemote(View view){

		String message = "";
		
		try{
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			List<DbxFileInfo> rList = dbxFs.listFolder(DbxPath.ROOT);
			
			Iterator<DbxFileInfo> iterator = rList.iterator();
			while (iterator.hasNext()) {
				//Log.d("Remote List",iterator.next().path.getName());
				if(message!="")message+=";";
				message+=iterator.next().path.getName();
			}
			
		}catch(Exception e){
			Log.e("ERROR RETRIEVING FILES:",e.getMessage());
		}
		Intent intent = new Intent(this, FolderBrowserActivity.class);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}
	
	
	public void DoClearMessages(View view){
		mTestOutput.setText("");
	}
	
	
	public boolean AddPath(String pPath){
			File tmpFile = new File(pPath);
			//if( tmpFile.exists() ){
                Log.d("AddPath","Adding");
			    pStore.Add(tmpFile.getName(), tmpFile.getPath());
                Log.d("AddPath","Saving");
				SavePathStore();
			//}
			return true;
	}
	
	
	public boolean SavePathStore(){
		ObjectOutputStream out = null;
		try{
			//out = new ObjectOutputStream(new FileOutputStream(dataStore));
			out = new ObjectOutputStream(this.openFileOutput(dataStore, Context.MODE_PRIVATE));
			out.writeObject(pStore.filePaths);
			out.close();

            Log.d("SavePathStore","Clearing");
            PathAdapter pa = (PathAdapter)mList.getAdapter();
            pa.clear();
            for(Path key: pStore.filePaths.values()){
                Log.d("SavePathStore","Adding: "+key.name);
                pa.add(key);
            }
            Log.d("SavePathStore","Notify");
            pa.notifyDataSetChanged();

			return true;
		}catch( IOException ex ){
			ex.printStackTrace();
        }catch( Exception ex ){
            ex.printStackTrace();
		}

		return false;
	}
	
	
	public boolean PathUpload( String pPathName ){
		
		String fileName = pPathName;
		String localPath = pStore.GetLocalPath(fileName);
		Long dbDate = null;
		Long locDate = null;
		
		File localFile = new File(localPath);
		Log.d("Looking for local file",localPath);
		if( localFile.exists() ){
			try{
				DbxPath dbPath = new DbxPath(DbxPath.ROOT, localFile.getName());
				DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
				
				DbxFile dbFile;
				if( dbxFs.exists(dbPath) ){
					Log.d("REMOTE FILE EXISTS", "Deleting");
					dbxFs.delete(dbPath);
				}
				dbFile = dbxFs.create(dbPath);
				
				try {
					dbFile.writeFromExistingFile(localFile,false);
					while( dbFile.getSyncStatus().pending.toString() != "NONE" ){
						//Log.d("ULSYNC STATUS", dbFile.getSyncStatus().pending.toString());
					}
					// Update the time offset
					dbDate = dbxFs.getFileInfo(dbPath).modifiedTime.getTime();
					locDate = localFile.lastModified();
					Log.d("saving offset", String.valueOf((dbDate-locDate)));
					pStore.SetOffset(fileName, (dbDate-locDate));
					SavePathStore();
				} finally {
					dbFile.close();
				}
				mTestOutput.append("\nPosted file to DropBox: " + dbPath + ".\n");
				
			}catch(Exception e){
				Log.e("ERROR POSTING FILE:",e.getMessage());
			}
		}else{
			Log.d("PathUpload","file not found. error. ready_");
		}
		
		return true;
	}

	
	public void DoUpload(View view){

		Spinner fileSpin = (Spinner)findViewById(R.id.filespin);
		String fileName = fileSpin.getSelectedItem().toString();
		String localPath = fsRoot+pStore.filePaths.get(fileSpin.getSelectedItem())+fileSpin.getSelectedItem().toString();
		
		File tf = new File(localPath);
		if( tf.exists() ){

			Log.d("upload",tf.getName());
			Log.d("filesize",String.valueOf(tf.length()));
			
			try{
				DbxPath testPath = new DbxPath(DbxPath.ROOT, tf.getName());
				DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
	
				// Print the contents of the root folder.  This will block until we can
				// sync metadata the first time.
//				List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
//				mTestOutput.setText("\nContents of app folder:\n");
//				for (DbxFileInfo info : infos) {
//					mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
//				}
				
				DbxFile testFile;
				if( dbxFs.exists(testPath) ){
					Log.d("FILE EXISTS", "Deleting");
					dbxFs.delete(testPath);
				}
				testFile = dbxFs.create(testPath);
				
				try {
					testFile.writeFromExistingFile(tf,false);
					while( testFile.getSyncStatus().pending.toString() != "NONE" ){
						//Log.d("ULSYNC STATUS", testFile.getSyncStatus().pending.toString());
					}

				} finally {
					testFile.close();
				}
				mTestOutput.append("\nPosted file to DropBox: " + testPath + ".\n");

			}catch(Exception e){
				Log.e("ERROR POSTING FILE:",e.getMessage());
			}
			
		}else{
			Log.d("upload","file not found. error. ready_");
		}	
	}
	
	
	public boolean PathDownload( String pPathName ){
		
		String fileName = pPathName;
		String localPath = pStore.GetLocalPath(fileName);
		Long dbDate = null;
		Long locDate = null;
		
		try{

			DbxPath dbPath = new DbxPath(DbxPath.ROOT, fileName);
			File localFile = new File(localPath);
			
			// Create DbxFileSystem for synchronized file access.
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			
			if (dbxFs.isFile(dbPath)) {
				FileInputStream resultData;
				DbxFile dbFile = dbxFs.open(dbPath);
				try {
					
					// Wait for file sync
					while( dbFile.getSyncStatus().pending.toString() != "NONE" )
							Log.d("SYNC STATUS", dbFile.getSyncStatus().pending.toString());
					
					resultData = dbFile.getReadStream();
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = resultData.read(b)) != -1) {
						bos.write(b, 0, bytesRead);
					}
					byte[] bytes = bos.toByteArray();
					// Delete local file before writing
					if( localFile.exists() ){
						Log.d("LOCAL FILE EXISTS", "Deleting");
						localFile.delete();
					}
					FileOutputStream fos = new FileOutputStream(localFile);
					fos.write(bytes);
					fos.close();
					Log.d("filewrite","File Write Complete!!!");
					// Update the time offset
					dbDate = dbxFs.getFileInfo(dbPath).modifiedTime.getTime();
					locDate = localFile.lastModified();
					Log.d("saving offset", String.valueOf((dbDate-locDate)));
					pStore.SetOffset(fileName, (dbDate-locDate));
					SavePathStore();
				} finally {
					dbFile.close();
				}
				mTestOutput.append("\nRead file '" + dbPath + "' and got data:\n    " + resultData);
			} else if (dbxFs.isFolder(dbPath)) {
				mTestOutput.append("'" + dbPath.toString() + "' is a folder.\n");
			}
			
		}catch(Exception e){
			Log.e("ERROR POSTING FILE:",e.getMessage());
		}
		
		return true;
	}

	
	public void DoDownload( View view ){
		
		Spinner fileSpin = (Spinner)findViewById(R.id.filespin);
		String fileName = fileSpin.getSelectedItem().toString();
		String localPath = fsRoot+pStore.filePaths.get(fileSpin.getSelectedItem())+fileSpin.getSelectedItem().toString();
		
		try{
			DbxPath testPath = new DbxPath(DbxPath.ROOT, fileName);
			File tf = new File(localPath);
	
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
					// Delete local file before writing
					if( tf.exists() )
						tf.delete();
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
	
	public void DoSync(View view){
		
		try{
			// Create DbxFileSystem for synchronized file access.
			//DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			// Big problems with this function.
			//dbxFs.syncNowAndWait();
		}catch(Exception e){
			Log.e("ERROR RETRIEVING DB DATA:",e.getMessage());
		}
		
		for( HashMap.Entry entry : pStore.filePaths.entrySet() ){
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() );
			PathSync(entry.getKey().toString());
		}
	}
	
	public void PathSync( String pPathName ){
		
		String fileName = pPathName;
		String localPath = pStore.GetLocalPath(fileName);
		Long dbDate = null;
		Long locDate = null;
		
		mTestOutput.append("Checking status of: "+fileName+"\n");
		mTestOutput.append("Local path: "+localPath+"\n");
		
		try{
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			Log.d("Sync Looking on DB",fileName);
			DbxPath dbPath = new DbxPath(DbxPath.ROOT, fileName);
			if( dbxFs.exists(dbPath) ){
				Log.e("REMOTE FILE FOUND",fileName);
				//First check for a newer version in the cloud
				//DbxFileStatus getNewerStatus()
				DbxFile dbFile = dbxFs.open(dbPath);
				try{
					Log.e("REMOTE CHECKING NEWER STATUS",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
					if( dbFile.getNewerStatus() != null ){
						Log.e("REMOTE NEWER STATUS != NULL",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
						while( dbFile.getNewerStatus().pending.toString() != "NONE" ){
							Log.d("SYNC STATUS", dbFile.getNewerStatus().pending.toString());
						}
					}
					Log.e("REMOTE UPDATE CACHE",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
					dbFile.update();
//					Thread.sleep(30000);
//					Log.e("REMOTE CHECKING NEWER STATUS",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
//					if( dbFile.getNewerStatus() != null ){
//						Log.e("REMOTE NEWER STATUS != NULL",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
//						while( dbFile.getNewerStatus().pending.toString() != "NONE" ){
//							Log.d("SYNC STATUS", dbFile.getNewerStatus().pending.toString());
//						}
//					}
					Log.e("REMOTE CHECKING DATE",fileName+":"+String.valueOf(dbFile.getInfo().modifiedTime.getTime()));
					dbDate = dbFile.getInfo().modifiedTime.getTime();
				}catch(Exception e){
					Log.e("ERROR RETRIEVING REMOTE FILE:","");
				} finally {
					dbFile.close();
				}
			}else{
				Log.e("REMOTE FILE NOT FOUND",fileName);
			}
		}catch(Exception e){
			Log.e("ERROR RETRIEVING REMOTE FILE:","");
		}
		try{
			File localFile = new File(localPath);
			if(localFile.exists())
				locDate = localFile.lastModified();
		}catch(Exception e){
			Log.e("ERROR RETRIEVING LOCAL FILE:","");
		}
		
		// Remove this output at some point
		if(dbDate != null)
			mTestOutput.append("Dropbox timestamp: "+dbDate.toString()+"\n");
		if(locDate != null)
			mTestOutput.append("Local timestamp: "+locDate.toString()+"\n");
			
		// Now we check and do the sync
		if( dbDate == null && locDate == null ){
			mTestOutput.append("File does not exist anywhere. Doing Nothing.\n");
		}else if( dbDate == null ){
			mTestOutput.append("File does not exist in Dropbox. Upload.\n");
			PathUpload(fileName);
		}else if( locDate == null ){
			mTestOutput.append("File does not exist in local. Download.\n");
			PathDownload(fileName);
		}else{
			// Do some checking
			Long comparison = (dbDate-locDate) - pStore.filePaths.get(fileName).timeoffset;
			mTestOutput.append("Time offset ("+dbDate+"-"+locDate+") - "+pStore.filePaths.get(fileName).timeoffset+": "+String.valueOf(comparison)+"\n");
			if( comparison > 0 ){
				mTestOutput.append("Dropbox file newer. Download.\n");
				PathDownload(fileName);
			}else if( comparison < 0 ){
				mTestOutput.append("Local file newer. Upload.\n");
				PathUpload(fileName);
			}else{
				mTestOutput.append("Files match. Do Nothing\n");
			}
		}
			
	}

    public class PathAdapter extends ArrayAdapter<Path> {

        int resource;
        String response;
        Context context;
        private final static String TAG = "PathAdapter";

        //Initialize adapter
        public PathAdapter(Context context, int resource, List<Path> items) {
            super(context, resource, items);
            this.resource=resource;

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            TextView textTitle;
            TextView textTimer;
            final ImageView image;

            LinearLayout alertView;
            //Get the current alert object
            final Path al = getItem(position);

            //Inflate the view
            if(convertView==null)
            {
                Log.d("VIEW","isnull");
                alertView = new LinearLayout(getContext());
                Log.d("VIEW","inflater");
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi;
                Log.d("VIEW","LayoutInflater");
                vi = (LayoutInflater)getContext().getSystemService(inflater);
                Log.d("VIEW","inflate");
                vi.inflate(resource, alertView, true);
            }
            else
            {
                Log.d("VIEW","not null");
                alertView = (LinearLayout) convertView;
            }

            //Get the text boxes from the listitem.xml file
            TextView alertText =(TextView)alertView.findViewById(R.id.txtAlertText);
            TextView alertDate =(TextView)alertView.findViewById(R.id.txtAlertDate);
            ImageButton btnRemove = (ImageButton)alertView.findViewById(R.id.pathRemove);

            //Assign the appropriate data from our alert object above
            alertText.setText(al.name);
            alertDate.setText(al.localpath);

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "string: " + al.name);
                    PathAdapter.this.RemovePath(al.name);

                    //Toast.makeText(context, "button clicked: " + al.name, Toast.LENGTH_SHORT).show();
                }
            });

            return alertView;
        }

        public void RemovePath(String pIdx){
            Log.d(TAG, "remove: " + pIdx);
            MainActivity.this.RemovePath(pIdx);
        }

    }

}
