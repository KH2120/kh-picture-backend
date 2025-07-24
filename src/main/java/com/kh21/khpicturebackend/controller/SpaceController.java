package com.kh21.khpicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kh21.khpicturebackend.annotation.AuthCheck;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.DeleteRequest;
import com.kh21.khpicturebackend.common.ResultUtils;
import com.kh21.khpicturebackend.constant.UserConstant;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.model.dto.picture.*;
import com.kh21.khpicturebackend.model.dto.space.SpaceAddRequesst;
import com.kh21.khpicturebackend.model.dto.space.SpaceEditrequest;
import com.kh21.khpicturebackend.model.dto.space.SpaceQueryRequest;
import com.kh21.khpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.PicturereviewStatusEnum;
import com.kh21.khpicturebackend.model.enums.SpaceLevelEnum;
import com.kh21.khpicturebackend.model.vo.PictureTagCategory;
import com.kh21.khpicturebackend.model.vo.PictureVO;
import com.kh21.khpicturebackend.model.vo.SpaceLevel;
import com.kh21.khpicturebackend.model.vo.SpaceVO;
import com.kh21.khpicturebackend.service.PictureService;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;


    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验数据
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        // 判断空间是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员才可以删除
        spaceService.checkSpaceAuth(loginUser, oldSpace);
        // 操作数据库
        boolean removed = spaceService.removeById(deleteRequest);
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     *
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        // 判断空
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 转实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 空间校验
        spaceService.validSpace(space, false);
        // 判断空间是否存在
        Space oldSpace = spaceService.getById(spaceUpdateRequest.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 自动填充
        spaceService.fillSpaceBySpaceLevel(space);
        // 操作数据
        boolean updated = spaceService.updateById(space);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间（不脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSapceById(Long id) {
        // 判空
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回
        return ResultUtils.success(space);
    }

    /**
     * 根据id获取空间封装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVO(Long id, HttpServletRequest request) {
        // 判空
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回
        return ResultUtils.success(spaceService.getSpaceVO(space));
    }

    /**
     * 分页查询空间列表
     *
     * @param queryRequest
     * @return
     */
    @PostMapping("/list/space")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpacePage(@RequestBody SpaceQueryRequest queryRequest) {
        // 查询
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(queryRequest));
        // 返回
        return ResultUtils.success(spacePage);

    }


    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditrequest spaceEditrequest, HttpServletRequest request) {
        // 判断空
        if (spaceEditrequest == null || spaceEditrequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 转实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditrequest, space);

        // 校验
        spaceService.validSpace(space, false);
        // 判断空间是否存在
        Space oldSpace = spaceService.getById(spaceEditrequest.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可以编辑
        User loginUser = userService.getLoginUser(request);
        spaceService.checkSpaceAuth(loginUser, space);
        // 填充
        spaceService.fillSpaceBySpaceLevel(space);
        // 操作数据
        boolean updated = spaceService.updateById(space);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevel() {
        List<SpaceLevel> spaceLevels = Arrays.stream(SpaceLevelEnum.values()).map(spaceLevelEnum -> new SpaceLevel(
                spaceLevelEnum.getValue(),
                spaceLevelEnum.getText(),
                spaceLevelEnum.getMaxCount(),
                spaceLevelEnum.getMaxSize()
        )).collect(Collectors.toList());

        return ResultUtils.success(spaceLevels);
    }

    /**
     * @param spaceAddRequesst
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequesst spaceAddRequesst, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequesst == null, ErrorCode.PARAMS_ERROR);
        Long id = spaceService.addSpace(spaceAddRequesst, userService.getLoginUser(request));
        return ResultUtils.success(id);
    }

}
