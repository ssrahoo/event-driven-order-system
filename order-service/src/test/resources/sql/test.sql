use edosdb;

select * from orders;
select * from order_items;
select * from outbox_events;
select * from payments;
select * from inventory_reservations;
select * from notifications;

drop table if exists outbox_events;
drop table if exists notifications;
drop table if exists inventory_reservations;
drop table if exists payments;
drop table if exists order_items;
drop table if exists orders;

