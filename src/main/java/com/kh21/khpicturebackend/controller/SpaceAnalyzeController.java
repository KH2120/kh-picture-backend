package com.kh21.khpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kh21.khpicturebackend.annotation.AuthCheck;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.DeleteRequest;
import com.kh21.khpicturebackend.common.ResultUtils;
import com.kh21.khpicturebackend.constant.UserConstant;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.model.dto.space.SpaceAddRequesst;
import com.kh21.khpicturebackend.model.dto.space.SpaceEditrequest;
import com.kh21.khpicturebackend.model.dto.space.SpaceQueryRequest;
import com.kh21.khpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.kh21.khpicturebackend.model.dto.space.analyze.*;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.SpaceLevelEnum;
import com.kh21.khpicturebackend.model.vo.SpaceLevel;
import com.kh21.khpicturebackend.model.vo.SpaceVO;
import com.kh21.khpicturebackend.model.vo.space.analyze.*;
import com.kh21.khpicturebackend.service.SpaceAnalyzeService;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;
    @Resource
    private UserService userService;


    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> usageAnalyze(@RequestBody SpaceUsageAnalyzeRequest analyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(analyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse response = spaceAnalyzeService.getSpaceUsageAnalyzeResponse(analyzeRequest, loginUser);
        return ResultUtils.success(response);
    }

    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> categoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest analyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(analyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> list = spaceAnalyzeService.getSpaceCategoryAnalyze(analyzeRequest, loginUser);
        return ResultUtils.success(list);
    }

    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }


    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagsAnalyzeResponse>> tagAnalyze(@RequestBody SpaceTagsAnalyzeRequest analyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(analyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagsAnalyzeResponse> list = spaceAnalyzeService.getSpaceTagsAnalyzeResponse(analyzeRequest, loginUser);
        return ResultUtils.success(list);
    }

    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }


}
