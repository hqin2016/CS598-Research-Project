terraform {
  backend "gcs" {
    bucket = "db-storage-centralus"
    prefix = "terraform-state/ccc-project.tfstate"
  }
}