<h1>Medical Informatics Knowledge Authoring Tool (MIKAT)</h1>
<h2>Required programs</h2>
<ul>
  <li>Java 11</li>
  <li>Tomcat 8.5 (no higher version, clashes with Java version and crashes project)</li>
  <li>NodeJS 16.14.0</li>
  <li>Maven 3.8</li>
</ul>
Run <code>npm -i</code> to install JavaScript dependencies

<i>N.B. This project has been built and tested on Windows 10/11 and using Google Chrome. While the project should be platform-independent and browser-independent, this cannot be guaranteed.</i>

<h2>MIKAT features</h2>
This project is a prototype graphical knowledge authoring tool for rule-based clinical decision support. It is domain-specific language-independent and allows projects created by users to be exported to any programming language. At the moment, only Arden Syntax is supported, but modules for other languages can be written and added. 'Choosing' a language is then simply changing the call in the servlet (chartservlet.java) on line 190 (under <code>case "translateAS":</code>).

<h3>MIKAT projects</h3>
MIKAT allows creation of projects that do not use time-based functionality (e.g., no delays such as wait for <i>x</i>). Projects can use conditional statements, for loops, subroutines (using another MIKAT project's output), dossier data (retrieve medical data) based on mappings, return values, and a multitude of medical actions including writing prescriptions. 
Keep in mind that this is decision support, the engine and electronic health record used determine <b>if and how</b> return values and medical actions are displayed.

<h3>Testing and test-driven development</h3>
MIKAT allows for test-driven development. Users can load existing files with testcases (either JSON in MIKAT accepted format or CSV) and edit these. It is also possible to create test cases within MIKAT, reuse these in other projects, and export the test cases to CSV. Test cases are required to run tests on the model. Testing is facilitated through translation of the model state to JavaScript.

<h3>Database and standardized terminology mappings</h3>
The terminology used within a MIKAT project is usually the same as used within the target healthcare institution. MIKAT facilitates mappings between this local terminology, the database queries used to retrieve data and standarized terminology such as SNOMED CT. When a project is exported to a domain-specific language, the database mappings are used. In the future, it will likely be possible to share projects using the standardized terminology and change this back to local terminology.

<h2>Local storage</h2>
MIKAT saves projects as JSON files in the preferred working directory (which is set as a project property). This means that the local file system is accessed by MIKAT and files are created and edited. However, files are searched for within the working directory. If they are not found there, the default working directory is searched and everything under the root path if necessary. Settings are saved in Program Files or it's Mac or Linux equivalent.

<h2>License</h2>
This work is licenced under the GNU General Public License (GPL) version 3.

<h2>Possible future changes</h2>
<ul>
  <li>Export project to Arden Syntax (before May 30th)</li>
  <li>Facilitate insertion of conditionals between existing ChartItems</li>
  <li>Allow sharing MIKAT projects using standardized terminology</li>
  <li>Add more information boxes about the status of a query (e.g., saved successfully)</li>
  <li>Add an example model + explanation, which is shown when no project is opened</li>
  <li>Fix absolute path issues when saving changes. // is multiplied</li>
  <li>Fix overlap between conditionals if they are placed under one another (e.g., both the if and else branches have their own conditionals)</li>
  <li>Allow to use elements from a loop within a loop (e.g., <code>for (let row in rows) { do something with row }</code>)</li>
  <li>Generate testcases based on model</li>
  <li>Stop running tests</li>
</ul>
