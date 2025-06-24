# IAM Role para os nós do EKS
resource "aws_iam_role" "eks_node_role" {
  name = "eks-node-role-dev"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      },
    ]
  })
}

# Anexando políticas necessárias para os nós do EKS
resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

resource "aws_iam_role_policy_attachment" "eks_container_registry_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Grupo de nós para o cluster EKS - Configuração econômica para DEV
resource "aws_eks_node_group" "primary" {
  cluster_name    = aws_eks_cluster.eks.name
  node_group_name = "dev-node-group"
  node_role_arn   = aws_iam_role.eks_node_role.arn
  subnet_ids      = aws_subnet.private_subnet[*].id
  
  capacity_type = "ON_DEMAND"
  # Instâncias mais econômicas para ambiente de desenvolvimento
  instance_types = ["t3.small"]  # Reduzindo de medium para small (2GB RAM)
  
  # Configuração mínima para reduzir custos
  scaling_config {
    desired_size = 2  # Apenas 1 nó
    min_size     = 1  # Mantendo o mínimo em 1
    max_size     = 3  # Reduzindo o máximo para 2
  }

  # Configuração para atualizações
  update_config {
    max_unavailable = 1
  }

  # Tags para ambiente de desenvolvimento
  tags = {
    Name = "eks-worker-node-dev"
    Environment = "development"
    Purpose = "exam-demo"
  }

  # Aguarda as políticas IAM estarem anexadas
  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry_policy,
  ]
}