package com.goalias.mcp.tools;

import com.goalias.mcp.tools.support.JsonSchemas;
import com.goalias.mcp.tools.support.McpResults;
import com.goalias.mcp.tools.support.McpToolConstants;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.service.IMcpBusinessService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户画像 MCP 工具
 *
 * @author Goalias
 */
@Component
@RequiredArgsConstructor
public class UserProfileTool implements McpToolContributor {

    private final IMcpBusinessService businessService;

    @Override
    public List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        return List.of(McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder()
                        .name("get_user_profile")
                        .description("获取用户基本个人画像：昵称、邮箱、人生规划、成长等级、自我描述等")
                        .inputSchema(JsonSchemas.empty())
                        .build())
                .callHandler((exchange, request) -> McpResults.call(this::getProfile))
                .build());
    }

    private Map<String, Object> getProfile() {
        SysUserVo user = businessService.getUserProfile(McpToolConstants.DEFAULT_USER_ID);
        if (user == null) {
            return Map.of("message", "用户不存在");
        }
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("nickName", user.getNickName());
        profile.put("userName", user.getUserName());
        profile.put("sex", user.getSex());
        profile.put("email", user.getEmail());
        profile.put("userPlan", user.getUserPlan());
        profile.put("userGrade", user.getUserGrade());
        profile.put("userBalance", user.getUserBalance());
        profile.put("loginLocation", user.getLoginLocation());
        profile.put("createTime", user.getCreateTime());
        return profile;
    }
}
