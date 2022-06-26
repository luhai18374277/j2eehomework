package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Record {
    @Id
    @TableId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rid = 0;

    private Integer id;     //未初始化，外键
    private String symbolNum;       //未初始化，外键
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date borrowTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date returnTime;
    /**
     * 1:已借出；
     * 2：已归还;
     * 3:待领取
     */
    private Integer isReturn=4;       //设计表是char

}
