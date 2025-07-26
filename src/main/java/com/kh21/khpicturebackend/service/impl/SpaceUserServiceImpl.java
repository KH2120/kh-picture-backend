package com.kh21.khpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.SpaceUser;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.SpaceRoleEnum;
import com.kh21.khpicturebackend.model.enums.SpaceTypeEnum;
import com.kh21.khpicturebackend.model.vo.SpaceUserVO;
import com.kh21.khpicturebackend.model.vo.SpaceVO;
import com.kh21.khpicturebackend.model.vo.UserVO;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.SpaceUserService;
import com.kh21.khpicturebackend.mapper.SpaceUserMapper;
import com.kh21.khpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author KH2120
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-07-25 15:30:38
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {
    @Autowired
    UserService userService;
    @Autowired
    @Lazy
    SpaceService spaceService;

    @Override
    public Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);

        // 数据库操作
        boolean saved = this.save(spaceUser);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean isAdd) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时候，userId和spaceId必须不为空
        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        // 增加校验
        if (isAdd) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(userId, spaceId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间角色不存在");
        }

    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) return queryWrapper;
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVO(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVo(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> list) {
        if (CollUtil.isEmpty(list)) return Collections.emptyList();
        List<SpaceUserVO> spaceUserVOList = list.stream().map(SpaceUserVO::objToVO).collect(Collectors.toList());
        // 获取id
        Set<Long> userIdSet = list.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = list.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());

        Map<Long, User> userMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors
                        .toMap(User::getId, user -> user, (existing, replacement) -> existing));

        Map<Long, Space> spaceMap = spaceService.listByIds(spaceIdSet)
                .stream()
                .collect(Collectors.toMap(Space::getId, space -> space, (e, r) -> e));
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            User user = userMap.getOrDefault(userId, null);
            Space space = spaceMap.getOrDefault(spaceId, null);
            UserVO userVo = userService.getUserVo(user);
            SpaceVO spaceVO = spaceService.getSpaceVO(space);
            spaceUserVO.setUser(userVo);
            spaceUserVO.setSpace(spaceVO);
        });
        return spaceUserVOList;
    }
}




