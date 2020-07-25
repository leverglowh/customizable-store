package it.polimi.tiw.customizablestore.beans;

public class User {
	private int id;
	private String role;
	private String username;
	private String email;

	public int getId() {
		return id;
	}

	public String getRole() {
		return role;
	}

	public String getUsername() {
		return username;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

}
