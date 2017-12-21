package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.ConnectionId;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 15:13 2017/12/21
 * @Description:
 */
public class ConnectionIdSchema implements Schema<ConnectionId> {

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
    }

    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "id";
            default:
                return null;
        }
    }


    @Override
    public void mergeFrom(Input input, ConnectionId message) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch (number){
                case 0:
                    return;
                case 1:
                    message.setId(input.readString());
                    break;
                default:
                    input.handleUnknownField(number, this);
            }
        }
    }

    @Override
    public void writeTo(Output output, ConnectionId message) throws IOException {
        if (message.id() != null){
            output.writeString(1,message.id(),false);
        }
    }

    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(ConnectionId message) {
        return message.id() != null;
    }

    @Override
    public ConnectionId newMessage() {
        return new ConnectionId();
    }

    @Override
    public String messageName() {
        return ConnectionId.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return ConnectionId.class.getName();
    }

    @Override
    public Class<? super ConnectionId> typeClass() {
        return ConnectionId.class;
    }

}
