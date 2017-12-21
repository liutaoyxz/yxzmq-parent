package com.liutaoyxz.yxzmq.io.message.schema;

import com.liutaoyxz.yxzmq.io.message.ConnectionId;
import com.liutaoyxz.yxzmq.io.message.SessionId;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date: 16:52 2017/12/21
 * @Description:
 */
public class SessionIdSchema implements Schema<SessionId>{

    private static final Map<String,Integer> NAME_MAP = new HashMap<>();

    static {
        NAME_MAP.put("id",1);
        NAME_MAP.put("connectionId",2);
    }


    @Override
    public String getFieldName(int number) {
        switch (number){
            case 1:
                return "id";
            case 2:
                return "connectionId";
            default:
                return null;
        }
    }

    @Override
    public void mergeFrom(Input input, SessionId message) throws IOException {
        while (true){
            int number = input.readFieldNumber(this);
            switch (number){
                case 0:
                    return;
                case 1:
                    message.setId(input.readString());
                    break;
                case 2:
                    message.setConnectionId(input.mergeObject((ConnectionId) null,Schemas.CONNECTION_ID_SCHEMA));
                    break;
                default:
                    input.handleUnknownField(number,this);

            }

        }
    }

    @Override
    public void writeTo(Output output, SessionId message) throws IOException {
        if (message.id() != null){
            output.writeString(1,message.id(),false);
        }
        if (message.getConnectionId() != null){
            output.writeObject(2,message.getConnectionId(),Schemas.CONNECTION_ID_SCHEMA,false);
        }
    }
    @Override
    public int getFieldNumber(String name) {
        return NAME_MAP.get(name);
    }

    @Override
    public boolean isInitialized(SessionId message) {
        return message.id() != null && message.getConnectionId() != null;
    }

    @Override
    public SessionId newMessage() {
        return new SessionId();
    }

    @Override
    public String messageName() {
        return SessionId.class.getSimpleName();
    }

    @Override
    public String messageFullName() {
        return SessionId.class.getName();
    }

    @Override
    public Class<? super SessionId> typeClass() {
        return SessionId.class;
    }

}
