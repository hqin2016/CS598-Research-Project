data "google_client_config" "default" {}

# resource "google_compute_address" "default" {
#   name   = "gca-centralus-1"
#   region = "us-central1"
# }

# ~9min creation time, ~3min destroy time
resource "google_container_cluster" "default" {
  name     = "gke-cluster-centralus-1"
  location = "us-central1-a"

  # We can't create a cluster with no node pool defined, but we want to only use
  # separately managed node pools. So we create the smallest possible default
  # node pool and immediately delete it.
  remove_default_node_pool = true
  initial_node_count       = 1

  # Enabled system monitoring to output to Google Monitoring
  monitoring_config {
    enable_components = ["SYSTEM_COMPONENTS"]
  }
}

# ~2min creation time, ~4min destroy time
resource "google_container_node_pool" "default" {
  name       = "gke-node-pool-centralus-1"
  location   = "us-central1-a"
  cluster    = google_container_cluster.default.name
  node_count = 5

  node_config {
    preemptible  = true
    machine_type = "n1-standard-2" # 2 vCPUs, 7.5 GB Memory, $0.092 hourly

    # Google recommends custom service accounts that have cloud-platform scope and permissions granted via IAM Roles.
    service_account = google_service_account.gke_node_pool_service_account.email
    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform"
    ]
  }
}

# resource "helm_release" "ingress_nginx" {
#   name       = "ingress-nginx"
#   repository = "https://kubernetes.github.io/ingress-nginx"
#   chart      = "ingress-nginx"
#   version    = "4.3.0"
#   wait       = false

#   values = [templatefile("./ingress.yaml", { cluster_ip = google_compute_address.default.address })]
# }