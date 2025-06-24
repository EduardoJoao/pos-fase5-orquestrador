provider "aws" {
  region = var.aws_region
}

// Substituir aws_subnet_ids (obsoleto) por aws_subnets
data "aws_subnets" "private_subnets" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.eks_vpc.id]
  }
  
  filter {
    name   = "tag:Name"
    values = ["eks-private-subnet-*"]
  }
}

data "aws_vpc" "eks_vpc" {
  filter {
    name   = "tag:Name"
    values = ["eks-vpc"]
  }
}

data "aws_security_group" "cluster_security_group" {
  name = "cluster-security-group"
  vpc_id = data.aws_vpc.eks_vpc.id
}

// Atualize a referência para o novo formato de aws_subnets
resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "db-subnet-group-infra-db"
  subnet_ids = data.aws_subnets.private_subnets.ids

  tags = {
    Name = "db-subnet-group-infra-db"
  }
}

resource "aws_security_group" "rds_security_group" {
  name        = "rds-security-group"
  description = "Security group para o RDS"
  vpc_id      = data.aws_vpc.eks_vpc.id

  // Permitir tráfego da porta PostgreSQL vindo do cluster EKS
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [data.aws_security_group.cluster_security_group.id]
  }

  // Adicione esta ingress rule para permitir acesso direto dos pods
  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]  // CIDR da VPC do EKS
  }

  // Permitir saída de todo tráfego
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rds-security-group"
  }
}

resource "aws_db_instance" "postgres" {
  identifier              = "postgres-db"
  engine                  = "postgres"
  engine_version          = "17.2"
  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  storage_encrypted       = true
  username                = var.db_username
  password                = var.db_password
  db_name                 = "video_core"
  db_subnet_group_name    = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids  = [aws_security_group.rds_security_group.id]
  skip_final_snapshot     = true
  publicly_accessible     = false
  
  tags = {
    Name = "main-db"
  }
}

output "db_endpoint" {
  value     = aws_db_instance.postgres.endpoint
  sensitive = true
}

output "db_address" {
  value     = aws_db_instance.postgres.address
  description = "O endereço do banco de dados PostgreSQL"
}

