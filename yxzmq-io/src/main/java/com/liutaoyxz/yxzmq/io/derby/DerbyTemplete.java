package com.liutaoyxz.yxzmq.io.derby;

import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * @author Doug Tao
 * @Date 下午11:09 2017/12/1
 * @Description: derby templete,执行 持久化操作
 */
public class DerbyTemplete {

    public static final Logger log = LoggerFactory.getLogger(DerbyTemplete.class);

    private static int DEFAULT_MAXIDLE = 10;

    private static int DEFAULT_MAXTOTAL = 10;

    private static int DEFAULT_MINIDLE = 5;

    /**
     * database name
     */
    private static final String DATABASE = "yxzmq";

    private int maxIdle;

    private int maxTotal;

    private int minIdle;

    /**
     * 数据目录
     */
    private String dataDir;

    private GenericObjectPool<Connection> pool;

    private static DerbyTemplete templete;

    private DerbyTemplete(int maxIdle, int maxTotal, int minIdle, String dataDir) {
        if (maxTotal <= 0 || minIdle < 0 || minIdle > maxIdle
                || maxIdle > maxTotal || maxIdle < 0) {
            throw new IllegalArgumentException();
        }
        this.maxIdle = maxIdle;
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.dataDir = dataDir;
    }


    public synchronized static DerbyTemplete createTemplete(int maxIdle, int maxTotal, int minIdle, String dataDir) throws Exception {
        log.info("start create derby db");
        if (templete == null) {
            templete = new DerbyTemplete(maxIdle, maxTotal, minIdle, dataDir);
            String url = createUrl();
            templete.initDB(url);
            DerbyPoolFactory factory = new DerbyPoolFactory(url, DATABASE);
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxIdle(maxIdle);
            config.setMaxTotal(maxTotal);
            config.setMinIdle(minIdle);
            templete.pool = new GenericObjectPool<>(factory, config);
        }
        log.info("create derby db finished");
        return templete;
    }

    public synchronized static DerbyTemplete createTemplete(String dataDir) throws Exception {
        return createTemplete(DEFAULT_MAXIDLE,DEFAULT_MAXTOTAL,DEFAULT_MINIDLE,dataDir);
    }


    /**
     * 检查目录,生成url  格式  jdbc:derby:dir/yxzmq/db;create=true
     *
     * @return
     * @throws IOException
     */
    private static String createUrl() throws IOException {
        final String dir = templete.dataDir;
        String dataDir = null;
        if (dir.endsWith("/")) {
            dataDir = dir + "yxzmq";
        } else {
            dataDir = dir + "/yxzmq";
        }
        File file = new File(dataDir);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);
        return "jdbc:derby:" + dataDir + "/db;create=true";
    }

    private void initDB(String url) throws Exception {
        log.info("derby url is {}",url);
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = null;
        Statement sm = null;
        String createTable = "CREATE TABLE queue " +
                "(" +
                "  ID int PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "  QUEUE_ID varchar(255) UNIQUE NOT NULL," +
                "  BROKER_NAME varchar(255) NOT NULL," +
                "  NAME varchar(255) NOT NULL," +
                "  CREATE_TIME timestamp DEFAULT current timestamp" +
                ")";

        try {
            conn = DriverManager.getConnection(url);
            sm = conn.createStatement();
            sm.execute("create SCHEMA " + DATABASE);
            sm.execute("SET SCHEMA " + DATABASE);
            sm.execute(createTable);
        } finally {
            if (sm != null){
                sm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void test() throws Exception {
        for (int j = 0; j < 20; j++) {
            Connection conn = pool.borrowObject();
            try {
                String selectSql = "select * from queue";
                Statement sm = conn.createStatement();
                sm.execute(selectSql);
                ResultSet resultSet = sm.getResultSet();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder lableSb = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    lableSb.append(metaData.getColumnName(i) + "\t");
                }
                log.debug(lableSb.toString());
                log.debug("----------------------------------");
                while (resultSet.next()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(resultSet.getString(i) + "\t");
                    }
                    log.debug(sb.toString());
                }
                log.debug("----------------" + (j + 1) + "--------------");
            } finally {
                pool.returnObject(conn);
            }

        }
    }

}
