package cn.ltshark.domain;

/**
 * Active Derictory 中的用户信息
 *
 * @author Administrator
 *
 */
public class ADUserInfo {
	private String sn;// lastname

	private String givenName;// firstname

	private String displayName; // 显示名称

	private String samAccountName;// cn、用户登陆名、用户登陆名（2000以前版本）都用该属性表示

	private String password;// 密码

	private String company; // 公司

	private String department; // 部门

	private String description; // 描述

	private String email;// 电子邮件、LCS账号

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSamAccountName() {
		return samAccountName;
	}

	public void setSamAccountName(String samAccountName) {
		this.samAccountName = samAccountName;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}