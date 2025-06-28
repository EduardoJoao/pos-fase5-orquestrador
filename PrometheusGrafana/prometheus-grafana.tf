resource "helm_release" "kube_prometheus_stack" {
  name       = "kube-prometheus"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-prometheus-stack"
  namespace  = kubernetes_namespace.monitoring.metadata[0].name
  version    = "56.6.1"

  values = [
    file("${path.module}/prometheus-values.yaml")
  ]

  atomic  = true
  timeout = 600
}