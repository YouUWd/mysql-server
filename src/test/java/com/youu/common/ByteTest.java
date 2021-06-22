package com.youu.common;

import lombok.Data;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/22
 */
@Data
public class ByteTest {
    private byte index = 0;

    public static void main(String[] args) {
        System.out.println(new ByteTest().getIndex());
    }
}
