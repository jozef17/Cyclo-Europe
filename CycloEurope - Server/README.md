*[SK](README.sk.md)*

[View source code](https://github.com/jozef17/Cyclo-Europe/tree/master/CycloEurope%20-%20Server/src/sk/blazicek/cycloEurope/server)

# CycloEurope - Server

- Data source - https://www.openstreetmap.org/
- Import data with osm2pgsql
- Database - [PostgreSQL](https://www.postgresql.org/) with [PostGIS](http://postgis.net/) extension

## URI

### Server discovery
```
http://[IP]:1234/Identify
```
### Finds closest cycleway
```
http://[IP]:1234/Closest?long=[longitude]&lat=[latitude]&
```
### Includes connected cycleways
```
http://[IP]:1234/Connected?long=[longitude]&lat=[latitude]
```
### Additional parameters
```
&shop=true
&glass=true
&sleep=true
```

## Selects
### Closest cycleway
```SQL
-- Finds closest
WITH closest AS ( 
  SELECT ref, ST_Distance(ST_GeomFromText('POINT(" + lonLat[0] + " " + lonLat[1] + ")',4326), ST_Transform(way,4326) ) AS distance 
  FROM planet_osm_line
  WHERE (((bicycle IS NOT NULL ) AND (bicycle <> 'no')) OR (highway = 'cycleway') OR ((route = 'mtb') OR (route='bicycle'))) AND ref IS NOT NULL
  ORDER BY distance ASC LIMIT 1 ),
-- Finds other parts of cycleway
road AS (
  SELECT line.way, line.ref as prop 
  FROM planet_osm_line AS line, closest " + "WHERE line.ref = closest.ref )
-- Return whole cycleway
SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop FROM road;
```

### Include points of interest near by cycleway
```SQL
WITH closest AS ( 
  -- Same as before
 ),
road AS (
  -- Same as before
), 
point AS (
  SELECT * FROM planet_osm_point WHERE 
  -- If bike shop selected
  (shop = 'bicycle')
  OR 
  -- If refreshment selected
  (((amenity like '%bar%') OR (amenity like '%caffe%')  OR (amenity like '%food%') OR (amenity = 'pub')))
  OR
  -- If accommodation Selected
  (((tourism = 'hotel') OR (tourism = 'motel') OR (tourism = 'hostel') OR (tourism = 'apartment')))
  )
-- Merge results - closest cycleway and near by points of interest
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
  -- Same as before
 ),
road AS (
  -- Same as before
)
-- Connecting cycleways
roads AS (
  SELECT line.way, line.ref AS prop 
	FROM  planet_osm_line AS line, road 
	WHERE line.ref <> road.prop AND ST_DWithin(line.way,road.way,0)
      AND (((bicycle IS NOT NULL ) AND ... 
  )
SELECT ST_AsGeoJson(ST_Transform(way,4326)) AS geojson, prop 
FROM roads;
```



## Class Structure

![component](https://github.com/jozef17/Cyclo-Europe/blob/master/other/Server-class.png)