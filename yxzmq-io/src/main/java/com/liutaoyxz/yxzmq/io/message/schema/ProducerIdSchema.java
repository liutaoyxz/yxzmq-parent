package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.ProducerId;
import com.liutaoyxz.yxzmq.io.message.SessionId;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 17:12 2017/12/21
 * @Description:
 */
public class ProducerIdSchema implements Schema<ProducerId> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
        NAME_MAP.put("sessionId",2);
    }


    @Override
    public void mergeFrom(Input input, ProducerId message) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch (number){
                case 0:
                    return;
                case 1:
                    message.setId(input.readString());
                    break;
                case 2:
                    message.setSessionId(input.mergeObject((SessionId)null,Schemas.SESSION_ID_SCHEMA));
                    break;
                default:
                    input.handleUnknownField(number,this);
            }
        }
    }

    @Override
    public void writeTo(Output output, ProducerId message) throws IOException {
        if (message.id() != null){
            output.writeString(1,message.id(),false);
        }
        if (message.getSessionId() != null){
            output.writeObject(2,message.getSessionId(),Schemas.SESSION_ID_SCHEMA,false);
        }
    }

    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "id";
            case 2:
                return "sessionId";
            default:
                return null;
        }
    }

    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(ProducerId message) {
        return message.getSessionId() != null && StringUtils.isNotBlank(message.id());
    }

    @Override
    public ProducerId newMessage() {
        return new ProducerId();
    }

    @Override
    public String messageName() {
        return ProducerId.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return ProducerId.class.getName();
    }

    @Override
    public Class<? super ProducerId> typeClass() {
        return ProducerId.class;
    }

}
