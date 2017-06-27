package spoon.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
 *
 *
 * DirectorySearch search all and save all name of files who finish per .java
 *
 */
@SuppressWarnings("javadoc")
public class DirectorySearch {

	private String fileName;
	
	public DirectorySearch(String fileFilter) {
		this.fileName = fileFilter;
	}


	public Map<String,String> run(String path) {
		Map<String,String> result = new HashMap<String,String>();
		this.searchDirectory(new File(path),result);
		return result;
		
	}

	public void searchDirectory(File directory,Map<String,String> result) {
		if (directory.isDirectory()) {
			search(directory,result);
		} else {
			System.out.println(directory.getAbsoluteFile() + " is not a directory!");
		}
	}

	private void search(File file,Map<String,String> result) {

		if (file.isDirectory()) {
			System.out.println("Searching directory ... " + file.getAbsoluteFile());

			if (file.canRead()) {
				for (File temp : file.listFiles()) {
					if (temp.isDirectory()) {
						search(temp,result);
					} else if(temp.getName().endsWith(this.fileName)){
						if(temp.getName()==null) continue;
						if(temp.getPath()==null) continue;
						result.put(temp.getName(),temp.getPath());
					}

				}

			} else {
				System.out.println(file.getAbsoluteFile() + "Permission Denied");
			}
		}

	}

}
