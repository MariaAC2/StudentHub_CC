# StudentHub_CC

1) Enable Kubernetes in Docker Desktop
Docker Desktop → Settings → Kubernetes → Enable Kubernetes → Apply & Restart
kubectl cluster-info

2)Deploy StudentHub via Helm
helm upgrade --install studenthub . -n studenthub --create-namespace
kubectl get pods -n studenthub

3)Install metrics server
helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/
helm repo update
helm upgrade --install metrics-server metrics-server/metrics-server -n kube-system \
  --set args="{--kubelet-insecure-tls,--kubelet-preferred-address-types=InternalIP\,ExternalIP\,Hostname}"

kubectl get pods -n kube-system | findstr metrics
kubectl top nodes
kubectl top pods -n studenthub

4)Promotheus + Grafana
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  --set prometheus-node-exporter.enabled=false

(Optional, if not fixed)
helm upgrade monitoring prometheus-community/kube-prometheus-stack -n monitoring \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.insecureSkipVerify=true \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.https=true \
  --set prometheus.prometheusSpec.kubeletServiceMonitor.scheme=https \
  --set prometheus.prometheusSpec.externalLabels.cluster=docker-desktop

kubectl rollout restart -n monitoring statefulset/prometheus-monitoring-kube-prometheus-prometheus
kubectl rollout status  -n monitoring statefulset/prometheus-monitoring-kube-prometheus-prometheus


6)Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

password:
$pw = kubectl get secret -n monitoring monitoring-grafana -o jsonpath="{.data.admin-password}"
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($pw))

7)Promotheus metrics
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090
promsql: container_cpu_usage_seconds_total

8) Test
kubectl port-forward -n studenthub svc/studenthub-auth 8081:80
kubectl port-forward -n studenthub svc/studenthub-business 8082:80







