package com.yjb.core.catmgr;

import java.io.*;
import java.util.*;

import com.yjb.core.utils.PropUtils;

public class CatalogManager {
	private static Hashtable<String,Table> tables=new Hashtable<String, Table>() ;
	private static Hashtable<String,Index> indexes=new Hashtable<String, Index>();
	
	public static void InitialCatalog() throws IOException {
		InitialTableCatalog();
		InitialIndexCatalog();
	}
	private static void InitialIndexCatalog() throws IOException  {
		File file=new File(PropUtils.getValue("indexFileName"));
		if(!file.exists()) return;
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);	
		String tmpIndexName,tmpTableName,tmpAttriName;	
		int tmpIndexBlockNum,tmpRootNum;	
		while(dis.available()>0) {
			tmpIndexName=dis.readUTF();
			tmpTableName=dis.readUTF();
			tmpAttriName=dis.readUTF();
			tmpIndexBlockNum=dis.readInt();
			tmpRootNum = dis.readInt();
			indexes.put(tmpIndexName, new Index(tmpIndexName,tmpTableName,tmpAttriName,tmpIndexBlockNum,tmpRootNum));				
		}		
		dis.close();		
			
	}

	private static void InitialTableCatalog() throws IOException {
		File file = new File(PropUtils.getValue("tableFileName"));
		if (!file.exists())
			return;
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);
		String tmpTableName, tmpPriKey;
		int tmpIndexNum, tmpAttriNum, tmpTupleNum;

		while (dis.available() > 0) {
			Vector<Attribute> tmpAttributes = new Vector<Attribute>();
			Vector<Index> tmpIndexes = new Vector<Index>();
			tmpTableName = dis.readUTF();
			tmpPriKey = dis.readUTF();
			tmpTupleNum = dis.readInt();
			tmpIndexNum = dis.readInt();
			for (int i = 0; i < tmpIndexNum; i++) {
				String tmpIndexName, tmpAttriName;
				tmpIndexName = dis.readUTF();
				tmpAttriName = dis.readUTF();
				tmpIndexes.addElement(new Index(tmpIndexName, tmpTableName, tmpAttriName));
			}
			tmpAttriNum = dis.readInt();
			for (int i = 0; i < tmpAttriNum; i++) {
				String tmpAttriName, tmpType;
				int tmpLength;
				boolean tmpIsU;
				tmpAttriName = dis.readUTF();
				tmpType = dis.readUTF();
				tmpLength = dis.readInt();
				tmpIsU = dis.readBoolean();
				tmpAttributes.addElement(new Attribute(tmpAttriName, tmpType, tmpLength, tmpIsU));
			}
			tables.put(tmpTableName, new Table(tmpTableName, tmpAttributes, tmpIndexes, tmpPriKey, tmpTupleNum));

		}
		dis.close();
	}

	public static void storeCatalog() throws IOException{
		storeTableCatalog();
		storeIndexCatalog();
	}
	private static void storeIndexCatalog() throws IOException {
		// TODO Auto-generated method stub		
		
		File file=new File(PropUtils.getValue("indexFileName"));
		if(file.exists())file.delete();
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);	
		Index tmpIndex;
		Enumeration<Index> en = indexes.elements();
		while(en.hasMoreElements()) {
			tmpIndex=en.nextElement();	
			dos.writeUTF(tmpIndex.indexName);
			dos.writeUTF(tmpIndex.tableName);
			dos.writeUTF(tmpIndex.attriName);
			dos.writeInt(tmpIndex.blockNum);
			dos.writeInt(tmpIndex.rootNum);
		}
	
		dos.close();				
	}
	private static void storeTableCatalog() throws IOException {
		// TODO Auto-generated method stub
		File file=new File(PropUtils.getValue("tableFileName"));
		//if(file.exists())file.d;
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);	
		Table tmpTable;
		Enumeration<Table> en = tables.elements();
        while(en.hasMoreElements()) {
        	tmpTable=en.nextElement();
        	dos.writeUTF(tmpTable.tableName);
        	dos.writeUTF(tmpTable.primaryKey);
        	dos.writeInt(tmpTable.tupleNum);
        	dos.writeInt(tmpTable.indexNum);
        	for(int i=0;i<tmpTable.indexNum;i++){
        		Index tmpIndex=tmpTable.indexes.get(i);
        		dos.writeUTF(tmpIndex.indexName);
        		dos.writeUTF(tmpIndex.attriName);
           	}
        	dos.writeInt(tmpTable.attriNum);
        	for(int i=0;i<tmpTable.attriNum;i++){
        		Attribute tmpAttri=tmpTable.attributes.get(i);
        		dos.writeUTF(tmpAttri.attriName);
        		dos.writeUTF(tmpAttri.type);
        		dos.writeInt(tmpAttri.length);
        		dos.writeBoolean(tmpAttri.isUnique);
        	}
        }
		dos.close();
	}
	
	public static void showCatalog(){
		showTableCatalog();
		System.out.println();
		showIndexCatalog();
	}
	public static void showIndexCatalog() {
		// TODO Auto-generated method stub
		Index tmpIndex;
		Enumeration<Index> en = indexes.elements();
		int cnt=1;
		System.out.println("There are "+indexes.size()+" indexes in the database: ");
        System.out.println("\tIndex name\tTable name\tAttribute name:");
		while(en.hasMoreElements()) {
			tmpIndex=en.nextElement();			
			System.out.println(cnt+++"\t"+tmpIndex.indexName+"\t\t"+tmpIndex.tableName+"\t\t"+tmpIndex.attriName);
		}
	}
	public static void showTableCatalog() {
		// TODO Auto-generated method stub
		Table tmpTable;
		Index tmpIndex;
		Attribute tmpAttribute;
		Enumeration<Table> en = tables.elements();
		int cnt=1;
		System.out.println("There are "+tables.size()+" tables in the database: ");
        while(en.hasMoreElements()) {
           tmpTable=en.nextElement();
           System.out.println("\nTable "+cnt++);
           System.out.println("Table name: "+tmpTable.tableName);
           System.out.println("Number of Columns: "+tmpTable.attriNum);
           System.out.println("Primary key: "+tmpTable.primaryKey);
           System.out.println("Number of tuples: "+tmpTable.tupleNum);
           System.out.println("Index keys: "+tmpTable.indexNum);
           System.out.println("\tIndex name\tTable name\tAttribute name:");
           for(int i=0;i<tmpTable.indexNum;i++){
        	   tmpIndex=tmpTable.indexes.get(i);
        	   System.out.println("\t"+tmpIndex.indexName+"\t"+tmpIndex.tableName+"\t\t"+tmpIndex.attriName);
           }
           System.out.println("Attributes: "+tmpTable.attriNum);
           System.out.println("\tAttribute name\tType\tlength\tisUnique");
           for(int i=0;i<tmpTable.attriNum;i++){
        	   tmpAttribute=tmpTable.attributes.get(i);
        	   System.out.println("\t"+tmpAttribute.attriName+"\t\t"+tmpAttribute.type+"\t"+tmpAttribute.length+"\t"+tmpAttribute.isUnique);
           }
        }      		
	}
	public static Table getTable(String tableName){
		return tables.get(tableName);
	}
	public static Index getIndex(String indexName){
		return indexes.get(indexName);
	}	
	public static String getPrimaryKey(String tableName) {
		return getTable(tableName).primaryKey;
	}
	public static int getTupleLength(String tableName){
		return getTable(tableName).tupleLength;
	}
	public static int getTableAttriNum(String tableName){
		return getTable(tableName).attriNum;
	}
	public static int getTupleNum(String tableName){
		return getTable(tableName).tupleNum;
	}
	public static boolean isPrimaryKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			Table tmpTable=getTable(tableName);
			if(tmpTable.primaryKey.equals(attriName))return true;
			else return false;
		}
		else{
			System.out.println("The table "+tableName+" doesn't exist");
			return false;
		}
	}
	public static boolean inUniqueKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			Table tmpTable=getTable(tableName);
			int i;
			for(i=0;i<tmpTable.attributes.size();i++){
				Attribute tmpAttribute=tmpTable.attributes.get(i);
				if(tmpAttribute.attriName.equals(attriName)){
					return tmpAttribute.isUnique;
				}
			}
			if(i>=tmpTable.attributes.size()){
				System.out.println("The attribute "+attriName+" doesn't exist");
				return false;
			}
		}
		System.out.println("The table "+tableName+" doesn't exist");
		return false;
		
	}
	public static boolean isIndexKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			Table tmpTable=getTable(tableName);
			if(isAttributeExist(tableName,attriName)){
				for(int i=0;i<tmpTable.indexes.size();i++){
					if(tmpTable.indexes.get(i).attriName.equals(attriName))
						return true;
				}
				//System.out.println(" The attribute "+attriName+" is not an index key");留给interpreter
			}
			else{
				System.out.println("The attribute "+attriName+" doesn't exist");
			}
		}
		else
			System.out.println("The table "+tableName+" doesn't exist");
		return false;	
	}
	public static boolean isTableExist(String tableName){
		return tables.containsKey(tableName);
			}
	public static boolean isIndexExist(String indexName){
		return indexes.containsKey(indexName);
	}
	public static boolean isAttributeExist(String tableName,String attriName){
		Table tmpTable=getTable(tableName);
		for(int i=0;i<tmpTable.attributes.size();i++){
			if(tmpTable.attributes.get(i).attriName.equals(attriName))
				return true;
		}
		return false;
	}
	public static String getIndexName(String tableName,String attriName){
		if(isTableExist(tableName)){
			Table tmpTable=getTable(tableName);
			if(isAttributeExist(tableName,attriName)){
				for(int i=0;i<tmpTable.indexes.size();i++){
					if(tmpTable.indexes.get(i).attriName.equals(attriName))
						return tmpTable.indexes.get(i).indexName;
				}
			}
			else{
				System.out.println("The attribute "+attriName+" doesn't exist");
			}
		}
		else
			System.out.println("The table "+tableName+" doesn't exist");
		return null;	
	}
	public static String getAttriName(String tableName,int i){
		return tables.get(tableName).attributes.get(i).attriName;
	}
	public static int getAttriOffest(String tableName,String attriName){
		Table tmpTable=tables.get(tableName);
		Attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return i;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return -1;
	}
	public static String getType(String tableName,String attriName){//用于where
		Table tmpTable=tables.get(tableName);
		Attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return tmpAttri.type;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return null;
	}
	public static int getLength(String tableName,String attriName){//用于where
		Table tmpTable=tables.get(tableName);
		Attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return tmpAttri.length;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return -1;
	}
	public static String getType(String tableName,int i){
		Table tmpTable=tables.get(tableName);
		//System.out.println(tmpTable.attributes.get(i).type+tmpTable.attributes.get(i).attriName);
		return tmpTable.attributes.get(i).type;
	}
	public static int getLength(String tableName,int i){
		Table tmpTable=tables.get(tableName);
		return tmpTable.attributes.get(i).length;
	}


	public static void addTupleNum(String tableName){
		tables.get(tableName).tupleNum++;
	}
	public static void deleteTupleNum(String tableName,int num){
		tables.get(tableName).tupleNum-=num;
	}
	public static boolean updateIndexTable(String indexName,Index indexinfo){
		indexes.replace(indexName, indexinfo);
		return true;
	}
	public static boolean isAttributeExist(Vector<Attribute> attributes, String attriName) {
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i).attriName.equals(attriName))
				return true;
		}
		return false;
	} 
	
	public static boolean createTable(Table newTable){		
		try{
			tables.put(newTable.tableName, newTable);
			//indexes.put(newTable.indexes.firstElement().indexName, newTable.indexes.firstElement());
			return true;		
		}
		catch(NullPointerException e){
			e.printStackTrace();
			return false;
		}
		
	}

	public static boolean dropTable(String tableName){
		try{
			Table tmpTable=tables.get(tableName);
			for(int i=0;i<tmpTable.indexes.size();i++){ 
				indexes.remove(tmpTable.indexes.get(i).indexName);
			}			
			tables.remove(tableName);
			return true;
		}
		catch(NullPointerException e){
			System.out.println("Error: drop null table. "+e.getMessage());
			return false;
		}
	}

	public static boolean createIndex(Index newIndex){
		try{
		Table tmpTable=getTable(newIndex.tableName);
		//更新tableCatalog
		tmpTable.indexes.addElement(newIndex);
		tmpTable.indexNum=tmpTable.indexes.size();
		//更新indexCatalog
		indexes.put(newIndex.indexName, newIndex);
		return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static boolean dropIndex(String indexName){
	
		try{
			Index tmpIndex=getIndex(indexName);
			Table tmpTable=getTable(tmpIndex.tableName);				
			tmpTable.indexes.remove(tmpIndex) ;
			tmpTable.indexNum=tmpTable.indexes.size();
			indexes.remove(indexName);
			return true;
		}		
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}


}