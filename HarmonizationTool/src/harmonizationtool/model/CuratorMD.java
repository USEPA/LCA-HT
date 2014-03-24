package harmonizationtool.model;

import harmonizationtool.utils.Util;

public class CuratorMD {
	private String name;
	private String affiliation;
	private String email;
	private String phone;

	public CuratorMD() {
	}

	public CuratorMD(boolean preferences) {
		if (preferences) {
			setName(Util.getPreferenceStore().getString("curatorName"));
			setAffiliation(Util.getPreferenceStore().getString(
					"curatorAffiliation"));
			setEmail(Util.getPreferenceStore().getString("curatorEmail"));
			setPhone(Util.getPreferenceStore().getString("curatorPhone"));
		}
	}

	public String getName() {
		if (name == null) {
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAffiliation() {
		if (affiliation == null) {
			affiliation = "";
		}
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getEmail() {
		if (email == null) {
			email = "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		if (phone == null) {
			phone = "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
