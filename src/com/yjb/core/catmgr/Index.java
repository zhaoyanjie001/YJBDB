package com.yjb.core.catmgr;

public class Index{
	public String indexName;
	public String tableName;
	public String attriName;
	public int column;		//on which column the index is created
	public int columnLength;//
	public int rootNum;
	public int blockNum=0;	//number of block the datas of the index occupied in the file index_name.table

	public Index(String indexName,String tableName,String attriName, int blockNum, int rootNum){
		this.indexName=indexName;
		this.tableName=tableName;
		this.attriName=attriName;
		this.blockNum = blockNum;
		this.rootNum = rootNum;
	}

	public Index(String indexName,String tableName,String attriName){
		this.indexName=indexName;
		this.tableName=tableName;
		this.attriName=attriName;
	}
	public void PickInfo(){
		column = CatalogManager.getAttriOffest(tableName, attriName);
		columnLength = CatalogManager.getLength(tableName, attriName);
	}
}