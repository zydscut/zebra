提供分布式锁功能，使用Redis

配置说明
```properties
# redis方式的分布式锁，当前只支持redis
zebra.distributed.lock.type=redis

# redis ip或者域名
zebra.distributed.lock.redis.host=172.24.154.238

# redis端口
zebra.distributed.lock.redis.port=6379

# redis数据库索引，非必填，默认为0
zebra.distributed.lock.redis.db=0
   
# redis密码，非必填
zebra.distributed.lock.redis.password=passwordOfRedis
```

样例代码
```Java
// 第二个参数30L为持有锁的超时时间（单位秒），请评估业务执行时间，设置为合理的值
DistributedLock distributedLock = DistributedLocks.getLock(lockName, 30L);
if (distributedLock == null) {
    LOGGER.error("Failed to get lock of name : {}", lockName);
    return "Failed to get lock " + lockName;
}

if (!distributedLock.tryLock()) {
    LOGGER.error("Failed to try lock");
    return "Failed to try lock " + lockName;
}

// 注意，必须用try/finally or try/catch/finally来释放锁
try {
    // 干活
    doSomeThingWithinLock();
}
finally {
    distributedLock.unlock();
}
```