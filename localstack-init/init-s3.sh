#!/bin/bash

echo "Initializing S3 buckets..."

# アップロード用バケット作成
awslocal s3 mb s3://meal-manager-upload

# 配信用バケット作成
awslocal s3 mb s3://meal-manager-distribution

# アップロード用バケットの設定（24時間で自動削除）
awslocal s3api put-bucket-lifecycle-configuration \
  --bucket meal-manager-upload \
  --lifecycle-configuration '{
    "Rules": [
      {
        "ID": "DeleteAfter24Hours",
        "Status": "Enabled",
        "Expiration": {
          "Days": 1
        },
        "Filter": {
          "Prefix": ""
        }
      }
    ]
  }'

# 配信用バケットをパブリック読み取り可能に設定
awslocal s3api put-bucket-policy \
  --bucket meal-manager-distribution \
  --policy '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "PublicReadGetObject",
        "Effect": "Allow",
        "Principal": "*",
        "Action": "s3:GetObject",
        "Resource": "arn:aws:s3:::meal-manager-distribution/*"
      }
    ]
  }'

echo "S3 buckets initialized successfully!"
