CREATE DATABASE IF NOT EXISTS `colacode` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `colacode`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==== 权限管理模块 ====

-- =========================================================
-- auth_permission
-- =========================================================
DROP TABLE IF EXISTS `auth_permission`;
CREATE TABLE `auth_permission` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                   `name` varchar(64) DEFAULT NULL COMMENT '权限名称',
                                   `parent_id` bigint(20) DEFAULT NULL COMMENT '父级权限ID',
                                   `type` tinyint(4) DEFAULT NULL COMMENT '权限类型：0菜单 1操作',
                                   `menu_url` varchar(255) DEFAULT NULL COMMENT '菜单路由',
                                   `status` tinyint(2) DEFAULT '0' COMMENT '状态：0启用 1禁用',
                                   `is_show` tinyint(2) DEFAULT '0' COMMENT '展示状态：0展示 1隐藏',
                                   `icon` varchar(128) DEFAULT NULL COMMENT '图标',
                                   `permission_key` varchar(64) DEFAULT NULL COMMENT '权限唯一标识',
                                   `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                   `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                   `active_permission_key` varchar(64)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `permission_key` ELSE NULL END) STORED COMMENT '有效权限标识（仅未删除）',

                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_active_permission_key` (`active_permission_key`),
                                   KEY `idx_parent_id` (`parent_id`),
                                   KEY `idx_type_status` (`type`, `status`),
                                   KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- =========================================================
-- auth_role
-- =========================================================
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE `auth_role` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                             `role_name` varchar(32) DEFAULT NULL COMMENT '角色名称',
                             `role_key` varchar(64) DEFAULT NULL COMMENT '角色唯一标识',
                             `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                             `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                             `active_role_key` varchar(64)
                                 GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `role_key` ELSE NULL END) STORED COMMENT '有效角色标识（仅未删除）',

                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_active_role_key` (`active_role_key`),
                             KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =========================================================
