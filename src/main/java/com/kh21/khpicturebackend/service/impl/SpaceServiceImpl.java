package com.kh21.khpicturebackend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.mapper.SpaceMapper;
import com.kh21.khpicturebackend.model.dto.space.SpaceAddRequesst;
import com.kh21.khpicturebackend.model.dto.space.SpaceQueryRequest;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.SpaceUser;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.SpaceLevelEnum;
import com.kh21.khpicturebackend.model.enums.SpaceRoleEnum;
import com.kh21.khpicturebackend.model.enums.SpaceTypeEnum;
import com.kh21.khpicturebackend.model.enums.UserRoleEnum;
import com.kh21.khpicturebackend.model.vo.SpaceVO;
import com.kh21.khpicturebackend.model.vo.UserVO;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.SpaceUserService;
import com.kh21.khpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author KH2120
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-07-21 15:43:22
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    UserService userService;
    @Resource
    SpaceUserService spaceUserService;
    @Resource
    TransactionTemplate transactionTemplate;

    @Override
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);

        if (isAdd) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceLevelEnum == null && spaceLevel != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (spaceTypeEnum == null && spaceType != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }

    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }

    }

    @Override
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = new SpaceVO();

        BeanUtils.copyProperties(space, spaceVO);

        Long userId = space.getUserId();

        if (userId != null && userId > 0) {
            User loginUser = userService.getById(userId);
            UserVO userVO = userService.getUserVo(loginUser);
            spaceVO.setUser(userVO);
        }

        return spaceVO;
    }

    /**
     * 获取QueryWrapper
     *
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return spaceQueryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        Long userId = spaceQueryRequest.getUserId();
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        spaceQueryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceType), "spaceType", spaceType);


        return spaceQueryWrapper;
    }

    @Override
    public Long addSpace(SpaceAddRequesst spaceAddRequesst, User loginUser) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequesst, space);
        // 填充默认值
        if (StrUtil.isBlank(spaceAddRequesst.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        // 默认为普通版
        if (spaceAddRequesst.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 默认为私人空间
        if (spaceAddRequesst.getSpaceType() == null) {
            space.setSpaceLevel(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充版本数据
        fillSpaceBySpaceLevel(space);
        // 校验参数
        validSpace(space, true);

        if (!userService.isAdmin(loginUser) && !spaceAddRequesst.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别空间");
        }
        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 加锁
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long resultId = transactionTemplate.execute(status -> {
//                if (!userService.isAdmin(loginUser)) {
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId) //
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间仅能创建一个");
//                }
                boolean saved = this.save(space);
                ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);


                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequesst.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(loginUser.getId());
                    boolean saved1 = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!saved1, ErrorCode.OPERATION_ERROR, "创建团队成员失败");
                }

                return space.getId();
            });
            return Optional.ofNullable(resultId).orElse(-1L);
        }

    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }


}




