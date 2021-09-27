package com.lyloou.seckill.redis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {


    public static volatile int num = 0;

    public static void main(String[] args) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 20L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                testLock();
            });
        }

    }

    public static void testLock() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        JedisLock jedisLock = new JedisLock(jedis, "test", 8000, 3000);
        try {
            //第一步：获取锁
            if (jedisLock.lock()) {
                num += 1;
                //该步骤是为了验证操作超时
                if (num == 3) {
                    Thread.sleep(5000);
                }
                System.out.println(num);
                //第二步：验证是否操作超时
                if (!jedisLock.checkTimeOut()) {
                    System.out.println("######操作超时#######");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //第三步：释放锁
            if (null != jedisLock) {
                jedisLock.release();
            }
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
