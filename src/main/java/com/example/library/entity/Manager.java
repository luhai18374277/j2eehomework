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
public class Manager {
    @Id
    @TableId
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //主键且自主递增
    private Integer id = 0;

    private String email;
    private String name;
    private String password;

}
