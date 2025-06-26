# Configuração do provider Helm
terraform {
  required_providers {
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.9.0"
    }
  }
}

provider "helm" {
  kubernetes {
    host                   = aws_eks_cluster.eks.endpoint
    cluster_ca_certificate = base64decode(aws_eks_cluster.eks.certificate_authority[0].data)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", aws_eks_cluster.eks.name]
    }
  }
}

# Instalação do ingress-nginx usando Helm
resource "helm_release" "ingress_nginx" {
  name       = "ingress-nginx"
  repository = "https://kubernetes.github.io/ingress-nginx"
  chart      = "ingress-nginx"
  namespace  = "ingress-nginx"
  version    = "4.7.1"
  
  create_namespace = true

  set {
    name  = "controller.service.type"
    value = "LoadBalancer"
  }

  set {
    name  = "controller.replicaCount"
    value = "1" 
  }

  set {
    name  = "controller.resources.requests.cpu"
    value = "100m"
  }

  set {
    name  = "controller.resources.requests.memory"
    value = "128Mi"
  }

  set {
    name  = "controller.resources.limits.cpu"
    value = "200m"
  }

  set {
    name  = "controller.resources.limits.memory"
    value = "256Mi"
  }

  # CONFIGURAÇÕES GLOBAIS PARA RESOLVER O ERRO 413
  set {
    name  = "controller.config.proxy-body-size"
    value = "100m"
  }

  set {
    name  = "controller.config.client-max-body-size"
    value = "100m"
  }

  set {
    name  = "controller.config.proxy-connect-timeout"
    value = "600"
  }

  set {
    name  = "controller.config.proxy-send-timeout"
    value = "600"
  }

  set {
    name  = "controller.config.proxy-read-timeout"
    value = "600"
  }

  set {
    name  = "controller.config.proxy-buffer-size"
    value = "128k"
  }

  depends_on = [
    aws_eks_cluster.eks,
    aws_eks_node_group.primary
  ]
}