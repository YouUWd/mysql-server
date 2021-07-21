package com.youu.common.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/16
 */
public class StringBufferFactoryTest {

    @Test
    public void test() throws Exception {
        ObjectPool<StringBuffer> pool = new GenericObjectPool<>(
            new StringBufferFactory());

        StringBuffer buffer1 = pool.borrowObject();
        System.out.println(buffer1.hashCode());
        StringBuffer buffer2 = pool.borrowObject();
        System.out.println(buffer2.hashCode());
        pool.returnObject(buffer1);
        StringBuffer buffer3 = pool.borrowObject();
        System.out.println(buffer3.hashCode());
        System.out.println(buffer1 == buffer3);
        pool.invalidateObject(buffer1);
        System.out.println(pool.borrowObject().hashCode());
        pool.returnObject(buffer2);
        System.out.println(pool.borrowObject().hashCode());

    }

    private class StringBufferFactory extends BasePooledObjectFactory<StringBuffer> {

        @Override
        public StringBuffer create() {
            return new StringBuffer();
        }

        /**
         * Use the default PooledObject implementation.
         */
        @Override
        public PooledObject<StringBuffer> wrap(StringBuffer buffer) {
            return new DefaultPooledObject<>(buffer);
        }

        /**
         * When an object is returned to the pool, clear the buffer.
         */
        @Override
        public void passivateObject(PooledObject<StringBuffer> pooledObject) {
            pooledObject.getObject().setLength(0);
        }

        // for all other methods, the no-op implementation
        // in BasePooledObjectFactory will suffice
    }
}
