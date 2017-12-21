package com.liutaoyxz.yxzmq.io.message.schema;

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
 * @Date: 17:27 2017/12/21
 * @Description:
 */
public class MessageIdSchema implements Schema<MessageId> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
        NAME_MAP.put("producerId",2);
    }

    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "id";
            case 2:
                return "producerId";
            default:
                return null;
        }
    }

    @Override
    public void mergeFrom(Input input, MessageId message) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch (number) {
                case 0:
                    return;
                case 1:
                    message.setId(input.readString());
                    break;
                case 2:
                    message.setProducerId(input.mergeObject((ProducerId)null,Schemas.PRODUCER_ID_SCHEMA));
                    break;
                default:
                    input.handleUnknownField(number,this);
            }
        }
    }

    @Override
    public void writeTo(Output output, MessageId message) throws IOException {
        if (message.id() != null){
            output.writeString(1,message.id(),false);
        }
        if (message.getProducerId() != null){
            output.writeObject(2,message.getProducerId(),Schemas.PRODUCER_ID_SCHEMA,false);
        }
    }

    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(MessageId message) {
        return message.id() != null && message.getProducerId() != null;
    }

    @Override
    public MessageId newMessage() {
        return new MessageId();
    }

    @Override
    public String messageName() {
        return MessageId.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return MessageId.class.getName();
    }

    @Override
    public Class<? super MessageId> typeClass() {
        return MessageId.class;
    }

}
