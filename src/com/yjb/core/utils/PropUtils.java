package com.yjb.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropUtils {
	static Properties properties;
	static {
		properties = new Properties();
		try {
			InputStream is = new FileInputStream(System.getProperty("user.dir")+File.separator+"bin"+File.separator+"prop.properties");
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private PropUtils() {
	}
	public static String getValue(String key) {
		return properties.getProperty(key);
	}
}
