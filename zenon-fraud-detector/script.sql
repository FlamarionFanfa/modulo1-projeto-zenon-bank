create table transactions (

                              id bigint auto_increment primary key,
                              step int not null,
                              type varchar(20) not null,
                              amount decimal(20,2) not null,
                              nome_origin varchar(50)not null,
                              old_balance_origin decimal(20,2) not null,
                              new_balance_origin decimal(20,2) not null,
                              name_recipient varchar(50) not null,
                              old_balance_recipient decimal(20,2) not null,
                              new_balance_recipient decimal(20,2) not null,
                              is_fraud tinyint(1) default 0,
                              is_flagged_fraud tinyint(1) default 0

);

insert into
    zenon_frauds.transactions
(step, `type`, amount, nome_origin, old_balance_origin, new_balance_origin, name_recipient, old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud)
values
    (1,'PAYMENT',9839.64,'C1231006815',170136.0,160296.36,'M1979787155',0.0,0.0,1,0);

SELECT * FROM zenon_frauds.transactions;

DELETE FROM zenon_frauds.transactions
WHERE id=1;