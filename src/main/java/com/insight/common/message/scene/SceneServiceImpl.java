package com.insight.common.message.scene;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.insight.common.message.common.client.LogClient;
import com.insight.common.message.common.client.LogServiceClient;
import com.insight.common.message.common.dto.SceneListDto;
import com.insight.common.message.common.dto.SceneTemplateListDto;
import com.insight.common.message.common.entity.Scene;
import com.insight.common.message.common.entity.SceneTemplate;
import com.insight.common.message.common.mapper.SceneMapper;
import com.insight.utils.ReplyHelper;
import com.insight.utils.Util;
import com.insight.utils.pojo.LoginInfo;
import com.insight.utils.pojo.OperateType;
import com.insight.utils.pojo.Reply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 宣炳刚
 * @date 2019-08-28
 * @remark 消息管理服务
 */
@Service
public class SceneServiceImpl implements SceneService {
    private static final String BUSINESS = "消息场景管理";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LogServiceClient client;
    private final SceneMapper mapper;

    /**
     * 构造方法
     *
     * @param client LogServiceClient
     * @param mapper SceneMapper
     */
    public SceneServiceImpl(LogServiceClient client, SceneMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * 获取消息场景列表
     *
     * @param keyword 查询关键词
     * @param page    分页页码
     * @param size    每页记录数
     * @return Reply
     */
    @Override
    public Reply getScenes(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<SceneListDto> scenes = mapper.getScenes(keyword);
        PageInfo<SceneListDto> pageInfo = new PageInfo<>(scenes);

        return ReplyHelper.success(scenes, pageInfo.getTotal());
    }

    /**
     * 获取消息场景
     *
     * @param id 消息场景ID
     * @return Reply
     */
    @Override
    public Reply getScene(String id) {
        Scene scene = mapper.getScene(id);
        if (scene == null) {
            return ReplyHelper.fail("ID不存在,未读取数据");
        }

        return ReplyHelper.success(scene);
    }

    /**
     * 新增消息场景
     *
     * @param info 用户关键信息
     * @param dto  消息场景DTO
     * @return Reply
     */
    @Override
    public Reply newScene(LoginInfo info, Scene dto) {
        String id = Util.uuid();
        int count = mapper.getSceneCount(id, dto.getCode());
        if (count > 0) {
            return ReplyHelper.invalidParam("场景编码已存在");
        }

        dto.setId(id);
        dto.setCreator(info.getUserName());
        dto.setCreatorId(info.getUserId());
        dto.setCreatedTime(LocalDateTime.now());

        mapper.addScene(dto);
        LogClient.writeLog(info, BUSINESS, OperateType.INSERT, id, dto);

        return ReplyHelper.created(id);
    }

    /**
     * 编辑消息场景
     *
     * @param info 用户关键信息
     * @param dto  消息场景DTO
     * @return Reply
     */
    @Override
    public Reply editScene(LoginInfo info, Scene dto) {
        String id = dto.getId();
        Scene scene = mapper.getScene(id);
        if (scene == null) {
            return ReplyHelper.fail("ID不存在,未更新数据");
        }

        int count = mapper.getSceneCount(id, dto.getCode());
        if (count > 0) {
            return ReplyHelper.invalidParam("场景编码已存在");
        }

        mapper.editScene(dto);
        LogClient.writeLog(info, BUSINESS, OperateType.UPDATE, id, dto);

        return ReplyHelper.success();
    }

    /**
     * 删除消息场景
     *
     * @param info 用户关键信息
     * @param id   消息场景ID
     * @return Reply
     */
    @Override
    public Reply deleteScene(LoginInfo info, String id) {
        Scene scene = mapper.getScene(id);
        if (scene == null) {
            return ReplyHelper.fail("ID不存在,未删除数据");
        }

        int count = mapper.getConfigCount(id);
        if (count > 0) {
            return ReplyHelper.fail("该消息场景下配置有模板,请先删除配置");
        }

        mapper.deleteScene(id);
        LogClient.writeLog(info, BUSINESS, OperateType.DELETE, id, scene);

        return ReplyHelper.success();
    }

    /**
     * 获取场景模板配置列表
     *
     * @param tenantId 租户ID
     * @param sceneId  场景ID
     * @param keyword  查询关键词
     * @param page     分页页码
     * @param size     每页记录数
     * @return Reply
     */
    @Override
    public Reply getSceneTemplates(String tenantId, String sceneId, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<SceneTemplateListDto> templates = mapper.getSceneTemplates(tenantId, sceneId, keyword);
        PageInfo<SceneTemplateListDto> pageInfo = new PageInfo<>(templates);

        return ReplyHelper.success(templates, pageInfo.getTotal());
    }

    /**
     * 添加渠道模板
     *
     * @param info 用户关键信息
     * @param dto  渠道模板DTO
     * @return Reply
     */
    @Override
    public Reply addSceneTemplate(LoginInfo info, SceneTemplate dto) {
        String id = Util.uuid();
        dto.setId(id);
        dto.setCreator(info.getUserName());
        dto.setCreatorId(info.getUserId());
        dto.setCreatedTime(LocalDateTime.now());

        mapper.addSceneTemplate(dto);
        LogClient.writeLog(info, BUSINESS, OperateType.INSERT, id, dto);

        return ReplyHelper.created(id);
    }

    /**
     * 移除渠道模板
     *
     * @param info 用户关键信息
     * @param id   渠道模板ID
     * @return Reply
     */
    @Override
    public Reply removeSceneTemplate(LoginInfo info, String id) {
        SceneTemplate config = mapper.getSceneTemplate(id);
        if (config == null) {
            return ReplyHelper.fail("ID不存在,未删除数据");
        }

        mapper.deleteSceneTemplate(id);
        LogClient.writeLog(info, BUSINESS, OperateType.DELETE, id, config);

        return ReplyHelper.success();
    }

    /**
     * 获取日志列表
     *
     * @param keyword 查询关键词
     * @param page    分页页码
     * @param size    每页记录数
     * @return Reply
     */
    @Override
    public Reply getSceneLogs(String keyword, int page, int size) {
        return client.getLogs(BUSINESS, keyword, page, size);
    }

    /**
     * 获取日志详情
     *
     * @param id 日志ID
     * @return Reply
     */
    @Override
    public Reply getSceneLog(String id) {
        return client.getLog(id);
    }
}
