package com.liutaoyxz.yxzmq.io.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 14:44 2017/12/20
 * @Description: protostuff test
 */
public class ProtostuffUtilTest {

    private TestUserSchema schema = new TestUserSchema();


    @Test
    public void get() throws Exception {


    }

    @Test
    public void serializable() throws Exception {

        TestUser t1 = new TestUser();
        t1.setName("laowang");
        t1.setAge(50);
        TestUser t2 = new TestUser();
        t2.setName("laoli");
        t2.setAge(60);
        TestUser t3 = new TestUser();
        t3.setName("laoliu");
        t3.setAge(35);
        List<TestUser> teachers = new ArrayList<>();
        teachers.add(t1);
        teachers.add(t2);
        teachers.add(t3);
        Map<String,TestUser> friends = new HashMap<>();
        friends.put("laowang",t1);
        friends.put("laoli",t2);
        friends.put("laoliu",t3);

        TestUser user = new TestUser();
        user.setAge(15);
        user.setName("liutao");
        user.setFriends(friends);
        user.setTeachers(teachers);

//        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);

        byte[] parray = ProtobufIOUtil.toByteArray(user, schema, LinkedBuffer.allocate());
        System.out.println("-------------------------------protostuff------------------------------------");

        System.out.println(Arrays.toString(parray));
        System.out.println(parray.length);
        TestUser pu = new TestUser();
        ProtobufIOUtil.mergeFrom(parray, pu, schema);
        System.out.println(pu);
        System.out.println("-------------------------------protostuff------------------------------------");
        System.out.println("-------------------------------java------------------------------------");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(user);
        oos.flush();
        byte[] barray = bos.toByteArray();
        System.out.println(Arrays.toString(barray));
        System.out.println(barray.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(barray);
        ObjectInputStream ois = new ObjectInputStream(bis);
        TestUser ju = (TestUser) ois.readObject();
        System.out.println(ju);
        System.out.println("-------------------------------java------------------------------------");


    }

}