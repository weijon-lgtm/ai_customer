-- User table
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    create_time DATETIME,
    update_time DATETIME
);
-- QA History table
CREATE TABLE IF NOT EXISTS qa_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    create_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
-- Knowledge base table
CREATE TABLE IF NOT EXISTS knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50),
    create_time DATETIME,
    update_time DATETIME
);
-- Insert admin user
INSERT INTO user (username, password, email, is_admin, create_time, update_time)
VALUES ('admin', 'admin123', 'admin@example.com', TRUE, NOW(), NOW());
INSERT INTO user (username, password, email, is_admin, create_time, update_time)
VALUES ('admin123456', 'admin123456', 'admin@example.com', TRUE, NOW(), NOW());
INSERT INTO user (username, password, email, is_admin, create_time, update_time)
VALUES ('admin1', 'admin1', 'admin@example.com', TRUE, NOW(), NOW());
-- Insert some sample knowledge
INSERT INTO knowledge (title, content, category, create_time, update_time)
VALUES 
('Product Returns', 'You can return a product within 30 days of purchase. Please ensure the product is in its original packaging.', 'Returns', NOW(), NOW()),
('Shipping Policy', 'We offer free shipping on orders over $50. Standard shipping takes 3-5 business days.', 'Shipping', NOW(), NOW()),
('Payment Methods', 'We accept Visa, MasterCard, American Express, and PayPal.', 'Payment', NOW(), NOW());
CREATE TABLE `knowledge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question` TEXT NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    FULLTEXT INDEX `idx_question` (`question`),
    FULLTEXT INDEX `idx_answer` (`answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答知识库表';

-- 插入示例数据
INSERT INTO `knowledge` (`question`, `answer`) VALUES
('如何重置密码？', '请点击登录页面的"忘记密码"链接，输入您的邮箱地址，系统会发送重置链接到您的邮箱。'),
('系统维护时间是什么时候？', '系统维护时间为每周三凌晨2:00-4:00，维护期间系统暂停服务。'),
('如何联系客服？', '您可以通过以下方式联系客服：1. 在线客服聊天 2. 拨打客服热线400-123-4567 3. 发送邮件至service@company.com');