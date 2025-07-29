package com.kh21.khpicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.kh21.khpicturebackend.manager.auth.SpaceUserAuthManager;
import com.kh21.khpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.kh21.khpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.kh21.khpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.kh21.khpicturebackend.manager.websocket.model.PicutureEditRequestMessage;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.service.PictureService;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.UserService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PictureEditHandler extends TextWebSocketHandler {
    @Resource
    UserService userService;

    @Resource
    PictureService pictureService;

    @Resource
    SpaceService spaceService;

    @Resource
    SpaceUserAuthManager spaceUserAuthManager;
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage responseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            String message = objectMapper.writeValueAsString(responseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession socketSession : sessionSet) {
                if (excludeSession != null && excludeSession.equals(socketSession)) {
                    continue;
                }
                if (socketSession.isOpen()) {
                    socketSession.sendMessage(textMessage);
                }
            }
        }

    }

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage responseMessage) throws IOException {
        broadcastToPicture(pictureId, responseMessage, null);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.put(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getText());
        String message = String.format("%s 加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVo(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 解析
        PicutureEditRequestMessage requestMessage = JSONUtil.toBean(message.getPayload(), PicutureEditRequestMessage.class);
        String type = requestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);
        // 公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                handleEnterEditMessage(requestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                handleEditActionMessage(requestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                handleExitEditMessage(requestMessage, session, user, pictureId);
                break;
        }
    }

    private void handleExitEditMessage(PicutureEditRequestMessage requestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditingUsers.remove(pictureId);

            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("%s退出编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }

    }

    private void handleEditActionMessage(PicutureEditRequestMessage requestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = requestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            return;
        }
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%s", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            pictureEditResponseMessage.setEditAction(editAction);
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }

    }

    private void handleEnterEditMessage(PicutureEditRequestMessage requestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        if (!pictureEditingUsers.containsKey(pictureId)) {
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("%s正在编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");
        handleExitEditMessage(null, session, user, pictureId);
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);

        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVo(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }


}
