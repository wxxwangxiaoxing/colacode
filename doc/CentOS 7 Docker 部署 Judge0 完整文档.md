cd /opt/judge0
docker compose down

# 使用官方推荐的配置
cat > docker-compose.yml << 'EOF'
version: '3'

services:
  db:
    image: postgres:13.0
    restart: always
    environment:
      POSTGRES_DB: judge0
      POSTGRES_USER: judge0
      POSTGRES_PASSWORD: YourPasswordHere
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - judge0-network

  redis:
    image: redis:6.0
    restart: always
    networks:
      - judge0-network

  judge0:
    image: judge0/judge0:1.13.0
    restart: always
    ports:
      - "2358:2358"
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - POSTGRES_HOST=db
      - POSTGRES_PORT=5432
      - POSTGRES_DB=judge0
      - POSTGRES_USER=judge0
      - POSTGRES_PASSWORD=YourPasswordHere
    depends_on:
      - db
      - redis
    networks:
      - judge0-network
    privileged: true

volumes:
  postgres_data:
  redis_data:

networks:
  judge0-network:
    driver: bridge
EOF

docker compose up -d
sleep 60