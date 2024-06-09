# Dev Setup Guide

## Initial Build

- `gradle assemble`
- `gradle jar`
- `gradle compileDemoRocker`
- `DemoTablesCodegenMain`

After this `Ctrl-F9` in IJ should work normally.

## IntelliJ Tips

- `Rebuild Project`

## Main Tests

- `All Tests`
- `Fast Tests`

## Running Locally

- Make sure `demo-frontend\.data\userdata` directory exists
- `Main`

## Windows Issues

> `UnsatisfiedLinkError: Could not load library. Reasons: [no leveldbjni64-1.8 in java.library.path ...`
 
Install Microsoft Visual C++ 2010 Redistributable package. Sources: \
https://github.com/fusesource/leveldbjni/issues/80 \
https://github.com/fusesource/leveldbjni/issues/41

## MySQL Shell Tips

- `\connect localhost:3306`
- `\sql`
- `CREATE USER 'test'@'localhost';`
- `CREATE DATABASE test;`
- `GRANT ALL PRIVILEGES ON test.* To 'test'@'localhost';`
