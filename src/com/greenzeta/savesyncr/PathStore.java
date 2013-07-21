package com.greenzeta.savesyncr;

import android.util.Log;

import java.util.Map;
import java.util.*;
import java.io.Serializable;

public class PathStore implements Serializable
{
	public HashMap<String, Path> filePaths;
	
	public PathStore(){
		filePaths = new HashMap<String, Path>();
	}

    public List ToList(){
        List<Path> list = new ArrayList<Path>(filePaths.values());
        return list;
    }
	
	public void Add(String pKey, String pValue){
		filePaths.put(pKey, new Path(pKey,pValue,new Long(0)));
		//filePaths.put("test","test2");
	}

    public void Remove(String pIdx){
        for(String key: filePaths.keySet()){
            Log.d("PathStore","testing: "+pIdx+"="+key);
            if(pIdx.equals(key)){
                filePaths.remove(key);
                break;
            }
        }
    }
	
	public String GetLocalPath(String pKey){
		
		Path tmpPath = this.filePaths.get(pKey);
		
		return tmpPath.localpath;
	}
	
	public boolean SetOffset(String pKey, Long pOffset){
		this.filePaths.get(pKey).timeoffset = pOffset;
		return true;
	}
	
	public void Loop(){
		for( Map.Entry entry : filePaths.entrySet() ){
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() );
		}
	}
}
