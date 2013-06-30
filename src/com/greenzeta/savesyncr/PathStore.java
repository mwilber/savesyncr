package com.greenzeta.savesyncr;

import java.util.Map;
import java.util.*;
import java.io.Serializable;

public class PathStore implements Serializable
{
	public HashMap<String, Path> filePaths;
	
	public PathStore(){
		filePaths = new HashMap<String, Path>();
	}
	
	public void Add(String pKey, String pValue){
		filePaths.put(pKey, new Path(pKey,pValue,new Long(0)));
		//filePaths.put("test","test2");
	}
	
	public void Loop(){
		for( Map.Entry entry : filePaths.entrySet() ){
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() );
		}
	}
}
