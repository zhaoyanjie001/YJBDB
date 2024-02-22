package com.yjb.core.idxmgr;


import java.io.*;
import java.util.Vector;

import com.yjb.core.bufmgr.*;
import com.yjb.core.catmgr.*;
import com.yjb.core.filmgr.*;
import com.yjb.core.recmgr.*;

public class IndexManager{
	 

	public static boolean createIndex(index indexInfo){

    	indexInfo.PickInfo();
        	BPlusTree thisTree=new BPlusTree(indexInfo/*,buf*/); 
        	String tableName=indexInfo.tableName;
        	try{   	
        		int tinb = Block.BLOCKSIZE
    				/ (4 + CatalogManager.getTupleLength(tableName));
        		int offset=1,count=1,blockOffset=0;
        		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
        		byte[] bkey = null;
        		while(count<=CatalogManager.getTupleNum(tableName)){
            		tuple k = RecordManager.getTuple(tableName,offset);
            		if(offset%tinb==0)
            			blockOffset++;
            		if(k==null) continue;
            		count++;
            		String key = k.units.elementAt(CatalogManager.getAttriOffest(tableName, indexInfo.attriName));
            		if (type.equals("int")) {
            			bkey = StringInttoByte(key);
            		} else if (type.equals("float")) {
            			bkey = StringFloattoByte(key);
            		} else if (type.equals("char")) {
            			bkey = key.getBytes();
            		}
            		thisTree.insert(bkey,blockOffset,offset);
            		offset++;
        		}
        		
        	}catch(NullPointerException e){
        		//System.err.println("must not be null for key.");
        		return false;
        	}
        	catch(Exception e){
        		//System.err.println("the index has not been created.");
        		return false;
        	}
        	
        	
        	return true;
	}
	
	public static boolean dropIndex(String filename ){
		filename+=".index";
		File file = new File(filename);
		
		try{
			if(file.exists())
				if(file.delete())   {
					System.out.println("index deleted");
					return true;}
			else
				
				return false;
        }catch(Exception   e){
            System.out.println(e.getMessage());
            //System.out.println("deleted failed");
            return false;
        }

		return true;
	}
	
	public static Integer searchEqual(index indexInfo, byte[] key) throws Exception{
    	indexInfo.PickInfo();
		offsetInfo off=new offsetInfo();
		try{
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum); 
			off=thisTree.searchKey(key);  
			if(off==null) return null;
			else{
				return new Integer(off.offsetInfile.elementAt(0));
			}
		}catch(NullPointerException e){
			System.err.println();
			return null;
		}
	}
	
	public static Vector<Integer> searchRange(index indexInfo,String startkey, String endkey) throws Exception{
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] skey = null;
		byte[] ekey = null;
		if (type.equals("int")) {
			skey = StringInttoByte(startkey);
			ekey = StringInttoByte(endkey);
		} else if (type.equals("float")) {
			skey = StringFloattoByte(startkey);
			ekey = StringFloattoByte(endkey);
		} else if (type.equals("char")) {
			skey = startkey.getBytes();
			ekey = endkey.getBytes();
		}
    	indexInfo.PickInfo();
		offsetInfo off=new offsetInfo();
		Vector<Integer> res = new Vector<Integer>();
		try{
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);
			off=thisTree.searchKey(skey,ekey); 
			if(off==null) return null;
			else{
				for(int i=0;i<off.length;i++){
					res.add((off.offsetInBlock.elementAt(i)));
				}
				return res;
			}
		}catch(NullPointerException e){
			System.err.println();
			return null;
		}
	}
	
	static public void insertKey(index indexInfo,String key,int blockOffset,int offset) throws Exception{
    	indexInfo.PickInfo();
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] bkey = null;
		try{
			if (type.equals("int")) {
    			bkey = StringInttoByte(key);
    		} else if (type.equals("float")) {
    			bkey = StringFloattoByte(key);
    		} else if (type.equals("char")) {
    			bkey = key.getBytes();
    		}
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);
			thisTree.insert(bkey, blockOffset, offset);	
		}catch(NullPointerException e){
			System.err.println();
		}
		
	}
	
	static public void deleteKey(index indexInfo,String deleteKey) throws Exception{
    	indexInfo.PickInfo();
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] bkey = null;
		try{
			if (type.equals("int")) {
    			bkey = StringInttoByte(deleteKey);
    		} else if (type.equals("float")) {
    			bkey = StringFloattoByte(deleteKey);
    		} else if (type.equals("char")) {
    			bkey = deleteKey.getBytes();
    		}
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);
			thisTree.delete(bkey);	
		}catch(NullPointerException e){
			System.err.println();
		}
		
	}

	public static byte[] StringInttoByte(String num) {
		Integer j = new Integer(num);
		int i = j;
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		byte[] data = new byte[4];
		try {
			doutput.writeInt(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[0] = temp[0];
		data[1] = temp[1];
		data[2] = temp[2];
		data[3] = temp[3];
		return data;
	}
	
	public static byte[] StringFloattoByte(String num){
		Float j = new Float(num);
		float i = j;
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		byte[] data = new byte[4];
		try {
			doutput.writeFloat(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[0] = temp[0];
		data[1] = temp[1];
		data[2] = temp[2];
		data[3] = temp[3];
		return data;
	}
}