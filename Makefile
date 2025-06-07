.PHONY: build test clean publish

build:
	./gradlew :querysentinel:build

test:
	./gradlew :demo:test

clean:
	./gradlew clean

publish:
	./gradlew :querysentinel:publishToMavenLocal

version:
	@echo "Querysentinel version: 1.0.0"