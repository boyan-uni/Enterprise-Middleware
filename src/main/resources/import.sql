-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-1');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-2');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-3');

-- Insert Users
insert into users (email, name, phoneNumber) values ('alice@email.com', 'TestUserAlice', '01234567890');
insert into users (email, name, phoneNumber) values ('bob@email.com', 'TestUserBob', '09876543210');

-- Insert Restaurants
insert into restaurants (name, phoneNumber, postcode) values ('TestRestaurantA', '01234567890', 'AB123C');
insert into restaurants (name, phoneNumber, postcode) values ('TestRestaurantB', '09876543210', 'XY987Z');

-- Insert Reviews
insert into reviews (rating, review, restaurant_id, user_id) values (5, 'Great!', (select id from restaurants where name = 'TestRestaurantA'), (select id from users where email = 'alice@email.com'));