-- auth_role_permission
-- =========================================================
DROP TABLE IF EXISTS `auth_role_permission`;
CREATE TABLE `auth_role_permission` (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                                        `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                        `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
                                        `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                        `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                        `active_role_id` bigint(20)
                                            GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `role_id` ELSE NULL END) STORED COMMENT '有效角色ID（仅未删除）',
                                        `active_permission_id` bigint(20)
                                            GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `permission_id` ELSE NULL END) STORED COMMENT '有效权限ID（仅未删除）',

                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_active_role_permission` (`active_role_id`, `active_permission_id`),
                                        KEY `idx_role_id` (`role_id`),
                                        KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =========================================================
-- auth_user
-- =========================================================
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE `auth_user` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                             `user_name` varchar(32) DEFAULT NULL COMMENT '用户名/账号',
                             `nick_name` varchar(32) DEFAULT NULL COMMENT '昵称',
                             `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
                             `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
                             `password` varchar(128) DEFAULT NULL COMMENT '密码',
                             `sex` tinyint(2) DEFAULT NULL COMMENT '性别：0未知 1男 2女',
                             `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                             `status` tinyint(2) DEFAULT '0' COMMENT '状态：0启用 1禁用',
                             `introduce` varchar(255) DEFAULT NULL COMMENT '个人介绍',
                             `ext_json` text COMMENT '扩展信息(JSON)',
                             `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                             `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                             `active_user_name` varchar(32)
                                 GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `user_name` ELSE NULL END) STORED COMMENT '有效用户名（仅未删除）',
                             `active_email` varchar(64)
                                 GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `email` ELSE NULL END) STORED COMMENT '有效邮箱（仅未删除）',
                             `active_phone` varchar(32)
                                 GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `phone` ELSE NULL END) STORED COMMENT '有效手机号（仅未删除）',

                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_active_user_name` (`active_user_name`),
                             UNIQUE KEY `uk_active_email` (`active_email`),
                             UNIQUE KEY `uk_active_phone` (`active_phone`),
                             KEY `idx_status_created_time` (`status`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- =========================================================
-- auth_user_role
-- =========================================================
DROP TABLE IF EXISTS `auth_user_role`;
CREATE TABLE `auth_user_role` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                                  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                  `active_user_id` bigint(20)
                                      GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `user_id` ELSE NULL END) STORED COMMENT '有效用户ID（仅未删除）',
                                  `active_role_id` bigint(20)
                                      GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `role_id` ELSE NULL END) STORED COMMENT '有效角色ID（仅未删除）',

                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_active_user_role` (`active_user_id`, `active_role_id`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ==== 面试模块 ====

-- =========================================================
-- interview_history
-- =========================================================
DROP TABLE IF EXISTS `interview_history`;
CREATE TABLE `interview_history` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '面试记录ID',
                                     `avg_score` decimal(5,2) DEFAULT NULL COMMENT '平均分',
                                     `key_words` varchar(512) DEFAULT NULL COMMENT '关键词（逗号分隔）',
                                     `tip` text COMMENT '总结建议',
                                     `interview_url` varchar(255) DEFAULT NULL COMMENT '面试链接',
                                     `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                     `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_created_by_created_time` (`created_by`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试历史表';

-- =========================================================
-- interview_question_history
-- =========================================================
DROP TABLE IF EXISTS `interview_question_history`;
CREATE TABLE `interview_question_history` (
                                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '题目作答记录ID',
                                              `interview_id` bigint(20) NOT NULL COMMENT '面试记录ID',
                                              `score` decimal(5,2) DEFAULT NULL COMMENT '本题得分',
                                              `key_words` varchar(512) DEFAULT NULL COMMENT '本题关键词',
                                              `question` text COMMENT '题目内容',
                                              `answer` text COMMENT '标准答案/参考答案',
                                              `user_answer` text COMMENT '用户回答',
                                              `sort_num` int(11) DEFAULT NULL COMMENT '题目顺序',
                                              `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                              `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                              PRIMARY KEY (`id`),
                                              KEY `idx_interview_id` (`interview_id`),
                                              KEY `idx_interview_sort` (`interview_id`, `sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试题目作答历史表';

-- ==== 练习模块 ====

-- =========================================================
-- practice_info
-- =========================================================
DROP TABLE IF EXISTS `practice_info`;
CREATE TABLE `practice_info` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '练习记录ID',
                                 `set_id` bigint(20) DEFAULT NULL COMMENT '套题ID',
                                 `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
                                 `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0进行中 1已提交 2已放弃',
                                 `total_score` int(11) DEFAULT NULL COMMENT '总分',
                                 `correct_count` int(11) DEFAULT '0' COMMENT '正确题数',
                                 `wrong_count` int(11) DEFAULT '0' COMMENT '错误题数',
                                 `started_time` datetime DEFAULT NULL COMMENT '开始时间',
                                 `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
                                 `time_use` int(11) DEFAULT NULL COMMENT '总用时（秒）',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_status_created_time` (`user_id`, `status`, `created_time`),
                                 KEY `idx_set_id` (`set_id`),
                                 KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习主表';

-- =========================================================
-- practice_detail
-- =========================================================
DROP TABLE IF EXISTS `practice_detail`;
CREATE TABLE `practice_detail` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '练习详情ID',
                                   `practice_id` bigint(20) NOT NULL COMMENT '练习记录ID',
                                   `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                   `subject_type` tinyint(4) DEFAULT NULL COMMENT '题目类型快照：1单选 2多选 3判断 4简答',
                                   `subject_name` varchar(255) DEFAULT NULL COMMENT '题目标题快照',
                                   `subject_snapshot` longtext COMMENT '题目完整快照(JSON)，建议包含题干、选项、答案、解析',
                                   `user_answer` text COMMENT '用户答案',
                                   `correct_answer` text COMMENT '正确答案快照',
                                   `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否正确：1正确 0错误',
                                   `time_use` int(11) DEFAULT NULL COMMENT '答题耗时（秒）',
                                   `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                   `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                   `active_practice_id` bigint(20)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `practice_id` ELSE NULL END) STORED COMMENT '有效练习ID（仅未删除）',
                                   `active_subject_id` bigint(20)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',

                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_active_practice_subject` (`active_practice_id`, `active_subject_id`),
                                   KEY `idx_practice_id` (`practice_id`),
                                   KEY `idx_subject_id` (`subject_id`),
                                   KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习详情表';

-- =========================================================
-- practice_set
-- =========================================================
DROP TABLE IF EXISTS `practice_set`;
CREATE TABLE `practice_set` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '套题ID',
                                `set_name` varchar(255) DEFAULT NULL COMMENT '套题名称',
                                `set_type` tinyint(4) NOT NULL DEFAULT '2' COMMENT '套题类型：1随机 2预设',
                                `description` varchar(512) DEFAULT NULL COMMENT '套题描述',
                                `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
                                `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_status_created_time` (`status`, `created_time`),
                                KEY `idx_set_name` (`set_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套题信息表';

-- =========================================================
-- practice_set_detail
-- =========================================================
DROP TABLE IF EXISTS `practice_set_detail`;
CREATE TABLE `practice_set_detail` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '套题内容ID',
                                       `set_id` bigint(20) NOT NULL COMMENT '套题ID',
                                       `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                       `sort` int(11) DEFAULT NULL COMMENT '排序',
                                       `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
                                       `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
                                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                       `active_set_id` bigint(20)
                                           GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `set_id` ELSE NULL END) STORED COMMENT '有效套题ID（仅未删除）',
                                       `active_subject_id` bigint(20)
                                           GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',

                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_active_set_subject` (`active_set_id`, `active_subject_id`),
                                       KEY `idx_set_sort` (`set_id`, `sort`),
                                       KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套题内容表';

-- ==== 敏感词模块 ====

-- =========================================================
-- sensitive_words
-- =========================================================
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
                                   `words` varchar(1024) DEFAULT NULL COMMENT '敏感词内容',
                                   `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型：1黑名单 2白名单',
                                   `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ==== 分享圈子模块 ====

-- =========================================================
-- share_circle
-- =========================================================
DROP TABLE IF EXISTS `share_circle`;
CREATE TABLE `share_circle` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '圈子ID',
                                `parent_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '父级ID，-1表示大类',
                                `circle_name` varchar(32) NOT NULL COMMENT '圈子名称',
                                `icon` varchar(255) DEFAULT NULL COMMENT '圈子图标URL',
                                `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子信息表';

-- =========================================================
-- share_moment
-- =========================================================
DROP TABLE IF EXISTS `share_moment`;
CREATE TABLE `share_moment` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '动态ID',
                                `circle_id` bigint(20) NOT NULL COMMENT '所属圈子ID',
                                `user_id` bigint(20) DEFAULT NULL COMMENT '发布用户ID',
                                `content` text COMMENT '动态内容',
                                `pic_urls` text COMMENT '动态图片URL列表（JSON或逗号分隔）',
                                `reply_count` int(11) NOT NULL DEFAULT '0' COMMENT '回复数',
                                `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_circle_id` (`circle_id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_circle_created_time` (`circle_id`, `created_time`),
                                KEY `idx_created_by_created_time` (`created_by`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态信息表';

-- =========================================================
-- share_comment_reply
-- =========================================================
DROP TABLE IF EXISTS `share_comment_reply`;
CREATE TABLE `share_comment_reply` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论/回复ID',
                                       `moment_id` bigint(20) NOT NULL COMMENT '所属动态ID',
                                       `reply_type` tinyint(4) NOT NULL COMMENT '类型：1评论 2回复',
                                       `target_id` bigint(20) DEFAULT NULL COMMENT '评论目标ID',
                                       `target_user_id` bigint(20) DEFAULT NULL COMMENT '评论目标用户ID',
                                       `to_user_author` tinyint(1) DEFAULT '0' COMMENT '目标是否为作者：1是 0否',
                                       `reply_id` bigint(20) DEFAULT NULL COMMENT '回复目标ID',
                                       `reply_user_id` bigint(20) DEFAULT NULL COMMENT '被回复用户ID',
                                       `reply_author` tinyint(1) DEFAULT '0' COMMENT '回复人是否为作者：1是 0否',
                                       `content` text COMMENT '内容',
                                       `pic_urls` text COMMENT '图片URL列表',
                                       `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父评论ID，0表示根评论',
                                       `leaf_node` varchar(255) DEFAULT NULL COMMENT '叶子节点信息（冗余路径）',
                                       `children` text COMMENT '子节点信息（JSON）',
                                       `root_node` varchar(255) DEFAULT NULL COMMENT '根节点信息（冗余路径）',
                                       `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                       `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_moment_id` (`moment_id`),
                                       KEY `idx_moment_parent_created_time` (`moment_id`, `parent_id`, `created_time`),
                                       KEY `idx_reply_id` (`reply_id`),
                                       KEY `idx_target_user_id` (`target_user_id`),
                                       KEY `idx_reply_user_id` (`reply_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论及回复信息表';

