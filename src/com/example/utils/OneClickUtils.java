package com.example.utils;
/**
 * 确保一定时间内至于一次点击有效
 * @author dell
 *
 */
public class OneClickUtils {
    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();   
        if ( time - lastClickTime < 500) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
}
