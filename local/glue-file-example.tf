terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# ---------------------------
# Variáveis
# ---------------------------
variable "region" {
  type    = string
  default = "us-east-1"
}

# IAM não disponível no LocalStack free → usar ARN dummy
variable "glue_role_arn" {
  type    = string
  # ARN “fake” que o LocalStack aceita sem validar existência
  default = "arn:aws:iam::000000000000:role/glue-crawler-role"
}

locals {
  localstack_endpoint = "http://localhost:4566"
}

# ---------------------------
# Provider AWS apontando para LocalStack
# ---------------------------
provider "aws" {
  region = var.region

  access_key = "test"
  secret_key = "test"

  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_region_validation      = true
  skip_requesting_account_id  = true

  endpoints {
    s3   = local.localstack_endpoint
    glue = local.localstack_endpoint
    # NÃO declarar iam/sts pois o serviço não está habilitado no free
  }
}

# ---------------------------
# S3 para o Glue Data Catalog
# ---------------------------
resource "aws_s3_bucket" "glue_catalog_bucket" {
  bucket        = "glue-catalog-bucket"
  force_destroy = true

  # Evita caracteres que o LocalStack às vezes rejeita
  tags = {
    Name = "GlueDataCatalogBucket"
    Env  = "local"
  }
}

# ---------------------------
# Glue Catalog Database
# ---------------------------
resource "aws_glue_catalog_database" "example" {
  name         = "example_database"
  location_uri = "s3://${aws_s3_bucket.glue_catalog_bucket.bucket}/database/"

  # Tags simples (sem parênteses)
  tags = {
    Name = "ExampleGlueDatabaseLocal"
  }
}

# ---------------------------
# Glue Crawler (SEM recursos IAM)
# Usamos um ARN dummy no role.
# ---------------------------
resource "aws_glue_crawler" "example" {
  name          = "example_crawler"
  database_name = aws_glue_catalog_database.example.name
  role          = var.glue_role_arn
  schedule      = "cron(0 1 * * ? *)"

  configuration = jsonencode({
    Version        = 1,
    CrawlerOutput  = { Partitions = { AddOrUpdateBehavior = "InheritFromTable" } },
    GroupingPolicy = "MergeUpdate"
  })

  # Use o próprio bucket criado acima
  s3_target {
    path = "s3://${aws_s3_bucket.glue_catalog_bucket.bucket}/your-data-path/"
  }

  tags = {
    Name = "ExampleGlueCrawlerLocal"
  }
}

# ---------------------------
# Outputs
# ---------------------------
output "glue_database_name" {
  value = aws_glue_catalog_database.example.name
}

output "glue_crawler_name" {
  value = aws_glue_crawler.example.name
}
