package app.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * To delete.
 *
 */
public class PaprikaZip {
	private List<String> fileList;
	private final String OUTPUT_ZIP_FILE;
	private final String SOURCE_FOLDER; 

	public PaprikaZip() {
		this.fileList = new ArrayList<String>();
		this.OUTPUT_ZIP_FILE = "fichier.zip";
		this.SOURCE_FOLDER = "./test";

	}

	public PaprikaZip(String output, String source) {
		this.fileList = new ArrayList<String>();
		this.OUTPUT_ZIP_FILE = output;
		this.SOURCE_FOLDER = source;

	}

	public void run() {
		this.generateFileList(new File(SOURCE_FOLDER));
		this.zipIt(OUTPUT_ZIP_FILE);
	}

	public void zipIt(String zipFile) {
		byte[] buffer = new byte[1024];
		String source = new File(SOURCE_FOLDER).getName();
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList) {
				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();
			System.out.println("Folder successfully compressed");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void generateFileList(File node) {
		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(SOURCE_FOLDER.length(), file.length());
	}
	
	
	/*
	 * private static Object getFile(Request request, Response response) {
	 * 
	 * PaprikaZip zip = new PaprikaZip("fichier.zip","./test/"); zip.run(); File
	 * zipFileName = Paths.get("fichier.zip").toFile();
	 * 
	 * 
	 * response.raw().setContentType("application/octet-stream");
	 * response.raw().setHeader("Content-Disposition", "attachment; filename=" +
	 * zipFileName.getName()+".zip"); System.out.println(zipFileName.getName());
	 * try {
	 * 
	 * try (ZipOutputStream zipOutputStream = new ZipOutputStream( new
	 * BufferedOutputStream(response.raw().getOutputStream()));
	 * BufferedInputStream bufferedInputStream = new BufferedInputStream( new
	 * FileInputStream(zipFileName))) { ZipEntry zipEntry = new
	 * ZipEntry(zipFileName.getName());
	 * 
	 * zipOutputStream.putNextEntry(zipEntry); byte[] buffer = new byte[1024];
	 * int len = bufferedInputStream.read(buffer); while (len > 0) {
	 * zipOutputStream.write(buffer, 0, len); len =
	 * bufferedInputStream.read(buffer); } zipOutputStream.flush();
	 * zipOutputStream.close(); }
	 * 
	 * } catch (Exception e) { halt(405, "server error"); }
	 * 
	 * return null; }
	 */
}
