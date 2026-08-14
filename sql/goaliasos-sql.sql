create table chat_message
(
    id           bigint auto_increment comment '主键'
        primary key,
    session_id   bigint                     null comment '会话id',
    user_id      bigint                     not null comment '用户id',
    content      longtext                   null comment '消息内容',
    role         varchar(255)               null comment '对话角色',
    deduct_cost  double(20, 2) default 0.00 null comment '扣除金额',
    total_tokens int           default 0    null comment '累计 Tokens',
    model_name   varchar(255)               null comment '模型名称',
    create_by    bigint                     null comment '创建者',
    create_time  datetime                   null comment '创建时间',
    update_by    bigint                     null comment '更新者',
    update_time  datetime                   null comment '更新时间',
    remark       varchar(500)               null comment '备注',
    billing_type char                       null comment '计费类型（1-token计费，2-次数计费，null-普通消息）'
)
    comment '聊天消息表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table chat_model
(
    id             bigint auto_increment comment '主键'
        primary key,
    category       varchar(20)            null comment '模型分类',
    model_name     varchar(50)            null comment '模型名称',
    provider_name  varchar(20)            null comment '模型供应商',
    model_describe varchar(255)           null comment '模型描述',
    model_price    double                 null comment '模型价格（输出/百万Token）',
    model_type     char       default '2' null comment '计费类型(1 - token 2 - 计次 )',
    model_show     char       default '0' null comment '是否显示( 1 - 不显示)',
    api_host       varchar(255)           null comment '请求地址(baseUrl)',
    api_key        varchar(255)           null comment '密钥',
    enable_search  tinyint(1) default 0   null comment '是否支持联网搜索(0-否  1-是)',
    dimension      int                    null comment '模型维度',
    priority       int        default 1   null comment '模型优先级(值越大优先级越高)',
    remark         varchar(500)           null comment '备注',
    create_by      bigint                 null comment '创建者',
    create_time    datetime               null comment '创建时间',
    update_by      bigint                 null comment '更新者',
    update_time    datetime               null comment '更新时间'
)
    comment '聊天模型' collate = utf8mb4_general_ci
                       row_format = DYNAMIC;

create table chat_pay_order
(
    id             bigint auto_increment comment '主键'
        primary key,
    order_no       varchar(100)   not null comment '订单编号',
    order_name     varchar(100)   not null comment '订单名称',
    amount         decimal(10, 2) not null comment '金额',
    payment_status char           null comment '支付状态',
    payment_method char(10)       null comment '支付方式',
    user_id        bigint         null comment '用户ID',
    create_by      bigint         null comment '创建者',
    create_time    datetime       null comment '创建时间',
    update_by      bigint         null comment '更新者',
    update_time    datetime       null comment '更新时间',
    remark         varchar(500)   null comment '备注'
)
    comment '支付订单表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table chat_session
(
    id              bigint auto_increment comment '主键'
        primary key,
    user_id         bigint            null comment '用户id',
    session_title   varchar(255)      null comment '会话标题',
    session_content text              null comment '会话内容',
    create_by       bigint            null comment '创建者',
    create_time     datetime          null comment '创建时间',
    update_by       bigint            null comment '更新者',
    update_time     datetime          null comment '更新时间',
    remark          varchar(500)      null comment '备注',
    conversation_id varchar(32)       null comment '会话ID',
    archive_status  tinyint default 0 not null comment '归档状态（0：正常，1：已归档）'
)
    comment '会话管理' collate = utf8mb4_general_ci
                       row_format = DYNAMIC;

create table chat_usage_token
(
    id           bigint auto_increment comment '主键'
        primary key,
    model_name   varchar(64) null comment '模型名称',
    input_token  bigint      null comment '模型输入token',
    output_token bigint      null comment '输出token',
    update_time  datetime    null comment '更新时间'
)
    comment '用户token使用详情' collate = utf8mb4_general_ci
                                row_format = DYNAMIC;

