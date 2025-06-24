# Import VPC resources
variable "vpc_id" {
  description = "ID of the VPC created in the infra-kubernets module"
  type        = string
  default     = ""
}

variable "private_subnet_ids" {
  description = "IDs of private subnets created in the infra-kubernets module"
  type        = list(string)
  default     = []
}

variable "public_subnet_ids" {
  description = "IDs of private subnets created in the infra-kubernets module"
  type        = list(string)
  default     = []
}

variable "cluster_security_group_id" {
  description = "ID of the EKS cluster security group"
  type        = string
  default     = ""
}

# These variables should be set when applying Terraform or in terraform.tfvars:
# vpc_id = "vpc-xxxxx" (output from your infra-kubernets module)
# private_subnet_ids = ["subnet-xxxxx", "subnet-yyyyy"] (outputs from your infra-kubernets module)
# cluster_security_group_id = "sg-xxxxx" (output from your infra-kubernets module)
