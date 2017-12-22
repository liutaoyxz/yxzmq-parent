package com.liutaoyxz.yxzmq.io.message;

import org.junit.Test;

import javax.jms.JMSException;

import java.util.Enumeration;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 16:04 2017/12/22
 * @Description:
 */
public class AbstractYxzMessageTest {

    static class TestMessage extends AbstractYxzMessage{
        @Override
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
            return new byte[0];
        }

        @Override
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {

        }

        @Override
        public void setJMSCorrelationID(String correlationID) throws JMSException {

        }

        @Override
        public String getJMSCorrelationID() throws JMSException {
            return null;
        }

        @Override
        public boolean getJMSRedelivered() throws JMSException {
            return false;
        }

        @Override
        public void setJMSRedelivered(boolean redelivered) throws JMSException {

        }

        @Override
        public long getJMSDeliveryTime() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSDeliveryTime(long deliveryTime) throws JMSException {

        }

        @Override
        public int getJMSPriority() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSPriority(int priority) throws JMSException {

        }

        @Override
        public void clearProperties() throws JMSException {

        }

        @Override
        public void acknowledge() throws JMSException {

        }

        @Override
        public void clearBody() throws JMSException {

        }

        @Override
        public <T> T getBody(Class<T> c) throws JMSException {
            return null;
        }

        @Override
        public boolean isBodyAssignableTo(Class c) throws JMSException {
            return false;
        }
    }

    @Test
    public void getByteProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getIntProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

    }

    @Test
    public void getShortProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getShortProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getIntProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getIntProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getLongProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getLongProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getFloatProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getFloatProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getDoubleProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getDoubleProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getStringProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getStringProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getObjectProperty() throws Exception {
        TestMessage message = new TestMessage();
        message.setBooleanProperty("boolean",true);
        message.setByteProperty("byte", (byte) 1);
        message.setShortProperty("short", (short) 3);
        message.setIntProperty("int",2);
        message.setLongProperty("long",4L);
        message.setFloatProperty("float",1.5F);
        message.setDoubleProperty("double",1.6D);
        message.setStringProperty("string","string");
        Enumeration<String> names = message.getPropertyNames();

        while (names.hasMoreElements()){
            try {
                String s = names.nextElement();
                System.out.println(s);
                System.out.println(message.getObjectProperty(s));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void getPropertyNames() throws Exception {
    }

}