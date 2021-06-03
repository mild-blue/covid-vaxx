run: db
	./gradlew run

detekt:
	./gradlew detekt

test: test-db
	./gradlew test;
	$(MAKE) stop-test-db

check: detekt test

test-db: stop-test-db stop-db
	docker-compose -f docker-compose.test.yml up -d db

stop-test-db:
	docker-compose -f docker-compose.test.yml stop db

db: stop-test-db stop-db
	docker-compose -f ../docker-compose.yml up -d db

stop-db:
	docker-compose -f ../docker-compose.yml stop db

docker-build:
	docker build -t mildblue/covid-vaxx ../

docker-run: stop-test-db
	docker-compose -f ../docker-compose.yml up

docker-rerun:
	$(MAKE) stop-db;
	docker-compose -f ../docker-compose.yml rm -f be;
	$(MAKE) docker-rebuild;

docker-rebuild:
	docker-compose -f ../docker-compose.yml up --build be
