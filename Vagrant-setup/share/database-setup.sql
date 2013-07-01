CREATE DATABASE proper_ssl ;

GRANT ALL PRIVILEGES ON proper_ssl.* TO proper_ssl@'%' IDENTIFIED BY 'real_passwords_should_be_random';

FLUSH PRIVILEGES;
