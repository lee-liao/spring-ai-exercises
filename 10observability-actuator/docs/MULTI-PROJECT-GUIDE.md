# Multi-Project Quick Start Guide

This guide explains how to use the observability stack across multiple development projects.

## Quick Setup for a New Project

### Step 1: Start the Observability Stack

```bash
cd /path/to/observability
docker-compose up -d
```

All services will start and be available on their standard ports.

### Step 2: Configure Your Application

Configure your application to send telemetry to the OTel Collector:

**OpenTelemetry SDK Configuration:**
```
OTLP gRPC: http://localhost:4317
OTLP HTTP: http://localhost:4318
```

### Step 3: Add Project-Specific Scraping (Optional)

If you have Prometheus metrics endpoints, add them to `prometheus.yml`:

```yaml
scrape_configs:
  # Your project's API
  - job_name: 'my-project-api'
    static_configs:
      - targets: ['host.docker.internal:8080']
        labels:
          project: 'my-project'
          environment: 'development'
```

### Step 4: View Your Data

- **Metrics**: http://localhost:3000 (Grafana)
- **Traces (Jaeger)**: http://localhost:16686
- **Traces (Zipkin)**: http://localhost:9412

## Usage Patterns

### Pattern 1: Shared Stack for All Projects (Recommended)

**Best for:** Small to medium teams, resource efficiency

**Setup:**
1. Run one observability stack
2. All projects send telemetry to the same endpoints
3. Use service names and labels to differentiate

**Example:**
```python
# Project A
tracer = tracer_provider.get_tracer("project-a-service")

# Project B
tracer = tracer_provider.get_tracer("project-b-service")
```

**Pros:**
- Single set of containers to manage
- Lower resource usage
- Easy cross-project correlation
- Unified dashboards

**Cons:**
- Shared data volume
- Need good naming conventions
- Potential for mixed data in dashboards

### Pattern 2: Separate Stack Per Project

**Best for:** Large teams, independent projects, different environments

**Setup:**
1. Copy this folder to each project
2. Modify `COMPOSE_PROJECT_NAME` in `.env`
3. Change port mappings in `docker-compose.override.yml`

**Example `.env`:**
```bash
COMPOSE_PROJECT_NAME=myproject-observability
```

**Example `docker-compose.override.yml`:**
```yaml
services:
  grafana:
    ports:
      - "3001:3000"  # Different port
  prometheus:
    ports:
      - "9091:9090"  # Different port
```

**Pros:**
- Complete isolation
- Different configurations per project
- Separate data retention
- No naming conflicts

**Cons:**
- Higher resource usage
- Multiple containers to manage
- Can't correlate across projects easily

### Pattern 3: Shared Stack with Environment Isolation

**Best for:** Multi-environment setups (dev, staging, prod)

**Setup:**
1. Use OpenTelemetry resource attributes
2. Configure environment labels in your applications
3. Use Grafana variables for filtering

**Example:**
```python
resource = Resource.create({
    "service.name": "my-service",
    "deployment.environment": "development",
    "project.name": "my-project"
})
```

**Grafana Dashboard Variable:**
```javascript
${environment:dev,staging,prod}
```

## Project Labeling Best Practices

### Service Naming

Use consistent, hierarchical service names:
```
project-name-service-type
example: auth-service-api, auth-service-db
```

### Resource Attributes

Always include these attributes in your telemetry:
```yaml
service.name: my-service
service.version: 1.0.0
deployment.environment: development
project.name: my-project
team.name: backend-team
```

### Grafana Folder Organization

Organize dashboards by project:
```
Dashboards/
├── Project A/
│   ├── API Dashboard
│   └── DB Dashboard
├── Project B/
│   ├── API Dashboard
│   └── Frontend Dashboard
└── Shared/
    ├── Overview
    └── Infrastructure
```

## Configuration Examples

### Python Application

```python
from opentelemetry import trace, metrics
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.resources import Resource
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.exporter.otlp.proto.grpc.metric_exporter import OTLPMetricExporter

# Create resource with project labels
resource = Resource.create({
    "service.name": "my-service",
    "service.version": "1.0.0",
    "deployment.environment": "development",
    "project.name": "my-project"
})

# Setup tracing
trace.set_tracer_provider(TracerProvider(resource=resource))
tracer = trace.get_tracer(__name__)
otlp_exporter = OTLPSpanExporter(
    endpoint="http://localhost:4317",
    insecure=True
)
trace.get_tracer_provider().add_span_processor(
    BatchSpanProcessor(otlp_exporter)
)

# Setup metrics
metrics.set_meter_provider(MeterProvider(resource=resource))
meter = metrics.get_meter(__name__)
otlp_metric_exporter = OTLPMetricExporter(
    endpoint="http://localhost:4317",
    insecure=True
)
```

