package com.kh21.khpicturebackend.controller;

import cn.hutool.core.util.RadixUtil;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("你好");
    }
}
