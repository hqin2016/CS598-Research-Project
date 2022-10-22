terraform {
  required_version = ">= 1.3.2"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 4.40.0"
    }

    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.7.1"
    }
  }
}

provider "google" {
  project = "cs598-cloud-computing-capstone"
  region  = "us-central1"
}

provider "helm" {
  kubernetes {
    host = "https://${google_container_cluster.default.endpoint}"

    token                  = data.google_client_config.default.access_token
    client_certificate     = base64decode(google_container_cluster.default.master_auth.0.client_certificate)
    client_key             = base64decode(google_container_cluster.default.master_auth.0.client_key)
    cluster_ca_certificate = base64decode(google_container_cluster.default.master_auth.0.cluster_ca_certificate)
  }
}
