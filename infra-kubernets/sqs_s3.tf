# SQS - Fila para processamento de vídeos
resource "aws_sqs_queue" "video_processing_queue" {
  name                      = "video-processing-queue"
  delay_seconds             = 0
  max_message_size          = 262144  # 256 KB
  message_retention_seconds = 86400   # 24 horas
  receive_wait_time_seconds = 10      # Long polling para reduzir custos
  visibility_timeout_seconds = 300    # 5 minutos

  tags = {
    Environment = "development"
    Purpose     = "video-processing"
  }
}

# S3 Bucket - Upload de vídeos de usuários
resource "aws_s3_bucket" "user_video_uploads" {
  bucket = "fiapfase5-user-video-uploads"

  tags = {
    Name        = "User Video Uploads"
    Environment = "development"
    Purpose     = "raw-video-storage"
  }
}

# S3 Bucket - Vídeos processados
resource "aws_s3_bucket" "processed_videos" {
  bucket = "fiapfase5-processed-videos"

  tags = {
    Name        = "Processed Videos"
    Environment = "development"
    Purpose     = "processed-video-storage"
  }
}

# Configurações de bloqueio de acesso público para os buckets
resource "aws_s3_bucket_public_access_block" "user_video_uploads_block" {
  bucket = aws_s3_bucket.user_video_uploads.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_public_access_block" "processed_videos_block" {
  bucket = aws_s3_bucket.processed_videos.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Configuração de lifecycle para economizar custos
# Transfere arquivos mais antigos para classe de armazenamento mais econômica
resource "aws_s3_bucket_lifecycle_configuration" "processed_videos_lifecycle" {
  bucket = aws_s3_bucket.processed_videos.id

  rule {
    id     = "archive-old-videos"
    status = "Enabled"

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER"
    }
  }
}

# Outputs para referência dos recursos
output "sqs_queue_url" {
  description = "URL da fila SQS de processamento de vídeos"
  value       = aws_sqs_queue.video_processing_queue.url
}

output "upload_bucket_name" {
  description = "Nome do bucket para upload de vídeos"
  value       = aws_s3_bucket.user_video_uploads.bucket
}

output "processed_bucket_name" {
  description = "Nome do bucket para vídeos processados"
  value       = aws_s3_bucket.processed_videos.bucket
}