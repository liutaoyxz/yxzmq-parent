package com.liutaoyxz.yxzmq.io.derby;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Doug Tao
 * @Date 下午1:11 2017/12/2
 * @Description:
 */
public class DerbyPoolFactory implements PooledObjectFactory<Connection>{


    /**
     * conn url
     */
    private String url;

    private String database;

    /**
     * 内嵌式的derby
     */
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final Logger log = LoggerFactory.getLogger(DerbyPoolFactory.class);


    DerbyPoolFactory(String url,String database) {
        this.database = database;
        this.url = url;
    }

    /**
     * 创建连接
     * @return
     * @throws Exception
     */
    @Override
    public PooledObject<Connection> makeObject() throws Exception {
        Class.forName(DRIVER);
        Connection connection = DriverManager.getConnection(url);
        return new DefaultPooledObject<>(connection);
    }

    /**
     * 销毁连接
     * @param p
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        Connection conn = p.getObject();
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * 验证连接
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<Connection> p) {
        Connection conn = p.getObject();
        if (conn == null){
            return false;
        }
        try {
            boolean closed = conn.isClosed();
            if (!closed){
                return true;
            }
        } catch (SQLException e) {
            log.error("validate connection error",e);
            return false;
        }
        return false;
    }

    /**
     * 激活连接 设置数据库yxzmq
     * @param p
     * @throws Exception
     */
    @Override
    public void activateObject(PooledObject<Connection> p) throws Exception {
        String setSchema = "set schema " + database;
        Connection conn = p.getObject();
        Statement sm = conn.createStatement();
        sm.execute(setSchema);
    }

    /**
     * 钝化连接
     * @param p
     * @throws Exception
     */
    @Override
    public void passivateObject(PooledObject<Connection> p) throws Exception {

    }
}
