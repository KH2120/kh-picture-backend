package com.kh21.khpicturebackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.kh21.khpicturebackend.annotation.SaSpaceCheckPermission;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.DeleteRequest;
import com.kh21.khpicturebackend.common.ResultUtils;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.kh21.khpicturebackend.model.entity.SpaceUser;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.SpaceUserVO;
import com.kh21.khpicturebackend.service.SpaceUserService;
import com.kh21.khpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {
    @Autowired
    SpaceUserService spaceUserService;
    @Autowired
    UserService userService;

    /**
     * 添加空间用户
     *
     * @param addRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = spaceUserService.addSpaceUser(addRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        SpaceUser old = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        boolean removed = spaceUserService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(removed);
    }

    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = queryRequest.getUserId();
        Long spaceId = queryRequest.getSpaceId();
        ThrowUtils.throwIf(ObjUtil.hasEmpty(userId, spaceId), ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(queryRequest));
        return ResultUtils.success(spaceUser);
    }

    @PostMapping("/get/vo")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUserVO> getSpaceUserVO(@RequestBody SpaceUserQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = queryRequest.getUserId();
        Long spaceId = queryRequest.getSpaceId();
        ThrowUtils.throwIf(ObjUtil.hasEmpty(userId, spaceId), ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(queryRequest));
        SpaceUserVO spaceUserVO = spaceUserService.getSpaceUserVO(spaceUser);
        return ResultUtils.success(spaceUserVO);
    }

    @PostMapping("/get/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> getSpaceUserVOList(@RequestBody SpaceUserQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> list = spaceUserService.list(spaceUserService.getQueryWrapper(queryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(list));
    }

    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest editRequest) {
        ThrowUtils.throwIf(editRequest == null || editRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 校验是否存在
        Long id = editRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);

        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(editRequest, spaceUser);
        spaceUserService.validSpaceUser(spaceUser, false);

        boolean updated = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> list = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(list));
    }


}
