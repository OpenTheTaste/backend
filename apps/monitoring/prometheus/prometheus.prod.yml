global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: "prometheus"
    static_configs:
      - targets: ["prometheus:9090"]

  - job_name: "user-api"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["__USER_API_TARGET__"]
        labels:
          app: "user-api"
          application: "user-api"
          env: "prod"
    relabel_configs:
      - target_label: instance
        replacement: "user-api"

  - job_name: "admin-api"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["__ADMIN_API_TARGET__"]
        labels:
          app: "admin-api"
          application: "admin-api"
          env: "prod"
    relabel_configs:
      - target_label: instance
        replacement: "admin-api"

  - job_name: "transcoder"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["__TRANSCODER_TARGET__"]
        labels:
          app: "transcoder"
          application: "transcoder"
          env: "prod"
    relabel_configs:
      - target_label: instance
        replacement: "transcoder"

  - job_name: "node-exporter"
    static_configs:
      - targets: ["__NODE_EXPORTER_TARGET__"]
        labels:
          app: "node-exporter"
          application: "node-exporter"
          env: "prod"

  - job_name: "machine-node-exporter"
    static_configs:
      - targets: ["__MACHINE_NODE_EXPORTER_TARGET__"]
        labels:
          app: "machine"
          application: "machine"
          env: "prod"
