package com.kh21.khpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 4594464712670695759L;
    private Long id;
    private String fileUrl;
    /**
     * 图片名称
     */
    private String picName;
    /**
     * 空间id
     */
    private Long spaceId;


}
