variable "aws_region" {
  description = "The AWS region to create resources in"
  type        = string
  default     = "us-east-1"
}

variable "db_username" {
  description = "The username for the RDS instance"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "The password for the RDS instance"
  type        = string
  default     = "admin123"
}

variable "db_instance_class" {
  description = "The instance class for the RDS instance"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "The allocated storage for the RDS instance"
  type        = number
  default     = 5
}

variable "enable_detailed_logging" {
  description = "Habilita logs detalhados para depuração da infraestrutura"
  type        = bool
  default     = true
}
