package com.yjb.core.filmgr;

import java.io.File;

public class FileManager {
	
	public static boolean findFile(String filename) {
		File file = new File(filename);
		if (!file.exists())
			return false;
		return true;
	}

	public static void creatFile(String filename) {
		try {
			File myFile = new File(filename);
			if (!myFile.exists())
				myFile.createNewFile();
			else
				throw new Exception("The new file already exists!");
		} catch (Exception ex) {
			System.out.println("无法创建新文件！");
			ex.printStackTrace();
		}
	}
	
	public static void dropFile(String filename) {
		File f=new File(filename);
		if(f.exists())f.delete();		
	}
}