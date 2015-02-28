import java.util.Comparator;


public class MavenArtifactBean {

	private String id;
	private String g;
	private String a;
	private String latestVersion;
	private int searchHit;
	private String scope;
	private String systemPath;
	
	public String getSystemPath() {
		return systemPath;
	}
	public void setSystemPath(String systemPath) {
		this.systemPath = systemPath;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	
	
	public int getSearchHit() {
		return searchHit;
	}
	public void setSearchHit(int searchHit) {
		this.searchHit = searchHit;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getG() {
		return g;
	}
	public void setG(String g) {
		this.g = g;
	}
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public String getLatestVersion() {
		return latestVersion;
	}
	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}
	
	public String toString(){
		return this.a+" "+this.g+" "+this.id+" "+this.latestVersion;
	}
	
	public class SortBasedOnRank implements Comparator<MavenArtifactBean>{

		@Override
		public int compare(MavenArtifactBean bean1, MavenArtifactBean bean2) {
			// TODO Auto-generated method stub
			return bean1.getSearchHit()>bean2.getSearchHit()?1:(bean1.getSearchHit()<bean2.getSearchHit())?-1:0;
		}

		
		
	}
		
	
	
	
}
