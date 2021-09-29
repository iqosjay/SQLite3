package com.roy.sqlite3.data;

/**
 * Created by Roy on 2021/9/26.
 */
public class Student implements Cloneable {
  private long id;        //主键id
  private String name;    //名字
  private int age;        //年龄
  private double height;  //身高
  private byte[] avatar;  //头像

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public byte[] getAvatar() {
    return avatar;
  }

  public void setAvatar(byte[] avatar) {
    this.avatar = avatar;
  }

  @Override
  public Student clone() {
    try {
      return (Student) super.clone();
    } catch (CloneNotSupportedException ignored) {
      final Student copyOne = new Student();
      copyOne.setId(id);
      copyOne.setAge(age);
      copyOne.setName(name);
      copyOne.setAvatar(avatar);
      copyOne.setHeight(height);
      return copyOne;
    }
  }
}
