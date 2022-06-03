package com.example.library.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date=getMonthDate(new Date(),1);
        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy年MM月dd日");
        System.out.println("后一个月的时间:"+sdf1.format(date));
    }
    /**
     * 获取startDate日期后month月的日期
     * @param startDate 开始日期
     * @param month  几个月后
     * @return
     */
    public static Date getMonthDate(Date startDate, int month){
        LocalDateTime localDateTime = startDate.toInstant()
                .atZone(ZoneId.systemDefault() )
                .toLocalDateTime().plusMonths(month);
        Date date = Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
        return date;
    }
}
