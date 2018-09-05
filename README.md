[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/verticle-io/fireboard)

oculus
======

**Kubernetes event monitoring for [Fireboard](https:/fireboard.verticle.io)**

Oculus is a small Spring Boot application that connects to the k8s api service and forwards the events in all namespaces to the Fireboard API.

<img src="https://raw.githubusercontent.com/verticle-io/oculus/master/images/fireboard-oculus-5.png" alt="Screenshot" style="max-width:100%;">

<img src="https://raw.githubusercontent.com/verticle-io/oculus/master/images/fireboard-oculus-6.png" alt="Screenshot" style="max-width:100%;">

<img src="https://raw.githubusercontent.com/verticle-io/oculus/master/images/fireboard-oculus-7.png" alt="Screenshot" style="max-width:100%;">

<img src="https://raw.githubusercontent.com/verticle-io/oculus/master/images/fireboard-oculus-8.png" alt="Screenshot" style="max-width:100%;">

<img src="https://raw.githubusercontent.com/verticle-io/oculus/master/images/fireboard-oculus-9.png" alt="Screenshot" style="max-width:100%;">





Installation
------------

Clone this repository.

### Prerequisites

Create a ServiceAccount if needed:

```
$ kubectl apply -f serviceaccount-rbac.yaml
```


### Configure & Deploy

Open a free Fireboard beta account at https:/fireboard.verticle.io . You can use your github account to sign in.

When done, access Fireboard, head to "set" in the nav bar and retrieve

* your API key
* your tenant ID
* the default data bucket ID


Adjust `oculus-configmap.yaml` accordingly ...


```
apiVersion: v1
kind: ConfigMap
metadata:
  name: oculus
  namespace: default

data:
  application.properties: |-
    tenantId=1101
    bucketId=59bab939749c252e09f3770c
    authToken=eyJhbGciOiJ...

```


... and deploy the configuration:

```
$ kubectl apply -f oculus-configmap.yaml
```


Finally deploy the oculus service:

```
$ kubectl apply -f oculus-deployment.yaml
```

Now head to https://fireboard.verticle.io and open the Fireboard web application and trigger some events on your cluster.
