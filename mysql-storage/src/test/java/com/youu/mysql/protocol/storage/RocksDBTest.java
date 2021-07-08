package com.youu.mysql.protocol.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/7
 */
public class RocksDBTest {
    @Test
    public void test() {
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        try (final Options options = new Options().setCreateIfMissing(true)) {

            // a factory method that returns a RocksDB instance
            try (final RocksDB db = RocksDB.open(options, "/tmp/rocksdb/d1")) {
                db.put("a".getBytes(), "A".getBytes());
                System.out.println(Arrays.toString(db.get("a".getBytes())));
                System.out.println(Arrays.toString(db.get("b".getBytes())));
                // do something
            }
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();

        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {

            // list of column family descriptors, first entry must always be default column family
            final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                new ColumnFamilyDescriptor("my-first-columnfamily".getBytes(), cfOpts),
                new ColumnFamilyDescriptor("my-second-columnfamily".getBytes(), cfOpts)
            );

            // a list which will hold the handles for the column families once the db is opened
            final List<ColumnFamilyHandle> columnFamilyHandleList =
                new ArrayList<>();

            try (final DBOptions options = new DBOptions()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);
                 final RocksDB db = RocksDB.open(options, "/tmp/rocksdb/d2", cfDescriptors,
                     columnFamilyHandleList)) {

                try {
                    // do something
                    System.out.println(columnFamilyHandleList);
                    db.put(columnFamilyHandleList.get(1), "1".getBytes(), "k1".getBytes());
                    db.put(columnFamilyHandleList.get(2), "1".getBytes(), "k2".getBytes());

                    WriteBatch writeBatch = new WriteBatch();
                    writeBatch.put(columnFamilyHandleList.get(1), "2".getBytes(), "kk1".getBytes());
                    writeBatch.put(columnFamilyHandleList.get(2), "2".getBytes(), "kk2".getBytes());

                    db.write(new WriteOptions(), writeBatch);

                    System.out.println(Arrays.toString(db.get("1".getBytes())));
                    System.out.println(new String(db.get(columnFamilyHandleList.get(1), "1".getBytes())));
                    System.out.println(new String(db.get(columnFamilyHandleList.get(2), "1".getBytes())));

                    Map<byte[], byte[]> map = db.multiGet(
                        Lists.newArrayList(columnFamilyHandleList.get(1), columnFamilyHandleList.get(2)),
                        Lists.newArrayList("1".getBytes(), "1".getBytes()));
                    System.out.println(map);
                    map.forEach((k, v) -> System.out.println(new String(k) + ": " + new String(v)));
                } finally {

                    // NOTE frees the column family handles before freeing the db
                    for (final ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                        columnFamilyHandle.close();
                    }
                } // frees the db and the db options
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        } // frees the column family options
    }
}
