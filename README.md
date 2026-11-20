# Introduction

WordBee is a word building game that requires thinking of as many **BeeWords** as possible, using the letters in the honey comb, to obtain a greater score and thus earning a higher badge.

```bash
# Honey comb with 'G' as the golden letter
                     _____
                    /     \
              _____/   U   \_____ 
             /     \       /     \
            /   L   \_____/   R   \ 
            \       /     \       /  
             \_____/   G   \_____/  
             /     \       /     \  
            /   W   \_____/   E   \      
            \       /     \       /  
             \_____/   D   \_____/
                   \       / 
                    \_____/            
```

A **BeeWord** has to satisfy the following criteria

- Must be a valid dictionary word having at-least 3 letters -- Repetitions are allowed

- Must have the center letter (Golden Letter)

- Must use only the letters in the comb

# Pre-requisite softwares

To build and run the application, we need [ApacheAnt](https://ant.apache.org/) and [JDK](https://www.oracle.com/in/java/technologies/downloads/)

# Build and Run

```bash
# Execute the following on Ubuntu (Linux)
> bash run.sh

# The above command does the following
> ant 
> java -jar WordBeeTUI.jar
```

# Technologies Used

| Tech                                                                                                    | Version      | Detail                   |
| ------------------------------------------------------------------------------------------------------- | ------------ | ------------------------ |
| Java                                                                                                    | 26.0.1       |                          |
| Apache Ant                                                                                              | 1.10.17      |                          |
| [Google Lanterna](https://javadoc.io/doc/com.googlecode.lanterna/lanterna/latest/overview-summary.html) | 3.2.0-alpha1 | Java library for text UI |
| Apache Commons                                                                                          | 3-3.12.0     | Lang3 Java library       |
| Apache Ivy                                                                                              | 2.6.0        | Dependency management    |
