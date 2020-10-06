# Instana Pipeline Feedback Integration for Jenkins

This plugin provides you with the possibility to inject Release Markers into Instana


## Features

The following features are available in both Pipeline and traditional
project types:

* Injecting Release Markers into Instana 

### Pipeline features
To ingest a Release Marker into Instana you simply need to add 
`releaseMarker` to your Pipeline script .

There is one mandatory parameter `releaseName` and two optional parameters `serviceNames` and `applicationNames`.

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

To create a release scoped to a given service or application you can add the `serviceNames` or `applicationNames` as parameter

```groovy
// service scoped
releaseMarker releaseName: "Release 4711", serviceNames: ["my-service"]
```

```groovy
// application scoped
releaseMarker releaseName: "Release 4711", applicationNames: ["My Application"]
```

