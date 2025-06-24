resource "aws_vpc" "eks_vpc" {
  cidr_block = "10.0.0.0/16"
  enable_dns_support = true
  enable_dns_hostnames = true
  tags = {
    Name = "eks-vpc"
  }
}

resource "aws_subnet" "public_subnet" {
  count = 2
  vpc_id = aws_vpc.eks_vpc.id
  cidr_block = "10.0.${count.index}.0/24"
  availability_zone = element(["us-east-1a", "us-east-1b"], count.index)
  map_public_ip_on_launch = true
  tags = {
    Name = "eks-public-subnet-${count.index}"
    "kubernetes.io/role/elb" = "1"
    "kubernetes.io/cluster/events-cluster" = "shared"
  }
}

resource "aws_subnet" "private_subnet" {
  count = 2
  vpc_id = aws_vpc.eks_vpc.id
  cidr_block = "10.0.${count.index + 2}.0/24"
  availability_zone = element(["us-east-1a", "us-east-1b"], count.index)
  map_public_ip_on_launch = false
  tags = {
    Name = "eks-private-subnet-${count.index}"
    "kubernetes.io/role/internal-elb" = "1"
    "kubernetes.io/cluster/events-cluster" = "shared"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.eks_vpc.id
  tags = {
    Name = "eks-igw"
  }
}

resource "aws_eip" "nat" {
  domain = "vpc"
  
  tags = {
    Name = "eks-nat-eip"
  }
}

resource "aws_nat_gateway" "nat_gw" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_subnet[0].id
  
  tags = {
    Name = "eks-nat-gateway"
  }

  depends_on = [aws_internet_gateway.igw]
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.eks_vpc.id
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  
  tags = {
    Name = "eks-public-route-table"
  }
}

resource "aws_route_table_association" "public" {
  count = 2
  subnet_id      = aws_subnet.public_subnet[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.eks_vpc.id
  
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_gw.id
  }
  
  tags = {
    Name = "eks-private-route-table"
  }
}

resource "aws_route_table_association" "private" {
  count = 2
  subnet_id      = aws_subnet.private_subnet[count.index].id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "balancer_security_group" {
  vpc_id = aws_vpc.eks_vpc.id
  name   = "balancer-security-group"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "balancer-security-group"
  }
}

resource "aws_security_group" "cluster_security_group" {
  vpc_id = aws_vpc.eks_vpc.id
  name   = "cluster-security-group"

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    security_groups = [aws_security_group.balancer_security_group.id]
  }

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    security_groups = [aws_security_group.balancer_security_group.id]
  }

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
    description = "Allow nodes to communicate with each other"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow communication with the cluster API Server"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "cluster-security-group"
    "kubernetes.io/cluster/events-cluster" = "owned"
  }
}

resource "aws_lb_target_group" "events_tg" {
  name     = "events-tg"
  port     = 80
  protocol = "HTTP" // Alterado de "TCP" para "HTTP"
  vpc_id   = aws_vpc.eks_vpc.id
  target_type = "ip"
  
  lifecycle {
    ignore_changes = [name]
    create_before_destroy = true
  }
}

resource "aws_lb" "ecs_events_load_balancer" {
  name               = "ecs-events-load-balancer"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.balancer_security_group.id]
  subnets            = [aws_subnet.private_subnet[0].id, aws_subnet.private_subnet[1].id]
  ip_address_type    = "ipv4"

  lifecycle {
    ignore_changes = [name]
    create_before_destroy = true
  }

  tags = {
    Name = "ecs-events-load-balancer"
  }
}

resource "aws_lb_listener" "http_listener" {
  load_balancer_arn = aws_lb.ecs_events_load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.events_tg.arn
  }
}

resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "db-subnet-group"
  subnet_ids = aws_subnet.private_subnet[*].id

  tags = {
    Name = "db-subnet-group"
  }
}