package com.kh21.khpicturebackend.api.imageSearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImageSearchPageUrlApi {
    public static String getImageSearchPageUrl(String imageUrl) {
        // 表单数据
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 时间戳
        long upTime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + upTime;

        try {
            // 发送请求
            HttpResponse response = HttpRequest
                    .post(url)
                    .header("acs-token", RandomUtil.randomString(1))
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用接口失败");
            }
            // 解析结果
            String body = response.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            log.info("result = {}", result);
            // 处理结果
            if (result == null || !result.get("status").equals(Integer.valueOf(0))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用接口失败");
            }

            Map<String, Object> data = (Map<String, Object>) result.get("data");

            String rawUrl = (String) data.get("url");

            String searchPageUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);

            if (searchPageUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未有返回结果");
            }
            return searchPageUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }

    }

    public static void main(String[] args) {
        String imageUrl = "https://www.codefather.cn/logo.png";
        String result = getImageSearchPageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}
