package com.liutaoyxz.yxzmq;

import com.liutaoyxz.yxzmq.io.protocol.Metadata;

import java.util.Arrays;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        BlockingDeque<String> bq = new LinkedBlockingDeque<>();
        bq.add("1");
        bq.add("2");
        bq.add("3");
        bq.add("4");
        bq.add("5");
        bq.add("6");
        System.out.println(bq.poll());

    }

}
