worker:
	cd $$(git rev-parse --show-toplevel) && \
	./gradlew -q execute -PmainClass=io.temporal.samples.helloupdateactivity.MyWorker

run:
	cd $$(git rev-parse --show-toplevel) && \
	./gradlew -q execute -PmainClass=io.temporal.samples.helloupdateactivity.MyStarter

.PHONY: worker