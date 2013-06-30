package com.greenzeta.savesyncr;

import java.io.Serializable;

public class Path implements Serializable
{
	public String name;
	public String localpath;
	public Long timeoffset;
	
	public Path( String pName, String pPath, Long pOffset ){
		this.name = pName;
		this.localpath = pPath;
		this.timeoffset = pOffset;
	}
	
	public String toString(){
		return this.localpath;
	}
}
