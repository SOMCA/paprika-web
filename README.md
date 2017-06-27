# paprika-Project

You can access to the site on the: https://spirals-somca.inria.lille.fr/paprika/index/


## Description:

This open-source project is the Web version of Paprika. This version is cut to three parts.
The native Paprika is here: https://github.com/GeoffreyHecht/paprika

The project use Docker and Maven, you need to have installed these functionality for run the project.


* paprika-analyze: Part who run the analyze with file.apk and send data to neo4J database.

* paprika-web: Part who launch the web page and launch paprika-analyze when it needed.

* paprika-tandoori: Part who run the analyze for GitHub.


## Run:

For run Paprika-web, you need to compile paprika-analyze, paprika-web ad paprika-Tandoori,CodesmellsAnno per the command:
>> mvn clean package

Then move new jar of target/file.jar on dockerweb,dockeranalyze or dockerTandoori
CodesmellsAnno.jar go only on dockerTandoori.

If you want use with GitHub:
You need also to read also readme of dockerweb,analyze,tandoori
For have "token", you need to create a GitHub account, then create a token who have access to repo.

When done, you need just to launch:
```
$./run.sh
```

Then you need to go on your docker-machine, for me, this is
```
docker-machine ssh spirals-somca.
```
Then install docker-compose if not installed, see
https://docs.docker.com/compose/install/
For finish, just launch:
```
sudo docker-compose up web
```



The first run launch always a error you need to:
go on <address>:7474 for create a small account.  ( address is "localhost"  per default)
Where the username need to be neo4j(the default) and you need to put the same password than the "neo4j_pwd" put on your json.


### CSAnnotations, Paprika, Tandoori:
On the folder of each.
After the mvn clean package.

Install the jar:
```
mvn install:install-file -Dfile=./target/CodeSmellsAnnotations.jar -DpomFile=./pom.xml

```
```
mvn install:install-file -Dfile=./target/Paprika.jar -DpomFile=./pom.xml

```
```
mvn install:install-file -Dfile=./target/Tandoori.jar -DpomFile=./pom.xml

```

Then on your pom.xml, you can put the dependency:
Maven:
```
<dependency>
	<groupId>codesmells.annotations</groupId>
	<artifactId>CSAnnotations</artifactId>
	<version>1</version>
</dependency>
<dependency>
	<groupId>paprika</groupId>
	<artifactId>Paprika</artifactId>
	<version>latest</version>
</dependency>
<dependency>
	<groupId>tandoori</groupId>
	<artifactId>Tandoori</artifactId>
	<version>latest</version>
</dependency>
```


## Paprika-Web use:
* Spark-core framework web.
* Neo4J graph database.(With Cypher).
* Spark Bolt for linked Java to Neo4J.
* Velocity template for page html.
* Docker
* Spoon of Inria
* Paprika of Inria



## Bug:
-On the menu->Project, new versions can be inverted with the old, only if 0 versions have be analyzed on the project.


## Requirement of the site:
* Paprika-analyze:
** accept only File.apk.
** Can be very long.

* Paprika-Tandoori
** require these Github Link "https://github.com/Snrasha/paprika-web.git"
** If you have  two files who have the same name and the same package, theses files will no be analyzed correctly.
