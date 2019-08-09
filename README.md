# Instana Pipeline Feedback Integration for Jenkins

This plugin provides you with the possibility to inject events into Instana


## Features

The following features are available in both Pipeline and traditional
project types:

* Injecting releases into Instana 

### Pipeline features
To ingest a release event into Instana you simply need to add 
`releaseMarker` to your Pipeline script .

There is one mandatory parameter `releaseName`

For example
```groovy
releaseMarker releaseName: "Release 4711"
```
or 
```groovy
releaseMarker releaseName: "Test Release ${currentBuild.number}"
```

If you wish to create a release for a particular timestamp you can use the optional `releaseStartTimestamp`

```groovy
releaseMarker releaseName: "Test Release ${currentBuild.number}", releaseStartTimestamp: "1564486446000"
```