create table daily_health
(
    id          bigint auto_increment comment '主键id'
        primary key,
    user_id     bigint       not null comment '关联用户id',
    up_time     time         null comment '起床时间',
    sleep_time  time         null comment '睡觉时间',
    food        varchar(300) null comment '每日饮食',
    exercise    varchar(500) null comment '今日运动',
    create_time datetime     null comment '创建时间',
    create_by   bigint       null comment '创建人',
    update_time datetime     null comment '修改时间',
    update_by   bigint       null comment '更新人',
    remark      varchar(200) null comment '备注'
);

create index daily_health_user_id_create_time_index
    on daily_health (user_id, create_time);

create table daily_knowledge
(
    id          bigint auto_increment comment '主键id'
        primary key,
    type        varchar(20)  null comment '知识类型',
    title       varchar(50)  null comment '知识标题',
    content     varchar(500) null comment '每日知识内容',
    create_time datetime     null,
    create_by   bigint       null,
    update_time datetime     null,
    update_by   bigint       null
)
    comment '每日知识表';

create table finance_category
(
    id          bigint auto_increment
        primary key,
    user_id     bigint      not null,
    name        varchar(50) not null comment '具体分类名称',
    type        tinyint     not null comment '分类类型 1-支出 2-收入',
    icon        varchar(50) null comment '前端展示icon',
    create_time datetime    null,
    create_by   bigint      null,
    update_time datetime    null,
    update_by   bigint      null
)
    comment '财务分类表';

create table finance_transaction
(
    id          bigint auto_increment
        primary key,
    user_id     bigint       not null,
    category_id bigint       not null comment '财务分类ID',
    amount      bigint       not null comment '金额',
    tag         tinyint      not null comment '流水标签 1-必要支出 2-弹性支出 3-工薪收入 4-额外收入',
    remark      varchar(100) null comment '备注内容',
    create_time datetime     null,
    create_by   bigint       null,
    update_time datetime     null,
    update_by   bigint       null
)
    comment '财务流水表';

create table knowledge_attach
(
    id          bigint auto_increment
        primary key,
    kid         varchar(50)  not null comment '知识库ID',
    doc_id      varchar(50)  not null comment '文档ID',
    doc_name    varchar(500) null comment '文档名称',
    doc_type    varchar(50)  not null comment '文档类型',
    oss_id      int          null comment '对象存储ID',
    content     longtext     null comment '文档内容',
    create_by   varchar(50)  null comment '创建人',
    create_time datetime     null comment '创建时间',
    update_by   bigint       null comment '更新者',
    update_time datetime     null comment '更新时间',
    remark      varchar(500) null comment '备注',
    constraint idx_kname
        unique (kid, doc_name)
)
    comment '知识库附件' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table knowledge_fragment
(
    id          bigint auto_increment
        primary key,
    kid         varchar(50)  not null comment '知识库ID',
    doc_id      varchar(50)  null comment '文档ID',
    fid         varchar(50)  not null comment '知识片段ID',
    idx         int          not null comment '片段索引下标',
    content     text         not null comment '文档内容',
    create_by   varchar(50)  null comment '创建人',
    create_time datetime     null comment '创建时间',
    update_by   bigint       null comment '更新者',
    update_time datetime     null comment '更新时间',
    remark      varchar(500) null comment '备注'
)
    comment '知识片段' collate = utf8mb4_general_ci
                       row_format = DYNAMIC;

create table knowledge_info
(
    id                   bigint auto_increment
        primary key,
    kid                  varchar(50)   not null comment '知识库ID',
    uid                  bigint        not null comment '用户ID',
    kname                varchar(50)   not null comment '知识库名称',
    description          varchar(1000) null comment '描述',
    knowledge_separator  varchar(255)  null comment '知识分隔符',
    question_separator   varchar(255)  null comment '提问分隔符',
    overlap_char         int           null comment '重叠字符数',
    retrieve_limit       int           null comment '知识库中检索的条数',
    text_block_size      int           null comment '文本块大小',
    embedding_model_name varchar(50)   null comment '向量模型',
    embedding_model_id   bigint        null comment '模型id',
    create_by            varchar(50)   null comment '创建人',
    create_time          datetime      null comment '创建时间',
    update_by            bigint        null comment '更新者',
    update_time          datetime      null comment '更新时间',
    remark               varchar(500)  null comment '备注',
    constraint idx_kid
        unique (kid)
)
    comment '知识库' collate = utf8mb4_general_ci
                     row_format = DYNAMIC;

