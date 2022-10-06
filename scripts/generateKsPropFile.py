# This script reads the keystore properties from environment variable
# and generates a keystore.properties file in app module.
#
# More info about gradle properties:
# https://ubuntudroid.medium.com/handling-environment-variables-in-gradle-fb1b8bb6c758

import os

outputPath = "app/keystore.properties"

keystoreFile = os.getenv('KS_PATH')
keystorePassword = os.getenv('KS_PASSWORD')
keyAlias = os.getenv('KS_KEY_ALIAS')
keyPassword = os.getenv('KS_KEY_PASSWORD')

fhand = open(outputPath, 'w')
fhand.write("# Gradle properties for module app\n\n")
fhand.write("release.file=" + str(keystoreFile) + "\n")
fhand.write("release.storePassword=" + str(keystorePassword) + '\n')
fhand.write("release.keyAlias=" + str(keyAlias) + '\n')
fhand.write("release.keyPassword=" + str(keyPassword))
fhand.close()