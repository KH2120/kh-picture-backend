package com.kh21.khpicturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class SpaceAddRequesst implements Serializable {
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    private static final long serialVersionUID = 1L;
}
