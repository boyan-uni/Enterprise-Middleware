-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-1');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-2');
-- insert into myentity (id, field) values(nextval('hibernate_sequence'), 'field-3');
-- Insert User
--insert into users (email, name, phoneNumber) values ('testuserinreview@email.com', 'TestUserInReview', '01234567890');

-- Insert Restaurant
--insert into restaurants (name, phoneNumber, postcode) values ('TestRestaurant', '01234567890', 'AB123C');

-- Insert Review
-- This assumes the review table has foreign keys to users and restaurants
-- Ensure that users and restaurants are inserted before reviews
-- Replace user_id and restaurant_id with the actual IDs if needed
-- Here we are assuming the first inserted user and restaurant are used
--insert into reviews (rating, review, restaurant_id, user_id) values (5, 'Great!', (select id from restaurants where name = 'TestRestaurant'), (select id from users where email = 'testuserinreview@email.com'));
