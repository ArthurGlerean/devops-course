## TP1 - Docker

## Database

**1-1 Why should we run the container with a flag -e to give the environment variables?**

Il serait préférable de passer par des variables d'environnement pour des raisons de sécurité et de bonnes pratiques.

**1-2 Why do we need a volume to be attached to our postgres container ?**

Les volumes sont notamment utiles pour les dump ou imports des bases. Ici, il est utile pour tirer les données avant la destruction d'un conteneur (données persistantes).

**1-3 Document your database container essentials: commands and Dockerfile.**

`docker run --net=app-network -v /home/dev/devops-project/tp1/bdd/data:/var/lib/postgresql/data --name=mydatabase
 -d  aglerean/mydatabase`

Dockerfile:
```yaml
FROM postgres:14.1-alpine

ENV POSTGRES_DB=db \
   POSTGRES_USER=user \
   POSTGRES_PASSWORD=password

COPY initial_data.sql /docker-entrypoint-initdb.d
```
## Backend API

![alt text](resources/image.png)

*Dockerfile :*
```yaml
# Build
FROM maven:3.9.9-amazoncorretto-21 AS myapp-build
ENV MYAPP_HOME=/opt/myapp 
WORKDIR $MYAPP_HOME
COPY simpleapi/pom.xml .
COPY simpleapi/src ./src
RUN mvn package -DskipTests

# Run
FROM amazoncorretto:21
ENV MYAPP_HOME=/opt/myapp 
WORKDIR $MYAPP_HOME
COPY --from=myapp-build $MYAPP_HOME/target/*.jar $MYAPP_HOME/myapp.jar

ENTRYPOINT ["java", "-jar", "myapp.jar"]
```

*Commande de lancement :* `docker run -p 8080:8080 aglerean/myapi`

**1-4 Why do we need a multistage build? And explain each step of this dockerfile.**

Nous avons besoin de 2 images: une image maven pour compiler l'applicatif Spring et une image JDK pour exécuter le .jar généré.

Dans un premier temps, on copie les ressources nécessaires à la compilation du projet dans l'image maven (sources et pom.xml pour les dépendances). Le projet est ensuite compilé avec la commande maven (`mvn package -DskipTests`).

Ensuite, le .jar généré dans l'image maven est copié pour pouvoir l'exécuter depuis java, via la commande: `ENTRYPOINT ["java", "-jar", "myapp.jar"]`.

Le build de l'image et le démaragge du conteneur étant fait, on obtient:

![alt text](resources/image-2.png)

Avec le projet d'API et la configuration rattachée à notre base Postgresql, on obtient:

![alt text](resources/image-3.png)

Configuration BDD `application.properties` > jdbc:postgresql://mydatabase:5432/db

**1-5 Pourquoi avons-nous besoin d’un proxy inverse ?**

On utilise un reverse proxy principalement pour des raisons de sécurité.

On expose uniquement le serveur proxy, et non directement l'app.

On peut gérer la répartition de charge si l'on avait plusieurs sources.

\+ Gestion du SSL/TLS

\+ Gestion de la configuration du serveur web

Après avoir lancé Apache, on accède aux APIs via localhost :

![alt text](resources/image-4.png)

**1-6 Pourquoi docker-compose est -il si important ?**

Docker-compose est important car il permet de construire un système de manière cohérente et logique, réuni dans un seul fichier. Il permet d'être beaucoup plus rapide et d'avoir une vision d'ensemble sur les applicatifs d'un projet.

**1-7 Documentez les commandes les plus importantes de docker-compose. 1-8 Documentez votre fichier docker-compose.**

`docker-compose up -d` : build et run les conteneurs en tâche de fond.
`docker-compose down -v` : arrêter et supprimer les conteneurs, ainsi que leurs volumes.
`docker-compose logs -f` : afficher les logs.
`docker-compose ps` : afficher l'état des conteneurs.
`docker-compose exec <conteneur> ...` : exécuter une commande dans un conteneur en cours d'exécution.

*Le docker-compose crée :*

```yaml
version: '3.7'

services:
    myapi:
        build:
          context: ./java
          dockerfile: Dockerfile
        networks:
          - app-network
        depends_on:
          - mydatabase
        ports:
          - "8080:8080"

    mydatabase:
        env_file: ".env"
        image: postgres:14.1-alpine
        environment:
          POSTGRES_DB: ${PSQL_DB}
          POSTGRES_USER: ${PSQL_USR}
          POSTGRES_PASSWORD: ${PSQL_PWD}
        volumes:
          # - ./bdd/initial_data.sql:/docker-entrypoint-initdb.d
          - ./bdd/data:/var/lib/postgresql/data
        networks:
          - app-network

    httpd:
        image: httpd:2.4
        container_name: myhttpd
        ports:
          - "80:80"
        volumes:
          - ./httpd/index.html:/usr/local/apache2/htdocs/index.html
          - ./httpd/httpd.conf:/usr/local/apache2/conf/httpd.conf
        depends_on:
          - myapi
        networks:
          - app-network

networks:
    app-network:
      name: app-network
```

**1-9 Documentez vos commandes de publication et vos images publiées dans dockerhub.**

`docker tag tp1_backend aglerean/tp1_backend:1.0` > Tag sur mon image backend (API sur les étudiants CPE au numéro de version 1.0).

