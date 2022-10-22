#!/bin/bash -e

[[ -n "$DEBUG_SCRIPT" ]] && set -x

# Create storage bucket
gcloud storage buckets create gs://db-storage-centralus --location=us-central1
# Enable object versioning on storage bucket
gcloud storage buckets update gs://db-storage-centralus --versioning