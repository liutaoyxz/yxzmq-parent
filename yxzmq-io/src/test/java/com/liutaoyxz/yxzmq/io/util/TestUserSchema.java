package com.liutaoyxz.yxzmq.io.util;

import io.protostuff.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Doug Tao
 * @Date: 14:46 2017/12/20
 * @Description:
 */
public class TestUserSchema implements Schema<TestUser> {

    private static final HashMap<String,Integer> fieldMap = new HashMap<>();

    private StringMapSchema<TestUser> sm = new StringMapSchema<TestUser>(this);
    static {
        fieldMap.put("name",1);
        fieldMap.put("age",2);
        fieldMap.put("friends",3);
        fieldMap.put("teachers",4);
    }



    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "name";
            case 2:
                return "age";
            case 3:
                return "friends";
            case 4:
                return "teachers";
            default:
                return null;
        }
    }

    @Override
    public void mergeFrom(Input input, TestUser user) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch(number)
            {
                case 0:
                    return;
                case 1:
                    user.setName(input.readString());
                    break;
                case 2:
                    user.setAge(input.readInt32());
                    break;
                case 3:
                    user.setFriends(input.mergeObject(null,sm));
                    break;
                case 4:
                    if(user.getTeachers() == null) {
                        user.setTeachers(new ArrayList<>());
                    }
                    user.getTeachers().add(input.mergeObject(null, this));
                    break;
                default:
                    input.handleUnknownField(number, this);
            }
        }
    }

    @Override
    public void writeTo(Output output, TestUser user) throws IOException {
        if (user.getName() != null){
            output.writeString(1, user.getName(), false);
        }

        if(user.getAge() != null)
            output.writeInt32(2, user.getAge(), false);

        if (user.getFriends() != null){
            output.writeObject(3,user.getFriends(),sm,false);
        }

        if(user.getTeachers() != null)
        {
            for(TestUser teacher : user.getTeachers())
            {
                if(teacher != null)
                    output.writeObject(4, teacher, this, true);
            }
        }
    }


    @Override
    public int getFieldNumber(String name) {
        Integer number = fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    @Override
    public boolean isInitialized(TestUser message) {
        return true;
    }

    @Override
    public TestUser newMessage() {
        return new TestUser();
    }

    @Override
    public String messageName() {
        return TestUser.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return TestUser.class.getName();
    }

    @Override
    public Class<? super TestUser> typeClass() {
        return TestUser.class;
    }

}
