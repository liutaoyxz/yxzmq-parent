package com.liutaoyxz.yxzmq.io.derby;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Doug Tao
 * @Date 下午11:09 2017/12/1
 * @Description: derby templete,执行 持久化操作
 */
public class DerbyTemplate {

    public static final Logger log = LoggerFactory.getLogger(DerbyTemplate.class);

    private static final int DEFAULT_MAXIDLE = 10;

    private static final int DEFAULT_MAXTOTAL = 10;

    private static final int DEFAULT_MINIDLE = 5;

    /**
     * database name
     */
    private static final String DATABASE = "yxzmq";

    private int maxIdle;

    private int maxTotal;

    private int minIdle;

    /**
     * 执行sql任务的线程池
     */
    private ExecutorService executor;

    /**
     * 数据目录
     */
    private String dataDir;

    private GenericObjectPool<Connection> pool;

    private static DerbyTemplate template;

    private DerbyTemplate(int maxIdle, int maxTotal, int minIdle, String dataDir) {
        if (maxTotal <= 0 || minIdle < 0 || minIdle > maxIdle
                || maxIdle > maxTotal || maxIdle < 0) {
            throw new IllegalArgumentException();
        }
        this.maxIdle = maxIdle;
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.dataDir = dataDir;
        this.executor = new ThreadPoolExecutor(minIdle, maxTotal, 5L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("derby-task");
                return thread;
            }
        });
    }


    public synchronized static DerbyTemplate createTemplate(int maxIdle, int maxTotal, int minIdle, String dataDir) throws Exception {
        log.info("start create derby db");
        if (template == null) {
            template = new DerbyTemplate(maxIdle, maxTotal, minIdle, dataDir);
            String url = createUrl();
            template.initDB(url);
            DerbyPoolFactory factory = new DerbyPoolFactory(url, DATABASE);
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxIdle(maxIdle);
            config.setMaxTotal(maxTotal);
            config.setMinIdle(minIdle);
            template.pool = new GenericObjectPool<>(factory, config);
        }
        log.info("create derby db finished");
        return template;
    }

    public synchronized static DerbyTemplate createTemplate(String dataDir) throws Exception {
        return createTemplate(DEFAULT_MAXIDLE, DEFAULT_MAXTOTAL, DEFAULT_MINIDLE, dataDir);
    }


    /**
     * 检查目录,生成url  格式  jdbc:derby:dir/yxzmq/db;create=true
     *
     * @return
     * @throws IOException
     */
    private static String createUrl() throws IOException {
        final String dir = template.dataDir;
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
        log.info("derby url is {}", url);
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = null;
        Statement sm = null;
        String createTable = "CREATE TABLE queue " +
                "(" +
                "  ID int PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "  QUEUE_ID varchar(255) NOT NULL," +
                "  BROKER_NAME varchar(255) NOT NULL," +
                "  QUEUE_NAME varchar(255) NOT NULL," +
                "  MESSAGE varchar(255) NOT NULL," +
                "  CREATE_TIME timestamp DEFAULT current timestamp" +
                ")";

        try {
            conn = DriverManager.getConnection(url);
            sm = conn.createStatement();
            sm.execute("create SCHEMA " + DATABASE);
            sm.execute("SET SCHEMA " + DATABASE);
            sm.execute(createTable);
        } finally {
            if (sm != null) {
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


    public boolean insertQueue(QueueModal queue,boolean sync) throws Exception {
        if (queue == null) {
            return false;
        }
        String sql = "INSERT INTO QUEUE (QUEUE_ID,BROKER_NAME,QUEUE_NAME,MESSAGE) VALUES(?,?,?,?)";
        Object[] params = new Object[4];
        params[0] = queue.getQueueId();
        params[1] = queue.getBrokerName();
        params[2] = queue.getQueueName();
        params[3] = queue.getMessage();
        DbTask task = new DbTask(sql, params);
        Future<Boolean> future = executor.submit(task);
        if (sync) {
            //异步执行
            return true;
        }
        return future.get();
    }

    public List<QueueModal> selectByBrokerName(String brokerName) throws Exception {
        if (StringUtils.isBlank(brokerName)) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM queue WHERE BROKER_NAME = ?";
        Connection conn = pool.borrowObject();
        PreparedStatement pstm = null;
        List<QueueModal> result = null;
        try {
            pstm = conn.prepareCall(sql);
            pstm.setString(1, brokerName);
            ResultSet resultSet = pstm.executeQuery();
            int size = resultSet.getFetchSize();
            result = new ArrayList<>(size);
            while (resultSet.next()) {
                String message = resultSet.getString("MESSAGE");
                String queueId = resultSet.getString("QUEUE_ID");
                String queueName = resultSet.getString("QUEUE_NAME");
                int id = resultSet.getInt("ID");
                QueueModal queue = new QueueModal();
                queue.setBrokerName(brokerName);
                queue.setId(id);
                queue.setMessage(message);
                queue.setQueueId(queueId);
                queue.setQueueName(queueName);
                result.add(queue);
            }
        } finally {
            pstm.close();
            pool.returnObject(conn);
        }
        return result;
    }

    /**
     * 根据brokerName 和 queueId 删除消息
     *
     * @param brokerName
     * @param queueId
     * @throws Exception
     */
    public boolean deleteByQueueIdAndBrokerName(String brokerName, String queueId, boolean sync) throws Exception {
        String sql = "DELETE FROM queue WHERE BROKER_NAME = ? AND queueId = ?";
        Object[] params = new Object[2];
        params[0] = brokerName;
        params[1] = queueId;
        DbTask task = new DbTask(sql, params);
        Future<Boolean> future = executor.submit(task);
        if (sync) {
            //异步执行
            return true;
        }
        return future.get();
    }

    /**
     * 根据brokerName 删除消息
     *
     * @param brokerName
     * @param sync       是否同步,false 同步, true 异步
     * @throws Exception
     */
    public boolean deleteByBrokerName(String brokerName, boolean sync) throws Exception {
        String sql = "DELETE FROM queue WHERE BROKER_NAME = ?";
        Object[] params = new Object[1];
        params[0] = brokerName;
        DbTask task = new DbTask(sql, params);
        Future<Boolean> future = executor.submit(task);
        if (sync) {
            //异步执行
            return true;
        }
        return future.get();
    }

    /**
     * sql 任务
     */
    class DbTask implements Callable<Boolean> {

        private final Logger log = LoggerFactory.getLogger(DbTask.class);

        private String sql;

        private Object[] params;

        DbTask(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }

        @Override
        public Boolean call() {
            GenericObjectPool<Connection> pool = DerbyTemplate.this.pool;
            Connection conn = null;
            PreparedStatement pstm = null;
            try {
                conn = pool.borrowObject();
                pstm = conn.prepareCall(sql);
                if (params != null && params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        pstm.setObject((i + 1), params[i]);
                    }
                }
                return pstm.execute();
            } catch (Exception e) {
                log.error("run sql task error", e);
            } finally {
                try {
                    if (pstm != null) pstm.close();
                } catch (SQLException e) {
                    log.error("preparedStatement close error", e);
                }
                pool.returnObject(conn);
            }
            return false;
        }
    }

}