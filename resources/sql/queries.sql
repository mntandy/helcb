-- :name insert-row! :! :n
INSERT INTO :i:name
(:i*:column-names)
VALUES (:v*:column-values)

-- :name get-columns :? :*
SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name

--:name get-first-row :? :*
select * from :i:name LIMIT '1'

--:name get-every-row-from-table :? :*
SELECT * from :i:name

--:name get-from-table-ascending-where :? :*
SELECT * FROM :i:name :sql:filters ORDER BY :i:sort-by ASC LIMIT :i:limit OFFSET :i:offset

--:name get-from-table-descending-where :? :*
SELECT * FROM :i:name :sql:filters ORDER BY :i:sort-by DESC LIMIT :i:limit OFFSET :i:offset

--:name get-from-table :? :*
SELECT * FROM :i:table-name :sql:filters :sql:sort LIMIT :i:limit OFFSET :i:offset

--:name get-column-from-table :? :*
SELECT :i:column FROM :i:name :sql:filters :sql:sort

--:name get-data-types-of-table :? :*
SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name

--:name get-data-type-of-column-in-table :? :*
SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name AND column_name = :column

--:name create-new-table! :! :n
CREATE TABLE :i:name
(id SERIAL PRIMARY KEY,
:sql:columns)

--:name drop-table! :! :n
DROP TABLE :i:name

--:name show-tables :? :*
SELECT tablename
FROM pg_catalog.pg_tables
WHERE schemaname != 'pg_catalog' AND 
    schemaname != 'information_schema';