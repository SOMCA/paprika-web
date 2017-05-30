# paprika-Project


## English:

This open-source project is the Web version of Paprika. This version is cut to two parts.
The native Paprika is here: https://github.com/GeoffreyHecht/paprika


* paprika-analyze: Part who run the analyse and send data to neo4J database.

* paprika-web: Part who launch the web page and launch paprika-analyze when it need.

## Français:


Ce projet open-source est la version web de Paprika.
Celle ci est coupé en deux parties.

* Paprika-analyze.jar: Partie qui s'occupe de lancer l'analyse en utilisant en très grande partie la version offline : https://github.com/GeoffreyHecht/paprika .
* Paprika-web.jar: Partie web du projet. Celle ci lance paprika-analyse dans d'autres containers.



## Run: You need build paprika-analyze and web before with mvn clean package then put target/file.jar on the main repertory (with run.sh)
Go on shell and launch:

Docker version:
$./run.sh

Semi docker Version:
$docker-compose build neoj
$docker-compose up -d neoj
$java -jar Paprika-web.jar

## Paprika online use:
* Spark-core framework web.
* Neo4J graph database.(With Cypher).
* Bolt for linked Java to Neo4J.
* Velocity template for page html.
* All things on  https://github.com/GeoffreyHecht/paprika
* Docker




## Bug:
On the menu->Project, new versions can be inverted with the old, only if 0 versions have be analyzed on the project.



