.PHONY: build test clean publish

build:
	./gradlew :querykeeper:build

test:
	./gradlew :demo:test

clean:
	./gradlew clean

publish:
	./gradlew :querykeeper:publishToMavenLocal

version:
	@echo "Querykeeper version: 1.1.0"