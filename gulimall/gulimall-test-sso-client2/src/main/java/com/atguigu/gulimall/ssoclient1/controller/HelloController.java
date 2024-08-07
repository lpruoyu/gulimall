package com.atguigu.gulimall.ssoclient1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    @GetMapping("/boss")
    public String boss(Model model, HttpSession session,
                            @RequestParam(value = "token",required = false) String token){
        if(!StringUtils.isEmpty(token)){
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity =
                    restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser",body);
        }

        Object loginUser = session.getAttribute("loginUser");
        if(loginUser==null){
            return "redirect:"+ssoServerUrl+"?redirect_url=http://client2.com:8082/boss";
        }else{
            List<String> boss = new ArrayList<>();
            boss.add("666");
            boss.add("777");
            model.addAttribute("boss",boss);
            return "list";
        }
    }

}
