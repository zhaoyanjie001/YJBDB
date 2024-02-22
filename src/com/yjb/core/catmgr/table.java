package com.yjb.core.catmgr;

import java.util.Vector;

public class table{
	String tableName;			
	String primaryKey;			
	Vector<attribute>attributes;
	Vector<index> indexes;			
	int indexNum;				
	int attriNum;				
	int tupleNum;				
	int tupleLength;			

	public table(String tableName,Vector<attribute> attributes,String primaryKey){
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.indexes=new Vector<index>();				
		this.indexNum=0;		
		this.attributes=attributes;
		this.attriNum=attributes.size();
		this.tupleNum=0;
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i).attriName.equals(primaryKey))
				attributes.get(i).isUnique=true;
			this.tupleLength+=attributes.get(i).length;
		}
	}

	public table(String tableName, Vector<attribute> attributes, Vector<index> indexes, String primaryKey,int tupleNum) {//initial table
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.attributes=attributes;
		this.indexes=indexes;
		this.attriNum=attributes.size();
		this.indexNum=indexes.size();	
		this.tupleNum=tupleNum;
		for(int i=0;i<attributes.size();i++){
			this.tupleLength+=attributes.get(i).length;
		}
	}
	
}