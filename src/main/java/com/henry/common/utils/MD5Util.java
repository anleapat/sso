package com.henry.common.utils;

import java.security.MessageDigest;

/**
 * MD5加密工具类
 * 
 * <pre>
 *
 * Copyright: Copyright XXX 2015, Inc. All rights reserved Company leajoy
 * 
 * @author Yerne
 * @version 1.0
 * @date 2015年4月24日 下午6:22:05
 * @history
 */
public class MD5Util {

    public static String getMD5(byte[] source) {
        String str = null;
        // 用来将字节转换成16进制表示的字符
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(source);

            // MD5的计算结果是一个128位的长整数
            byte tmp[] = md.digest();

            // 用字节表示就是16个字节
            char s[] = new char[16 * 2];

            // 表示16成16进制需要32个字符
            int k = 0;// 表示转换结果中对应的字符为止
            for (int i = 0; i < 16; i++) {

                // 从第一个字节开始，对MD5的某一个字节转换成16进制字符的转换
                byte byte0 = tmp[i];// 取第i个字节

                s[k++] = hexDigits[byte0 >>> 4 & 0xf];// 去掉字节中高4位的数字转换

                // >>>为右逻辑右移，将符号位一起右移
                s[k++] = hexDigits[byte0 & 0xf];// 取字节中低4位的数字转换
            }
            // 换后的结果转换为字符串
            str = new String(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void main(String[] args) {
        System.out.println(MD5Util.getMD5("a".getBytes()));
    }
}
