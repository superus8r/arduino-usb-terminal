import os

outputPath = "app/google-services.json"

fileContent = os.getenv('GOOGLE_SERVICES_JSON')

fhand = open(outputPath, 'w')
fhand.write(str(fileContent))
fhand.close()