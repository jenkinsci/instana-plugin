# Developing the Instana Jenkins Plugin

## Run a local Jenkins with the plugin

To execute a local Jenkins with the current SNAPSHOT version of the plugin run
```
mvn clean hpi:run
```
and open `http://localhost:8080/jenkins/`.
Then set the Instana configuration at `http://localhost:8080/jenkins/configure` with URL and API token.s

Create a free style job and set as build action `Mark a release in Instana`.

Create a pipeline with the following snippet as script.
```
node {
    stage('release') {
        releaseMarker releaseName: "Test Release ${currentBuild.number}"
    }
}
```
A pause/resume might be needed to get the build done.