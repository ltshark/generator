drop table if exists g_task;
drop table if exists g_user;

create table g_task (
	id bigint generated by default as identity,
	title varchar(128) not null,
	description varchar(255),
	user_id bigint not null,
    primary key (id)
);

create table g_user (
	id bigint generated by default as identity,
	login_name varchar(64) not null unique,
	name varchar(64) not null,
	password varchar(255) not null,
	salt varchar(64) not null,
	roles varchar(255) not null,
	register_date timestamp not null,
	primary key (id)
);

create table g_key_task (
	id bigint generated by default as identity,
	user_id bigint not null,
	status varchar(32) not null,
	type varchar(32) not null,
	primary key (id)
);