package com.atguigu.common.to;

import lombok.Data;

@Data
public class EmailTo {
    private String receiveMail;
    private String subject;
    private String content;
}
