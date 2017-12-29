package com.liutaoyxz.yxzmq.io.message.schema;

import io.protostuff.Schema;

/**
 * @author Doug Tao
 * @Date: 16:50 2017/12/21
 * @Description:
 */
public interface Schemas {

    Schema CONNECTION_ID = new ConnectionIdSchema();

    Schema SESSION_ID = new SessionIdSchema();

    Schema PRODUCER_ID = new ProducerIdSchema();

    Schema MESSAGE_ID = new MessageIdSchema();

    Schema DESTINATION_ID = new DestinationIdSchema();

    Schema TOPIC = new YxzTopicSchema();

}
