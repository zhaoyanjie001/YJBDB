package com.yjb.core.logmgr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {
	static BufferedWriter writer;
	static {
		try {
			if (writer == null)
				writer = new BufferedWriter(new FileWriter("methodLog.txt",true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void write(String str) {
		try {
			writer.write(str);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
