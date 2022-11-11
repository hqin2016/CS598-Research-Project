#!/bin/bash -e

unique_string=${unique:?}

[[ -n "$DEBUG_SCRIPT" ]] && set -x

bucket="${unique_string}-storage-centralus"
bucket_url="gs://${bucket}"
echo $bucket

{
    gcloud storage buckets describe $bucket_url &>/dev/null &&
    echo "[INFO] bucket exists, skipping create" >&2
} || {
    echo "[INFO] bucket does not exist, creating" >&2
    # Create storage bucket
    # gcloud storage buckets create $bucket_url --location=us-central1
    # Enable object versioning on storage bucket
    # gcloud storage buckets update $bucket_url --versioning
}
