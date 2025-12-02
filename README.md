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

- Exécuter le JAR construit :
	```sh
	java -jar target/xslt-transformer-app-1.0.0-shaded.jar
	```
- En développement vous pouvez lancer via Maven :
	```sh
	mvn -DskipTests exec:java
	```

## Création de packages natifs (macOS / Windows)

Le projet inclut des workflows GitHub Actions pour produire des installateurs :

- macOS : un workflow génère un `.dmg` et un `.app` via `jpackage` (fichier dans les artefacts de release).
- Windows : un workflow génère un `MSI` via `jpackage` sur un runner Windows (artefact de release).

Localement, vous pouvez utiliser `jpackage` (présent dans les distributions JDK modernes) :

### macOS (exemple local)

```sh
# génère un .icns depuis target/app-icon.png (outil `iconutil` disponible sur macOS)
# puis :
jpackage --type dmg \
	--input target \
	--name XsltTransformer \
	--main-jar xslt-transformer-app-1.0.0.jar \
	--main-class XsltTransformerApp \
	--icon target/app-icon.icns \
	--app-version 1.0.0 \
	--dest target/installer
```

### Windows (exemple local — nécessite jpackage exécuté sur Windows)

```ps1
jpackage --type msi `
	--input target `
	--name XsltTransformer `
	--main-jar xslt-transformer-app-1.0.0.jar `
	--main-class XsltTransformerApp `
	--icon target/app-icon.ico `
	--app-version 1.0.0 `
	--dest target/installer
```

Note : pour Windows, `jpackage` requiert WiX Toolset installé pour produire un MSI.

## Release et artefacts CI

Les workflows GitHub Actions sont configurés pour se déclencher lors d'une **release publiée** et téléverser les installateurs (DMG / MSI) directement dans la Release GitHub. Pour produire les installateurs via CI :

1. Poussez votre code sur la branche `main`.
2. Créez une Release via l'interface GitHub (nouveau tag / release). Les workflows déclencheront et, si tout se passe bien, les installateurs seront ajoutés à la Release.

## Utilisation

1. Collez votre feuille de style XSLT dans l'éditeur de gauche.
2. Collez le XML d'entrée dans l'éditeur de droite.
3. Le bouton `Appliquer` est activé lorsque les deux entrées sont valides.
4. Cliquez sur `Appliquer XSLT →` pour générer le résultat, qui s'affiche en bas.

## Dépannage

- Si l'application n'apparaît pas ou si l'icône du Dock est celle de Java par défaut, créez un package natif (`.app` ou `.dmg`) avec `jpackage` pour une intégration complète.
- Pour des builds signés/notarisés (macOS), un certificat Apple Developer est requis.

