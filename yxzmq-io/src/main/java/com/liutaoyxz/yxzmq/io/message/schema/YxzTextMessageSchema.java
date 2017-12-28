package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.YxzTextMessage;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.runtime.ClassSchema;
import io.protostuff.runtime.DefaultIdStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 13:21 2017/12/25
 * @Description:
 */
public class YxzTextMessageSchema implements Schema<YxzTextMessage> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("messageId",1);
        NAME_MAP.put("timestamp",2);
        NAME_MAP.put("destination",3);
        NAME_MAP.put("replyDestination",4);
        NAME_MAP.put("expiration",5);
        NAME_MAP.put("writable",6);
        NAME_MAP.put("properties",7);
        NAME_MAP.put("priority",8);
        NAME_MAP.put("messageClass",9);
        NAME_MAP.put("text",10);
    }


    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "messageId";
            case 2:
                return "timestamp";
            case 3:
                return "destination";
            case 4:
                return "replyDestination";
            case 5:
                return "expiration";
            case 6:
                return "writable";
            case 7:
                return "properties";
            case 8:
                return "priority";
            case 9:
                return "messageClass";
            case 10:
                return "text";
            default:
                return null;
        }
    }

    @Override
    public void mergeFrom(Input input, YxzTextMessage message) throws IOException {

    }

    @Override
    public void writeTo(Output output, YxzTextMessage message) throws IOException {
        if (message.getMessageId() != null){
            output.writeObject(1,message.getMessageId(),Schemas.MESSAGE_ID,false);
        }
        output.writeInt64(2,message.getTimestamp(),false);
        if (message.getDestination() != null){

        }
        if (message.getMessageClass() != null){
            output.writeString(9,message.getMessageClass(),false);
        }
    }

    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(YxzTextMessage message) {
        return message != null && message.getMessageClass() != null;
    }

    @Override
    public YxzTextMessage newMessage() {
        return new YxzTextMessage();
    }

    @Override
    public String messageName() {
        return YxzTextMessage.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return YxzTextMessage.class.getName();
    }

    @Override
    public Class<? super YxzTextMessage> typeClass() {
        return YxzTextMessage.class;
    }
}
