create table `groups`
(
    id   int auto_increment
        primary key,
    name varchar(45) not null
);

create table marks
(
    id         int auto_increment
        primary key,
    student_id int not null,
    mark       int not null
);

create table students
(
    id       int auto_increment,
    name     varchar(45) not null,
    age      int         not null,
    group_id int         not null,
    primary key (id, group_id)
);


