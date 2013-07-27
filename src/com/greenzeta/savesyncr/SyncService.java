package com.greenzeta.savesyncr;

import android.*;
import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by matt on 7/21/13.
 */
public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();
    private static final String appKey = "908lm07bru67cqc";
    private static final String appSecret = "e77781n0tunfpqb";
    private static final String dataStore = "savesyncr.dat";
    private static final int REQUEST_LINK_TO_DBX = 0;
    private static final int updateInterval = 1800;

    private Timer timer;
    private String fsRoot;
    private PathStore pStore;
    private DbxAccountManager mDbxAcctMgr;

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.i(TAG, "Timer task doing work");

            //Load the tracked paths from local storage
            pStore = new PathStore();

            ObjectInputStream in = null;
            try{
                in = new ObjectInputStream( SyncService.this.openFileInput(dataStore));
                pStore.filePaths = (HashMap<String,Path>)in.readObject();
                Log.d("STORED HASH", pStore.filePaths.toString());
                in.close();

                Intent intent = new Intent(SyncService.this, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(SyncService.this, 0, intent, 0);

                // Build notification
                Notification noti = new Notification.Builder(SyncService.this)
                        .setContentTitle("SaveSyncr synchronization in progress...")
                        .setContentText("Syncing")
                        .setTicker("SaveSyncr synchronization in progress...")
                                //.setLargeIcon((com.greenzeta.savesyncr.R.drawable.ic_launcher))
                        .setSmallIcon(com.greenzeta.savesyncr.R.drawable.ic_launcher)
                        .setContentIntent(pIntent)
                        //.addAction(com.greenzeta.savesyncr.R.drawable.ic_launcher, "SaveSyncr synchronization in progress... ", pIntent)
                        .build();


                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                // Hide the notification after its selected
                noti.flags |= Notification.FLAG_AUTO_CANCEL;

                notificationManager.notify(0, noti);

                for( HashMap.Entry entry : pStore.filePaths.entrySet() ){
                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() );
                    PathSync(entry.getKey().toString());
                }

                notificationManager.cancel(0);

            }catch(IOException ex){
                ex.printStackTrace();
            }catch(ClassNotFoundException ex){
                ex.printStackTrace();
            }
        }

        public boolean SavePathStore(){
            ObjectOutputStream out = null;
            try{
                //out = new ObjectOutputStream(new FileOutputStream(dataStore));
                out = new ObjectOutputStream(SyncService.this.openFileOutput(dataStore, Context.MODE_PRIVATE));
                out.writeObject(pStore.filePaths);
                out.close();

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
                    System.out.println("\nPosted file to DropBox: " + dbPath + ".\n");

                }catch(Exception e){
                    Log.e("ERROR POSTING FILE:",e.getMessage());
                }
            }else{
                Log.d("PathUpload","file not found. error. ready_");
            }

            return true;
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
                    System.out.println("\nRead file '" + dbPath + "' and got data:\n    " + resultData);
                } else if (dbxFs.isFolder(dbPath)) {
                    System.out.println("'" + dbPath.toString() + "' is a folder.\n");
                }

            }catch(Exception e){
                Log.e("ERROR POSTING FILE:",e.getMessage());
            }

            return true;
        }

        public void PathSync( String pPathName ){

            String fileName = pPathName;
            String localPath = pStore.GetLocalPath(fileName);
            Long dbDate = null;
            Long locDate = null;

            System.out.println("Checking status of: " + fileName + "\n");
            System.out.println("Local path: " + localPath + "\n");

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
                System.out.println("Dropbox timestamp: " + dbDate.toString() + "\n");
            if(locDate != null)
                System.out.println("Local timestamp: " + locDate.toString() + "\n");

            // Now we check and do the sync
            if( dbDate == null && locDate == null ){
                System.out.println("File does not exist anywhere. Doing Nothing.\n");
                //Toast.makeText(getBaseContext(), fileName+" does not exist anywhere. Doing Nothing.", Toast.LENGTH_LONG).show();
            }else if( dbDate == null ){
                System.out.println("File does not exist in Dropbox. Upload.\n");
                //Toast.makeText(getBaseContext(), fileName+" does not exist in Dropbox. Upload.", Toast.LENGTH_LONG).show();
                PathUpload(fileName);
            }else if( locDate == null ){
                System.out.println("File does not exist in local. Download.\n");
                //Toast.makeText(getBaseContext(), fileName+" does not exist in local. Download.", Toast.LENGTH_LONG).show();
                PathDownload(fileName);
            }else{
                // Do some checking
                Long comparison = (dbDate-locDate) - pStore.filePaths.get(fileName).timeoffset;
                System.out.println("Time offset (" + dbDate + "-" + locDate + ") - " + pStore.filePaths.get(fileName).timeoffset + ": " + String.valueOf(comparison) + "\n");
                if( comparison > 0 ){
                    System.out.println(" Dropbox file newer. Download.\n");
                    //Toast.makeText(getBaseContext(), fileName+" Dropbox file newer. Download.", Toast.LENGTH_LONG).show();
                    PathDownload(fileName);
                }else if( comparison < 0 ){
                    System.out.println(" Local file newer. Upload.\n");
                    //Toast.makeText(getBaseContext(), fileName+" Local file newer. Upload.", Toast.LENGTH_LONG).show();
                    PathUpload(fileName);
                }else{
                    System.out.println("Files match. Do Nothing\n");
                    //Toast.makeText(getBaseContext(), fileName+" Files match. Do Nothing", Toast.LENGTH_LONG).show();
                }
            }

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);

        Log.i(TAG, "Service creating");

        timer = new Timer("SaveSyncrTimer");
        timer.schedule(updateTask, 1000L, updateInterval * 1000L);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroying");

        timer.cancel();
        timer = null;
    }

}
