#!/bin/bash -e

[[ -n "$DEBUG_SCRIPT" ]] && set -x

gcloud storage buckets delete gs://db-storage-centralus
