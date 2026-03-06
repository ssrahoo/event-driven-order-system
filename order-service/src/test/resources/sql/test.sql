use edosdb;

select * from orders;
select * from orderitems;
select * from outbox_events;
select * from payments;


drop table if exists outbox_events;
drop table if exists payments;
drop table if exists orderitems;
drop table if exists orders;
