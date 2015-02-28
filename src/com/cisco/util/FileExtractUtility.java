package com.cisco.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;



public class FileExtractUtility {
	private static String targetDir;
	private static String SOURCE_DIR=".//UnzippedDir//";
	private static String WEB_DIR="//WebContent//WEB-INF//lib";
	
	public static File[]  getAllAntProjectJars()throws Exception{
		try{
		File jarHeadDir = new File(SOURCE_DIR);
		File[] listFiles = jarHeadDir.listFiles();
		String jarPath = listFiles[0].toString()+WEB_DIR;
		File jarDirPath = new File(jarPath);
		File[] jarFiles = jarDirPath.listFiles();
		return jarFiles;
		}finally{
			
		}
		
	}
	
		
	public static String extractWarToTargetDir(String sourceDir, String targetDir) throws IOException{
		File f = new File(targetDir);
		if(f.exists()){
		FileUtils.cleanDirectory(f);
		}else
		FileUtils.forceMkdir(f);
		targetDir = targetDir+"\\";
		File sourceFile = new File(sourceDir);
		File files[]=sourceFile.listFiles();
		File[] files2 = files;
		String fileName="";
		if(files2 !=null){
		if(files2[0].isFile()){
			fileName = files2[0].getPath();
		}
		}else{
			if(sourceFile.isFile()){	
				fileName = sourceFile.getPath();
			}
		}
		extractZipFiles(fileName,targetDir);  
		return targetDir;
		
	}
	
	
 
  public static void getAllFileAndFolder(File folder, List<File> all, Map<String,List<String>> folderContent) {
	  
    all.add(folder);
    if (folder.isFile()) {
      return;
    }
    
    if(folder.listFiles().length==0)
    	all.remove(folder);
    List<String> fileList = new ArrayList<String>();
    for (File file : folder.listFiles()) {
       if(file.toString().endsWith(".class")){
    	   fileList.add(file.toString().substring(file.toString().indexOf("classes")).replace("\\", "."));
    	   all.add(file);
       }
    	
      if (file.isDirectory()) {
    	if(file.listFiles().length!=0)
    		
        getAllFileAndFolder(file, all,folderContent);
      }
      folderContent.put(folder.toString().substring(folder.toString().indexOf("classes")).replace("\\", "."), fileList);
    }
   
  }
  
 
  
  public static void extractZipFiles(String filename, String destination) {
	   try {
	             String destinationname = destination;          
	             byte[] buf = new byte[1024];
	             ZipInputStream zipinputstream = null;
	             ZipEntry zipentry;
	             zipinputstream = new ZipInputStream(new FileInputStream(filename));
	            zipentry = zipinputstream.getNextEntry();
	  
	           while (zipentry != null) {

	        	   String entryName = zipentry.getName();
	     
	                 int n;
	                FileOutputStream fileoutputstream;
	                File newFile = new File(entryName);

	              String directory = newFile.getParent();

	              // to creating the parent directories
	              if (directory == null) {
	                   if (newFile.isDirectory()){
	                         break;
	                      }
	             } else {
	                 new File(destinationname+directory).mkdirs();
	              }

	            if(!zipentry.isDirectory()){ 
	                       fileoutputstream = new FileOutputStream(destinationname  + entryName);
	                      while ((n = zipinputstream.read(buf, 0, 1024)) > -1){
	                              fileoutputstream.write(buf, 0, n);
	                       }
	                      fileoutputstream.close();
	            }
	           zipinputstream.closeEntry();
	           zipentry = zipinputstream.getNextEntry();
	          }// while
	     zipinputstream.close();
	   } catch (Exception e) {
	    e.printStackTrace();
	   }
	  }
 
}