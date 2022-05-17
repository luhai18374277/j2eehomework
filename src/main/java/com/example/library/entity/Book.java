package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.DocValueFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Book {
    @Id
    @TableId
    private String symbolNum = "未设置";

    private String ISBN = "未设置";
    private String book_name = "未设置";
    private String tag = "未设置";
    private String author = "未设置";
    private String publisher = "未设置";
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishTime;
    @Column(length = 2, scale = 1)
    private BigDecimal score = BigDecimal.valueOf(0.0);
    private Integer scoreNum = 0;
    private Double price = 0.00;

}
