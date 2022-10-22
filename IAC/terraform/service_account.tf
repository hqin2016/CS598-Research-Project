resource "google_service_account" "gke_node_pool_service_account" {
  account_id   = "sa-gke-node-pool"
  display_name = "GKE Node Pool Service Account"
}

resource "google_project_iam_member" "gke_node_pool_gcr_reader" {
  project = data.google_client_config.default.project
  role    = "roles/storage.objectViewer"
  member  = "serviceAccount:${google_service_account.gke_node_pool_service_account.email}"
}

resource "google_project_iam_member" "gke_node_pool_log_writer" {
  project = data.google_client_config.default.project
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.gke_node_pool_service_account.email}"
}

resource "google_project_iam_member" "gke_node_pool_monitor_writer" {
  project = data.google_client_config.default.project
  role    = "roles/monitoring.metricWriter"
  member  = "serviceAccount:${google_service_account.gke_node_pool_service_account.email}"
}

resource "google_project_iam_member" "gke_node_pool_stackdriver_writer" {
  project = data.google_client_config.default.project
  role    = "roles/stackdriver.resourceMetadata.writer"
  member  = "serviceAccount:${google_service_account.gke_node_pool_service_account.email}"
}