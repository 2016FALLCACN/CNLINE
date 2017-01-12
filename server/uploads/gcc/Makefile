##################################################################################################################
# Makefile: For building the client application
##################################################################################################################
# Assumed filepath
#
# CNLINE
# ├── Makefile
# ├── out
# │   └── linux
# │       └── (packagename, e.g. sample)
# │           └── (classes)
# ├── src
# │   └── (packagename, e.g. sample)
# │       └── (.java source code)
# └── (Some .jar external library files)
#
#
##################################################################################################################
# NOT USED VARIABLES
##################################################################################################################
# source: temp, not flexible
source = AlertBox.java ChatApplication.java Constants.java Controller.java Friend.java Main.java User.java


##################################################################################################################
# USED VARIABLES
##################################################################################################################
javalibs = socket.io-client-0.1.0.jar:org.json.jar:Java-WebSocket-1.3.0.jar:engine.io-client-0.2.1.jar

srcpackages = sample

sourcepath = src/

objdir = out/linux/

# sourcefiles: in terms of src/(packagename)/(source).java
# classfiles: in terms of out/linux/(packagename)/(object).class
sourcefiles = $(foreach dir,$(srcpackages),$(wildcard $(sourcepath)$(dir)/*.java))
classfiles = $(addprefix $(objdir), $(subst $(sourcepath),,$(sourcefiles:.java=.class)))

mainclass = sample.Main

csssrc = $(sourcepath)sample/style.css
cssdst = $(objdir)sample/




default: note dirs packages css
	

.PHONY: clean run_client makefile_debug auto help note

auto: default run_client

css:
	@echo "Creating stylesheet"
	@cp $(csssrc) $(cssdst)

dirs:
	@echo "Creating $(objdir)"
	@mkdir -p $(objdir)

packages: $(sourcefiles)
	@echo "Building packages"
	@javac -d $(objdir) -classpath $(javalibs) -sourcepath $(sourcepath) $^

	
run_client: 
	@echo "Running..."
	@java -classpath $(javalibs):$(objdir)  $(mainclass)

makefile_debug:
	@echo "sourcefiles = $(sourcefiles)"
	@echo "classfiles = $(classfiles)"

clean:
	rm -rf $(objdir)

note:
	@echo "Note: If you don't know how to use this Makefile, type 'make help'."
help:
	@echo "Usage: make [default | auto | run_client | clean]"
	@echo "Note: 'make auto' combines 'make (default)' and 'make run_client'."