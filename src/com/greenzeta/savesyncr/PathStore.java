package com.greenzeta.savesyncr;

import java.util.Map;
import java.util.*;

public class PathStore
{
	public HashMap<String, String> filePaths;
	
	public PathStore(){
		filePaths = new HashMap<String, String>();
	}
	
	public void Add(String pKey, String pValue){
		filePaths.put(pKey, pValue);
		//filePaths.put("test","test2");
	}
	
	public void Loop(){
		for( Map.Entry entry : filePaths.entrySet() ){
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() );
		}
	}
}
