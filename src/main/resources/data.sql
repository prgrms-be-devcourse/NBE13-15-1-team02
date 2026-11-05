-- 초기 데이터 (앱을 다시 실행해도 중복 등록되지 않음)

-- 카테고리: name 이 UNIQUE 이므로 INSERT IGNORE 로 중복 방지
INSERT IGNORE INTO category (name) VALUES ('싱글오리진'), ('블렌드');

-- 메뉴: 같은 이름이 이미 있으면 넣지 않음 (모두 싱글오리진 소속, available=true)
INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Columbia Nariñó', 5000, '콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.name = 'Columbia Nariñó');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Brazil Serra Do Caparaó', 7000, '브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.name = 'Brazil Serra Do Caparaó');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, '콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.name = 'Columbia Quindío (White Wine Extended Fermentation)');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Ethiopia Sidamo', 11000, '에티오피아 시다모 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.name = 'Ethiopia Sidamo');