create table life_category
(
    id          bigint auto_increment comment '主键id'
        primary key,
    user_id     bigint      null comment '关联用户id',
    name        varchar(50) null comment '分类场景名称',
    sort_order  int         null comment '展示排序（越小越靠前）',
    create_time datetime    null comment '创建时间',
    create_by   bigint      null comment '创建人',
    update_time datetime    null,
    update_by   bigint      null comment '更新人'
);

create index life_category_user_index
    on life_category (user_id);

create table life_record
(
    id            bigint auto_increment
        primary key,
    user_id       bigint            null comment '所属用户ID',
    category_id   bigint            null comment '关联分类ID',
    title         varchar(100)      null comment '实体标题',
    content       text              null comment '详细内容/感想',
    rating        tinyint default 0 null comment '评分',
    record_date   date              null comment '记录日期',
    attachs_id    varchar(500)      null comment 'oss表文件 多个以英文,分隔',
    favorite_flag tinyint default 0 null comment '是否收藏(0-否 1-是)',
    create_time   datetime          null comment '创建时间',
    create_by     bigint            null comment '上传人',
    update_time   datetime          null comment '更新时间',
    update_by     bigint            null comment '更新人',
    remark        varchar(200)      null comment '备注'
)
    comment '生活记录信息详情表';

create table prompt_template
(
    id               bigint auto_increment comment '主键'
        primary key,
    template_name    varchar(128)  null comment '提示词模板名称',
    template_content text          null comment '提示词模板内容',
    category         varchar(50)   null comment '提示词分类，knowledge 知识库类型，chat 对话类型，draw绘画类型 ...',
    priority         int default 1 null comment '模板优先级',
    create_by        bigint        null comment '创建者',
    create_time      datetime      null comment '创建时间',
    update_by        bigint        null comment '更新者',
    update_time      datetime      null comment '更新时间',
    remark           varchar(256)  null comment '备注'
)
    comment '提示词模板表' collate = utf8mb4_general_ci
                           row_format = DYNAMIC;

create table sys_config
(
    config_id    bigint auto_increment comment '参数主键'
        primary key,
    tenant_id    varchar(20)  default '000000' null comment '租户编号',
    config_name  varchar(100) default ''       null comment '参数名称',
    config_key   varchar(100) default ''       null comment '参数键名',
    config_value varchar(500) default ''       null comment '参数键值',
    config_type  char         default 'N'      null comment '系统内置（Y是 N否）',
    create_dept  bigint                        null comment '创建部门',
    create_by    bigint                        null comment '创建者',
    create_time  datetime                      null comment '创建时间',
    update_by    bigint                        null comment '更新者',
    update_time  datetime                      null comment '更新时间',
    remark       varchar(500)                  null comment '备注'
)
    comment '参数配置表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default ''  null comment '用户账号',
    ipaddr         varchar(128) default ''  null comment '登录IP地址',
    login_location varchar(255) default ''  null comment '登录地点',
    browser        varchar(50)  default ''  null comment '浏览器类型',
    os             varchar(50)  default ''  null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default ''  null comment '提示消息',
    login_time     datetime                 null comment '访问时间'
)
    comment '系统访问记录' collate = utf8mb4_general_ci
                           row_format = DYNAMIC;

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

create table sys_oss
(
    oss_id        bigint auto_increment comment '对象存储主键'
        primary key,
    file_name     varchar(255) default ''      not null comment '文件名',
    original_name varchar(255) default ''      not null comment '原名',
    file_suffix   varchar(10)  default ''      not null comment '文件后缀名',
    url           varchar(500)                 not null comment 'URL地址',
    file_size     bigint                       null comment '文件大小',
    create_time   datetime                     null comment '创建时间',
    create_by     bigint                       null comment '上传人',
    update_time   datetime                     null comment '更新时间',
    update_by     bigint                       null comment '更新人',
    service       varchar(20)  default 'minio' not null comment '服务商'
)
    comment 'OSS对象存储表' collate = utf8mb4_general_ci
                            row_format = DYNAMIC;

