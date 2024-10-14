worker:
	ls *.java | entr -r \
		sh -c 'cd $$(git rev-parse --show-toplevel) && ./gradlew -q execute -P mainClass=io.temporal.samples.blockingupdate.MyWorker'

run:
	cd $$(git rev-parse --show-toplevel) && \
	./gradlew -q execute -P mainClass=io.temporal.samples.blockingupdate.MyStarter

.PHONY: run worker
