package com.stockAnalysis.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

	// Primary Key
	@Id
	private String id;
	private String password;
	private String name;

	public void setId(String id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", password=" + password + ", name=" + name + "]";
	}

	public void update(User newUser) {
		// TODO Auto-generated method stub
		this.name = newUser.name;
		this.password = newUser.password;
	}

}
