package com.kh21.khpicturebackend.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PicutureEditRequestMessage {
    /**
     * 类型
     */
    private String type;
    /**
     * 执行的编辑动作
     */
    private String editAction;
}
