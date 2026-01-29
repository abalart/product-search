#!/bin/bash

# Product Search Service - Development Scripts
# Autor: Arquitecto de Software Senior

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Product Search Service - Scripts    ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""

# Function to check if a service is running
check_service() {
    local service=$1
    local port=$2
    if nc -z localhost $port 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $service is running on port $port"
        return 0
    else
        echo -e "${RED}✗${NC} $service is NOT running on port $port"
        return 1
    fi
}

# Function to start MongoDB
start_mongodb() {
    echo -e "${YELLOW}Starting MongoDB...${NC}"
    if docker ps | grep -q mongodb; then
        echo -e "${GREEN}MongoDB already running${NC}"
    else
        docker run -d \
            --name mongodb \
            -p 27017:27017 \
            -v $(pwd)/mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro \
            -e MONGO_INITDB_DATABASE=product_search \
            mongo:6.0
        echo -e "${GREEN}✓ MongoDB started${NC}"
        sleep 5
    fi
}

# Function to start Redis
start_redis() {
    echo -e "${YELLOW}Starting Redis...${NC}"
    if docker ps | grep -q redis; then
        echo -e "${GREEN}Redis already running${NC}"
    else
        docker run -d \
            --name redis \
            -p 6379:6379 \
            redis:7-alpine
        echo -e "${GREEN}✓ Redis started${NC}"
        sleep 2
    fi
}

# Function to stop services
stop_services() {
    echo -e "${YELLOW}Stopping services...${NC}"
    docker stop mongodb redis 2>/dev/null || true
    docker rm mongodb redis 2>/dev/null || true
    echo -e "${GREEN}✓ Services stopped${NC}"
}

# Function to build the application
build_app() {
    echo -e "${YELLOW}Building application...${NC}"
    mvn clean install -DskipTests
    echo -e "${GREEN}✓ Application built${NC}"
}

# Function to run tests
run_tests() {
    echo -e "${YELLOW}Running tests...${NC}"
    mvn test
    echo -e "${GREEN}✓ Tests completed${NC}"
}

# Function to run the application
run_app() {
    echo -e "${YELLOW}Starting application...${NC}"
    mvn spring-boot:run
}

# Function to check system health
check_health() {
    echo -e "${YELLOW}Checking system health...${NC}"
    echo ""
    check_service "MongoDB" 27017
    check_service "Redis" 6379
    check_service "Application" 8080
    echo ""
    
    if check_service "Application" 8080; then
        echo -e "${YELLOW}Checking application endpoints...${NC}"
        
        # Health check
        if curl -s http://localhost:8080/api/v1/products/health | grep -q "UP"; then
            echo -e "${GREEN}✓${NC} Health endpoint is working"
        else
            echo -e "${RED}✗${NC} Health endpoint failed"
        fi
        
        # Actuator health
        if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
            echo -e "${GREEN}✓${NC} Actuator health is working"
        else
            echo -e "${RED}✗${NC} Actuator health failed"
        fi
    fi
}

# Function to view logs
view_logs() {
    echo -e "${YELLOW}Viewing logs (Ctrl+C to exit)...${NC}"
    docker-compose logs -f
}

# Function to generate test data
generate_data() {
    echo -e "${YELLOW}Generating test data...${NC}"
    docker exec -it mongodb mongosh product_search --eval '
        const categories = ["Electronics", "Clothing", "Books", "Home", "Sports"];
        const brands = ["Brand A", "Brand B", "Brand C", "Brand D", "Brand E"];
        
        let bulkOps = [];
        for (let i = 0; i < 10000; i++) {
            bulkOps.push({
                insertOne: {
                    document: {
                        name: `Product ${i}`,
                        description: `Description for product ${i}`,
                        sku: `SKU-${i}-${Date.now()}`,
                        brand: brands[Math.floor(Math.random() * brands.length)],
                        category: categories[Math.floor(Math.random() * categories.length)],
                        tags: [`tag${i % 10}`, `tag${i % 20}`],
                        price: NumberDecimal((Math.random() * 2000 + 100).toFixed(2)),
                        currency: "USD",
                        stock: Math.floor(Math.random() * 100),
                        active: Math.random() > 0.1,
                        rating: Number((Math.random() * 2 + 3).toFixed(1)),
                        reviewCount: Math.floor(Math.random() * 500),
                        createdAt: new Date(),
                        updatedAt: new Date()
                    }
                }
            });
        }
        
        db.products.bulkWrite(bulkOps);
        print("✅ 10,000 products generated!");
    '
    echo -e "${GREEN}✓ Test data generated${NC}"
}

# Function to clean data
clean_data() {
    echo -e "${YELLOW}Cleaning data...${NC}"
    docker exec -it mongodb mongosh product_search --eval 'db.products.deleteMany({})'
    docker exec -it redis redis-cli FLUSHALL
    echo -e "${GREEN}✓ Data cleaned${NC}"
}

# Function to show metrics
show_metrics() {
    echo -e "${YELLOW}Application Metrics:${NC}"
    echo ""
    curl -s http://localhost:8080/actuator/metrics | jq '.names' || echo "jq not installed, showing raw output:"
    curl -s http://localhost:8080/actuator/metrics
}

# Function to benchmark
benchmark() {
    echo -e "${YELLOW}Running benchmark...${NC}"
    
    # Create test payload
    cat > /tmp/search.json << EOF
{
  "query": "laptop",
  "page": 0,
  "size": 20
}
EOF
    
    echo "Testing with 1000 requests, 10 concurrent..."
    ab -n 1000 -c 10 -p /tmp/search.json -T application/json \
        http://localhost:8080/api/v1/products/search
    
    rm /tmp/search.json
}

# Main menu
show_menu() {
    echo ""
    echo "Select an option:"
    echo "  1) Start all services (MongoDB + Redis + App)"
    echo "  2) Stop all services"
    echo "  3) Build application"
    echo "  4) Run tests"
    echo "  5) Check health"
    echo "  6) Generate test data (10k products)"
    echo "  7) Clean all data"
    echo "  8) View logs"
    echo "  9) Show metrics"
    echo " 10) Run benchmark"
    echo "  0) Exit"
    echo ""
    read -p "Enter option: " option
    
    case $option in
        1)
            start_mongodb
            start_redis
            build_app
            run_app
            ;;
        2)
            stop_services
            ;;
        3)
            build_app
            ;;
        4)
            run_tests
            ;;
        5)
            check_health
            show_menu
            ;;
        6)
            generate_data
            show_menu
            ;;
        7)
            clean_data
            show_menu
            ;;
        8)
            view_logs
            ;;
        9)
            show_metrics
            show_menu
            ;;
        10)
            benchmark
            show_menu
            ;;
        0)
            echo -e "${GREEN}Goodbye!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option${NC}"
            show_menu
            ;;
    esac
}

# Check if running with argument
if [ $# -eq 0 ]; then
    show_menu
else
    case $1 in
        start)
            start_mongodb
            start_redis
            ;;
        stop)
            stop_services
            ;;
        build)
            build_app
            ;;
        test)
            run_tests
            ;;
        health)
            check_health
            ;;
        data)
            generate_data
            ;;
        clean)
            clean_data
            ;;
        logs)
            view_logs
            ;;
        metrics)
            show_metrics
            ;;
        benchmark)
            benchmark
            ;;
        *)
            echo "Usage: $0 {start|stop|build|test|health|data|clean|logs|metrics|benchmark}"
            exit 1
            ;;
    esac
fi
