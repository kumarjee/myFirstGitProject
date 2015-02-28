

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtility {
	
	public static void main(String[] args) throws Exception{
		
		
		
		File file =new File("./UnzippedDir/AntExample1/WebContent/WEB-INF/lib/org.springframework.asm-3.0.0.M3.jar");
		
		String checkSumCode = ChecksumUtility.getCheckSum(file.toString());
		System.out.println(checkSumCode);
		
	}
	
	
	public static String getCheckSum(String file) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        FileInputStream fis = new FileInputStream(file);
  
        byte[] data = new byte[1024];
        int read = 0;
        while ((read = fis.read(data)) != -1) {
            sha1.update(data, 0, read);
        };
        byte[] hashBytes = sha1.digest();
  
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        String fileHash = sb.toString();
         
        return fileHash;
    }

}
