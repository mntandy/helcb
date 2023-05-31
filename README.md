# helcb

This is an SPA webapp to explore CityBike journeys in Helsinki. Backend is written in Clojure utilsing Conman and Jetty, Frontend is in Clojurescript with Reagent and React. Built with Leiningen and shadow-cljs.

It is a variation of the pre-assignment for Solita Dev Academy Finland 2023 [https://github.com/solita/dev-academy-2023-exercise](https://github.com/solita/dev-academy-2023-exercise). This was a very good exercise to dive into Clojure.

The app is still work in progress. A live version can be found at [https://helcb.fly.dev](https://helcb.fly.dev/). Please note that While the site is hosted at fly.io, the database is hosted by supabase, and the site can thus ract slightly slow. This can be annoying in combination with the lack of temporary info about that the site is waiting for the database.

Also, please note that I completely forgot to try out how the website looks when the window is resized or otherwise not fullscreen. I also did not test it on mobile. That's quite embarassing.

## Local build instructions
The project is setup to compile with [Leiningen](https://leiningen.org/) and [shadow-cljs](https://github.com/thheller/shadow-cljs).

For a development build of the ClojureScript code:

    npx shadow-cljs compile helcb

To compile and run locally a development build of the Clojure code:

    lein run

However, to try this out locally, you need to have a postgres server available. To inform the backend about its location, you create a file `dev-config.edn` at the project root with the following content:
     
    {:jetty-opt {:join? false :port 3000}
     :database-url "postgresql://insertyourpostgresconnectionhere" }

The database should contain two tables:

* The table `stations` with the columns `stationid, nimi, namn, name, osoite, adress, kaupunki, stad, x, y`.
* the table `journeys` with the columns `departure, return, departure_station_id, return_station_id, distance, duration`. 

The easiest way to create the tables with the appropriate types is to do it from within the REPL. Rather than just running the backend as above, you run the command:

    lein repl

You can now first run the function `(mount/start)` to initialise the web server and connect to the postgres server. You can now create the required tables by running `(db/create-stations-table)` and `(db/create-journeys-table)`. 

Once you have the server up and running, you can open the website in your browser at [http://localhost:3000/](http://localhost:3000/).

The app has not been tested on mobile devices.

## Importing data
After opening the website in your browser, you can from there now import the stations from [https://opendata.arcgis.com/datasets/726277c507ef4914b0aec3cbcfcbfafc_0.csv](https://opendata.arcgis.com/datasets/726277c507ef4914b0aec3cbcfcbfafc_0.csv) and then journey data from 

* [https://dev.hsl.fi/citybikes/od-trips-2021/2021-05.csv](https://dev.hsl.fi/citybikes/od-trips-2021/2021-05.csv)
* [https://dev.hsl.fi/citybikes/od-trips-2021/2021-06.csv](https://dev.hsl.fi/citybikes/od-trips-2021/2021-06.csv)
* [https://dev.hsl.fi/citybikes/od-trips-2021/2021-07.csv](https://dev.hsl.fi/citybikes/od-trips-2021/2021-07.csv)
 
If you want to add additional stations or journeys, just make sure that the csv files for journeys contain the columns 

    Departure,Return,Departure station id,Return station id,Covered distance (m),Duration (sec.)

The csv file for importing stations should include the columns

    ID,Nimi,Namn,Name,Osoite,Adress,Kaupunki,Stad,x,y

You can use `;` instead of `,` as separator in the csv file.

It is important that stations are imported BEFORE journeys, because a journey can only be imported if it goes between two stations ID that are already in the database. To avoid the issue that sometimes the station ID is provided with leading zeros and other times not, leading zeros will be trimmed away from station ID's both when importing stations and when importing journeys. In addition to checking whether the stations exists when importing a journey, a journey is only imported if the return time is at least 10 seconds after the departure time, and the distance travelled is more than 10 meters. To avoid duplicates, it also checks whether a journey with the same properties has already been imported.

It can take a while to import a large csv file, and the app does not give you any report on the progress until it is done or there is an error. If you prefer a bit more control, the import can also be done directly through the REPL by executing

    (batch-stations-from-csv {:uri "thefile" :separator ","})
    (batch-journeys-from-csv {:uri "thefile" :separator ","})

Imported journeys are associated the id's of departing and returning station, not their names. Only the id's are imported. This means that if the id is wrong, then the journey is associated with the wrong stations. This was done because the station names are "all over the place".

## Production build
The current production build is obtained as follows. First, we compile a release build of the ClojureScript:

    npx shadow-cljs release helcb

And an uberjar of the Clojure code with lein:

    lein uberjar

To this, you can now make a file `prod-config.edn`just like the one above. To simply run the server

    java -Dconf=dev-config.edn -jar target/uberjar/helcb.jar

Otherwise, you can use the `Dockerfile` to get a container after compiling the uberjar and the cljs release build.

## Main features
With the app, you can search for particular journeys and stations, and then open a view to display info about particular stations. 

You can search for stations either using the map or with the list view. You can only use the list view to search for journeys. When searching for stations in the list, you can:
* Choose between displaying station names in Finnish, "English" or Swedish
* Open the station view of a station by clicking the name
* Center on the station on the map by clicking the address

To search for stations using the map, click "show all stations" and then find on the map the one you are curious about. Once you found and opened the station view of the station or stations in question, you can click "hide all stations" to clear the map.

The station view contains the following:
* A button to open a little table through which one can edit the info about the station
* A button to center the map on the station (and add a marker for it if a marker for is not already visible)
* Statistical information about the station
    * Top 5 destinations and average daily traffic per hour from the station during either week days or weekends
    * Top 5 origins for journeys and average daily traffic per hour to station during either week days or weekends

The station view thus contain info about the station and a chart displaying how often, on average, someone departs or returns a bike to that particular station, either during weekdays or weekends, and where they are most likely to go. 

Importantly, you can open multiple stations views at the same time for comparison. In fact, you can open as many as your browser can handle.

## Todo
* Add a "loading"-functionality, for example to the pointer, that provides a hint to the user that the site is waiting for info from the database.
* Complete the rest of input validation functions in `validation.cljc`.
* Make it function and look nice when the window is smaller or also on mobile.
* Add some authentication procedure to block malicious and random imports.
* Speed up the import process by replacing the batching with a transaction.
* Make the filter for searching time stamps more versatile and interesting as it is currently limited to complete timestamps with date.

## License

Copyright © 2023 Andreas Fjellstad

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
