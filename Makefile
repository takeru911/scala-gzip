compress:
	sbt "run -c $(FILE)"

decompress:
	sbt "run -d $(FILE)"

run: sbt/run

build: compile

compile: sbt/compile

sbt/%:
	sbt $*
