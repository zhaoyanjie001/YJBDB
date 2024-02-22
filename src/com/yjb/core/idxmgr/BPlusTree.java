package com.yjb.core.idxmgr;

import com.yjb.core.bufmgr.*;
import com.yjb.core.catmgr.*;
import com.yjb.core.filmgr.*;

public class BPlusTree {

	private static final int POINTERLENGTH = 4;
	private static final double BLOCKSIZE = 4096.0;
	private int MIN_CHILDREN_FOR_INTERNAL; 
    private int MAX_CHILDREN_FOR_INTERNAL;  
    private int MIN_FOR_LEAF; 
    private int MAX_FOR_LEAF;
    
    public String filename;
	public Block myRootBlock;
	public index myindexInfo;  

	public BPlusTree(index indexInfo){
		try{	
			 filename=indexInfo.indexName+".index";
			 FileManager.creatFile(filename);
		}catch(Exception e){
			 
	    }
		int columnLength=indexInfo.columnLength; 
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1-4-POINTERLENGTH-POINTERLENGTH)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		indexInfo.rootNum = 0;
		myindexInfo=indexInfo;
		myindexInfo.blockNum++;

		new LeafNode(myRootBlock=BufferManager.getBlock(filename,0)); 
	}
	
	
	public BPlusTree(index indexInfo,int rootBlockNum){
		int columnLength=indexInfo.columnLength; 
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1-4-POINTERLENGTH-POINTERLENGTH)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		myindexInfo=indexInfo;	
		filename = myindexInfo.indexName+".index";
		new LeafNode(myRootBlock=BufferManager.getBlock(filename,rootBlockNum),true); 
	}

	public void insert(byte[] originalkey,int blockOffset, int offset){
		if (originalkey == null)    throw new NullPointerException();  

		Node rootNode;

		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		Block newBlock=rootNode.insert(key, blockOffset, offset); 
    
		if(newBlock!=null){ 
			myRootBlock=newBlock;
		}
		
		myindexInfo.rootNum = myRootBlock.blockoffset;
		
	}
	
	public offsetInfo searchKey(byte[] originalkey){
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		
		return rootNode.searchKey(key); 
	}
	
	public offsetInfo searchKey(byte[] originalkey,byte[] endkey){
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] skey=new byte[myindexInfo.columnLength];
		byte[] ekey=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			skey[j]=originalkey[j];
			ekey[j]=endkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			skey[j]='&';
			ekey[j]='&';
		}
		
		
		return rootNode.searchKey(skey,ekey); 
	}

	public void delete(byte[] originalkey){
		if (originalkey == null)   
			throw new NullPointerException();  
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
	
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		Block newBlock=rootNode.delete(key);
    
		if(newBlock!=null){ 
			myRootBlock=newBlock;
		}

		myindexInfo.rootNum = myRootBlock.blockoffset;
		
	}
	
	abstract class Node {
		Block block;
		
		Node createNode(Block blk){
			block=blk;
			return this;
		}

		abstract Block insert(byte[] inserKey,int blockOffset, int offset);
		abstract Block delete(byte[] deleteKey);
		abstract offsetInfo searchKey(byte[] Key);
		abstract offsetInfo searchKey(byte[] skey, byte[] ekey);
    }
	
	public int compareTo(byte[] buffer1,byte[] buffer2) {
		
		for (int i = 0, j = 0; i < buffer1.length && j < buffer2.length; i++, j++) {
			int a = (buffer1[i] & 0xff);
			int b = (buffer2[j] & 0xff);
			if (a != b) {
				return a - b;
			}
		}
		return buffer1.length - buffer2.length;
	}
	

	class InternalNode extends Node{
		
		InternalNode(Block blk){		
			block=blk; 
	    	
	    	block.readData()[0]='I';  
			block.writeInt(1,0);
			int i=5;
			byte[] a = new byte[9];
	    	for(;i<9;i++)
	    		a[i]='$'; 
	    	block.writeData(5,a,9);
		}
		
		InternalNode(Block blk,boolean t){
			block=blk; 
		}
		
		Block insert(byte[] insertKey,int blockOffset, int offset){
			int keyNum=block.readInt(1); 
					
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				if(compareTo(insertKey, block.getBytes(pos,myindexInfo.columnLength)) < 0) break; 
			}
			

			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum); 
			
	
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true);  
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.insert(insertKey, blockOffset, offset); 
		}
		Block branchInsert(byte[] branchKey,Node leftChild,Node rightChild){
			int keyNum = block.readInt(1);
			
			if(keyNum==0){ 
				keyNum++;
				block.writeInt(1, keyNum);
				block.writeData(9+POINTERLENGTH, branchKey,branchKey.length); 
				block.writeInt(9, leftChild.block.blockoffset);
				block.writeInt(9+POINTERLENGTH+branchKey.length, rightChild.block.blockoffset);
				
				return this.block; 
			}
			
			if(++keyNum>MAX_CHILDREN_FOR_INTERNAL){  
				boolean half=false; 
		
				int newBlockOffset=myindexInfo.blockNum;
				
				myindexInfo.blockNum++;
				
				Block newBlock=BufferManager.getBlock(filename, newBlockOffset);
				InternalNode newNode=new InternalNode(newBlock);
				
				block.writeInt(1, MIN_CHILDREN_FOR_INTERNAL);
				newBlock.writeInt(1, MAX_CHILDREN_FOR_INTERNAL+1-MIN_CHILDREN_FOR_INTERNAL);
				
				for(int i=0;i<MIN_CHILDREN_FOR_INTERNAL;i++){ 
					
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength))< 0){		
						System.arraycopy(block.data, 
								9+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH));	
						System.arraycopy(block.data,  
								9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
								block.data, 
								9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
								
								(MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH)+myindexInfo.columnLength);	
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
						
						half=true;
						break;
					}
				}
				if(!half){
					System.arraycopy(block.data,  
							9+(MIN_CHILDREN_FOR_INTERNAL+1)*(myindexInfo.columnLength+POINTERLENGTH), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1)*(myindexInfo.columnLength+POINTERLENGTH));
					for(int i=0;i<MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1;i++){
						int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
						
						if(compareTo(branchKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data, 
									9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),
									newBlock.data, 
									9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
									(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH));								
							
							
							newBlock.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
							break;							
						}	
					}
				}
				
				
				byte[] spiltKey=block.getBytes(9+POINTERLENGTH+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH),
						myindexInfo.columnLength);
				
				
				for(int j=0;j<=newBlock.readInt(1);j++){
					int childBlockNum=newBlock.readInt(9+j*(myindexInfo.columnLength+POINTERLENGTH));
					BufferManager.getBlock(filename, childBlockNum).writeInt(5, newBlockOffset);					
				}	
				
				int parentBlockNum;
				Block ParentBlock;
				InternalNode ParentNode;
				if(block.readData()[5]=='$'){  
					
					parentBlockNum=myindexInfo.blockNum;
					
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					
					
					myindexInfo.blockNum++;
					
					block.writeInt(5, parentBlockNum);
					newBlock.writeInt(5,parentBlockNum);
					
					ParentNode=new InternalNode(ParentBlock);
				}
				else{
					parentBlockNum=block.readInt(5);				
					newBlock.writeInt(5, parentBlockNum);	
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);	
					ParentNode=new InternalNode(ParentBlock,true);
				}
						
				return  ParentNode.branchInsert(spiltKey, this, newNode);
			}
			
			else{  
				int i;
				for(i=0;i<keyNum-1;i++){
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){ 
						System.arraycopy(block.data,
										9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
										block.data, 
										9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH), 
										(keyNum-1-i)*(myindexInfo.columnLength+POINTERLENGTH));
						
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);									
						block.writeInt(1,keyNum);
						
						return null;
					}					
				}
				if(i==keyNum-1){				
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);									
						block.writeInt(1,keyNum);
						
						return null;							
				}
			}
						
			return null;
		}
		
		
		offsetInfo searchKey(byte[] key){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(key,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(key); 
		}
		
		offsetInfo searchKey(byte[] skey,byte[] ekey){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(skey,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);

			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(skey,ekey);
		}

		Block delete(byte[] deleteKey){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.delete(deleteKey); 
		}
	
		Block union(byte[] unionKey,Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			System.arraycopy(afterBlock.data,
					9,
					block.data,
					9+(keyNum+1)*(myindexInfo.columnLength+POINTERLENGTH),
					POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+POINTERLENGTH));
			
			block.writeData(9+keyNum*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, unionKey,unionKey.length);
			
			keyNum=keyNum+afterkeyNum+1;		
			block.writeInt(1, keyNum);
			
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			myindexInfo.blockNum--;
			
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		byte[] rearrangeAfter(Block siblingBlock,byte[] InternalKey){ 
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			int blockOffset=siblingBlock.readInt(9);
			block.writeInternalKey(9+POINTERLENGTH+keyNum*(myindexInfo.columnLength+POINTERLENGTH), InternalKey, blockOffset);
			keyNum++;
			block.writeInt(1, keyNum);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH, myindexInfo.columnLength);
			System.arraycopy(siblingBlock.data, 9+POINTERLENGTH+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength));
					
			return changeKey;
			
		}

		byte[] rearrangeBefore(Block siblingBlock,byte[] internalKey){ 
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength), myindexInfo.columnLength);		
			int blockOffset=siblingBlock.readInt(9+(siblingKeyNum+1)*(POINTERLENGTH+myindexInfo.columnLength));
			
			System.arraycopy(block.data, 9, block.data, 9+POINTERLENGTH+myindexInfo.columnLength, POINTERLENGTH+keyNum*(POINTERLENGTH+myindexInfo.columnLength));
			block.writeInt(9, blockOffset); 
			block.writeData(9+POINTERLENGTH, internalKey, internalKey.length); 
			keyNum++;
			block.writeInt(1, keyNum);
					
			return changeKey;
		}

		public void exchange(byte[] changeKey,int posBlockNum){
			int keyNum = block.readInt(1);
			
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+i*(myindexInfo.columnLength+POINTERLENGTH);
				int blockNum=block.readInt(pos);
				if(blockNum==posBlockNum) break;
			}
			
			if(i<keyNum) block.writeData(9+i*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, changeKey, changeKey.length);
		}
		
		Block	delete(Block blk){
			int keyNum = block.readInt(1);
			
			for(int i=0;i<=keyNum;i++){
				int pos=9+i*(myindexInfo.columnLength+POINTERLENGTH);
				int ptr=block.readInt(pos);
				if(ptr==blk.blockoffset){ 
					System.arraycopy(block.data, 
							9+POINTERLENGTH+(i-1)*(myindexInfo.columnLength+POINTERLENGTH), 
							block.data, 
							9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
							(keyNum-i)*(myindexInfo.columnLength+POINTERLENGTH));
					keyNum--;
					block.writeInt(1, keyNum);
			
					if(keyNum >=MIN_CHILDREN_FOR_INTERNAL) return null; 
			
					if(block.readData()[5]=='$'){ 
						
						if(keyNum==0){	
							myindexInfo.blockNum--;
							return BufferManager.getBlock(filename, block.readInt(9));
						}
							
						return null;
					}
			
					int parentBlockNum=block.readInt(5);
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					int parentKeyNum=parentBlock.readInt(1);
					
					int sibling;
					Block siblingBlock;
					int j=0;
					for(;j<parentKeyNum;j++){
						int ppos=9+j*(myindexInfo.columnLength+POINTERLENGTH);
						if(block.blockoffset==parentBlock.readInt(ppos)){ 
							
							sibling=parentBlock.readInt(ppos+POINTERLENGTH+myindexInfo.columnLength);
							siblingBlock=BufferManager.getBlock(filename, sibling);
								
							byte[] unionKey=parentBlock.getBytes(ppos+POINTERLENGTH, myindexInfo.columnLength);
							
							if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){				
								return this.union(unionKey,siblingBlock);
							}
							
							if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
							
							(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock,unionKey),block.blockoffset);
							return null;
					
						}				
					}
					
					sibling=parentBlock.readInt(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH));
					siblingBlock=BufferManager.getBlock(filename, sibling);		
								
					byte[] unionKey=parentBlock.getBytes(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, myindexInfo.columnLength);
					
					if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){		
						return (new InternalNode(siblingBlock,true)).union(unionKey,block);
					}
						
					if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
					
					(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock,unionKey),sibling);
					return null;
				}
	
			}		
			return null;
		}
				
	}
	class LeafNode extends Node{
				
		LeafNode(Block blk){
			block=blk;
			
	    	block.data[0]='L';  
	    	int i=5;
			block.writeInt(1, 0);
	    	for(;i<9;i++)
	    		block.data[i]='$';  
	    	for(;i<13;i++)
	    		block.data[i]='&';  
	    	block.writeData();
		}
		
		LeafNode(Block blk,boolean t){
			block=blk;	
		}

		Block insert(byte[] insertKey,int blockOffset, int offset){
			int keyNum = block.readInt(1);
			
			if(++keyNum>MAX_FOR_LEAF){  
				boolean half=false;
				Block newBlock=BufferManager.getBlock(filename, myindexInfo.blockNum);
				myindexInfo.blockNum++;
				LeafNode newNode=new LeafNode(newBlock);
				
				for(int i=0;i<MIN_FOR_LEAF-1;i++){ 
					int pos=17+i*(myindexInfo.columnLength+8);
					if(compareTo( insertKey,block.getBytes(pos,myindexInfo.columnLength))< 0){					
						System.arraycopy(block.data,  
								9+(MIN_FOR_LEAF-1)*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF+1)*(myindexInfo.columnLength+8));	
						System.arraycopy(block.data,  
								9+i*(myindexInfo.columnLength+8), 
								block.data, 
								9+(i+1)*(myindexInfo.columnLength+8),
								POINTERLENGTH+(MIN_FOR_LEAF-1-i)*(myindexInfo.columnLength+8));	
						
						
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);				
											
						half=true;
						break;
					}
				}				
				if(!half){ 
					System.arraycopy(block.data, 
							9+(MIN_FOR_LEAF)*(myindexInfo.columnLength+8), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF)*(myindexInfo.columnLength+8));
					int i=0;
					for(;i<MAX_FOR_LEAF-MIN_FOR_LEAF;i++){
						int pos=17+i*(myindexInfo.columnLength+8);
						if(compareTo(insertKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data, 
									9+i*(myindexInfo.columnLength+8), 
									newBlock.data, 
									9+(i+1)*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
							
							newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
							break;
						}	
					}
					if(i==MAX_FOR_LEAF-MIN_FOR_LEAF){
						System.arraycopy(newBlock.data, 
								9+i*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9+(i+1)*(myindexInfo.columnLength+8), 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
						
						newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
					}
				}
				
				block.writeInt(1,MIN_FOR_LEAF);
			    newBlock.writeInt(1,MAX_FOR_LEAF+1-MIN_FOR_LEAF);
			    
			    block.writeInt(9+MIN_FOR_LEAF*(myindexInfo.columnLength+8), newBlock.blockoffset);
				
			    int parentBlockNum;
			    Block ParentBlock;
			    InternalNode ParentNode;
				if(block.readData()[5]=='$'){  
					parentBlockNum=myindexInfo.blockNum;
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
				
					
					myindexInfo.blockNum++;
		
					block.writeInt(5, parentBlockNum);
					newBlock.writeInt(5, parentBlockNum );
					ParentNode=new InternalNode(ParentBlock);
				}
				else{
					parentBlockNum=block.readInt(5);				
					newBlock.writeInt(5, parentBlockNum);
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					ParentNode=new InternalNode(ParentBlock,true);
				}
			
				byte[] branchKey=newBlock.getBytes(17, myindexInfo.columnLength);
				
				return  ParentNode.branchInsert(branchKey, this, newNode);
			}
			
			else{ 
				if(keyNum-1==0){
					System.arraycopy(block.data,
							9, 
							block.data, 
							9+(myindexInfo.columnLength+8), 
							POINTERLENGTH);
			
					block.setKeydata(9,insertKey,blockOffset,offset);						
					block.writeInt(1, keyNum);
			
					return null;
				}
				int i; 
				for(i=0;i<keyNum;i++){
					int pos=17+i*(myindexInfo.columnLength+8);
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength))==0){
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
						return null;
					}
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){
						System.arraycopy(block.data,
										9+i*(myindexInfo.columnLength+8), 
										block.data, 
										9+(i+1)*(myindexInfo.columnLength+8), 
										POINTERLENGTH+(keyNum-1-i)*(myindexInfo.columnLength+8));
						
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);						
						block.writeInt(1, keyNum);
						
						return null;
					}					
				}
				if(i==keyNum){
					System.arraycopy(block.data,
							9+(i-1)*(myindexInfo.columnLength+8), 
							block.data, 
							9+i*(myindexInfo.columnLength+8), 
							POINTERLENGTH);
			
					block.setKeydata(9+(i-1)*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);						
					block.writeInt(1, keyNum);
			
					return null;
				}
			}
		    return null;		
		}
		
	
		offsetInfo searchKey(byte[] originalkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; 
		
			byte[] key=new byte[myindexInfo.columnLength];
			
			int i=0;
			for(;i<originalkey.length;i++){
				key[i]=originalkey[i];
			}
			
		    for(;i<myindexInfo.columnLength;i++){
				key[i]='&';
			}
			
			
			int start=0;
			int end=keyNum-1;
			int middle=0;

			while (start <= end) {  

				middle = (start + end) / 2;
								
                byte[] middleKey = block.getBytes(17+middle*(myindexInfo.columnLength+8), myindexInfo.columnLength);  
                if (compareTo(key,middleKey) == 0){  
                    break;  
                }  
                  
                if (compareTo(key,middleKey) < 0) {  
                    end = middle-1;  
                } else {  
                    start = middle+1;  
                }  
                
            }  
              			
			int pos=9+middle*(myindexInfo.columnLength+8);
			byte[] middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
			
			
            offsetInfo off=new offsetInfo();
            
            off.offsetInfile.add(block.readInt(pos));
            off.offsetInBlock.add(block.readInt(pos+4));
            off.length=1;
					
            return compareTo(middleKey,key) == 0 ? off : null;  
		}
		
		offsetInfo searchKey(byte[] originalkey, byte[] endkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; 
		
			byte[] key=new byte[myindexInfo.columnLength];
			byte[] ekey=new byte[myindexInfo.columnLength];
			
			int i=0;
			for(;i<originalkey.length;i++){
				key[i]=originalkey[i];
				ekey[i]=endkey[i];
			}
			
		    for(;i<myindexInfo.columnLength;i++){
				key[i]='&';
				ekey[i]='&';
			}
			
			int start=0;
			int end=keyNum-1;
			int middle=0;

			while (start <= end) {  

				middle = (start + end) / 2;
								
                byte[] middleKey = block.getBytes(17+middle*(myindexInfo.columnLength+8), myindexInfo.columnLength);  
                if (compareTo(key,middleKey) == 0){  
                    break;  
                }  
                  
                if (compareTo(key,middleKey) < 0) {  
                    end = middle-1;  
                } else {  
                    start = middle+1;  
                }  
                
            }  
              			
			int pos=9+middle*(myindexInfo.columnLength+8);
			byte[] middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
					
            if(compareTo(middleKey,key) != 0) return null;   
            else{
                offsetInfo off=new offsetInfo();
                while(compareTo(middleKey,ekey)<=0){
                    off.offsetInfile.add(block.readInt(pos));
                    off.offsetInBlock.add(block.readInt(pos+4));
                    middle+=1;
                    off.length++;
                    if(middle>=keyNum){
                    	if(block.readString(9+keyNum*(8+myindexInfo.columnLength)).equals("&&&&")){
                    		break;
                    	}
                    	block = BufferManager.getBlock(filename,
                    			block.readInt(9+keyNum*(8+myindexInfo.columnLength)));
                    	keyNum=block.readInt(1); 
                    	middle=0;
                    	pos=9;
                    	middleKey = block.getBytes(8+pos, myindexInfo.columnLength);
                    }
                    else{
                    	pos=9+middle*(myindexInfo.columnLength+8);
                    	middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
                    }
                }
                
                return off;
            }
		}

		

		Block union(Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			System.arraycopy(afterBlock.data,9,block.data,9+keyNum*(myindexInfo.columnLength+8),POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+8));
			
			keyNum+=afterkeyNum;		
			block.writeInt(1, keyNum);
						
			
			myindexInfo.blockNum--;
			
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		byte[] rearrangeAfter(Block siblingBlock){ 
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			int blockOffset=siblingBlock.readInt(9);
			int offset=siblingBlock.readInt(13);
			byte[] Key=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			System.arraycopy(siblingBlock.data, 9+8+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(8+myindexInfo.columnLength));
			
			byte[] changeKey=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			block.setKeydata(9+keyNum*(myindexInfo.columnLength+8), Key, blockOffset, offset);
			keyNum++;
			block.writeInt(1, keyNum);
			block.writeInt(9+keyNum*(myindexInfo.columnLength+8), siblingBlock.blockoffset);
			
			return changeKey;
			
		}
		
		byte[] rearrangeBefore(Block siblingBlock){  
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			
			int blockOffset=siblingBlock.readInt(9+siblingKeyNum*(myindexInfo.columnLength+8));
			int offset=siblingBlock.readInt(13+siblingKeyNum*(myindexInfo.columnLength+8));
			byte[] Key=siblingBlock.getBytes(17+siblingKeyNum*(myindexInfo.columnLength+8), myindexInfo.columnLength);
			
			siblingBlock.writeInt(9+siblingKeyNum*(myindexInfo.columnLength+8), block.blockoffset);
			
			System.arraycopy(block.data, 9, block.data, 9+8+myindexInfo.columnLength, POINTERLENGTH+keyNum*(8+myindexInfo.columnLength));
			block.setKeydata(9, Key, blockOffset, offset);
			keyNum++;
			block.writeInt(1, keyNum);
			
			byte[] changeKey=block.getBytes(17, myindexInfo.columnLength);
			
			return changeKey;
		}
		
		Block delete(byte[] deleteKey){
			
			int keyNum = block.readInt(1);
			
			for(int i=0;i<keyNum;i++){
				int pos=17+i*(myindexInfo.columnLength+8);
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength))<0){ 
			
					return null;
				}
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength)) == 0){ 					
					
					System.arraycopy(block.data,
									9+(i+1)*(myindexInfo.columnLength+8), 
									block.data, 
									9+i*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(keyNum-1-i)*(myindexInfo.columnLength+8));
					keyNum--;
					block.writeInt(1, keyNum);
					
					if(keyNum >=MIN_FOR_LEAF) return null; 
					
					if(block.readData()[5]=='$') return null;  
					
					boolean lastFlag=false;
					if(block.readData()[9+keyNum*(myindexInfo.columnLength+8)]=='&') lastFlag=true; 
					
					int sibling=block.readInt(9+keyNum*(myindexInfo.columnLength+8)); 
					Block siblingBlock=BufferManager.getBlock(filename, sibling);
					int parentBlockNum=block.readInt(5);
					
					if(lastFlag || siblingBlock==null || siblingBlock.readInt(5)!=parentBlockNum){ 
						
						Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
						int j=0;
						int parentKeyNum=parentBlock.readInt(1);
						for(;j<parentKeyNum;j++){
							int ppos=9+POINTERLENGTH+j*(myindexInfo.columnLength+POINTERLENGTH);
							if(compareTo(deleteKey,parentBlock.getBytes(ppos, myindexInfo.columnLength))<0){
								sibling=parentBlock.readInt(ppos-2*POINTERLENGTH-myindexInfo.columnLength);
								siblingBlock=BufferManager.getBlock(filename, sibling);
								break;
							}
						}
						
						
						if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
							return (new LeafNode(siblingBlock,true)).union(block);
						}
									
						
						if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
							
						(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock),sibling);
						return null;
					}
			
					if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
						return this.union(siblingBlock);
					}
					
					if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
					
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock),block.blockoffset);
					return null;
				}
			}
			
			return null;
		}
	}
}