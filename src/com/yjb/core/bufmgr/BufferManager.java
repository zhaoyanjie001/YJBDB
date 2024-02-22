package com.yjb.core.bufmgr;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferManager {
	static final int NUMOFBLOCKS = 20;
	static final int NOTEXIST = -1;

	private static Block[] blocks = new Block[NUMOFBLOCKS];
	private static int pointer = 0;
	

	public static void initialize() {
		for (int i = 0; i < NUMOFBLOCKS; i++)
			blocks[i] = new Block();
	}

	public static void close() {
		for (int i = 0; i < NUMOFBLOCKS; i++) {
			if (blocks[i].valid==true)
				writeToDisk(i);
		}
	}
	
	
	public static void dropblocks(String filename){
		for (int i = 0; i < NUMOFBLOCKS; i++)
			if (blocks[i].filename.equals(filename))
				blocks[i].valid=false;												
	}

	public static Block getBlock(String filename, int blockoffset) {
		int num = findBlock(filename, blockoffset);
		if (num != NOTEXIST)
			return blocks[num];
		else {
			num = getFreeBlockNum();
			File file = new File(filename);
			if (!file.exists()) {
				blocks[num].blockoffset = blockoffset;
				blocks[num].filename = filename;
				for (int i = 0; i < Block.BLOCKSIZE; i++)
					blocks[num].data[i] = 0;
				return blocks[num];
			}
			readFromDisk(filename, blockoffset, num);
			return blocks[num];
		}
	}

	private static int findBlock(String filename, int blockoffset) {
		for (int i = 0; i < NUMOFBLOCKS; i++)
			if (blocks[i].valid)
				if(blocks[i].filename.equals(filename))
					if(blocks[i].blockoffset == blockoffset) {
				return i;
			}
		return NOTEXIST;
	}

	private static int getFreeBlockNum() {
		do {
			pointer = (pointer + 1) % NUMOFBLOCKS;
			if (blocks[pointer].reference_bit == true
					&& blocks[pointer].fixed == false)
				blocks[pointer].reference_bit = false;
			else if (blocks[pointer].reference_bit == false) {
				writeToDisk(pointer);
				return pointer;
			}
		} while (true);
	}

	private static boolean readFromDisk(String filename, int blockoffset,
			int num) {
		File file = null;
		RandomAccessFile raf = null;
		blocks[num].filename = filename;
		blocks[num].blockoffset = blockoffset;
		blocks[num].valid = true;
		blocks[num].reference_bit = true;
		blocks[num].dirty = false;
		blocks[num].fixed = false;
		for (int i = 0; i < Block.BLOCKSIZE; i++)
			blocks[num].data[i] = 0;
		try {
			file = new File(filename);
			raf = new RandomAccessFile(file, "rw");

			if (raf.length() >= blocks[num].blockoffset * Block.BLOCKSIZE
					+ Block.BLOCKSIZE) {
				raf.seek(blockoffset * Block.BLOCKSIZE);
				raf.read(blocks[num].data, 0, Block.BLOCKSIZE);
			} else
				for (int j = 0; j < Block.BLOCKSIZE; j++)
					blocks[num].data[j] = 0;
			raf.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private static void writeToDisk(int num) {
		if (blocks[num].dirty == false) {
			blocks[num].valid = false;
			return;
		} else {
			File file = null;
			RandomAccessFile raf = null;
			try {
				file = new File(blocks[num].filename);
				raf = new RandomAccessFile(file, "rw");
				// if file doesn't exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				raf.seek(blocks[num].blockoffset * Block.BLOCKSIZE);
				raf.write(blocks[num].data);
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (raf != null) {
						raf.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			blocks[num].valid = false;
		}
	}
}