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
public class User {     //需要更新数据库中的表时只会加多余的属性，不会减去删掉的属性，需要的话删表重新运行代码即可
    //属性必须大写，例如int->Integer,bool->Boolean
    @Id     //这些@行只会影响紧接着的一行代码，例如这三行只影响了id，而对email无影响
    @TableId
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //主键且自主递增
    private Integer id = 0;     //=0似乎没用，但可以不改（由于设置了主键格式）
    private String email;
    private String name;
    private String password;
    private String organization="未设置";      //自动初始化值为“未设置”
    @JsonFormat(pattern = "yyyy-MM-dd")     //时间格式设置
    private Date createTime;        //驼峰命名法，数据库中create_time命名成createTime.
    private Integer appointNum=0;       //初始化为0，下同
    private Double debt=0.0;
    private Double deposit=0.0;
    private Integer renew=5;
    private String vercode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date codeDate;

//    private String avatar;
//    private Integer identity;
//    @JsonFormat(pattern = "yyyy-MM-dd")
//    private Date birthday;
//    private String bio;
//    private Integer state;
//    //0：未封禁，1：禁言，2：封禁
//    private Integer gender;
//    //0：未知，1：男，2：女
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date loginDate = new Date();
//    private Boolean isAdmin;

//    private Integer scholarid;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date banDate;   //封禁时间
}
