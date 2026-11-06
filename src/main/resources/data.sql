-- 초기 데이터 (명세 4절)
-- 여러 번 실행해도 중복 등록되지 않음

-- 카테고리: name이 UNIQUE라 이미 있으면 INSERT IGNORE가 건너뜀
INSERT IGNORE INTO category (name) VALUES ('싱글오리진'), ('블렌드');

-- 메뉴: name에 UNIQUE가 없으므로, 같은 카테고리에 같은 이름이 없을 때만 등록
INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Columbia Nariñó', 5000, '콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Columbia Nariñó');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Brazil Serra Do Caparaó', 7000, '브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Brazil Serra Do Caparaó');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, '콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Columbia Quindío (White Wine Extended Fermentation)');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Ethiopia Sidamo', 11000, '에티오피아 시다모 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Ethiopia Sidamo');

-- http 요청 테스트용 데이터 (requests/api-check.http의 E5에서 사용)
-- 취소 마감이 지난 PLACED 주문 (배송일 = 2일 전), 주문 ID 1번으로 등록됨
-- 같은 이메일의 주문이 없을 때만 등록
INSERT INTO `order` (email, address, postcode, total_amount, status, created_at, shipping_date, memo)
SELECT 'past@example.com', '서울시 강남구 테헤란로 1', '06236', 5000, 'PLACED',
       CURDATE() - INTERVAL 2 DAY + INTERVAL 10 HOUR, CURDATE() - INTERVAL 2 DAY, ''
WHERE NOT EXISTS (SELECT 1 FROM `order` WHERE email = 'past@example.com');

-- 위 주문의 항목: 주문 ID를 이메일로 찾고, 항목이 아직 없을 때만 등록
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount)
SELECT o.id, m.id, m.name, m.price, 1, m.price
FROM `order` o
         JOIN menu m ON m.name = 'Columbia Nariñó'
WHERE o.email = 'past@example.com'
  AND NOT EXISTS (SELECT 1 FROM order_item oi WHERE oi.order_id = o.id);









