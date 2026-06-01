JAVAC = javac
JAVA = java
SRCS = Model.java View.java Controller.java GameMap.java Player.java Guard.java SecurityDevice.java Minigame.java Ranking.java
CLASSES = $(SRCS:.java=.class)
DIR = $(shell pwd)

all: $(CLASSES)

$(CLASSES): $(SRCS)
	LANG=C.utf8 LC_ALL=C.utf8 $(JAVAC) -encoding UTF-8 $(addprefix $(DIR)/,$(SRCS)) -d $(DIR)

run: all
	bash read_key.sh | $(JAVA) Model

clean:
	rm -f *.class ranking.dat
