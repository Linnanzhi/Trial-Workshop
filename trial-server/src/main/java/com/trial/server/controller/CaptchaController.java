package com.trial.server.controller;

import com.trial.server.common.CaptchaUtil;
import com.trial.server.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 */
@Api(tags = "验证码")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    
    @ApiOperation("获取验证码")
    @GetMapping
    public Result<Map<String, String>> getCaptcha(HttpSession session) {
        String[] captcha = CaptchaUtil.generateCaptcha();
        String code = captcha[0];
        String image = captcha[1];
        
        // 存储到session，不区分大小写
        session.setAttribute("captcha", code.toLowerCase());
        
        Map<String, String> result = new HashMap<>();
        result.put("image", image);
        
        return Result.success(result);
    }
}
