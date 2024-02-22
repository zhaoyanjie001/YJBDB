package com.yjb.core.catmgr;

import java.util.Vector;

public class Table{
	String tableName;			
	String primaryKey;			
	Vector<Attribute>attributes;
	Vector<Index> indexes;			
	int indexNum;				
	int attriNum;				
	int tupleNum;				
	int tupleLength;			

	public Table(String tableName,Vector<Attribute> attributes,String primaryKey){
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.indexes=new Vector<Index>();				
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

	public Table(String tableName, Vector<Attribute> attributes, Vector<Index> indexes, String primaryKey,int tupleNum) {//initial table
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