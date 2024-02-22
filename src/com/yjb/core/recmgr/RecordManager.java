package com.yjb.core.recmgr;

import java.util.Comparator;
import java.util.Vector;

import com.yjb.core.bufmgr.*;
import com.yjb.core.catmgr.*;
import com.yjb.core.filmgr.*;
import com.yjb.core.recmgr.*;
import com.yjb.core.idxmgr.*;
import com.yjb.core.common.Constants;
public class RecordManager {
	static final char EMPTY = 0;

	
	public static Vector<Tuple> getTuple(String tablename,
			Vector<Integer> tupleOffsets) {
		final int tinb = Constants.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));
		Vector<Tuple> res = new Vector<Tuple>(0);
		for (int ii = 0; ii < tupleOffsets.size(); ii++) {
			int blockoffset = tupleOffsets.elementAt(ii) / tinb;
			Block block = BufferManager.getBlock(tablename, blockoffset);
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename))
					* (tupleOffsets.elementAt(ii) % tinb);
			if (block.readInt(byteoffset) >= 0)
				continue;
			byteoffset += 4;
			Tuple T = new Tuple();
			
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}
			res.add(T);
		}
		return res;
	}

		public static Tuple getTuple(String tablename, int tupleOffset) {
		final int tinb = Constants.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));
		int blockoffset = tupleOffset / tinb;
		Block block = BufferManager.getBlock(tablename, blockoffset);
		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleOffset % tinb);
		if (block.readInt(byteoffset) >= 0)
			return null;
		byteoffset += 4;
		Tuple T = new Tuple();
		for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
			if (CatalogManager.getType(tablename, i).equals("int")) {
				T.units.add(i, String.valueOf(block.readInt(byteoffset)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("float")) {
				T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("char")) {
				T.units.add(
						i,
						block.readString(byteoffset,
								CatalogManager.getLength(tablename, i)));
				byteoffset += CatalogManager.getLength(tablename, i);
			}
		}
		return T;
	}

	public static boolean createTable(String tableName) {
		if (FileManager.findFile(tableName) == true)
			return false;
		FileManager.creatFile(tableName);
		Block block = BufferManager.getBlock(tableName, 0);
		block.writeInt(0, 0);
		return true;
	}

	public static boolean dropTable(String tableName) {
		if (FileManager.findFile(tableName) == false)
			return false;
		BufferManager.dropblocks(tableName);
		FileManager.dropFile(tableName);
		return true;
	}

	public static int insert(String tablename, Tuple Tuple) {
		final int tinb = Constants.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));
		Block block1 = BufferManager.getBlock(tablename, 0);
		int tupleoffset = block1.readInt(0);
		Block block2 = null;
		if (tupleoffset > 0) {
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
			int nexttupleoffset = block2.readInt((CatalogManager
					.getTupleLength(tablename) + SIZEINT)
					* (tupleoffset % tinb));
			block1.writeInt(0, nexttupleoffset);
		} else {
			tupleoffset = 1 + CatalogManager.getTupleNum(tablename);
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
		}

		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleoffset % tinb);
		block2.writeInt(byteoffset, -1);
		byteoffset += 4;

		for (int i = 0; i < Tuple.units.size(); i++) {
			if (CatalogManager.getType(tablename, i).equals("int")) {
				block2.writeInt(byteoffset,
						Integer.parseInt(Tuple.units.elementAt(i)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("float")) {
				block2.writeFloat(byteoffset,
						Float.parseFloat(Tuple.units.elementAt(i)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("char")) {
				block2.writeString(byteoffset, Tuple.units.elementAt(i),
						CatalogManager.getLength(tablename, i));
				byteoffset += CatalogManager.getLength(tablename, i);
			}

		}
		return tupleoffset;
	}

	

	public static Vector<Tuple> project(Vector<Tuple> res, String tablename,
			Vector<String> attriNames) {
		Vector<Tuple> newres = new Vector<Tuple>(0);
		for (int i = 0; i < res.size(); i++) {
			Tuple T = new Tuple();
			for (int j = 0; j < attriNames.size(); j++) {
				T.units.add(res.elementAt(i).units.elementAt(CatalogManager
						.getAttriOffest(tablename, attriNames.elementAt(j))));
			}
			newres.add(T);
		}
		return newres;
	}

public static Vector<Tuple> select(String tablename, ConditionNode condition) {
		final int tinb = Constants.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));
		Vector<Tuple> res = new Vector<Tuple>(0);
		Block block = BufferManager.getBlock(tablename, 0);
		int blockoffset = 0;
		int tupleoffset = 1;
		int count = 0;
		while (count < CatalogManager.getTupleNum(tablename)) {
			if (blockoffset < tupleoffset / tinb) {
				blockoffset++;
				block = BufferManager.getBlock(tablename, blockoffset);
			}
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename)) * (tupleoffset % tinb);
			if (block.readInt(byteoffset) >= 0) {
				tupleoffset++;
				continue;
			} else
				byteoffset += 4;
			Tuple T = new Tuple();
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}

			if (condition == null || condition.calc(tablename, T) == true)
				res.add(T);
			count++;
			tupleoffset++;
		}
		return res;
	}

	public static Vector<Tuple> select(String tablename,
			ConditionNode condition, String orderAttriName, boolean isInc) {
		Vector<Tuple> res = select(tablename, condition);
		if (isInc)
			compareParaInc = true;
		else
			compareParaInc = false;
		comparePara = CatalogManager.getAttriOffest(tablename, orderAttriName);
		compareParaType = CatalogManager.getType(tablename, orderAttriName);
		res.sort(new MyCompare());
		return res;
	}

	static int comparePara;
	static boolean compareParaInc;
	static String compareParaType;

	static class MyCompare implements Comparator<Tuple> 
	{
		public int compare(Tuple t1, Tuple t2) {
			String num1 = t1.units.elementAt(comparePara);
			String num2 = t2.units.elementAt(comparePara);
			if (compareParaType.equals("int"))
				if (compareParaInc)
					return (Integer.parseInt(num1) - Integer.parseInt(num2));
				else
					return (Integer.parseInt(num2) - Integer.parseInt(num1));
			else if (compareParaType.equals("float"))
				if (compareParaInc)
					return (int) (Float.parseFloat(num1) - Float
							.parseFloat(num2));
				else
					return (int) (Float.parseFloat(num2) - Float
							.parseFloat(num1));
			else if (compareParaInc)
				return num1.compareTo(num2);
			else
				return num2.compareTo(num1);
		}
	}

	public static int delete(String tablename, ConditionNode condition) {
		final int tinb = Constants.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));
		Block block1 = BufferManager.getBlock(tablename, 0);
		block1.fix();
		Block block2 = BufferManager.getBlock(tablename, 0);
		int blockoffset = 0;
		int tupleoffset = 1;
		int count = 0;
		int numdeleted = 0;
		int tuplenum = CatalogManager.getTupleNum(tablename);
		while (count < tuplenum) {
			if (blockoffset < tupleoffset / tinb) {
				blockoffset++;
				block2 = BufferManager.getBlock(tablename, blockoffset);
			}
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename)) * (tupleoffset % tinb);
			if (block2.readInt(byteoffset) >= 0) {
				tupleoffset++;
				continue;
			} else
				byteoffset += 4;
			Tuple T = new Tuple();
			int pointer = byteoffset - 4;
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block2.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block2.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block2.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}

			if (condition == null || condition.calc(tablename, T) == true) {
				
				try{
					for(int i = 0; i < CatalogManager.getTableAttriNum(tablename);i++){
						String indexname = CatalogManager.getIndexName(tablename, CatalogManager.getAttriName(tablename, i));
						if(indexname==null)
							continue;
						Index tmpindex = CatalogManager.getIndex(indexname);
						if(indexname!=null){
							IndexManager.deleteKey(tmpindex, T.units.elementAt(i));
						}
						CatalogManager.updateIndexTable(indexname, tmpindex);
					}
				}catch(Exception e){
					System.err.println(e);
				}
				
				int head = block1.readInt(0);
				block2.writeInt(pointer, head);
				block1.writeInt(0, tupleoffset);
				numdeleted++;
			}
			count++;
			tupleoffset++;
		}
		block1.unfix();
		return numdeleted;

	}

	
	public static Vector<Tuple> join(String tableName1,
			String attributeName1, String tableName2, String attributeName2) {
		Vector<Tuple> res1 = select(tableName1, null);
		Vector<Tuple> res2 = select(tableName2, null);
		Vector<Tuple> res = new Vector<Tuple>(0);
		for (int i = 0; i < res1.size(); i++)
			for (int j = 0; j < res2.size(); j++) {
				if (res1.elementAt(i).units.elementAt(
						(CatalogManager.getAttriOffest(tableName1,
								attributeName1))).equals(
						res2.elementAt(j).units.elementAt((CatalogManager
								.getAttriOffest(tableName2, attributeName2))))) {
					
					Tuple T = new Tuple();
					for (int k = 0; k < CatalogManager
							.getTableAttriNum(tableName1); k++) {
						T.units.addElement(res1.elementAt(i).units.elementAt(k));
					}
					for (int k = 0; k < CatalogManager
							.getTableAttriNum(tableName2); k++) {
						T.units.addElement(res2.elementAt(j).units.elementAt(k));
					}
					res.add(T);
				}
			}
		return res;

	}

	public static final int SIZEINT = 4;
	public static final int SIZEFLOAT = 4;
	public static final int SIZEBOOLEAN = 1;
	public static final int SIZECHAR = 2;
}