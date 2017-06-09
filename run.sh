
#run this command if spirals-somca do not exist on docker-machine ls
##docker-machine create --driver generic --generic-ip-address=spirals-somca --generic-ssh-key ~/.ssh/id_rsa --generic-ssh-user=gwillefe spirals-somca

#Connect to the spirals-somca VM
docker-machine env spirals-somca
eval $(docker-machine env spirals-somca)
#Delete parasite?
docker system prune -f
#Launch and create the container neo4j
docker-compose build neoj
docker-compose up -d neoj

#Container for Paprika web
docker build -t paprika-web dockerweb

#Container for analyze with a file.apk
docker build -t paprika-analyze dockeranalyze
#Container for analyze with a Github link.
docker build -t paprika-tandoori dockerTandoori

#Launch Paprika web
docker-compose build web
docker-compose up -d web



#Thing for me:

  #docker-machine ssh spirals-somca
    #sudo docker run -it paprika-web /bin/sh
      #ls -a
#--verbose

#docker system prune   delete all not-used things
#if problem you can use --verbose :  exemple: docker-compose --verbose up neo4j
#you can also docker rm/rmi containers/images


#for stop a container: docker stop neo4j-paprika
#for start a container:docker start neo4j-paprika

#For get the id adress of a container:
#docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' neo4j-paprika