-- =========================================================
-- share_message
-- =========================================================
DROP TABLE IF EXISTS `share_message`;
CREATE TABLE `share_message` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                 `from_user_id` bigint(20) NOT NULL COMMENT '发送人用户ID',
                                 `to_user_id` bigint(20) NOT NULL COMMENT '接收人用户ID',
                                 `content` varchar(512) DEFAULT NULL COMMENT '消息内容',
                                 `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0未读 1已读',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_to_user_id_is_read` (`to_user_id`, `is_read`),
                                 KEY `idx_from_user_id` (`from_user_id`),
                                 KEY `idx_from_to_created_time` (`from_user_id`, `to_user_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ==== 题库模块 ====

-- =========================================================
-- subject_category
-- =========================================================
DROP TABLE IF EXISTS `subject_category`;
CREATE TABLE `subject_category` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                    `category_name` varchar(32) DEFAULT NULL COMMENT '分类名称',
                                    `category_type` tinyint(2) DEFAULT NULL COMMENT '分类类型（预留）',
                                    `image_url` varchar(255) DEFAULT NULL COMMENT '图标URL',
                                    `parent_id` bigint(20) DEFAULT NULL COMMENT '父级分类ID',
                                    `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_parent_id` (`parent_id`),
                                    KEY `idx_category_type` (`category_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- =========================================================
-- subject_label
-- =========================================================
DROP TABLE IF EXISTS `subject_label`;
CREATE TABLE `subject_label` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                                 `label_name` varchar(32) DEFAULT NULL COMMENT '标签名称',
                                 `sort_num` int(11) DEFAULT NULL COMMENT '排序值',
                                 `category_id` bigint(20) DEFAULT NULL COMMENT '所属分类ID',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                 `active_label_name` varchar(32)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `label_name` ELSE NULL END) STORED COMMENT '有效标签名（仅未删除）',

                                 PRIMARY KEY (`id`),
                                 KEY `idx_category_id` (`category_id`),
                                 KEY `idx_category_sort` (`category_id`, `sort_num`),
                                 KEY `idx_active_label_name` (`active_label_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- =========================================================
-- subject_info
-- =========================================================
DROP TABLE IF EXISTS `subject_info`;
CREATE TABLE `subject_info` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
                                `subject_name` varchar(255) DEFAULT NULL COMMENT '题目名称/题干',
                                `subject_difficulty` tinyint(4) DEFAULT NULL COMMENT '题目难度：1简单 2中等 3困难',
                                `setter_name` varchar(32) DEFAULT NULL COMMENT '出题人名称',
                                `subject_type` tinyint(4) DEFAULT NULL COMMENT '题目类型：1单选 2多选 3判断 4简答',
                                `subject_score` tinyint(4) DEFAULT NULL COMMENT '题目分数',
                                `subject_parse` text COMMENT '题目解析',
                                `browse_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '浏览次数',
                                `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_subject_type` (`subject_type`),
                                KEY `idx_created_by` (`created_by`),
                                KEY `idx_subject_type_difficulty` (`subject_type`, `subject_difficulty`),
                                KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目信息表';

-- =========================================================
-- subject_brief
-- =========================================================
DROP TABLE IF EXISTS `subject_brief`;
CREATE TABLE `subject_brief` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '简答题ID',
                                 `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                 `subject_answer` text COMMENT '简答题参考答案',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                 `active_subject_id` bigint(20)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',

                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_active_subject_id` (`active_subject_id`),
                                 KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简答题表';

-- =========================================================
-- subject_judge
-- =========================================================
DROP TABLE IF EXISTS `subject_judge`;
CREATE TABLE `subject_judge` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '判断题ID',
                                 `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                 `is_correct` tinyint(2) DEFAULT NULL COMMENT '正确答案：1正确 0错误',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                 `active_subject_id` bigint(20)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',

                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_active_subject_id` (`active_subject_id`),
                                 KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='判断题表';

-- =========================================================
-- subject_radio
-- =========================================================
DROP TABLE IF EXISTS `subject_radio`;
CREATE TABLE `subject_radio` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '单选题选项ID',
                                 `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                 `option_type` tinyint(4) DEFAULT NULL COMMENT '选项标识：1=A 2=B 3=C 4=D',
                                 `option_content` varchar(255) DEFAULT NULL COMMENT '选项内容',
                                 `is_correct` tinyint(2) DEFAULT NULL COMMENT '是否正确：1正确 0错误',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                 `active_subject_id` bigint(20)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',
                                 `active_option_type` tinyint(4)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `option_type` ELSE NULL END) STORED COMMENT '有效选项标识（仅未删除）',

                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_active_subject_option` (`active_subject_id`, `active_option_type`),
                                 KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单选题选项表';

-- =========================================================
-- subject_multiple
-- =========================================================
DROP TABLE IF EXISTS `subject_multiple`;
CREATE TABLE `subject_multiple` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '多选题选项ID',
                                    `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                    `option_type` tinyint(4) DEFAULT NULL COMMENT '选项标识：1=A 2=B 3=C 4=D',
                                    `option_content` varchar(255) DEFAULT NULL COMMENT '选项内容',
                                    `is_correct` tinyint(2) DEFAULT NULL COMMENT '是否正确：1正确 0错误',
                                    `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                    `active_subject_id` bigint(20)
                                        GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',
                                    `active_option_type` tinyint(4)
                                        GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `option_type` ELSE NULL END) STORED COMMENT '有效选项标识（仅未删除）',

                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_active_subject_option` (`active_subject_id`, `active_option_type`),
                                    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多选题选项表';

-- =========================================================
-- subject_mapping
-- =========================================================
DROP TABLE IF EXISTS `subject_mapping`;
CREATE TABLE `subject_mapping` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '映射ID',
                                   `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                   `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
                                   `label_id` bigint(20) DEFAULT NULL COMMENT '标签ID',
                                   `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                   `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                   `active_subject_id` bigint(20)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',
                                   `active_category_id` bigint(20)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `category_id` ELSE NULL END) STORED COMMENT '有效分类ID（仅未删除）',
                                   `active_label_id` bigint(20)
                                       GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `label_id` ELSE NULL END) STORED COMMENT '有效标签ID（仅未删除）',

                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_active_subject_category_label` (`active_subject_id`, `active_category_id`, `active_label_id`),
                                   KEY `idx_subject_id` (`subject_id`),
                                   KEY `idx_category_id` (`category_id`),
                                   KEY `idx_label_id` (`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类标签映射表';

-- =========================================================
-- subject_liked
-- =========================================================
DROP TABLE IF EXISTS `subject_liked`;
CREATE TABLE `subject_liked` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
                                 `subject_id` bigint(20) NOT NULL COMMENT '题目ID',
                                 `like_user_id` varchar(32) NOT NULL COMMENT '点赞用户ID/账号',
                                 `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '点赞状态：1点赞 0取消',
                                 `created_by` varchar(32) DEFAULT NULL COMMENT '操作人标识',
                                 `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(32) DEFAULT NULL COMMENT '更新人标识',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',

                                 `active_subject_id` bigint(20)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `subject_id` ELSE NULL END) STORED COMMENT '有效题目ID（仅未删除）',
                                 `active_like_user_id` varchar(32)
                                     GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `like_user_id` ELSE NULL END) STORED COMMENT '有效点赞用户（仅未删除）',

                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_active_like` (`active_subject_id`, `active_like_user_id`),
                                 KEY `idx_like_user_id` (`like_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目点赞表';

-- =========================================================
-- es_sync_status
-- =========================================================
DROP TABLE IF EXISTS `es_sync_status`;
CREATE TABLE `es_sync_status` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `biz_id` bigint(20) NOT NULL COMMENT '业务ID，题目ID',
    `biz_type` varchar(32) NOT NULL COMMENT '业务类型：subject',
    `operation` tinyint(4) NOT NULL COMMENT '操作类型：1=新增/修改 2=删除',
    `payload_json` text COMMENT '同步数据快照',
    `status` tinyint(4) DEFAULT '0' COMMENT '状态：0=待同步 1=成功 2=失败 3=处理中 4=死信',
    `retry_count` int(11) DEFAULT '0' COMMENT '已重试次数',
    `max_retry_count` int(11) DEFAULT '3' COMMENT '最大重试次数',
    `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
    `last_sync_time` datetime DEFAULT NULL COMMENT '最后一次同步时间',
    `error_msg` text COMMENT '失败原因',
    `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`),
    KEY `idx_biz_id_biz_type` (`biz_id`, `biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ES同步状态表';
CREATE TABLE IF NOT EXISTS interview_session (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                 user_id BIGINT NOT NULL,
                                                 interview_type VARCHAR(32) NOT NULL,
    post_type VARCHAR(64) NOT NULL,
    difficulty_level INT DEFAULT 1,
    engine_type VARCHAR(32) NOT NULL,
    source_mode VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_question_no INT DEFAULT 0,
    total_question_count INT DEFAULT 0,
    total_score DECIMAL(5,2) DEFAULT 0,
    report_id BIGINT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    duration_seconds INT DEFAULT 0,
    version INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(64),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS interview_question_record (
                                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                         session_id BIGINT NOT NULL,
                                                         question_id BIGINT NULL,
                                                         question_source VARCHAR(32) NOT NULL,
    question_type VARCHAR(32) NOT NULL,
    category VARCHAR(64),
    difficulty INT DEFAULT 1,
    stem TEXT NOT NULL,
    standard_answer TEXT,
    rubric_id BIGINT NULL,
    round_no INT DEFAULT 1,
    parent_record_id BIGINT NULL,
    is_follow_up TINYINT DEFAULT 0,
    key_words VARCHAR(255),
    user_answer TEXT,
    rule_score DECIMAL(5,2) DEFAULT 0,
    ai_score DECIMAL(5,2) DEFAULT 0,
    final_score DECIMAL(5,2) DEFAULT 0,
    hit_keywords TEXT,
    miss_keywords TEXT,
    wrong_points TEXT,
    evaluation_comment TEXT,
    ask_time DATETIME NULL,
    answer_time DATETIME NULL,
    cost_seconds INT DEFAULT 0,
    status VARCHAR(32),
    is_deleted TINYINT DEFAULT 0,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS interview_report (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                session_id BIGINT NOT NULL,
                                                user_id BIGINT NOT NULL,
                                                total_score DECIMAL(5,2) DEFAULT 0,
    base_score DECIMAL(5,2) DEFAULT 0,
    logic_score DECIMAL(5,2) DEFAULT 0,
    expression_score DECIMAL(5,2) DEFAULT 0,
    engineering_score DECIMAL(5,2) DEFAULT 0,
    summary TEXT,
    weakness_tags_json JSON,
    advantage_tags_json JSON,
    suggestion TEXT,
    recommended_practice_json JSON,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS interview_rubric (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                post_type VARCHAR(64) NOT NULL,
    question_type VARCHAR(32) NOT NULL,
    category VARCHAR(64) NOT NULL,
    total_score DECIMAL(5,2) DEFAULT 10,
    scoring_items_json JSON NOT NULL,
    version INT DEFAULT 1,
    enabled TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );
-- ==== 权限管理初始化数据 ====
INSERT INTO `auth_permission` (`id`, `name`, `parent_id`, `type`, `menu_url`, `status`, `is_show`, `icon`, `permission_key`, `created_by`, `is_deleted`) VALUES
(1, '权限管理', 0, 0, '/auth', 0, 0, 'setting', 'auth:manage', 'system', 0),
(2, '用户管理', 1, 1, '/auth/user', 0, 0, 'user', 'user:manage', 'system', 0),
(3, '角色管理', 1, 1, '/auth/role', 0, 0, 'team', 'role:manage', 'system', 0),
(4, '权限项管理', 1, 1, '/auth/permission', 0, 0, 'safety', 'permission:manage', 'system', 0);

INSERT INTO `auth_role` (`id`, `role_name`, `role_key`, `created_by`, `is_deleted`) VALUES
(1, '管理员', 'admin', 'system', 0);

INSERT INTO `auth_role_permission` (`id`, `role_id`, `permission_id`, `created_by`, `is_deleted`) VALUES
(1, 1, 1, 'system', 0),
(2, 1, 2, 'system', 0),
(3, 1, 3, 'system', 0),
(4, 1, 4, 'system', 0);
SET FOREIGN_KEY_CHECKS = 1;


