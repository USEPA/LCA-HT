package harmonizationtool.model;

public class DataSetMD {
	private String name;
	private String version;
	private String comments;
	private String contactName;
	private String contactAffiliation;
	private String contactEmail;
	private String contactPhone;

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		if (version == null) {
			version = "";
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getContactName() {
		if (contactName == null) {
			contactName = "";
		}
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactAffiliation() {
		if (contactAffiliation == null) {
			contactAffiliation = "";
		}
		return contactAffiliation;
	}

	public void setContactAffiliation(String contactAffiliation) {
		this.contactAffiliation = contactAffiliation;
	}

	public String getContactEmail() {
		if (contactEmail == null) {
			contactEmail = "";
		}
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		if (contactPhone == null) {
			contactPhone = "";
		}
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getComments() {
		if (comments == null) {
			comments = "";
		}
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
