-- :name insert-row! :! :n
INSERT INTO :i:name
(:i*:column-names)
VALUES (:v*:column-values)
ON CONFLICT DO NOTHING

-- :name update-column-in-row! :! :n
UPDATE :i:name
SET :i:column = :value
WHERE id = :id

-- :name get-columns :? :*
SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name

--:name get-first-row :? :*
select * from :i:name LIMIT '1'

--:name get-rows-with-value
SELECT * FROM :i:name WHERE :i:column = :value;

--:name count-rows-with-value
SELECT COUNT(*) FROM :i:name WHERE :i:column = :value;

--:name get-every-row-from-table :? :*
SELECT * from :i:name

--:name get-from-table-ascending-where :? :*
SELECT * FROM :i:name :sql:filters ORDER BY :i:sort-by ASC LIMIT :i:limit OFFSET :i:offset

--:name get-from-table-descending-where :? :*
SELECT * FROM :i:name :sql:filters ORDER BY :i:sort-by DESC LIMIT :i:limit OFFSET :i:offset

--:name get-from-table :? :*
SELECT * FROM :i:table-name :sql:filters :sql:sort LIMIT :i:limit OFFSET :i:offset

--:name get-from-table-no-limit-no-offset :? :*
SELECT * FROM :i:table-name :sql:filters :sql:sort

--:name get-stations-for-map :? :*
SELECT stationid, name, x, y FROM stations

--:name get-journeys-with-station-names :? :*
SELECT 
journeys.*,
departure.name AS departure_name,
departure.namn AS departure_namn,
departure.nimi AS departure_nimi,
return.name AS return_name,
return.namn AS return_namn,
return.nimi AS return_nimi
FROM journeys JOIN stations departure ON departure.stationid = journeys.departure_station_id
JOIN stations return ON return.stationid = journeys.return_station_id
:sql:filters :sql:sort LIMIT :i:limit OFFSET :i:offset

--:name get-column-from-table :? :*
SELECT :i:column FROM :i:name :sql:filters :sql:sort

--:name get-data-types-of-table :? :*
SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name

--:name get-data-type-of-column-in-table :? :*
SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name AND column_name = :column

--:name create-new-table! :! :n
CREATE TABLE :i:name
(id SERIAL PRIMARY KEY,
:sql:columns-datatype,
UNIQUE(:sql:columns)
)

--:name drop-table! :! :n
DROP TABLE :i:name

--:name show-tables :? :*
SELECT tablename
FROM pg_catalog.pg_tables
WHERE schemaname != 'pg_catalog' AND 
    schemaname != 'information_schema';

--:name select-return-journeys-that-hour-from-that-departure-station :? :*
SELECT return_station_id, extract(hour from return)
FROM journeys 
WHERE departure_station_id LIKE :v:departure_station_id AND extract(hour from return) = :v:hour

--:name select-departure-journeys-that-hour-from-that-return-station :? :*
SELECT depature_station_id, extract(hour from departure)
FROM journeys 
WHERE return_station_id LIKE :v:return_station_id

--:name count-journeys-that-hour-from-that-station-during-weekdays :? :*
SELECT COUNT(*)
FROM journeys 
WHERE departure_station_id LIKE :v:departure_station_id AND extract (dow from departure) BETWEEN 1 and 5 AND extract(hour from departure) = :v:hour

--:name count-journeys-that-hour-from-that-station-during-weekends :? :*
SELECT COUNT(*)
FROM journeys 
WHERE departure_station_id LIKE :v:departure_station_id AND extract (dow from departure) NOT BETWEEN 1 and 5 AND extract(hour from departure) = :v:hour

--:name count-journeys-per-hour-from-station-during-weekends :? :*
SELECT extract(hour from departure), COUNT(*)
FROM journeys 
WHERE departure_station_id LIKE :v:departure_station_id AND extract (dow from departure) NOT BETWEEN 1 and 5
GROUP BY extract(hour from departure)

--:name count-journeys-per-hour-from-station-during-weekdays :? :*
SELECT extract(hour from departure), COUNT(*)
FROM journeys 
WHERE departure_station_id LIKE :v:departure_station_id AND extract (dow from departure) BETWEEN 1 and 5
GROUP BY extract(hour from departure)

--:name count-journeys-per-hour-to-station-during-weekends :? :*
SELECT extract(hour from return), COUNT(*)
FROM journeys 
WHERE return_station_id LIKE :v:return_station_id AND extract (dow from return) NOT BETWEEN 1 and 5
GROUP BY extract(hour from return)

--:name count-journeys-per-hour-to-station-during-weekdays :? :*
SELECT extract(hour from return), COUNT(*)
FROM journeys 
WHERE return_station_id LIKE :v:return_station_id AND extract (dow from return) BETWEEN 1 and 5
GROUP BY extract(hour from return)

--:name get-months :? :*
SELECT DISTINCT extract(month from departure)
FROM journeys

--:name first-and-last-journey :? :*
SELECT MIN(departure),MAX(departure)
FROM journeys


