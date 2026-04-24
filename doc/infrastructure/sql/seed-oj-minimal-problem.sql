USE `colacode`;

-- =========================================================
-- ColaCode OJ 最小联调数据
-- 作用：
-- 1. 清理同名旧种子数据
-- 2. 插入一条最小可跑通的编程题
-- 3. 插入 2 个样例用例 + 3 个隐藏用例
--
-- 执行后可用于联调：
-- - /subject/info/query?id={subjectId}
-- - /subject/code/judgeDetail?id={subjectId}
-- - /practice/judge/submit
-- - /practice/judge/submission/detail?id={submissionId}
-- =========================================================

SET @seed_created_by = 'codex_oj_seed';
SET @seed_subject_name = '两数之和（OJ联调题）';

-- 清理旧种子数据，保证脚本可重复执行
DELETE FROM `subject_code_case`
WHERE `subject_id` IN (
    SELECT `id`
    FROM (
        SELECT `id`
        FROM `subject_info`
        WHERE `subject_name` = @seed_subject_name
          AND `created_by` = @seed_created_by
          AND `subject_type` = 5
          AND `is_deleted` = 0
    ) t
);

DELETE FROM `subject_code`
WHERE `subject_id` IN (
    SELECT `id`
    FROM (
        SELECT `id`
        FROM `subject_info`
        WHERE `subject_name` = @seed_subject_name
          AND `created_by` = @seed_created_by
          AND `subject_type` = 5
          AND `is_deleted` = 0
    ) t
);

DELETE FROM `subject_mapping`
WHERE `subject_id` IN (
    SELECT `id`
    FROM (
        SELECT `id`
        FROM `subject_info`
        WHERE `subject_name` = @seed_subject_name
          AND `created_by` = @seed_created_by
          AND `subject_type` = 5
          AND `is_deleted` = 0
    ) t
);

DELETE FROM `subject_info`
WHERE `subject_name` = @seed_subject_name
  AND `created_by` = @seed_created_by
  AND `subject_type` = 5
  AND `is_deleted` = 0;

-- 插入题目主表
INSERT INTO `subject_info` (
    `subject_name`,
    `subject_difficulty`,
    `setter_name`,
    `subject_type`,
    `subject_score`,
    `subject_parse`,
    `browse_count`,
    `created_by`,
    `update_by`,
    `is_deleted`
) VALUES (
    @seed_subject_name,
    1,
    'Codex',
    5,
    10,
    '给定一行两个整数 a 和 b，输出它们的和。输入格式为一行两个整数，输出格式为一个整数。',
    0,
    @seed_created_by,
    @seed_created_by,
    0
);

SET @subject_id = LAST_INSERT_ID();

-- 插入编程题配置
INSERT INTO `subject_code` (
    `subject_id`,
    `judge_mode`,
    `time_limit_ms`,
    `memory_limit_kb`,
    `supported_languages_json`,
    `template_code_json`,
    `input_example`,
    `output_example`,
    `created_by`,
    `update_by`,
    `is_deleted`
) VALUES (
    @subject_id,
    'STANDARD_IO',
    1000,
    131072,
    '["java","python","cpp"]',
    '{"java":"import java.util.Scanner;\\npublic class Main {\\n    public static void main(String[] args) {\\n        Scanner in = new Scanner(System.in);\\n        int a = in.nextInt();\\n        int b = in.nextInt();\\n        System.out.println(a + b);\\n    }\\n}","python":"a, b = map(int, input().split())\\nprint(a + b)","cpp":"#include <bits/stdc++.h>\\nusing namespace std;\\nint main() {\\n    ios::sync_with_stdio(false);\\n    cin.tie(nullptr);\\n    int a, b;\\n    cin >> a >> b;\\n    cout << a + b << endl;\\n    return 0;\\n}"}',
    '2 7',
    '9',
    @seed_created_by,
    @seed_created_by,
    0
);

-- 插入测试用例
INSERT INTO `subject_code_case` (
    `subject_id`,
    `case_no`,
    `stdin_text`,
    `expected_stdout`,
    `is_sample`,
    `score`,
    `created_by`,
    `update_by`,
    `is_deleted`
) VALUES
    (@subject_id, 1, '2 7', '9', 1, 1, @seed_created_by, @seed_created_by, 0),
    (@subject_id, 2, '-3 5', '2', 1, 1, @seed_created_by, @seed_created_by, 0),
    (@subject_id, 3, '0 0', '0', 0, 1, @seed_created_by, @seed_created_by, 0),
    (@subject_id, 4, '123 456', '579', 0, 1, @seed_created_by, @seed_created_by, 0),
    (@subject_id, 5, '-100 -200', '-300', 0, 1, @seed_created_by, @seed_created_by, 0);

-- 如果你希望这道题出现在某个分类下，可按需补一条映射：
-- INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`, `update_by`, `is_deleted`)
-- VALUES (@subject_id, 你的分类ID, NULL, @seed_created_by, @seed_created_by, 0);

-- 执行结果确认
SELECT @subject_id AS seeded_subject_id;

SELECT
    `id`,
    `subject_name`,
    `subject_type`,
    `subject_difficulty`,
    `created_by`
FROM `subject_info`
WHERE `id` = @subject_id;

SELECT
    `subject_id`,
    `judge_mode`,
    `time_limit_ms`,
    `memory_limit_kb`,
    `supported_languages_json`
FROM `subject_code`
WHERE `subject_id` = @subject_id;

SELECT
    `subject_id`,
    `case_no`,
    `stdin_text`,
    `expected_stdout`,
    `is_sample`
FROM `subject_code_case`
WHERE `subject_id` = @subject_id
ORDER BY `case_no`;
