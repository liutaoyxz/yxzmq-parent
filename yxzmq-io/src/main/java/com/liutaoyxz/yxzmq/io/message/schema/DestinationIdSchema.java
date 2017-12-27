package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.DestinationId;
import com.liutaoyxz.yxzmq.io.message.MessageId;
import com.liutaoyxz.yxzmq.io.message.ProducerId;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 17:27 2017/12/27
 * @Description:
 */
public class DestinationIdSchema implements Schema<DestinationId> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
        NAME_MAP.put("messageId",2);
    }


    @Override
    public void mergeFrom(Input input, DestinationId message) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch (number){
                case 0:
                    return;
                case 1:
                    message.setId(input.readString());
                    break;
                case 2:
                    message.setMessageId(input.mergeObject((MessageId) null,Schemas.MESSAGE_ID));
                    break;
                default:
                    input.handleUnknownField(number,this);
            }
        }
    }

    @Override
    public void writeTo(Output output, DestinationId message) throws IOException {
        if (message.getMessageId() != null){
            output.writeObject(2,message.getMessageId(),Schemas.MESSAGE_ID,false);
        }
        if (message.id() != null){
            output.writeString(1,message.id(),false);
        }
    }

    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "id";
            case 2:
                return "messageId";
            default:
                return null;
        }
    }

    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(DestinationId message) {
        return message != null && message.getMessageId() != null;
    }

    @Override
    public DestinationId newMessage() {
        return new DestinationId();
    }

    @Override
    public String messageName() {
        return DestinationId.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return DestinationId.class.getName();
    }

    @Override
    public Class<? super DestinationId> typeClass() {
        return DestinationId.class;
    }

}
