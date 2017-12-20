package com.liutaoyxz.yxzmq.io.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 14:44 2017/12/20
 * @Description:
 */
public class TestUser implements Serializable{

    private static final long serialVersionUID = -6849794470754667710L;


    private String name;

    private Integer age;

    private Map<String,TestUser> friends;

    private List<TestUser> teachers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Map<String, TestUser> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, TestUser> friends) {
        this.friends = friends;
    }

    public List<TestUser> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<TestUser> teachers) {
        this.teachers = teachers;
    }

    @Override
    public String toString() {
        return "TestUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", friends=" + friends +
                ", teachers=" + teachers +
                '}';
    }
}
