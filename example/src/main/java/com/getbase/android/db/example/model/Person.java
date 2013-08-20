package com.getbase.android.db.example.model;

public class Person implements Sectionable<Character> {

  private final String firstName;
  private final String lastName;

  public Person(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  @Override
  public Character getSection() {
    return firstName.charAt(0);
  }

  public String getFullname() {
    return String.format("%s %s", firstName, lastName);
  }
}
