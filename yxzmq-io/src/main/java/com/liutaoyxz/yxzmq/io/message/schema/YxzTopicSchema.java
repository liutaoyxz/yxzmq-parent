package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.YxzTopic;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 10:24 2017/12/29
 * @Description:
 */
public class YxzTopicSchema implements Schema<YxzTopic> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
        NAME_MAP.put("connectionId",2);
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
    public boolean isInitialized(YxzTopic message) {
        return false;
    }

    @Override
    public YxzTopic newMessage() {
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
    public Class<? super YxzTopic> typeClass() {
        return null;
    }

    @Override
    public void mergeFrom(Input input, YxzTopic message) throws IOException {

    }

    @Override
    public void writeTo(Output output, YxzTopic message) throws IOException {

    }
}
