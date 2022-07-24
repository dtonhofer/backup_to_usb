# backup_to_usb

## What is this

This is basically a small Java program around the `rsync` command which I use to transfer the file system to contents to an USB-attached disk.

My previous bash program was not satisfying, so here we go.

This program is also an exercise in:

- Using Java 16 ;
- Using PicoCli to handle Unix-style command line arguments: https://picocli.info/ ;
- Using the java `Processbuilder` class [JavaDoc](https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/ProcessBuilder.html), to, in this case, create the `rsync` sub-process ;
- Using [SLF4J](https://www.slf4j.org/) as interface to a logging system (instead of `java.util.logging`) ;
- Using [Logback](https://logback.qos.ch/) as implementation behind the SLF4J façade ;
- And add some [JUnit5](https://junit.org/junit5/) testing code, mainly for ascertaining that the command-line processing works.

The general principle is simple:

- We define "batches", each of which is a set of filetrees to be backed-up, along with a selection of subtrees that shall be skipped;
- We determine from the command line what the taregt directory of the backup is (it should be an external USB disk mounted into the filetree)
- We determine from the command line arguments what batches the users wants to have backed up;
- For each batch in turn, we start `rsync` as sub-process using a `Processbuilder` ;
- Once a subprocess ends, we check its status and log accordingly.
