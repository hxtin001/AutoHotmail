## Environment

- OS : Linux, Windows, Mac
- Google Chrome latest version
- ChromeDriver latest version. Check here: https://sites.google.com/a/chromium.org/chromedriver/

## IDE: IntellIJ Idea
## JRE: 1.8

## Setting
- Clone source code
- File -> New -> Project from existing source -> select AOBookTicket/pom.xml file
- Edit pom file:
```
<configuration>
  <drivers>
    <driver>
      <name>chromedriver</name>
      <version>2.29</version>
			<!--             linux or mac or windows -->
      <platform>mac</platform>
    </driver>
  </drivers>
</configuration>
```

## RUN / RUN-CONFIG
- Config main class src/test/groovy/AOMain.groovy
- Use classpath of module: AOBookTicket
- JRE: 1.8

- Compile source using Maven
- mvn clean
- mvn install
- copy chromedriver-linux-64bit to target/test-classes
- Run the test

