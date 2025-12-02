# Transformateur XSTL — Application de bureau

Une application de bureau simple pour transformer du XML à l'aide de feuilles de style XSLT. 

## Installation de Java et Maven

### Sous macOS

1. Installez [Homebrew](https://brew.sh/) si ce n'est pas déjà fait :
   ```sh
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
2. Installez Java 21 ou mieux :
   ```sh
   brew install openjdk@21
   ```
   Ajoutez Java à votre PATH (si nécessaire) :
   ```sh
   echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```
3. Installez Maven :
   ```sh
   brew install maven
   ```

### Sous Windows

1. Téléchargez Java 21 (ou mieux) depuis [Adoptium.net](https://adoptium.net/fr/temurin/releases/).
   - Choisissez l'installateur MSI pour Windows, puis suivez les instructions pour installer Java.
   - Lors de l'installation, cochez l'option pour ajouter Java au PATH si disponible.
2. Installez Maven :
   - Téléchargez la dernière version binaire de Maven depuis [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
   - Décompressez l'archive ZIP dans un dossier (ex : `C:\maven`).
   - Ajoutez le chemin du dossier `bin` de Maven à la variable d'environnement `PATH` :
     1. Ouvrez le menu Démarrer, recherchez « variables d'environnement » et ouvrez « Modifier les variables d'environnement système ».
     2. Cliquez sur « Variables d'environnement… ».
     3. Dans la section « Variables système », sélectionnez la variable `Path` puis cliquez sur « Modifier… ».
     4. Cliquez sur « Nouveau » et ajoutez le chemin complet du dossier `bin` de Maven (ex : `C:\maven\bin`).
		 5. Cliquez sur OK pour valider et fermez toutes les fenêtres.
3. Redémarrez le terminal pour que les variables d'environnement soient prises en compte.

### Vérification

Vérifiez l'installation avec :
```sh
java -version
mvn -version
```
Les deux commandes doivent afficher la version installée.

## Compilation

Dans le dossier du projet, exécutez :

```bash
mvn clean package
```

Cette commande produit le JAR dans `target/` (un JAR ombré/fat qui contient les dépendances).

## Exécution

- En développement vous pouvez lancer via Maven :
	```sh
	mvn exec:java
	```
- Exécuter le JAR construit :
	```sh
	java -jar target/xslt-transformer-app-1.0.0.jar 
	```

## Utilisation

1. Collez votre feuille de style XSLT dans l'éditeur de gauche.
2. Collez le XML d'entrée dans l'éditeur de droite.
3. Le bouton `Appliquer` est activé lorsque les deux entrées sont valides.
4. Cliquez sur `Appliquer XSLT →` pour générer le résultat, qui s'affiche en bas.


