package it.asso.core.model.documenti;

import java.io.File;

public class AssoFile {

		 
	    private String id_file;
	    private String filename;
	    private String extension;
	    private String size;
	    private String id_documento;
	    private String full_path;
	    
	    private File file;
	    
	    
	    
		public String getId_documento() {
			return id_documento;
		}
		public void setId_documento(String id_documento) {
			this.id_documento = id_documento;
		}
		public String getId_file() {
			return id_file;
		}
		public void setId_file(String id_file) {
			this.id_file = id_file;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public String getExtension() {
			return extension;
		}
		public void setExtension(String extension) {
			this.extension = extension;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
		public String getFull_path() {
			return full_path;
		}
		public void setFull_path(String full_path) {
			this.full_path = full_path;
		}
		
	
}
