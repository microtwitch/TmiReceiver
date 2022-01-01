DROP TABLE IF EXISTS consumer_channel;
DROP TABLE IF EXISTS consumer;
DROP TABLE IF EXISTS channel;

CREATE TABLE consumer (
    consumer_id serial PRIMARY KEY,
    callback VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE channel (
    channel_id serial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE consumer_channel(
    consumer_id int REFERENCES consumer (consumer_id),
    channel_id int REFERENCES channel (channel_id),
    CONSTRAINT consumer_channel_pkey PRIMARY KEY (consumer_id, channel_id)
);