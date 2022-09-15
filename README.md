# Dromi Backend
This is the backend, that handles all the imports from and communication with other tools and also calculates the impacts and creates issues about these impacts

It is a Spring boot application and supposed to run as a service.

Additional Information is provided in the corresponding thesis: http://dx.doi.org/10.18419/opus-12038
A demonstration is on youtube : https://youtu.be/3E90neB-iUY 

## HTTP Endpoints 

* `/` : GET greetings
* `/api/model/{id}` : GET the model with the given id as XML or POST a newer version (as XML) of the model to update it.
* `/api/model` : POST an import request to create a new model. Creation in this case means importing models for architecture, process and Slo rules, as specified in the import request, and putting them into one model. 
* `/api/alert` : POST alerts here to trigger the computation of impacts (and creation of issues). 

## Requirements I

The thesis' backend is a Spring Boot application. 
Thus you need Java. 
You can make a Docker container and up it alone or along side its database. 
In that case you need Docker and for the latter docker compose, too.

Thing           | Version   
----------------|-----------
Java            | `11`
Docker          | `20.10.8`
docker-compose  | `1.26.2`
Apache Maven    | `3.6.3`


## Requirements II
The backend needs an instance of Gropius and the Solomon Tool each to work properly. 
It has been tested with these versions. 
Others might work as well.

Tool            | Version (commit)  | Purpose
----------------|-------------------|--------------------------------------
[Gropius backend](https://github.com/ccims/ccims-backend-gql) | 123381d602a53241d270256b471874578cd621be | Cross-component issue management tool. For us, it provides the architecture and manages the issues we create. If there is not yet any architecture you must add one yourself. When adding a new architecture, it might be recommendable to also run the [Gropius frontend](https://github.com/ccims/ccims-frontend).
[Solomon](https://github.com/ccims/solomon) | d1861e603a122b23c959511a35a5c6668deadb7f | Sla management tool. For us, it provides the slo rules. If there are not yet any Slo rules, you must add them yourself. When adding new Slo rules, it might be recommendable to also run the front end. Otherwise, the back end is sufficient.


## Spring Properties

Property    | Read from env var | Description
------------|-------------------|----------------
gropius.url | GROPIUS_URL       | Location of the Gropius Backend (e.g `http://localhost:8080/api`). This is the location at which the thesis' backend tries to create issues. 
spring.data.mongodb.uri | MONGO_HOST | The backend depends on a database. This is the database's hostname (e.g. `localhost`).


## Build & Run

1. Get into the backend repository : 
```
git clone https://github.com/stiesssh/ma-backend.git
cd ma-backend
```
2. The backend needs the models and the java bindings for the Gropius API as local maven dependencies. 
In the models' repository is a pom.xml to install each model, and in the API binding's repository is one for the API bindings. 
To install all the models and build a jar for the backend execute the `build.sh`.
If you cannot to that, just go to each model and do the maven thing manually. 
```
./build.sh
```

3. Build Docker image and up it together with database. You *must* start an instances of the Gropius before doing this and also set the value of the `GROPIUS_URL` environment variable to where your Gropius runs. 
```
docker build -t backend .
docker-compose up 
```

## FYI
At start up, the thesis backend loads existing models from the database. 
Thus you must only do the saga modeling part with the frontend *once*. 
As long as you do not clear or delete the database, you can stop and restart the backend to your liking without losing the models. 
