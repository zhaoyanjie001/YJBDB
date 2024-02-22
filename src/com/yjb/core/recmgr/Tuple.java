package com.yjb.core.recmgr;


import java.util.Vector;
public class Tuple {
	public Vector<String> units;
	public Tuple(Vector<String> units){
		this.units=units;
	}
	public Tuple(){units = new Vector<String>();}
	public String getString(){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<units.size();i++){
			sb.append("\t"+units.get(i));
		}
		return sb.toString();
	}
}