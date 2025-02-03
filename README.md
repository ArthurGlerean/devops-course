**1-1 Why should we run the container with a flag -e to give the environment variables?**

Il serait préférable de passer par des variables d'environnement pour des raisons de sécurité et de bonnes pratiques.

**1-2 Why do we need a volume to be attached to our postgres container ?**

Les volumes sont notamment utiles pour les dump ou imports des bases. Ici, il est utile pour tirer les données avant la destruction d'un conteneur.


*Commandes exécutées:*

`docker run --net=app-network -v /home/dev/devops-project/tp1/bdd/data:/var/lib/postgresql/data --name=mydatabase
 -d  aglerean/mydatabase`