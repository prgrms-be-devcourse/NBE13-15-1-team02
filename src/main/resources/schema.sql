-- 테이블 생성 (IF NOT EXISTS 이므로 재실행해도 안전)
-- 순서: category -> menu -> order -> order_item (FK 의존 순서)

CREATE TABLE IF NOT EXISTS category(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS menu(
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    price INT NOT NULL,
    description VARCHAR(300) NOT NULL DEFAULT '',
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT ck_menu_price CHECK (price BETWEEN 1 AND 1000000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order`(
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    address VARCHAR(200) NOT NULL,
    postcode VARCHAR(5) NOT NULL,
    total_amount INT NOT NULL,
    `status` ENUM ('PLACED', 'CANCELLED') NOT NULL DEFAULT 'PLACED',
    created_at DATETIME NOT NULL,
    shipping_date DATE NOT NULL,
    cancelled_at DATETIME,
    memo VARCHAR(200) NOT NULL DEFAULT '',
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item(
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    menu_name VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    unit_price INT NOT NULL,
    quantity INT NOT NULL,
    line_amount INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (id),
    CONSTRAINT fk_order_item_menu  FOREIGN KEY (menu_id)  REFERENCES menu(id),
    CONSTRAINT uk_order_item_order_menu UNIQUE (order_id, menu_id),
    CONSTRAINT ck_order_item_quantity CHECK (quantity BETWEEN 1 AND 20),
    CONSTRAINT ck_order_item_price CHECK (unit_price BETWEEN 1 AND 1000000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
