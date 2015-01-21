JC = javac

default: SimpleDatabase.class RunDatabase.class

SimpleDatabase.class: SimpleDatabase.java
		$(JC) $(JFLAGS) SimpleDatabase.java

RunDatabase.class: RunDatabase.java
		$(JC) $(JFLAGS) RunDatabase.java

clean:
		$(RM) *.class
