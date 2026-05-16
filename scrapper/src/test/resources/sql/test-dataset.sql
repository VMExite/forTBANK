delete from chat;
delete from link;
delete from chat_link;
delete from tag;


insert into chat (chat_id) select generate_series(1,1000);

insert into link (link_id, last_update, url)
select
    generate_series(1, 100),
    now(),
    'http://localhost:8080/test/' || generate_series(1, 100);

insert into chat_link (chat_id, link_id)
select
    c.chat_id,
    l.link_id
from chat c cross join link l;
