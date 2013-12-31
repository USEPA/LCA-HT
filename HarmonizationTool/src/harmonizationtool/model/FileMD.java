package harmonizationtool.model;

import java.util.Calendar;

public class FileMD {
	private String filename;
	private long size;
	private Calendar lastModified;
	private long readTime;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public Calendar getLastModified() {
		return lastModified;
	}
	public void setLastModified(Calendar lastModified) {
		this.lastModified = lastModified;
	}
	public long getReadTime() {
		return readTime;
	}
	public void setReadTime(long readTime) {
		this.readTime = readTime;
	}

}
