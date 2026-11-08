-- 페이징 API 확인용 추가 데이터 (추가 과제, 본 과제 시드 아님)
-- 스프링이 자동으로 읽는 파일이 아니므로, MySQL 클라이언트에서 직접 실행한다.
-- 여러 번 실행해도 중복 등록되지 않음 (같은 카테고리에 같은 이름이 있으면 건너뜀)
-- category_id 1, 2만 사용 / 각 5개 / 일부는 판매 중지(FALSE)
-- 이름은 NOT EXISTS 조건과 SELECT 두 곳에 똑같이 적혀 있으므로, 바꿀 때 둘 다 바꿀 것

-- ===== category_id = 1 (싱글오리진) =====
INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Kenya AA Nyeri', 12000, '케냐 니에리 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.id = 1
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Kenya AA Nyeri');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Guatemala Antigua', 8000, '과테말라 안티구아 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.id = 1
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Guatemala Antigua');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Costa Rica Tarrazú', 9500, '코스타리카 타라수 원두를 담은 커피빈 패키지입니다.', FALSE
FROM category c
WHERE c.id = 1
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Costa Rica Tarrazú');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Rwanda Huye Mountain', 10000, '르완다 후예 마운틴 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.id = 1
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Rwanda Huye Mountain');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Indonesia Sumatra Mandheling', 7500, '인도네시아 수마트라 만델링 원두를 담은 커피빈 패키지입니다.', TRUE
FROM category c
WHERE c.id = 1
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Indonesia Sumatra Mandheling');

-- ===== category_id = 2 (블렌드) =====
INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Morning Blend', 6000, '산뜻한 산미와 부드러운 단맛의 아침용 블렌드 원두 패키지입니다.', TRUE
FROM category c
WHERE c.id = 2
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Morning Blend');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Decaf Blend', 6500, '카페인을 제거한 디카페인 블렌드 원두 패키지입니다.', FALSE
FROM category c
WHERE c.id = 2
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Decaf Blend');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Espresso Roast Blend', 7000, '진한 바디감과 고소한 향의 에스프레소용 블렌드 원두 패키지입니다.', TRUE
FROM category c
WHERE c.id = 2
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Espresso Roast Blend');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Dark Roast Blend', 6000, '깊게 볶아 쌉싸름한 맛이 나는 다크 로스트 블렌드 원두 패키지입니다.', TRUE
FROM category c
WHERE c.id = 2
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Dark Roast Blend');

INSERT INTO menu (category_id, name, price, description, available)
SELECT c.id, 'Winter Seasonal Blend', 8000, '겨울 한정으로 구성한 시즌 블렌드 원두 패키지입니다.', FALSE
FROM category c
WHERE c.id = 2
  AND NOT EXISTS (SELECT 1 FROM menu m WHERE m.category_id = c.id AND m.name = 'Winter Seasonal Blend');
