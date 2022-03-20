package org.zjuwangg.tools;

import java.io.File;

public class Test {
	
	public static void main(String[] args) {
		
		File file = new File("F:/tomcat/apache-tomcat-6.0.18_new/webapps/suzhou/WEB-INF/classes/\\Data\\Configure.xml");
		System.out.println(file.getAbsolutePath());
		
		
	}

}
