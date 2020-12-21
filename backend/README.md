# How to run

`./gradlew bootRun`

You can access it by opening `http://localhost:8080/` in web browser or using `curl` like, 
`curl http://localhost:8080/`

Issues:
```
...PortInUseException: Port 8080 is already in use
```
First, find the `pid` for the port by,
```
lsof -i tcp:8080
```
Then, kill it by,
```
kill -9 pid 
```