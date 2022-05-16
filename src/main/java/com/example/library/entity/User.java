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
public class User {
    @Id
    @TableId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id = 0;
    private String name;
    private String avatar;
    private Integer identity;
    //1：普通用户，2：学者
    private String mail;
    private String pwd;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private String bio;
    private Integer state;
    //0：未封禁，1：禁言，2：封禁
    private Integer gender;
    //0：未知，1：男，2：女
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate = new Date();
    private Boolean isAdmin;
    private String vercode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date codeDate;
    private Integer scholarid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date banDate;   //封禁时间
}
