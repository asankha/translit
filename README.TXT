Translit - the Fast, Free and Open Source Transliteration Engine

Copyright (c) 2010 Asankha C. Perera. All Rights Reserved

= Release Notes ========================================================================================================
v 1.0.0 - 9th December 2010 - Initial source code released

= Introduction and User Guide ==========================================================================================
The Translit is a fast, free and open source transliteration engine that ships with English, Sinhala and Tamil language
support. The objective of this project is to build a very fast transliterator, that could be invoked as a Java library.
The engine performs a dictionary lookup, and failing which, uses a phonetic rule based approach by default. Phonetic
rules maybe added to support other languages, and existing rules maybe improved to increase accuracy of transliterations.
However, the easiest way to improve accuracy is by updating the dictionaries.
 
See http://code.google.com/p/translit/wiki/UserGuide for more details

e.g. Sample execution of the command line application (Specifying gender as male - as it defaults to unknown)
$java -jar translit-core-1.0.0.jar -g m
asankha perera
අසන්ඛ පෙරේරා

Transliterating an input file /tmp/input into /tmp/output using default options
$java -jar translit-core-1.0.0.jar < /tmp/input > /tmp/output