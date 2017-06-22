*[EN](README.md)*

[Viac o Android aplik�cii](https://github.com/jozef17/Cyclo-Europe/tree/master/CycloEurope%20-%20Android%20App) ,
[Viac o servery](https://github.com/jozef17/Cyclo-Europe/tree/master/CycloEurope%20-%20Server)

# CycloEurope

Klient - Server aplikácia:
- Zobrazenie aktuálnej polohy používateľa na mape (ak je dostupné GPS)
- Nájdenie najbližšej cyklocesty k zvolenej polohe
- Nájdenie napájajúcich cyklociest k najbližšej cykleoceste
- Nájdenie cykloobchodov, ubytovania a občerstvenia v okolí najbližšej cyklocesty

# Technické podrobnosti
## Android Aplikácia
- Na zobrazovanie mapy používa [MapBox Android SDK](https://www.mapbox.com/android-sdk/)
- Obsahuje mechanizmus na nájdenie servera na lokálnej sieti
- Využíva GPS zariadenia

![component](other/Component.png)

## Server
- Pristupuje ku geodatabáze (PostGIS rozšírenie pre PostgreSQL)
- Odpovedá na požiadavky vo forme GEOJSON správ

### Najbližšia cyklocesta k zvolenej polohe
![gif](other/Cycleway.gif)

### Napájajúce cyklocesty
![gif](other/Connected.gif)

### Ubytovanie v okolí najbližšej cyklocesty
![gif](other/Acomodation.gif)

### Detail
![gif](other/Details.gif)

### Aktuálna poloha podľa GPS
![gif](other/GPSLocation.gif)
