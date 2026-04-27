provider "aws" {
  region = var.region
}

resource "aws_lambda_function" "jenkins_ai" {
  function_name = "jenkins-ai-generator"
  package_type  = "Image"
  image_uri     = var.image_uri
  role          = aws_iam_role.lambda_role.arn
}

resource "aws_iam_role" "lambda_role" {
  name = "lambda-exec-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_apigatewayv2_api" "api" {
  name          = "jenkins-api"
  protocol_type = "HTTP"
}