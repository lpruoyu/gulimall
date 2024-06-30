// This file is auto-generated, don't edit it. Thanks.
package com.atguigu.gulimall.thirdparty.controller;

import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class SendSmsController {

    @Autowired
    private StaticCredentialProvider provider;

    /**
     * 提供接口，供别的服务调用
     *
     * @param phone
     * @param code
     * @return "body": {
     * "bizId": "774515119736291045^0",
     * "code": "OK",
     * "message": "OK",
     * "requestId": "D6BD5A90-8755-5C82-B631-0F40AB7B41B0"
     * }
     */
    @GetMapping("/sms/send")
    public R sendSms(@RequestParam("phone") String phone, @RequestParam("code") String code) throws ExecutionException, InterruptedException {

        AsyncClient client = AsyncClient.builder().region("cn-shanghai") // Region ID
                .credentialsProvider(provider).overrideConfiguration(ClientOverrideConfiguration.create().setEndpointOverride("dysmsapi.aliyuncs.com")).build();

        SendSmsRequest sendSmsRequest = SendSmsRequest.builder().signName("阿里云短信测试").templateCode("SMS_154950909").phoneNumbers(phone).templateParam("{\"code\":\"" + code + "\"}").build();

        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        SendSmsResponse resp = response.get();

        /*
            {
                "headers": {
                    "Keep-Alive": "timeout\u003d25" ......
                },
                "statusCode": 200,
                "body": {
                    "bizId": "774515119736291045^0",
                    "code": "OK",
                    "message": "OK",
                    "requestId": "D6BD5A90-8755-5C82-B631-0F40AB7B41B0"
                }
            }
         */
        client.close();

        if (resp.getBody().getMessage().equalsIgnoreCase("OK")) return R.ok();
        return R.error(BizCodeEnume.SMS_SEND_EXCEPTION);
    }

}