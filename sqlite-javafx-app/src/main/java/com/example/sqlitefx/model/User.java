package com.example.sqlitefx.model;

public class User {

  private int id;
  private String fullName;
  private String email;
  private String phone;
  private String createdAt;

  public User() {
  }

  public User(String fullName, String email, String phone) {
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
  }

  public User(int id, String fullName, String email, String phone, String createdAt) {
    this.id = id;
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.createdAt = createdAt;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
