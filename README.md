# de.unistuttgart.ma.backend
This is the backend, that handles all the imports and communication from and with other tools and also calculates the impacts.


# API 

GET :
curl localhost:8083/ --> greetings


POST :
/api/alert --> trigger impact computations

/api/foo --> post model here (frontend)

/api/model/{filename} --> post model here, to update, is this even in use??