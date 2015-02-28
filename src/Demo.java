import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.util.FileExtractUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Demo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Demo.class);
	public static final String MAVEN_REPO_SEARCH_URL="http://search.maven.org/solrsearch/select?q=";
	public static final String SEARCH_QUERY="&rows=20&wt=json";
	private static final String MAVEN_CHECKSUM_BASE_URL="http://search.maven.org/solrsearch/select?q=1:%22";
	private static final String MAVEN_CHECKSUM_QUERY_PARAMS="%22&rows=20&wt=json";
	private static final String SEARCH_BY_CHECKSUM="CHECKSUM";
	private static final String SEARCH_BY_JAR_NAME="JARNAME";
	private static final String workingDirectory = ".\\workingDir";
	private static final String mavenDir =".\\ProcessedMavenProject\\";
	
	
	
	public JsonObject makeRestCall(String searchType, String searchParam) {
		searchParam=searchParam.replace(" ", "%20");
		String returnVal="{error:'error fetching response from Maven repo'}";
		JsonObject jo =(JsonObject) new JsonParser().parse(returnVal);
		String mavenUrl="";
		if(searchType.equals(SEARCH_BY_CHECKSUM)){
			mavenUrl = MAVEN_CHECKSUM_BASE_URL+searchParam+MAVEN_CHECKSUM_QUERY_PARAMS;
		}else{
			mavenUrl = MAVEN_REPO_SEARCH_URL+searchParam+SEARCH_QUERY;
		}
		
		LOGGER.info("Maven Serach URL::"+mavenUrl);
		try{
			URL url = new URL(mavenUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
 
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
 
		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
 
		String output;
		while ((output = br.readLine()) != null) {
			JsonParser parser = new JsonParser();
			jo=(JsonObject)parser.parse(output);
		}
		
		conn.disconnect();
		 return jo;
 
	  } catch (MalformedURLException e) {
 
		e.printStackTrace();
		 return jo;
 
	  } catch (IOException e) {
 
		e.printStackTrace();
		 return jo;
	  }
	
	}
	
public List<MavenArtifactBean> getAllMavenArtifactBeans(JsonObject obj,String searchkey){
	JsonObject responseObj = obj.getAsJsonObject("response");
	JsonArray mavenRepoSearchResults = responseObj.getAsJsonArray("docs");
	List<MavenArtifactBean> mavenArtifacts = new ArrayList<MavenArtifactBean>();
	for(int i=0;i<mavenRepoSearchResults.size();i++){
		JsonObject mavenRepoFound = mavenRepoSearchResults.get(i).getAsJsonObject();
		
		MavenArtifactBean bean = new MavenArtifactBean();
		bean.setA(mavenRepoFound.get("a").getAsString());
		bean.setG(mavenRepoFound.get("g").getAsString());
		bean.setId(mavenRepoFound.get("id").getAsString());
		if(mavenRepoFound.has("latestVersion"))
		bean.setLatestVersion(mavenRepoFound.get("latestVersion").getAsString());
		if(mavenRepoFound.has("v"))
			bean.setLatestVersion(mavenRepoFound.get("v").getAsString());
			
		bean.setSearchHit(LevenshteinDistance.computeLevenshteinDistance(searchkey, bean.getA()));
		mavenArtifacts.add(bean);
		
	}
	return mavenArtifacts;
}
	
	public MavenArtifactBean getArtifactId(String searchkey, String checkSum,
			String version, String filePath) throws IOException {
		JsonObject obj;
		obj = makeRestCall(SEARCH_BY_CHECKSUM, checkSum);
		//
		List<MavenArtifactBean> mavenArtifacts = new ArrayList<MavenArtifactBean>();
		mavenArtifacts.addAll(getAllMavenArtifactBeans(obj, searchkey));
		MavenArtifactBean returnMavenArtifactBean = null;
		if (mavenArtifacts != null) {
			Collections.sort(mavenArtifacts,
					new MavenArtifactBean().new SortBasedOnRank());
			returnMavenArtifactBean = (mavenArtifacts == null || mavenArtifacts
					.size() == 0) ? null : mavenArtifacts.get(0);
		}
		if (mavenArtifacts == null || mavenArtifacts.size() == 0) {
			obj = makeRestCall(SEARCH_BY_JAR_NAME, searchkey);
			mavenArtifacts.addAll(getAllMavenArtifactBeans(obj, searchkey));
			Collections.sort(mavenArtifacts,
					new MavenArtifactBean().new SortBasedOnRank());
			returnMavenArtifactBean = (mavenArtifacts == null || mavenArtifacts
					.size() == 0) ? null : mavenArtifacts.get(0);
			if (returnMavenArtifactBean != null)
				returnMavenArtifactBean.setLatestVersion(version);

		}
		if (mavenArtifacts == null || mavenArtifacts.size() == 0) {

			String searchKey = Demo.readManifestFile(filePath);
			obj = makeRestCall(SEARCH_BY_JAR_NAME, searchKey);
			mavenArtifacts.addAll(getAllMavenArtifactBeans(obj, searchkey));
			Collections.sort(mavenArtifacts,
					new MavenArtifactBean().new SortBasedOnRank());
			returnMavenArtifactBean = (mavenArtifacts == null || mavenArtifacts
					.size() == 0) ? null : mavenArtifacts.get(0);
			if (returnMavenArtifactBean != null)
				returnMavenArtifactBean.setLatestVersion(version);
		}
		if (mavenArtifacts == null || mavenArtifacts.size() == 0) {
			String maventempDir = mavenDir + "lib";
			File file = new File(maventempDir);
			if (!file.exists()) {
				FileUtils.forceMkdir(file);
			}
			FileUtils.copyFileToDirectory(new File(filePath), file);
			/*
			 * <dependency> <groupId>getContractBPInfo</groupId>
			 * <artifactId>getContractBPInfo.jar</artifactId>
			 * <version>1.0</version> <scope>system</scope>
			 * <systemPath>${project.libdir}/getContractBPInfo.jar</systemPath>
			 * </dependency>
			 */
			String jarName = filePath.substring(filePath.lastIndexOf("\\") + 1,
					filePath.indexOf("jar") - 1);
			String v = jarName.substring(jarName.lastIndexOf("-") + 1);
			jarName = jarName.substring(0, jarName.lastIndexOf("-"));
			returnMavenArtifactBean = new MavenArtifactBean();
			returnMavenArtifactBean.setA(jarName);
			returnMavenArtifactBean.setG(jarName);
			returnMavenArtifactBean.setLatestVersion(v);
			returnMavenArtifactBean.setScope("system");
			returnMavenArtifactBean.setSystemPath(maventempDir
					+ filePath.substring(filePath.lastIndexOf("\\") + 1));
		}
		return returnMavenArtifactBean;
	}
	
public static void main(String[] args) {
		
	
	
	//MavenArtifactBean bean =  new Demo().getArtifactId("Spring Context");
	//System.out.println(bean.toString());
	//getArtifactId
	List<MavenArtifactBean> artifactBeans = new ArrayList<MavenArtifactBean>(); 
	try {
 		FileExtractUtility.extractWarToTargetDir(".\\SourceOfZip\\", ".\\UnzippedDir\\");
 		try {
			File[] allAntProjectJars = FileExtractUtility.getAllAntProjectJars();
			File processingDir = new File(".\\workingDir");
			if(!processingDir.exists())
				FileUtils.forceMkdir(processingDir);
			else{
				FileUtils.forceDelete(processingDir);
				FileUtils.forceMkdir(processingDir);
			}
			
			for(File jarPath:allAntProjectJars){
				String jarFilePath = jarPath.toString();
				//.\UnzippedDir\AntExample1\WebContent\WEB-INF\lib\antlr-runtime-3.0.jar
				String jarName = jarFilePath.substring(jarFilePath.lastIndexOf("\\")+1,jarFilePath.indexOf("jar")-1);
				String version = jarName.substring(jarName.lastIndexOf("-")+1);
				jarName = jarName.substring(0, jarName.lastIndexOf("-"));
				if(jarFilePath.endsWith("jar")){
					String checkSumCode = ChecksumUtility.getCheckSum(jarPath.toString());
					MavenArtifactBean artifactId = new Demo().getArtifactId(jarName, checkSumCode,version,jarFilePath);
					if(artifactId!=null){
						artifactBeans.add(artifactId);
					}else{
						
					}
 				
				}else
					continue;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		//based on jarcount, populate the dependencies
 		
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	File file = new File("c://pom.xml");
	try{
		if(!file.exists())
		file.createNewFile();
		Writer w = new FileWriter(file);
		Model model = new Model();
		model.setGroupId( "some.group.id" );
		model.setArtifactId("myArtifactid");
		
		List<Dependency> dependencyList = new ArrayList<Dependency>();
		for(MavenArtifactBean bean: artifactBeans){
			Dependency dependency = new Dependency();
			dependency.setArtifactId(bean.getA());
			dependency.setGroupId(bean.getG());
			dependency.setVersion(bean.getLatestVersion());
			if(bean.getScope()!=null && bean.getScope().equals("system")){
				dependency.setScope(bean.getScope());
				dependency.setSystemPath(bean.getSystemPath());
			}
			dependencyList.add(dependency);
			
		}
		
		model.setDependencies(dependencyList);
		new MavenXpp3Writer().write(w, model);
	}catch(Exception e){
		e.printStackTrace();
	}
	
}
/*public static void main(String[] args) {
	 Demo.readManifestFile();
}*/
	public static String readManifestFile(String filePath){
		//String filePath=".\\UnzippedDir\\AntExample1\\WebContent\\WEB-INF\\lib\\org.springframework.asm-3.0.0.M3.jar";
		File processingDir = new File(".\\workingDir");
		String jarName = filePath.substring(filePath.lastIndexOf("\\")+1,filePath.indexOf("jar")-1);
		jarName = jarName.substring(0, jarName.lastIndexOf("-"));
		FileExtractUtility.extractZipFiles(filePath, processingDir.toString()+"\\"+jarName+"\\");
		File processedJarDir = new File(".\\workingDir\\"+jarName+"\\META-INF\\MANIFEST.MF");
		FileInputStream fis = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(processedJarDir));
			String line;
			String jarBundleName="";
			while ((line = br.readLine()) != null) {
				  if(line.contains("Bundle-Name")){
					  String processed[]=line.split(":");  
					  if(processed!=null){
						  for(String str : processed){
							 if( str.trim().equals("Bundle-Name"))
									 continue;
							 else
								 jarBundleName=str.trim();
						  }
					  }
					  
				  }
				  
				}
			return jarBundleName;
			
 
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