### Java Spring Boot Application

**application.yml:**
```yaml
management:
  otlp:
    tracing:
      endpoint: http://localhost:4317
    metrics:
      export:
        endpoint: http://localhost:4317
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: development
      project: my-project

  endpoints:
    web:
      exposure:
        include: prometheus,health,info,metrics

spring:
  application:
    name: my-service
```

**pom.xml:**
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Node.js Application

```javascript
const { Resource } = require('@opentelemetry/resources');
const { NodeTracerProvider } = require('@opentelemetry/sdk-trace-node');
const { OTLPTraceExporter } = require('@opentelemetry/exporter-trace-otlp-grpc');
const { BatchSpanProcessor } = require('@opentelemetry/sdk-trace-base');

const resource = Resource.default().merge(
  new Resource({
    'service.name': 'my-service',
    'service.version': '1.0.0',
    'deployment.environment': 'development',
    'project.name': 'my-project'
  })
);

const provider = new NodeTracerProvider({ resource });

const exporter = new OTLPTraceExporter({
  url: 'http://localhost:4317'
});

provider.addSpanProcessor(new BatchSpanProcessor(exporter));
provider.register();

const tracer = provider.getTracer('my-service');
```

## Common Scenarios

### Scenario 1: Microservices Architecture

**Setup:** One observability stack for all microservices

**Configuration:**
```yaml
# Each microservice has unique name
service-a: service.name = "auth-service"
service-b: service.name = "user-service"
service-c: service.name = "order-service"
```

**Grafana Query:**
```
# Show all requests across microservices
sum(rate(http_requests_total{project="my-project"}[5m]))

# Trace a request across services
Jaeger: Find traces that span multiple services
```

### Scenario 2: Monorepo with Multiple Applications

**Setup:** Shared stack, differentiated by application name

**Configuration:**
```yaml
app1: service.name = "monorepo-app-api"
app2: service.name = "monorepo-app-worker"
app3: service.name = "monorepo-app-scheduler"
```

**Scraping:**
```yaml
scrape_configs:
  - job_name: 'monorepo-api'
    static_configs:
      - targets: ['host.docker.internal:8001']
        labels:
          app: 'api'
  - job_name: 'monorepo-worker'
    static_configs:
      - targets: ['host.docker.internal:8002']
        labels:
          app: 'worker'
```

### Scenario 3: Development vs Staging

**Setup:** Single stack, environment label

**Configuration:**
```yaml
# Development
resource: {environment: "development"}

# Staging
resource: {environment: "staging"}
```

**Grafana Dashboard:**
```
Variable: $environment
Query: label_values(environment)

Panel Query: rate(http_requests_total{environment="$environment"}[5m])
```

## Troubleshooting

### Services Not Appearing

**Problem:** Can't find your service in Grafana/Jaeger

**Solutions:**
1. Check service name is correctly set
2. Verify OTLP endpoint is correct
3. Check OTel Collector logs: `docker-compose logs -f otel-collector`
4. Look for errors in exporter configuration

### Mixed Data from Different Projects

**Problem:** Seeing data from other projects in your dashboards

**Solutions:**
1. Use proper resource attributes
2. Filter by project name in Grafana queries: `{project_name="my-project"}`
3. Use separate stacks if isolation is critical
4. Organize dashboards into project-specific folders

### Port Conflicts

**Problem:** Port already in use when starting stack

**Solutions:**
1. Check what's using the port: `netstat -ano | findstr :3000`
2. Use `docker-compose.override.yml` to change ports
3. Stop conflicting services
4. Use different COMPOSE_PROJECT_NAME for isolation

## Tips and Best Practices

1. **Start Simple**: Begin with basic metrics, add traces as needed
2. **Use Consistent Naming**: Establish naming conventions early
3. **Label Everything**: Add labels for project, environment, team
4. **Create Dashboards**: Build reusable dashboard templates
5. **Monitor the Monitor**: Set up alerts for the observability stack itself
6. **Document Config**: Keep notes on project-specific configurations
7. **Review Regularly**: Clean up unused services and dashboards
8. **Share Knowledge**: Document patterns for team members

## Getting Help

- Check logs: `docker-compose logs -f [service-name]`
- Verify configuration: `docker-compose config`
- Test connectivity: `docker-compose exec otel-collector ping jaeger`
- Review documentation: See README.md for detailed setup
