package com.liutaoyxz.yxzmq.io.message;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringUtils;

import javax.jms.*;

import java.util.*;

import static com.liutaoyxz.yxzmq.io.message.AbstractYxzMessage.MessageProperty.*;

/**
 * @author Doug Tao
 * @Date: 9:34 2017/12/21
 * @Description: 实现message 接口公用部分
 * 所有子类都是按照jms规范实现,每个子类需要有一个schema
 */
public abstract class AbstractYxzMessage implements Message {

    public static final String MESSAGE_ID_PREFIX = "ID:";

    public static final int MAX_PRIORITY = 10;

    /**
     * 消息id,经过封装,包含producerId 对象等
     **/
    protected MessageId messageId;

    /**
     * 消息发送时的时间,不是创建的时间
     **/
    protected Long timestamp;

    /**
     * 发送地址
     **/
    protected Destination destination;

    /**
     * 回复地址
     **/
    protected Destination replyDestination;

    /**
     * 过期时间
     **/
    protected Long expiration;

    /** 是否可写 **/
    protected boolean writable = true;

    /**
     * properties
     **/
    protected Map<String, MessageProperty> properties = new HashMap<>();

    /**
     * 优先级
     */
    protected int priority = Message.DEFAULT_PRIORITY;


    @Override
    public String getJMSMessageID() throws JMSException {
        return null;
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {

    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return this.timestamp == null ? 0 : timestamp;
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.timestamp = timestamp;
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return this.replyDestination;
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.replyDestination = replyTo;
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return this.destination;
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        this.destination = destination;
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {

    }

    @Override
    public String getJMSType() throws JMSException {
        return null;
    }

    @Override
    public void setJMSType(String type) throws JMSException {

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return this.expiration;
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        if (expiration <= 0) {
            //没有过期时间
            this.expiration = null;
            return;
        }
        this.expiration = expiration;
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        checkPropertyName(name);
        return properties.containsKey(name);
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == BOOLEAN_TYPE){
            return ((Boolean)value).booleanValue();
        }
        if (aClass == STRING_TYPE){
            return new Boolean((String) value).booleanValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to boolean");
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == BYTE_TYPE){
            return ((Byte)value).byteValue();
        }
        if (aClass == STRING_TYPE){
            return new Byte((String)value).byteValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to byte");
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == BYTE_TYPE){
            return ((Byte)value).shortValue();
        }
        if (aClass == SHORT_TYPE){
            return ((Short)value).shortValue();
        }
        if (aClass == STRING_TYPE){
            return new Short((String)value).shortValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to short");
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == BYTE_TYPE){
            return ((Byte)value).intValue();
        }
        if (aClass == SHORT_TYPE){
            return ((Short)value).intValue();
        }
        if (aClass == INT_TYPE){
            return ((Integer)value).intValue();
        }
        if (aClass == STRING_TYPE){
            return new Integer((String)value).intValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to int");
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == BYTE_TYPE){
            return ((Byte)value).longValue();
        }
        if (aClass == SHORT_TYPE){
            return ((Short)value).longValue();
        }
        if (aClass == INT_TYPE){
            return ((Integer)value).longValue();
        }
        if (aClass == LONG_TYPE){
            return ((Long)value).longValue();
        }
        if (aClass == STRING_TYPE){
            return new Long((String)value).longValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to long");
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == FLOAT_TYPE){
            return ((Float)value).floatValue();
        }
        if (aClass == STRING_TYPE){
            return new Float((String)value).floatValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to float");
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            throw new NullPointerException("property [" + name +  "] is null");
        }
        Class aClass = property.getType();
        Object value = property.getValue();
        if (aClass == FLOAT_TYPE){
            return ((Float)value).doubleValue();
        }
        if (aClass == DOUBLE_TYPE){
            return ((Double)value).doubleValue();
        }
        if (aClass == STRING_TYPE){
            return new Double((String)value).doubleValue();
        }
        throw new MessageFormatException("value [" + value + "] can not convert to double");
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            return null;
        }
        Object value = property.getValue();
        String s = value.toString();
        if (s == null){
            throw new MessageFormatException("value [" + value + "] can not convert to String");
        }
        return s;
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        checkPropertyName(name);
        MessageProperty property = properties.get(name);
        if (property == null){
            return null;
        }
        Object value = property.getValue();
        return value;
    }

