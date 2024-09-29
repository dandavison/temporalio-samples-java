worker:
	cd $$(git rev-parse --show-toplevel) && \
	./gradlew -q execute -P mainClass=io.temporal.samples.basic.SampleWorker

run:
	cd $$(git rev-parse --show-toplevel) && \
	./gradlew -q execute -P mainClass=io.temporal.samples.basic.Starter

.PHONY: run worker
