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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserActivity extends ListActivity {
	
	public final static String FILE_MESSAGE = "com.greenzeta.savesyncr.FILEBROWSER";
 
	 private List<String> item = null;
	 private List<String> path = null;
	 private String root;
	 private TextView myPath;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_filebrowser);
	        myPath = (TextView)findViewById(R.id.path);
	        
	        root = Environment.getExternalStorageDirectory().getPath();
	        
	        getDir(root);
	    }
	    
	    private void getDir(String dirPath)
	    {
	     myPath.setText("Location: " + dirPath);
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
	           item.add(file.getName());
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

	    }
	  Intent intent = new Intent(this, MainActivity.class);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		intent.putExtra(FILE_MESSAGE, file.getPath());
		startActivity(intent);
	 }

	}