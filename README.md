# Instana Pipeline Feedback Integration for Jenkins

This plugin provides you with the possibility to inject Release Markers into Instana


## Features

The following features are available in both Pipeline and traditional
project types:

* Injecting Release Markers into Instana 

### Pipeline features
To ingest a Release Marker into Instana you simply need to add 
`releaseMarker` to your Pipeline script .

There is one mandatory parameter `releaseName` and two optional parameters `services` and `applications`.

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

To create a release scoped to a given service or application you can add the `services` or `applications` as parameter

```groovy
// service scoped, single service
releaseMarker releaseName: "Release 4711", services: [service(name: "my-service")]

// service scoped, multiple services
releaseMarker releaseName: "Release 4711", services: [service(name: "my-service-1"), service(name: "my-service-2")]
```

```groovy
// application scoped, single application
releaseMarker releaseName: "Release 4711", applications: [application (name: "My Application")]

// application scoped, multiple applications
releaseMarker releaseName: "Release 4711", applications: [application (name: "My Application-1"), application (name: "My Application-2")]
```

