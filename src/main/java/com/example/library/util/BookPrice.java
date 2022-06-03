package com.example.library.util;

public class BookPrice {

    /**
     * 计算罚息
     * 计算方式为书价/30*2*天数
     */
    public static Double getPrice(double bookPrice,long timeZone){
        long dayNums = timeZone/24/60/60/1000;
        return (bookPrice / 30) *(2 *dayNums);
    }
}
