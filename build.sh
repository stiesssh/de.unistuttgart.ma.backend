#! /bin/bash


dir=$(pwd)
echo $dir

# clone models
git clone https://github.com/stiesssh/ma-models.git
cd ma-models

# install models
cd de.unistuttgart.gropius && mvn clean install && cd ..
cd de.unistuttgart.gropius.slo && mvn clean install && cd ..
cd de.unistuttgart.ma.saga && mvn clean install && cd ..
cd de.unistuttgart.ma.impact && mvn clean install && cd ..

cd $dir

# clone api bindings
git clone https://github.com/stiesssh/ma-gropius-apibinding.git
cd ma-gropius-apibinding && ./mvnw clean install


# build backend
cd $dir && ./mvnw clean install

rm -r ma-models
rm -r ma-gropius-apibinding
