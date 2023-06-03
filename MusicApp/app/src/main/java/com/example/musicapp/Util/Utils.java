package com.example.musicapp.Util;

import android.util.Log;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * @auther Norton-Lin
 * @date 2023.5.28
 * @brief 工具类
 */
public class Utils {
    // AES加密
    //此处针对网易云音乐的接口进行爬取
    //基本URL是/weapi/cloudsearch/get/web?csrf_token=
    //两个参数 &params (取决于个人账户）
    //       &encSecKey
    public static String encrypt(String content, String key) throws Exception {
        String iv = "0102030405060708";
        if (key == null) {
            return null;
        }
        // 判断Key是否为16位
        if (key.length() != 16) {
            return null;
        }
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
        IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 抓包好烦.jpg 抓不明白
     * @return
     */
    public static String getEncSecKey() {

        return "96082b986b9f636e80c4de5868d9798cd4f5008d09d19c39c21817d36b3df397" +
                "19a9c6d367e249eedba216ce536e839265edc6e1cc5486db3f9545e5c560f32" +
                "9476cf9bb962a3ef63c4ae48c08df1aac1244f056aa1a356becc10bd475bd95b" +
                "80442d17515070f50b7730d43c9db00a151a0d530786d336767df354ab9189e50";
        /**
        return "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152" +
                "b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda" +
                "92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe" +
                "4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";

        return  "622afb7307e3db35bc73db47ded21545b3d0b374075aa2c2e5ee8cb3698bbbf8"+
                "1509647f260838994546934fd854a66a1d05d98ee82f891d5dc9be1a56c194fa"+
                "df0900bfea222cf8835a8aeede5612e9c9f27120990d139d87d0a5a995e234fd9"+
                "dcd07740ea35a5a3afd15c917d56d967fa17e17674e81a4aae28427285fe067";
         */
    }

    public static String getParams(String s, int limit, int offset) {
        // 拼接要加密的字符串
        String tempStr = "{\"hlpretag\":\"<span class=\\\"s-fc7\\\">\",\"hlposttag\":\"</span>\",\"s\":\""
                + s + "\",\"type\":\"1\",\"offset\":\"" + offset
                + "\",\"total\":\"true\",\"limit\":\"" + limit + "\",\"csrf_token\":\"\"}";
        try {
            // 第一次加密的key固定
            // 第二次加密的key和EncSecKey一一对应
            // 返回前encode一下
            String first = encrypt(tempStr, "0CoJUm6Qyw8W8jud");
            //0CoJUm6Qyw8W8jud (["爱心", "女孩", "惊恐", "大笑"])的值
            return URLEncoder.encode(encrypt(first, "9cxqkYv1WsSmRWZ1"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 模拟URL
     * @param s
     * @param offset
     * @return
     */
    public static String getUrl(String s, int offset) {
        // 拼接成查询字符串
        Log.i("Query", "/weapi/cloudsearch/get/web?csrf_token="
                + "&params=" + getParams(s, 20, offset)
                + "&encSecKey=" + getEncSecKey());
        return "/weapi/cloudsearch/get/web?csrf_token="
                + "&params=" + getParams(s, 20, offset)
                + "&encSecKey=" + getEncSecKey();
    }

    // 将int类型的ms值 转换为mm：ss形式的字符串
    public static String timeTransform(int data) {
        data /= 1000;
        String m = "" + data / 60;
        String s = data % 60 < 10 ? ("" + 0 + data % 60) : "" + data % 60;
        return m + ":" + s;
    }
}
