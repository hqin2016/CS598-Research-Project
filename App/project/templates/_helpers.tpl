{{- define "application" -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .name }}
spec:
  selector:
    matchLabels:
      app: {{ .name }}
  replicas: 1
  template:
    metadata:
      labels:
        app: {{ .name }}
    spec:
      containers:
        - name: server
          image: {{ .Values.containerRegistry }}/sample-app:{{ .Values.appVersion | default "0.1.0" }}
          ports:
            - containerPort: 8080
          resources:
            limits:
              cpu: 400m
              memory: 400Mi
            requests:
              cpu: 200m
              memory: 200Mi
          env:
            - name: FACTORIAL_BASE
              value: {{ .factorial | default "100" | quote }}
            - name: SERVICE_TYPE
              value: {{ .type | default "LEAF" | upper | quote }}
            {{ if .dependencies }}
            - name: DEPENDENCIES
              value: {{ .dependencies | quote }}
            {{ end }}
          livenessProbe:
            initialDelaySeconds: 20
            httpGet:
              port: 8080
              path: /health
          readinessProbe:
            initialDelaySeconds: 30
            httpGet:
              port: 8080
              path: /health
---
apiVersion: v1
kind: Service
metadata:
  name: {{ .name }}
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
spec:
  type: ClusterIP
  selector:
    app: {{ .name }}
  ports:
    - port: 80
      targetPort: 8080
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ .name }}
spec:
  minReplicas: {{ .Values.minReplicas }}
  maxReplicas: {{ .Values.maxReplicas }}
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ .name }}
  behavior:
    scaleUp:
      selectPolicy: Disabled
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: {{ .Values.scaleUpThreshold }} 
{{- end -}}

{{- define "controller" -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .name }}-controller
  namespace: controller
spec:
  selector:
    matchLabels:
      app: {{ .name }}-controller
  replicas: 1
  template:
    metadata:
      labels:
        app: {{ .name }}-controller
    spec:
      containers:
        - name: server
          image: {{ .Values.containerRegistry }}/{{ .type }}-controller:{{ .Values.controllerVersion }}
          ports:
            - containerPort: 8080
          resources:
            limits:
              cpu: 400m
              memory: 400Mi
            requests:
              cpu: 200m
              memory: 200Mi
          env:
          {{ if eq .type "application" }}
            - name: MONITOR_ADDR
              value: "prometheus-internal.monitoring"
            - name: TOPOLOGY
              value: {{ .Values.topology | quote }}
          {{ else if eq .type "microservice" }}
            - name: SERVICE_NAME
              value: {{ .name }}
          {{ end }}
          livenessProbe:
            initialDelaySeconds: 20
            httpGet:
              port: 8080
              path: /health
          readinessProbe:
            initialDelaySeconds: 30
            httpGet:
              port: 8080
              path: /health
---
apiVersion: v1
kind: Service
metadata:
  name: {{ .name }}-controller
  namespace: controller
spec:
  type: ClusterIP
  selector:
    app: {{ .name }}-controller
  ports:
    - port: 80
      targetPort: 8080
{{ if .Values.enableDebug }}
---
apiVersion: v1
kind: Service
metadata:
  name: {{ .name }}-controller-external
  namespace: controller
spec:
  type: LoadBalancer
  selector:
    app: {{ .name }}-controller
  ports:
    - name: http
      port: 80
      targetPort: 8080
{{ end }}
{{- end -}}