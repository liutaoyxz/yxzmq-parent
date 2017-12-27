package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.YxzTextMessage;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

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
    }


    @Override
    public String getFieldName(int number) {
        return null;
    }

    @Override
    public int getFieldNumber(String name) {
        return 0;
    }

    @Override
    public boolean isInitialized(YxzTextMessage message) {
        return false;
    }

    @Override
    public YxzTextMessage newMessage() {
        return null;
    }

    @Override
    public String messageName() {
        return null;
    }

    @Override
    public String messageFullName() {
        return null;
    }

    @Override
    public Class<? super YxzTextMessage> typeClass() {
        return null;
    }

    @Override
    public void mergeFrom(Input input, YxzTextMessage message) throws IOException {

    }

    @Override
    public void writeTo(Output output, YxzTextMessage message) throws IOException {

    }
}