`docker push aglerean/tp1_backend:1.0` > Pousser mon image sur le docker hub.

**1-10 Pourquoi mettons-nous nos images dans un référentiel en ligne ?**

Pour pouvoir les récupérer sur n'importe quelle machine et permettre à d'autres personnes de pouvoir les utiliser.

## TP2 - Github Actions

**2-1 What are testcontainers?**

Ils sont issus d'une bibliothèque Java permettant d’exécuter des conteneurs Docker légers pour des tests d’intégration et les tests unitaires.

![alt text](resources/image-5.png)

**2-2 Document your Github Actions configurations.**

On a 3 étapes majeures:
- une vérification des ressources
- une installation du set-up JAVA/maven
- une exécution des tests d'intégrations via la commande maven

Pour les deux premières, on utilise des actions prédéfinies par Github. Pour la dernière, on se positionne sur le bon répertoire via l'indicateur 'working-directory' et on exécute la commande via 'run'.

**2-3 For what purpose do we need to push docker images?**

Les images doivent être poussées pour pouvoir sauvegarder nos images et pouvoir les réutiliser depuis n'importe quel environnement. De plus, ces images peuvent servir à d'autres développeurs si celles-ci sont accessibles en public.

![alt text](resources/image-6.png)


**2-4 Document your quality gate configuration.**

Ce job se déroule en 5 étapes:
- 1: Récupération du code source via l'action 'checkout'
- 2: Installation du JDK version 21
- 3: Télécharge les dépendances Sonarqube et les mets en cache pour faciliter la CI/CD par la suite
- 4: Télécharge les dépendances Maven et les mets en cache pour faciliter la CI/CD par la suite (si le pom.xml n'est pas changé, il réutilise les anciennes dépendances)
- 5: Exécute l'analyse Sonarqube en utilisant le TOKEN du projet Sonar (renseignée via les variables sécuriées)

![alt text](resources/image-7.png)

**Bonus :**

- Rajout de la ligne suivante pour permettre au job build-and-push-docker-image de s'exécuter uniquement depuis la branche main > `if: github.ref == 'refs/heads/main'`

- Pour faire en sorte que le job build-and-push-docker-image s'exécute uniquement si les tests unitaires/intégration sont passés, il faut ajouter la ligne suivante > `needs: test-backend` ainsi que `if success()`

Ce qui donne: `if: github.ref == 'refs/heads/main' && success()`

## TP3 - Ansible

**3-1 Document your inventory and base commands**

Récapitulatif des commandes et actions effectuées:

- Création d'un fichier de config Ansible (setup.yml):
  - Ce fichier contient des informations, notamment :
    - le nom d'utilisateur à utiliser
    - le chemin d'accès vers ma clé SSH => permettant de ne pas le resaisir à chaque commande
    - le nom d'hôte de ma machine distante
- ansible all -i inventories/setup.yml -m ping
  - On test uniquement le ping sur le serveur
  - 'all' => Cible tous les hôtes définis dans le fichier setup.yml
  - '-i inventories/setup.yml' => Spécifie le fichier d'inventaire utilisé
  - '-m ping' => Utilise le module (-m) ping pour tester la connexion SSH au serveur
- ansible all -i inventories/setup.yml -m setup -a "filter=ansible_distribution*"
  - Récupérer les informations sur l'OS du serveur
  - '-m setup' => Utilise le module setup, qui collecte et affiche les informations système (facteurs Ansible) des hôtes
  - '-a "filter=ansible_distribution*"' => Filtre les résultats pour n'afficher que les variables qui commencent par ansible_distribution, ce qui permet de récupérer le nom et la version du système d'exploitation sur chaque hôte
- ansible all -i inventories/setup.yml -m apt -a "name=apache2 state=absent" --become
  - Suppression du serveur Apache installé précédement
  - '-a "name=apache2 state=absent"' ⇒ Définit les arguments passés au module apt :
    - 'name=apache2' ⇒ Spécifie le paquet à gérer (ici apache2, le serveur web Apache).
    - 'state=absent' ⇒ Indique que le paquet doit être désinstallé s'il est présent sur le système.


**3-2 Document your playbook**

Le playbook passe par plusieurs tâches, que sont:
- l'installation des paquets nécessaires
- l'ajout de la clé gpg Docker pour télécharger les sources
- téléchargement du repository docker suivant notre OS (ici Debian bookworm)
- installation de docker
- installation de python
- création d'un environnement virtuel Python
- installation du SDK docker pour l'environnement virtuel Python
- vérification du bon fonctionnement de docker

**3-3 Document your docker_container tasks configuration.**

On a 4 rôles majeurs: 
- network : s'occupe de créer un réseau docker nommé "app-network"
- database : récupère l'image arthurglerean/tp-devops-bdd et monte un conteneur en se basant sur cette image
- app : récupère l'image arthurglerean/tp-devops-simple-api et monte un conteneur en se basant sur cette image
- proxy : récupère l'image arthurglerean/tp-devops-httpd et monte un conteneur en se basant sur cette image

Globalement dans chaque task, on pull l'image docker, on associe un network et éventuellement un port.

![alt text](resources/image-8.png)

Front en cours