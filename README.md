# StudentHub_CC

### Created by: Chirnogeanu Maria-Andreea & Pauna Irina-Catrinel

Helm-based Kubernetes deployment for the **StudentHub** microservices platform (Auth, Business, PostgreSQL, Adminer) with **Metrics Server**, **Prometheus**, and **Grafana**.

---

# Cluster Setup & Deployment

## Prerequisites

* Docker Desktop (Kubernetes enabled)
* `kubectl`
* `helm`

Verify:

```bash
kubectl version --client
helm version
```

---

## 1. Enable Kubernetes (Docker Desktop)

1. Docker Desktop → **Settings** → **Kubernetes**
2. Enable **Kubernetes**
3. Click **Apply & Restart**

Verify the cluster:

```bash
kubectl cluster-info
```

---

## 2. Build & Push Docker Images

Each microservice is packaged as a Docker image and referenced by Helm via `values.yaml`.

From the project root:

```bash
# Auth service
docker build -t mariaac53695/studenthub-auth:2.3 ./auth
docker push mariaac53695/studenthub-auth:2.3

# Business service
docker build -t mariaac53695/studenthub-business:2.1 ./business
docker push mariaac53695/studenthub-business:2.1
```

Verify images:

```bash
docker images | findstr studenthub
```

---

## 3. Deploy StudentHub via Helm

From the Helm chart root:

```bash
helm upgrade --install studenthub . -n studenthub --create-namespace
kubectl get pods -n studenthub
```

---

## 4. Install Portainer

Add repo + install:

```bash
helm repo add portainer https://portainer.github.io/k8s/
helm repo update

>> helm upgrade --install portainer portainer/portainer \                                                                                                                           
>>   -n portainer --create-namespace \                                                                                                                                             
>>   --set service.type=ClusterIP
```

Verify:

```bash
kubectl get pods -n portainer
kubectl get svc -n portainer
```

Deploy the Portainer Agent into the cluster:

```bash
kubectl apply -f https://downloads.portainer.io/ce2-33/portainer-agent-k8s-lb.yaml
```

Verify the agent is running:

```bash
kubectl get pods -n portainer
kubectl get svc -n portainer | findstr agent
```
---

## 5. Install Metrics Server

Add repo + install:

```bash
helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/
helm repo update

helm upgrade --install metrics-server metrics-server/metrics-server -n kube-system \
  --set args="{--kubelet-insecure-tls,--kubelet-preferred-address-types=InternalIP\,ExternalIP\,Hostname}"
```

Verify:

```bash
kubectl get pods -n kube-system | findstr metrics
kubectl top nodes
kubectl top pods -n studenthub
```

---

## 6. Install Prometheus & Grafana (using kube-prometheus-stack)

Add repo:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

Install stack:

```bash
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  --set prometheus-node-exporter.enabled=false
```

### Optional: Docker Desktop kubelet TLS workaround

If you get `Unable to connect to the server: net/http: TLS handshake timeout` error, run:

```bash
helm upgrade monitoring prometheus-community/kube-prometheus-stack -n monitoring \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.insecureSkipVerify=true \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.https=true \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.scheme=https \
  --set prometheus.prometheusSpec.externalLabels.cluster=docker-desktop

kubectl rollout restart -n monitoring statefulset/prometheus-monitoring-kube-prometheus-prometheus
kubectl rollout status  -n monitoring statefulset/prometheus-monitoring-kube-prometheus-prometheus
```

---

# Running the System (Port-forwards)

This section explains how to access the deployed **StudentHub** application services and the **monitoring/observability** stack from your local machine using `kubectl port-forward`.

> **Tip:** Keep each port-forward running in its own terminal window/tab.

---

## 1. Application Services

These are the services that make up the StudentHub application (what users interact with).

### 1.1 Auth service

```bash
kubectl port-forward -n studenthub svc/studenthub-auth 8081:80
```

Open:

* [http://localhost:8081](http://localhost:8081)

---

### 1.2 Business service

```bash
kubectl port-forward -n studenthub svc/studenthub-business 8082:80
```

Open:

* [http://localhost:8082](http://localhost:8082)

---

### 1.3 Adminer

```bash
kubectl port-forward -n studenthub svc/adminer 8083:80
```

Open:

* [http://localhost:8083](http://localhost:8083)

---

## 2. Portainer UI

Portainer provides a web UI for managing and inspecting Kubernetes resources (namespaces, deployments, pods, logs, services).

### 2.1. Access Portainer UI

Port-forward:

```bash
kubectl port-forward -n portainer svc/portainer 9000:9000
```

Open:
* [http://localhost:9000](http://localhost:9000)

Log in:
* Username: `admin`
* Password: `Cherestea!123` (set during first login)

---

### 2.2 Connect Portainer to the Kubernetes cluster

After login, Portainer may show an **Environment Wizard**.

1. Select **Kubernetes** → **Agent**
2. Set **Name**: `docker-desktop`
3. Set **Environment address**:

```text
portainer-agent.portainer.svc.cluster.local:9001
```

4. Click **Connect**

> If the DNS name above does not work, try: `portainer-agent:9001`.

---

### 2.3 Verify StudentHub workloads in Portainer

In Portainer:

1. Open the connected environment (**docker-desktop**)
2. Go to **Namespaces** → select **studenthub**
3. Confirm the workloads are visible (e.g., `studenthub-auth`, `studenthub-business`, `postgres`, `adminer`)

This confirms Portainer is connected and can inspect/manage the StudentHub Kubernetes resources.

---

## 3. Monitoring & Observability

These services are part of the platform/infrastructure (used by developers/DevOps).

### 3.1 Grafana

Port-forward:

```bash
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
```

Open:

* [http://localhost:3000](http://localhost:3000)

Log in:

* Username: `admin`
* Password: `Uflr1wB8ac51k0ekigP2JJQWPHS86KEBA72udAWQ`

Get admin password (PowerShell):

```powershell
$pw = kubectl get secret -n monitoring monitoring-grafana -o jsonpath="{.data.admin-password}"
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($pw))
```

---

### 3.2 Prometheus

Port-forward:

```bash
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090
```

Open:

* [http://localhost:9090](http://localhost:9090)

Example PromQL query (computes the real-time CPU usage per StudentHub pod by calculating the rate of CPU time consumed over the last 5 minutes):

```text
sum by (pod) (
  rate(container_cpu_usage_seconds_total{namespace="studenthub"}[5m])
)
```

# Project Archive Structure

```
StudentHub_CC_Final_Project/
├── auth-service/
│    ├── app + Dockerfile
├── business-service/
│    ├── app + Dockerfile
├── studenthub/
│    ├── Chart.yaml
│    ├── values.yaml
│    ├── templates/
│    │   ├── jwt-secret.yaml
│    │   ├── auth/
│    │   │   ├── deployment.yaml
│    │   │   └── service.yaml
│    │   ├── business/
│    │   │   ├── deployment.yaml
│    │   │   └── service.yaml
│    │   ├── postgres/
│    │   │   ├── postgres.yaml
│    │   └── adminer/
│    │       ├── adminer.yaml
├── images/
│   ├── adminer <- Images containing DB management tool
│   ├── grafana <- Images containing Grafana setup + dashboard
│   ├── prometheus <- Images containing Prometheus table + graph
    ├── portainer <- Images containing Portainer setup + namespaces
└── README.md
```

