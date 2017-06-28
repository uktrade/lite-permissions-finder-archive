# LITE Permissions Finder

End-user guidance for finding import and export licences.

## Getting started

* Download everything - `git clone <url>`, `cd lite-permissions-finder`, `git submodule init`, `git submodule update`
* Start a local Redis - `docker run -p 6379:6379 --name my-redis -d redis:latest`
* Copy `sample-application.conf` to `application.conf`, update `redis` details to point to your local Redis
* Run the application - `sbt run`
* Go to the index page (e.g. `http://localhost:9000`)