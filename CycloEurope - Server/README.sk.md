*[EN](README.md)*

[Pozrieù zdrojov˝ kÛd](https://github.com/jozef17/Cyclo-Europe/tree/master/CycloEurope%20-%20Server/src/sk/blazicek/cycloEurope/server)

# CycloEurope - Server

- Zdroj d·t - https://www.openstreetmap.org/
- Import d·t - osm2pgsql
- Datab·za - [PostgreSQL](https://www.postgresql.org/) z [PostGIS](http://postgis.net/) rozöÌrenÌm

## URI

### H?adanie servera
```
http://[IP]:1234/Identify
```
### N·jdenie najbliûöej cyklocesty
```
http://[IP]:1234/Closest?long=[longitude]&lat=[latitude]&
```
### Zahrnutie nap·jacÌch cyklociest
```
http://[IP]:1234/Connected?long=[longitude]&lat=[latitude]
```
### ?a?öie parametre
```
&shop=true
&glass=true
&sleep=true
```

## Selekty
### Najbliûöia cyklocesta
```SQL
WITH closest AS ( 
  SELECT ref, ST_Distance(ST_GeomFromText('POINT(" + lonLat[0] + " " + lonLat[1] + ")',4326), ST_Transform(way,4326) ) AS distance 
  FROM planet_osm_line
  WHERE (((bicycle IS NOT NULL ) AND (bicycle <> 'no')) OR (highway = 'cycleway') OR ((route = 'mtb') OR (route='bicycle'))) AND ref IS NOT NULL
  ORDER BY distance ASC LIMIT 1 ),
-- N·jdenie ?a?öÌch ?astÌ cyklocesty
road AS (
  SELECT line.way, line.ref as prop 
  FROM planet_osm_line AS line, closest " + "WHERE line.ref = closest.ref )
SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop FROM road;
```

### Include points of interest near by cycleway
```SQL
WITH closest AS ( 
  -- RovnakÈ ako v predoölom
 ),
road AS (
  -- RovnakÈ ako v predoölom
), 
point AS (
  SELECT * FROM planet_osm_point WHERE 
  -- Ak bol cykloobchod zvolen˝
  (shop = 'bicycle')
  OR 
  -- Ak bolo ob?erstvenie zvolenÈ
  (((amenity like '%bar%') OR (amenity like '%caffe%')  OR (amenity like '%food%') OR (amenity = 'pub')))
  OR
  -- Ak bolo ubytovanie zvolenÈ
  (((tourism = 'hotel') OR (tourism = 'motel') OR (tourism = 'hostel') OR (tourism = 'apartment')))
  )
-- Spojenie v˝sledkov - najbliûöia cyklocesta a body z·ujmu
SELECT ST_AsGeoJson(ST_Transform(p.way,4326)) AS geojson, p.name AS prop, p.shop, p.amenity, p.tourism 
FROM point AS p, road AS r 
WHERE ST_DWithin(r.way,p.way,1000)
UNION ALL
SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop, NULL, NULL, NULL
FROM road;
```

### Find connecting cycleways
```SQL
WITH closest AS ( 
  -- RovnakÈ ako v predoölom
 ),
road AS (
  -- RovnakÈ ako v predoölom
)
-- Nap·jacie cyklocesty
roads AS (
  SELECT line.way, line.ref AS prop 
	FROM  planet_osm_line AS line, road
	WHERE line.ref <> road.prop AND ST_DWithin(line.way,road.way,0)
      AND (((bicycle IS NOT NULL ) AND ... 
  )
SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop 
FROM roads;
```

## ätrukt˙ra tried

![component](https://github.com/jozef17/Cyclo-Europe/blob/master/other/Server-class.png)
