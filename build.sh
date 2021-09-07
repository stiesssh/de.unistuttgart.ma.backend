#! /bin/bash

git clone https://github.com/stiesssh/ma-sirius.git

cd ma-sirius

# build gropius api
cd de.unistuttgart.gropius.api && mvn clean install && cd ..
# build gropius
cd de.unistuttgart.gropius && mvn clean install && cd ..
# build gropius slo
cd de.unistuttgart.gropius.slo && mvn clean install && cd ..
# build saga
cd de.unistuttgart.ma.saga && mvn clean install && cd ..
# build impact
cd de.unistuttgart.ma.impact && mvn clean install && cd ..
# build backend
cd .. && ./mvnw clean install

#rm -rf ma-sirius
