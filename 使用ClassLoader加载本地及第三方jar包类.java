
这是一个项目上的过滤器实现。
因为在获取数据时，要对数据进行筛选，只同步符合某些条件的数据。当面对复杂需求的时候，将逻辑全部写在数据库查询SQL，是不太
明智的选择。庞大复杂的SQL对于debug和维护都较耗时，同时项目上未必均使用driver直连数据库，常常会使用映射框架，例如hibernate，
或者公司内部自身类似的框架，往往对于灵活性和语法都有限制。
所以这个时候，实现一套过滤器，可以减轻这方面的负担。同时，通过自定义过滤器，自动搜索加载对应过滤器，节省手动配置。
在这里，通过注解指定哪些过滤器会被加载，加载后执行所有的过滤器对数据进行过滤，满足需求。


package com.ebay.cbt.sf.service.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class AbstractFilter {
	public abstract List<CustomedObject> evaluate(List<CustomedObject> list);

}


public class GeneralFilterFactory {
	
	private static List<AbstractFilter> filterList;
	private static final String CURRENT_CLASS_PATH = GeneralFilterFactory.class.getPackage().getName();
	private static final String PROTOCOL_FILE = "file";
	private static final String PROTOCOL_JAR = "jar";
	/**
	 * 
	 * @param targetList
	 */
	public static void filter(List<CustomedObject> targetList){
		if (null == filterList) {
			init();
		}
		if (null == filterList) {
			return;
		}
		if (filterList.size() == 0) {
			return;
		}
		for (AbstractFilter filter : filterList) {
			filter.evaluate(targetList);
		}
	}
	
	/**
	 * 
	 */
	private static void init() {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		//Get current file path 
		String name = CURRENT_CLASS_PATH.replace(".", "/");
		URL url = loader.getResource(name);
		String protocol = url.getProtocol();
		String path = loader.getResource(name).getPath();
		String[] classNames = null;
		if (protocol.equals(PROTOCOL_FILE)){
			File[] classFiles = getClassFiles(path);
			classNames = getClassNames(classFiles);
			
		} else {
			try {
				//get connetion to outer jar file
				JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
				//get the jar file object
				JarFile jarFile = jarURLConnection.getJarFile();
				//if we want to create instance, we must use this kind of jar class loader.
				URL jarUrl = jarURLConnection.getJarFileURL();
				loader = new URLClassLoader(new URL[] { jarUrl });
				classNames = getJarClassNames(jarFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		createInstance(loader, classNames, Filter.class);
	}
	
	/**
	 * 
	 * @param classNames
	 * @param annotation
	 */
	private static void createInstance(ClassLoader loader, String[] classNames, Class<? extends java.lang.annotation.Annotation> annotation){
		if (null == classNames || classNames.length == 0){
			return;
		}
		
		filterList = new ArrayList<>();
		for (int i = 0; i< classNames.length; i++){
			try {
				Class<?> clazz = loader.loadClass(classNames[i]);
				if (clazz.isAnnotationPresent(annotation)){
					AbstractFilter object = (AbstractFilter) clazz.newInstance();
					filterList.add(object);
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param files
	 * @return
	 */
	private static String[] getClassNames(File[] files){
		if (null == files){
			return null;
		}
		
		String[] classNames = new String[files.length];
		for (int i = 0 ; i < classNames.length ; i++ ){
			classNames[i] = CURRENT_CLASS_PATH+"."+files[i].getName().split("\\.")[0];
		}
		return classNames;
	}
	
	/**
	 * 
	 * @param filePath
	 * @return
	 */
	private static File[] getClassFiles(String filePath){
		if (null == filePath){
			return null;
		}
		
		return new File(filePath).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {  
				// TODO Auto-generated method stub
				return pathname.isFile() && pathname.getName().endsWith("class");
			}
		});
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	private static String[] getJarClassNames(JarFile jarFile) {
		List<String> classNames = new ArrayList<>();
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String jarEntryName = jarEntry.getName();
			String clazzName = jarEntryName.replace("/", ".");
			if (clazzName.startsWith(CURRENT_CLASS_PATH) && clazzName.endsWith(".class")) {
				clazzName = clazzName.substring(0, clazzName.length() - 6);
				classNames.add(clazzName);
			}
		}
		String[] clazzNames = new String[classNames.size()];
		return classNames.toArray(clazzNames);
	}
}
