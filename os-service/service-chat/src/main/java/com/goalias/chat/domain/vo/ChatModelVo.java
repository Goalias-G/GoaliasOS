package com.goalias.chat.domain.vo;


import com.goalias.common.web.annotation.Sensitive;
import com.goalias.common.web.core.SensitiveStrategy;
import lombok.Data;


import java.io.Serial;
import java.io.Serializable;



/**
 * 聊天模型视图对象 chat_model
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class ChatModelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String modelDescribe;

    /**
     * 模型价格
     */
    private Double modelPrice;

    /**
     * 计费类型
     */
    private String modelType;

    /**
     * 是否显示
     */
    private String modelShow;

    /**
     * 模型维度
     */
    private Integer dimension;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 请求地址
     */
    private String apiHost;

    /**
     * 密钥
     */
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String apiKey;

    /**
     * 优先级(值越大优先级越高)
     */
    private Integer priority;

    /**
     * 模型供应商
     */
    private String ProviderName;

    /**
     * 备注
     */
    private String remark;


}