    @Override
    public Enumeration<String> getPropertyNames() throws JMSException {
        Map<String, MessageProperty> ups = getProperties();
        Vector<String> vns = new Vector<>(ups.keySet());
        return vns.elements();
    }


    private Map<String,MessageProperty> getProperties(){
        if (properties.isEmpty()){
            return Collections.EMPTY_MAP;
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Boolean.class,name,new Boolean(value)));
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Byte.class,name,new Byte(value)));
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Short.class,name,new Short(value)));
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Integer.class,name,new Integer(value)));
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Long.class,name,new Long(value)));
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Float.class,name,new Float(value)));
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(Double.class,name,new Double(value)));
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        checkWritable();
        checkPropertyName(name);
        properties.put(name,new MessageProperty(String.class,name,value));
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        checkPropertyName(name);
        Class type = checkObjectProperty(value);
        MessageProperty property = new MessageProperty(type, name, value);
        properties.put(name, property);
    }

    private Class checkObjectProperty(Object value) throws MessageFormatException {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Class<?> aClass = value.getClass();
        if (aClass == Byte.class) {
            return BYTE_TYPE;
        }
        if (aClass == Short.class) {
            return SHORT_TYPE;
        }
        if (aClass == Integer.class) {
            return INT_TYPE;
        }
        if (aClass == Long.class) {
            return LONG_TYPE;
        }
        if (aClass == Float.class) {
            return FLOAT_TYPE;
        }
        if (aClass == Double.class) {
            return DOUBLE_TYPE;
        }
        if (aClass == Character.class) {
            return CHAR_TYPE;
        }
        if (aClass == Boolean.class) {
            return BOOLEAN_TYPE;
        }
        if (aClass == String.class) {
            return STRING_TYPE;
        }
        throw new MessageFormatException("property " + value + "type error");
    }

    private void checkPropertyName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException();
        }
    }

    private void checkWritable() throws MessageNotWriteableException {
        if (!writable){
            throw new MessageNotWriteableException("message is readOnly");
        }
    }

    @Override
    public void clearProperties() throws JMSException {
        this.properties = new HashMap<>(10);
    }

    @Override
    public void clearBody() throws JMSException {

    }

    @Override
    public int getJMSPriority() throws JMSException {
        return this.priority;
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        if (priority < 1){
            this.priority = 1;
            return;
        }
        if (priority > MAX_PRIORITY){
            this.priority = MAX_PRIORITY;
            return;
        }
    }

    /**
     * 消息的property 属性对象
     */
    public static class MessageProperty<T> {


        private Class type;

        private String name;

        private T value;

        public MessageProperty() {
        }

        public MessageProperty(Class type, String name, T value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public static final Class<Byte> BYTE_TYPE = Byte.class;
        public static final Class<Short> SHORT_TYPE = Short.class;
        public static final Class<Integer> INT_TYPE = Integer.class;
        public static final Class<Long> LONG_TYPE = Long.class;
        public static final Class<Float> FLOAT_TYPE = Float.class;
        public static final Class<Double> DOUBLE_TYPE = Double.class;
        public static final Class<Boolean> BOOLEAN_TYPE = Boolean.class;
        public static final Class<Character> CHAR_TYPE = Character.class;
        public static final Class<String> STRING_TYPE = String.class;

        public Class getType() {
            return type;
        }

        public void setType(Class type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "MessageProperty{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AbstractYxzMessage{" +
                "messageId=" + messageId +
                ", timestamp=" + timestamp +
                ", destination=" + destination +
                ", replyDestination=" + replyDestination +
                ", expiration=" + expiration +
                ", writable=" + writable +
                ", properties=" + properties +
                '}';
    }
}