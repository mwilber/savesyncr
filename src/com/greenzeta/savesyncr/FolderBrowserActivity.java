package com.greenzeta.savesyncr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class FolderBrowserActivity extends ListActivity {
	
	public final static String FOLDER_MESSAGE = "com.greenzeta.savesyncr.FOLDERBROWSER";
 
	 private List<String> item = null;
	 private List<String> path = null;
	 private String root;
	 private TextView myPath;
	 private Spinner fileSpin;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_folderbrowser);
	        myPath = (TextView)findViewById(R.id.path);
	        fileSpin = (Spinner)findViewById(R.id.remotefilespin);
	        
	        root = Environment.getExternalStorageDirectory().getPath();
	        
	        getDir(root);
	        
	        //Populate the spinner
	        // Get the message from the intent
		    Intent intent = getIntent();
		    String message = "";
		    message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		    try{
		    	if( message != null ){
		    		Log.d("INTENT", message.toString());
		    		
		    		ArrayList<String> list = new ArrayList<String>();
					for( String key : message.toString().split(";")) {
						list.add(key);
					}
					
		    		ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

					fileSpin.setAdapter(adapter);
		    	}else{
		    		Log.d("INTENT", "NO INTENT FOUND");
					}
		    }catch(Exception e){
		    	Log.d("INTENT", "ERROR");
		    }
	    }
	    
	    private void getDir(String dirPath)
	    {
	     myPath.setText(dirPath);
	     item = new ArrayList<String>();
	     path = new ArrayList<String>();
	     File f = new File(dirPath);
	     File[] files = f.listFiles();
	     
	     if(!dirPath.equals(root))
	     {
	      item.add(root);
	      path.add(root);
	      item.add("../");
	      path.add(f.getParent()); 
	     }
	     
	     Arrays.sort(files, filecomparator);
	     
	     for(int i=0; i < files.length; i++)
	     {
	      File file = files[i];
	      
	      if(!file.isHidden() && file.canRead()){
	       path.add(file.getPath());
	          if(file.isDirectory()){
	           item.add(file.getName() + "/");
	          }else{
	           //item.add(file.getName());
	          }
	      } 
	     }

	     ArrayAdapter<String> fileList =
	       new ArrayAdapter<String>(this, R.layout.row, item);
	     setListAdapter(fileList); 
	    }
	    
	    Comparator<? super File> filecomparator = new Comparator<File>(){
	  
	  public int compare(File file1, File file2) {

	   if(file1.isDirectory()){
	    if (file2.isDirectory()){
	     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
	    }else{
	     return -1;
	    }
	   }else {
	    if (file2.isDirectory()){
	     return 1;
	    }else{
	     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
	    }
	   }
	    
	  }  
	 };

	 @Override
	 protected void onListItemClick(ListView l, View v, int position, long id) {
	  // TODO Auto-generated method stub
	  File file = new File(path.get(position));
	  
	  if (file.isDirectory())
	  {
	   if(file.canRead()){
	    getDir(path.get(position));
	   }else{
	    new AlertDialog.Builder(this)
	     .setIcon(R.drawable.ic_launcher)
	     .setTitle("[" + file.getName() + "] folder can't be read!")
	     .setPositiveButton("OK", null).show(); 
	   } 
		}else {
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
	     	.setTitle("[" + file.getPath() + "]")
	     	.setPositiveButton("OK", null).show();
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(FOLDER_MESSAGE, file.getPath());
			startActivity(intent);	
		}
	  
	 }
	 
	 public void DoDownload(View view){
		 
		 String message = myPath.getText().toString()+"/"+fileSpin.getSelectedItem().toString();
		 
		 Log.d("RESPONSE",message);
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(FOLDER_MESSAGE, message);
		startActivity(intent);	
	}

	}