JAVAC = javac
JAVA = java

SRC_DIR = src
BUILD_DIR = build

MAIN_CLASS = main.Main

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

CLASSES = $(SOURCES:$(SRC_DIR)/%.java=$(BUILD_DIR)/%.class)

all: $(BUILD_DIR) $(CLASSES)

$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)

$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java
	@$(JAVAC) -d $(BUILD_DIR) -sourcepath $(SRC_DIR) $<

clean:
	@rm -rf $(BUILD_DIR)

run: all
	@$(JAVA) -cp $(BUILD_DIR) $(MAIN_CLASS) $(ARGS)

.PHONY: all clean run
