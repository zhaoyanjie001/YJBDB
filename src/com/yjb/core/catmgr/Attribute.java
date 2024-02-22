package com.yjb.core.catmgr;


public class Attribute{
	String attriName;	//字段名称
	String type;		//字段类型int float char  boolean
	int length;			//字段长度
	boolean isUnique;	

	public Attribute(String attriName,String type,int length,boolean isU){
		this.attriName=attriName;
		this.type=type;
		this.length=length;
		this.isUnique=isU;
	}
}