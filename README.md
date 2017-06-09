# paprika-Project


## English:

This open-source project is the Web version of Paprika. This version is cut to two parts.
The native Paprika is here: https://github.com/GeoffreyHecht/paprika

The project use Docker and Maven, you need to have installed these functionality for run the project.


* paprika-analyze: Part who run the analyze and send data to neo4J database.

* paprika-web: Part who launch the web page and launch paprika-analyze when it needed.



## Run: 

For run Paprika-web, you need to compile paprika-analyze and paprika-web per the command:
>> mvn clean package

Then move new jar of target/file.jar on docker web or dockeranalyze (if analyze.jar or web.jar)

When done, you need just to launch:
$./run.sh

The first run launch always a error you need to:
go on <address>:7474 for create a small account.  ( address is "localhost"  per default) 

Where the username need to be neo4j(the default) and the password always: paprika



## Paprika-Web use:
* Spark-core framework web.
* Neo4J graph database.(With Cypher).
* Spark Bolt for linked Java to Neo4J.
* Velocity template for page html.
* Docker
* Spoon of Inria
* Paprika of Inria



## Bug:
On the menu->Project, new versions can be inverted with the old, only if 0 versions have be analyzed on the project.



