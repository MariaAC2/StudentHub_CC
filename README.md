# StudentHub_CC

Helm-based Kubernetes deployment for the **StudentHub** microservices platform (Auth, Business, PostgreSQL, Adminer) with **Metrics Server**, **Prometheus**, and **Grafana**.

---

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

## 2. Deploy StudentHub via Helm

From the Helm chart root:

```bash
helm upgrade --install studenthub . -n studenthub --create-namespace
kubectl get pods -n studenthub
```

---

## 3. Install Metrics Server

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

## 4. Prometheus + Grafana (kube-prometheus-stack)

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

If you see missing kubelet targets/metrics:

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

## 5. Grafana

Port-forward:

```bash
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
```

Open:

* [http://localhost:3000](http://localhost:3000)

Get admin password:

```powershell
$pw = kubectl get secret -n monitoring monitoring-grafana -o jsonpath="{.data.admin-password}"
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($pw))
```

Default username: `admin`

---

## 6. Prometheus

Port-forward:

```bash
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090
```

Open:

* [http://localhost:9090](http://localhost:9090)

Example PromQL:

```text
container_cpu_usage_seconds_total
```

---

## 7. Test services (port-forward)

Auth service:

```bash
kubectl port-forward -n studenthub svc/studenthub-auth 8081:80
```

Business service:

```bash
kubectl port-forward -n studenthub svc/studenthub-business 8082:80
```

Endpoints:

* **Auth** → [http://localhost:8081](http://localhost:8081)
* **Business** → [http://localhost:8082](http://localhost:8082)
