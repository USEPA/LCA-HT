package harmonizationtool.model;

//import java.util.Calendar;
import harmonizationtool.utils.FileEncodingUtil;

import java.util.Date;

public class FileMD {
	private String filename;
	private String path;
	private String encoding = null;
	private long size;
	private Date lastModified;
	private Date readTime;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public Date getReadTime() {
		return readTime;
	}
	public void setReadTime(Date readTime) {
		this.readTime = readTime;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		if (!FileEncodingUtil.containsEncoding(encoding)){
			// WARN THAT THIS ENCODING HAS NOT BEEN SEEN
		}
		this.encoding = encoding;
	}
	@Override
	public String toString() {
		return path;
	}

}
