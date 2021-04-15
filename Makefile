docker-build:
	docker build -t mildblue/covid-vaxx .

db:
	docker-compose up -d db

run: db
	docker-compose up

re-run:
	docker-compose stop be || true;
	docker-compose rm -f be || true;
	docker-compose up --build be;
