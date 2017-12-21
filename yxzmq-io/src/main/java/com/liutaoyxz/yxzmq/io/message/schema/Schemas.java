package com.liutaoyxz.yxzmq.io.message.schema;

import io.protostuff.Schema;

/**
 * @author Doug Tao
 * @Date: 16:50 2017/12/21
 * @Description:
 */
public interface Schemas {

    Schema CONNECTION_ID_SCHEMA = new ConnectionIdSchema();

    Schema SESSION_ID_SCHEMA = new SessionIdSchema();

    Schema PRODUCER_ID_SCHEMA = new ProducerIdSchema();

}
