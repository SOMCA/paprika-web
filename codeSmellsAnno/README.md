Install the local jar:


Compile the jar: 
>>mvn [optional]clean package 

Install the jar:
>>mvn install:install-file -Dfile=CodeSmellsAnno/target/CodeSmellsAnnotations.jar -DpomFile=CodeSmellsAnno/pom.xml
