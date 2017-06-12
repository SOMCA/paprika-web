package spoon.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
 *
 */
public class DirectorySearch {

	private String fileName;
	//private Map<String,String> result = new HashMap<String,String>();

	public DirectorySearch(String fileFilter) {
		this.fileName = fileFilter;
	}

/*
	public static void main(String[] args) {

		
		
		DirectorySearch fileSearch = new DirectorySearch();

		// try different directory and filename :)
		fileSearch.searchDirectory(new File("../"), ".java");

		int count = fileSearch.getResult().size();
		if (count == 0) {
			System.out.println("\nNo result found!");
		} else {
			System.out.println("\nFound " + count + " result!\n");
			for (String matched : fileSearch.getResult()) {
				System.out.println("Found : " + matched);
			}
		}
	}*/

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

			// do you have permission to read this directory?
			if (file.canRead()) {
				for (File temp : file.listFiles()) {
					if (temp.isDirectory()) {
						// if
						// (this.fileName.equals(temp.getName().toLowerCase())
						// &&
						// temp.getParentFile().getPath().endsWith("src/main")){
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
