package com.greenzeta.savesyncr;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

private DbxAccountManager mDbxAcctMgr;

public class MainActivity extends Activity
{

	//APP_KEY
	//APP_SECRET
	static final int REQUEST_LINK_TO_DBX = 0;  // This value is up to you

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "908lm07bru67cqc", "e77781n0tunfpqb");
        
        //ToDo: look into hasLinkedAccount()
        
        setContentView(R.layout.main);
    }
    
    private void onClickLinkToDropbox() {
	    mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            // ... Start using Dropbox files.
	            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
	            
	            DbxFile testFile = dbxFs.create(new DbxPath("hello.txt"));
				try {
				    testFile.writeString("Hello Dropbox!");
				} finally {
				    testFile.close();
				}
	        } else {
	            // ... Link failed or was cancelled by the user.
	        }            
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
}
