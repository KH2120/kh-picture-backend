package com.kh21.khpicturebackend.model.enums;

import lombok.Getter;
import org.springframework.util.ObjectUtils;
@Getter
public enum PicturereviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final Integer value;

    PicturereviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static PicturereviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (PicturereviewStatusEnum picturereviewStatusEnum : PicturereviewStatusEnum.values()) {
            if (picturereviewStatusEnum.value == value) {
                return picturereviewStatusEnum;
            }
        }

        return null;
    }
}
