-- Enter this into your MySQL to set up the database
create database subshepard;

create table subscriptions (
id		int auto_increment,
name	varchar(60),
price	double,
duration	varchar(90),
paymentFreq	varchar(90),
primary key(id)
);

create table users (
firstName	varchar(30),
lastName	varchar(30),
email	varchar(50),
primary key(email)
);
