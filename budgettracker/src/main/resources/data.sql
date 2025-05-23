INSERT INTO app_user (id, username, password, enabled)
VALUES (1, 'demo', '{noop}secret', true);

INSERT INTO budget (id, month, budget_limit)             --
VALUES                (1, '2025-05', 1000);

INSERT INTO category (id, name, budget_id)
VALUES (10, 'Rent', 1);

INSERT INTO expense (id, category_id, amount, note)
VALUES (100, 10, 850, 'May rent');
