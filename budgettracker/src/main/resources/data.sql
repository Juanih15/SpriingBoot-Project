insert into budget(id, month, amount) values (1, '2025-05', 1000);
insert into category(id, name, budget_id) values (10, 'Rent', 1);
insert into expense(id, category_id, amount, note) values
  (100, 10, 850, 'May rent');