create table sys_scheduled_task
(
    id                  bigint auto_increment comment '任务主键'
        primary key,
    user_id             bigint             not null comment '所属用户ID',
    task_name           varchar(64)        not null comment '任务名称',
    task_type           varchar(20)        not null comment '任务类型（FINANCE-财务 LIFE-生活 CHAT-对话）',
    cron_expression     varchar(64)        not null comment 'cron表达式',
    description         varchar(255)       null comment '任务描述',
    status              char   default '0' null comment '任务状态（0-暂停 1-运行）',
    last_execute_time   datetime           null comment '最近执行开始时间',
    next_execute_time   datetime           null comment '下次执行时间',
    last_execute_status char   default '0' null comment '最近一次执行状态（0-失败 1-成功）',
    execute_count       bigint default 0   null comment '累计执行次数',
    params              varchar(2000)      null comment '任务自定义参数（JSON字符串）',
    del_flag            char   default '0' null comment '删除标志（0-存在 1-删除）',
    create_by           bigint             null comment '创建者',
    create_time         datetime           null comment '创建时间',
    update_by           bigint             null comment '更新者',
    update_time         datetime           null comment '更新时间',
    remark              varchar(500)       null comment '备注'
)
    comment '动态任务调度表' collate = utf8mb4_general_ci
                             row_format = DYNAMIC;

create index idx_sys_scheduled_task_status
    on sys_scheduled_task (status);

create index idx_sys_scheduled_task_user
    on sys_scheduled_task (user_id);

create table sys_scheduled_task_log
(
    id             bigint auto_increment comment '日志主键'
        primary key,
    task_id        bigint           not null comment '关联任务ID',
    task_name      varchar(64)      not null comment '任务名称（冗余）',
    task_type      varchar(20)      not null comment '任务类型（冗余）',
    start_time     datetime         not null comment '执行开始时间',
    end_time       datetime         null comment '执行结束时间',
    duration_ms    bigint           null comment '耗时（毫秒）',
    status         char default '0' null comment '执行状态（0-失败 1-成功）',
    source         varchar(20)      null comment '触发来源：SCHEDULE-调度 MANUAL-手动',
    error_message  text             null comment '异常信息',
    result_message varchar(500)     null comment '执行结果摘要',
    create_time    datetime         null comment '入库时间'
)
    comment '任务执行日志表' collate = utf8mb4_general_ci
                             row_format = DYNAMIC;

create index idx_sys_scheduled_task_log_start
    on sys_scheduled_task_log (start_time);

create index idx_sys_scheduled_task_log_task
    on sys_scheduled_task_log (task_id);

create table sys_user
(
    user_id        bigint auto_increment comment '用户ID'
        primary key,
    user_grade     char          default '0'        null comment '用户等级',
    user_balance   double(20, 2) default 0.00       null comment '账户余额',
    user_name      varchar(30)                      not null comment '用户账号',
    nick_name      varchar(30)                      not null comment '用户昵称',
    user_type      varchar(10)   default 'sys_user' null comment '用户类型（sys_user系统用户）',
    user_plan      varchar(255)  default 'Free'     null comment '用户套餐',
    email          varchar(50)   default ''         not null comment '用户邮箱',
    phonenumber    varchar(11)   default ''         null comment '手机号码',
    sex            char          default '1'        null comment '用户性别（1男 0女 2未知）',
    avatar         varchar(255)                     null comment '头像地址',
    password       varchar(100)  default ''         null comment '密码',
    status         char          default '0'        null comment '帐号状态（0正常 1停用）',
    del_flag       char          default '0'        null comment '删除标志（0代表存在 2代表删除）',
    login_ip       varchar(128)  default ''         null comment '最后登录IP',
    login_location varchar(50)                      null comment '登录所在地',
    login_date     datetime                         null comment '最后登录时间',
    create_by      bigint                           null comment '创建者',
    create_time    datetime                         null comment '创建时间',
    update_by      bigint                           null comment '更新者',
    update_time    datetime                         null comment '更新时间',
    remark         varchar(500)                     null comment '备注'
)
    comment '用户信息表' collate = utf8mb4_general_ci
                         row_format = DYNAMIC;

