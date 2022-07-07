-- drop table if exist file_meta;
create table if not exist file_meta {
    name varchar(50) not null,
    path varchar(100) not null,
    is_directry boolean not null,
    size bigint not null,
    last_modified timestamp not null,
    pinyin varchar(200),
    pinyin _first varchar(50)
};
;
;
insert into file_meta(name,path,is_directory,size,last_modified)
    values ('测试.txt','/test/测试.txt',false,100,100);