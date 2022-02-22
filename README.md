# Challenge Dataiku
## Compilation
Le projet à été développé en Java 11 avec l'aide du Framework Spring.
On peut le compiler avec un classique mvn clean install -U

## Usage
Une fois le jar généré, on peut l'utiliser de deux façon différent
### Mode WebApp
On peut lancer l'application avec la commande suivante :

$ java -jar millenium-1.0.0.jar

Cela va démarrer un Tomcat embarqué qui va lancer l'application à l'adress localhost:8080. 
Il suffit ensuite d'uploader un fichier empire pour avoir la probabilité de succès.
Dans ce cas, on utilise le fichier falcon et universe fourni par déffaut pour les tests.

### Mode Console
On peut lancer l'application avec la commande suivante :

$ java -jar millenium-1.0.0.jar {chemin_vers_le_fichier_falcon.json} {chemin_vers_le_fichier_empire.json}

Cela va lancer les calculs à partir des fichiers fournie et imprimer dans la console le résultat
ou une erreur avant d'arrêter l'application.