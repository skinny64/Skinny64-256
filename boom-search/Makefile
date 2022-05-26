all: jar

jar: boomerangsearch
	jar cfm boomerangsearch.jar META-INF/boomerangManifest.txt boomerangsearch/

run1: boomerangsearch.jar
	java -ea -jar boomerangsearch.jar -verbose -nbRounds=17 -tk=3 -step1output=output/test1.json -noStep2 -t=3
run2: boomerangsearch.jar
	java -jar boomerangsearch.jar -verbose -nbRounds=17 -tk=3 -blockSize=4 -step2input=output/test1.json -step2output=output/test2.json -clusterGap=2 -nbStep2Sols=2 -t=2
runboth:
	java -ea -jar boomerangsearch.jar -verbose -nbRounds=10 -tk=1 -step1output=step1.json -nbStep1Sols=10 -step2output=step2.json -clusterGap=5 -nbStep2Sols=10 -t=3


tikz: solutiontotikz
	jar cfm solutiontotikz.jar META-INF/solutionToTikzManifest.txt boomerangsearch/

runtikz1: solutiontotikz.jar
	java -jar solutiontotikz.jar -step1input=patrickoutputs/step1-tk3-25-100-cheat.json -sol=0

runtikz2: solutiontotikz.jar
	java -jar solutiontotikz.jar -step2input=patrickoutputs/step2-tk3-25-128-cheat.json -step2best


step1:
	javac -cp ../newlibs/*:. -d . step1/*.java

step2: step1
	javac -cp ../newlibs/*:. -d . step2/*.java

solutiontotikz: step1 step2
	javac -cp ../newlibs/*:. -d . solutiontotikz/*.java

boomerangsearch: step1 step2 solutiontotikz
	javac -cp ../newlibs/*:. -d . *.java

clean:
	rm -rf boomerangsearch
	rm -f boomerangsearch.jar
	rm -f solutiontotikz.jar
	rm -f model.mps

.PHONY: step1 step2 boomerangsearch
