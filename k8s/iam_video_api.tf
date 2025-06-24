# OIDC provider do cluster EKS
data "aws_eks_cluster" "eks" {
  name = aws_eks_cluster.eks.name
}

data "aws_eks_cluster_auth" "eks" {
  name = aws_eks_cluster.eks.name
}

data "aws_iam_openid_connect_provider" "oidc" {
  url = data.aws_eks_cluster.eks.identity[0].oidc[0].issuer
}

# IAM Role para o video-api
resource "aws_iam_role" "video_api_irsa" {
  name = "video-api-irsa-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = data.aws_iam_openid_connect_provider.oidc.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${replace(data.aws_eks_cluster.eks.identity[0].oidc[0].issuer, "https://", "")}:sub" = "system:serviceaccount:default:video-api-sa"
          }
        }
      }
    ]
  })
}

# Política de acesso ao S3 e SQS
resource "aws_iam_policy" "video_api_policy" {
  name = "video-api-s3-sqs-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket",
          "s3:DeleteObject"
        ]
        Resource = [
          aws_s3_bucket.user_video_uploads.arn,
          "${aws_s3_bucket.user_video_uploads.arn}/*",
          aws_s3_bucket.processed_videos.arn,
          "${aws_s3_bucket.processed_videos.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.video_processing_queue.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "video_api_attach" {
  role       = aws_iam_role.video_api_irsa.name
  policy_arn = aws_iam_policy.video_api_policy.arn
}