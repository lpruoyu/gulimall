package com.atguigu.gulimall.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthApplicationTests {
    @Test
    public void contextLoads() {
        //MD5是不可逆的，但是利用它的抗修改性（一个字符串的MD5值永远是那个值），发明了彩虹表（暴力破解）。
        //所以，MD5不能直接进行密码的加密存储;
//        String s = DigestUtils.md5Hex("123456");

        //盐值加密；随机值 加盐 ：$1$ + 8位字符
//        只要是同一个材料，做出来的饭是一样的，如果给饭里随机撒点“盐”，那么，饭的口味就不一样了
        //"123456"+System.currentTimeMillis();

        //想要再次验证密码咋办？： 将密码再进行盐值（去数据库查当时保存的随机盐）加密一次，然后再去匹配密码是否正确
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes()); //随机盐
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$qqqqqqqq"); //指定盐
//        System.out.println(s1);

//        给数据库加字段有点麻烦，Spring有好用的工具：
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encode = passwordEncoder.encode("123456");
//        $2a$10$coLmFyeppkTPTfD0RJgqL.nx33s0wvUmj.shqEM/6hvwOO4TWiGmy
//        $2a$10$4IP4F/2iFO2gbSvQKyJzGuI3RhU5Qdtr519KsyoXGAy.b7WT4P1RW
//        $2a$10$0hEI3vMkTbTqK76990MGu.s9QKrkjDSpgyhfzR4zsy07oKB9Jw.PS

//        System.out.println(encode);
//        boolean matches = passwordEncoder.matches("123456", "$2a$10$0hEI3vMkTbTqK76990MGu.s9QKrkjDSpgyhfzR4zsy07oKB9Jw.PS");
        boolean matches = passwordEncoder.matches("lpruoyu123", "$2a$10$m7TmOQAin5Tj6QzV1TT0ceW6iLypdN8LHkYP16DUEngJUfYNgWVEm");
        System.out.println(matches);
    }
}
