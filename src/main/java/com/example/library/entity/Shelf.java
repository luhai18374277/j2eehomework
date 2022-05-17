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
public class Shelf {
    @Id
    @TableId
    private String bid = "未设置";

    private String symbolNum;       //外键，未初始化
    private String address = "未设置";
    private Integer totalHistory = 0;
    private Integer bookState = 0;      //设计书上是char 这里选用int，0:"未知"，1:"借出"，2:"归还"

}
