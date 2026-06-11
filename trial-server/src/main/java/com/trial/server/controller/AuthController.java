package com.trial.server.controller;

import com.trial.server.common.Result;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.LoginRequest;
import com.trial.server.dto.RegisterRequest;
import com.trial.server.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        String inputCaptcha = request.getCaptcha();
        
        if (sessionCaptcha == null || inputCaptcha == null || inputCaptcha.trim().isEmpty()) {
            return Result.error(400, "验证码不能为空");
        }
        
        if (!sessionCaptcha.equalsIgnoreCase(inputCaptcha.trim())) {
            return Result.error(400, "验证码错误");
        }
        
        // 验证通过后清除验证码
        session.removeAttribute("captcha");
        
        authService.register(request);
        return Result.success("注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        String inputCaptcha = request.getCaptcha();
        
        if (sessionCaptcha == null || inputCaptcha == null || inputCaptcha.trim().isEmpty()) {
            return Result.error(400, "验证码不能为空");
        }
        
        if (!sessionCaptcha.equalsIgnoreCase(inputCaptcha.trim())) {
            return Result.error(400, "验证码错误");
        }
        
        // 验证通过后清除验证码
        session.removeAttribute("captcha");
        
        Map<String, Object> data = authService.login(request);
        return Result.success("登录成功", data);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<?> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(authService.getUserInfo(userId));
    }
}
