package com.kh21.khpicturebackend.manager.websocket.model;

import com.kh21.khpicturebackend.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {
    /**
     * 类型
     */
    private String type;
    /**
     * 执行的编辑动作
     */
    private String editAction;
    /**
     * 消息
     */
    private String message;

    /**
     * 用户信息
     */
    private UserVO user;
}
