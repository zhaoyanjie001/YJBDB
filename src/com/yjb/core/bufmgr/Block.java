package com.yjb.core.bufmgr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.yjb.core.common.Constants;
import com.yjb.core.logmgr.LogWriter;

public class Block {

	String fileName = "";
	public int blockOffset = 0;
	boolean dirty = false;
	public boolean valid = false;
	boolean fixed = false;
	boolean referenceBit = false;

	public byte[] data = new byte[Constants.BLOCKSIZE];

	public byte[] readData() {
		referenceBit = true;
		return data;
	}

	public boolean writeData(int byteOffset, byte inputdata[], int size) {
		LogWriter.write("writeData: start");
		if (byteOffset + size >= 4096)
			return false;
		for (int i = 0; i < size; i++)
			data[byteOffset + i] = inputdata[i];
		dirty = true;
		referenceBit = true;
		LogWriter.write("writeData: end");
		return true;
	}

	public boolean writeData() {
		dirty = true;
		referenceBit = true;
		return true;
	}

	public void fix() {
		fixed = true;
	}

	public void unfix() {
		fixed = false;
	}

	public int readInt(int offset) {
		byte[] temp = new byte[4];
		temp[0] = data[offset + 0];
		temp[1] = data[offset + 1];
		temp[2] = data[offset + 2];
		temp[3] = data[offset + 3];
		ByteArrayInputStream bintput = new ByteArrayInputStream(temp);
		DataInputStream dintput = new DataInputStream(bintput);
		int res = 0;
		try {
			res = dintput.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		referenceBit = true;
		return res;
	}

	public void writeInt(int offset, int num) {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeInt(num);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[offset] = temp[0];
		data[offset + 1] = temp[1];
		data[offset + 2] = temp[2];
		data[offset + 3] = temp[3];
		dirty = true;
		referenceBit = true;
	}

	public float readFloat(int offset) {
		byte[] temp = new byte[4];
		temp[0] = data[offset + 0];
		temp[1] = data[offset + 1];
		temp[2] = data[offset + 2];
		temp[3] = data[offset + 3];
		ByteArrayInputStream bintput = new ByteArrayInputStream(temp);
		DataInputStream dintput = new DataInputStream(bintput);
		float res = 0;
		try {
			res = dintput.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		referenceBit = true;
		return res;
	}

	public void writeFloat(int offset, float num) {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeFloat(num);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[offset] = temp[0];
		data[offset + 1] = temp[1];
		data[offset + 2] = temp[2];
		data[offset + 3] = temp[3];
		dirty = true;
		referenceBit = true;
	}

	public String readString(int offset, int length) {
		byte[] buf = new byte[length];
		for (int i = 0; i < length; i++)
			buf[i] = data[offset++];
		referenceBit = true;
		String res = new String(buf);
		res = res.replaceAll("&", "");
		return res;
	}

	public String readString(int offset) {
		byte[] buf = new byte[4];
		for (int i = 0; i < 4; i++)
			buf[i] = data[offset++];
		referenceBit = true;
		String res = new String(buf);
		return res;
	}

	public void writeString(int offset, String num, int length) {
		byte[] buf = num.getBytes();
		int j;
		for (j = 0; j < buf.length; j++) {
			data[offset] = buf[j];
			offset += 1;
		}
		for (; j < length; j++) {
			data[offset] = '&';
			offset += 1;
		}
		dirty = true;
		referenceBit = true;
	}

	public int recordNum = 0;
	public Block next = null;
	public Block previous = null;
	public void writeInternalKey(int pos, byte[] key, int offset) {
		writeData(pos, key, key.length);
		writeInt(pos + key.length, offset);
		dirty = true;
	}

	public byte[] getBytes(int pos, int length) {
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			b[i] = data[pos + i];
		}
		return b;
	}

	public void setInternalKey(int pos, byte[] key, int offset) {
		writeData(pos, key, key.length);
		writeInt(pos + key.length, offset);
		dirty = true;
	}

	public void setKeydata(int pos, byte[] insertKey, int blockOffset, int offset) {
		writeInt(pos, blockOffset);
		writeInt(pos + 4, offset);
		writeData(pos + 8, insertKey, insertKey.length);
		dirty = true;
	}

}