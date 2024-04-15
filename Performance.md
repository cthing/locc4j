# Counting Performance

## Measurements
The line counting rate of the locc4j library has been measured at 270 lines/millisecond. This is for pure
line counting with no file system traversal or matching, and does not include the time to read the file
from the file system. The measurement is the result of counting the lines of every
[test data file](src/test/resources/data)<sup>1</sup> (over 17K lines) and dividing the total number of
lines by the elapsed time to count them. The measurement is the average of 20 runs with the highest and
lowest rates dropped. The measurement was performed in the following environment:

- CPU: 3.70 GHz Intel Core i9-10900K
- Motherboard: ASUS ROG STRIX Z590-I
- Memory: 64GB 2133 MHz DDR4
- OS: Ubuntu 20.04

The code for performing the measurement is in the [CounterTest](src/test/java/org/cthing/locc4j/CounterTest.java) `testCount` method.

<sup>1</sup> The measurements exclude the [Jupyter notebook file](src/test/resources/data/jupyter.ipynb)
because it requires JSON parsing in order to detect and count embedded languages. This results in the counting
of lines in Jupyter notebook files being an order of magnitude slower than counting any other language.

## Design
The locc4j library has been architected to quickly count lines of code across a wide range of languages.

When counting lines, the tradeoff is accuracy versus speed. On the one hand, a 100% accurate count of
code lines and comment lines in a particular language is only possible using a parser designed for that
language. However, for line counting purposes, a parser is slow because it must perform a large amount
of work unrelated to line counting. It is also impractical to obtain and integrate a parser for numerous
languages into one library written in one programming language. On the other hand, a simplistic application
of regular expressions, while fast, is unacceptably inaccurate. Quoted strings containing comment character
sequences, nested comments, and embedded languages cannot be properly accommodated using this approach.
The locc4j library uses a data driven parser that combines regular expressions and heuristics that
result in a good balance between accuracy and speed. Being data driven makes it practical to accommodate
a wide range of languages. Languages are described in a JSON file by specifying a small number of
characteristics relevant to line counting.

Another aspect of performance especially in Java, is avoiding unnecessary string copying. In Java,
strings are immutable. In general, this is a very good feature especially when strings are manipulated
in a multi-thread environment. However, when counting lines, all input data is read-only and all operations
on the data are read-only and single threaded. Therefore, operations such as obtaining a substring using
a Java string method, needlessly creates a new string object containing a copy of the desired region of
the original string. Given the large number of times these types of operations must be performed during
line counting, the performance impact of copying strings becomes significant. To avoid these inefficiencies,
a character data class [CharData](src/main/java/org/cthing/locc4j/CharData.java) has been implemented.
This class provides all required string methods without creating a new string object and copying the string
data. For example, when obtaining a substring, a new character data object is created which only maintains
offsets into the original character data. This is essentially a copy-on-write strategy without any writing.
