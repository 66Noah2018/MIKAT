/* 
 * Copyright (C) 2022 Amsterdam Universitair Medische Centra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

let testPatients = null;
let nrVarsTable = 0;
let outcomesTable = 0;
let headings = null;
let chartJS;
let newTestCasesFile = false;
const successIcon = "<span class='mif-done'></span>";
const errorIcon = "<span class='mif-cancel'></span>";
const waitingToRunIcon = "<span class='mif-spinner ani-pulse waiting-to-run-icon'></span>";
let testsPassedIds = [];
let testsFailedIds = [];

function cartesian(args) {
    var r = [], max = args.length-1;
    function helper(arr, i) {
        for (var j=0, l=args[i].length; j<l; j++) {
            var a = arr.slice(0); // clone arr
            a.push(args[i][j]);
            if (i===max){
                r.push(a);
            }
            else {
                helper(a, i+1);
            }
        }
    }
    helper([], 0);
    return r;
}

function highlight(elToHighlight, elToNormalize){
    if (document.getElementById(elToHighlight).classList.contains("highlight")){
        document.getElementById(elToHighlight).classList.remove("highlight");
    } else {
        document.getElementById(elToHighlight).classList.add("highlight");
    }
    document.getElementById(elToNormalize).classList.remove("highlight");
}

function generateTestcasesTableCode(variables, possibleValues, possibleOutcomes){
    nrVarsTable = variables.length;
    outcomesTable = possibleOutcomes;
    // possibleValues: array of arrays with every possible value for a variable (as in neg/pos or a value within a specific range)
    let code = "<table class='table cell-hover table-border row-border cell-border compact' id='testcases'>";
    code += "<colgroup><col span='1' style='width: 5%'></colgroup><thead><tr><th>#</th>";
    variables.forEach(element => {
        code += "<th>" + element + "</th>";
    });
    possibleOutcomes.forEach(element => {
        code += "<th>" + element + "</th>";
    });
    code += "</tr></thead><tbody contenteditable>";
    const valueCombinations = cartesian(possibleValues);
    let caseNr = 1;
    valueCombinations.forEach(combination => {
        let patient = {id: caseNr};
        code += "<tr><td>" + caseNr + "</td>";
        index = 0;
        combination.forEach(value => {
            code += "<td>" + value + "</td>";
            patient[variables[index]] = value;
            index += 1;
        });
        possibleOutcomes.forEach(element => {
            code += `<td><input type="checkbox" id="${element}_${caseNr}" name="${element}"></td>`;
        });
        code += "</tr>";
        testPatients[caseNr] = patient;
        caseNr += 1;
    });
    code += "</tbody></table>";
    return code;    
}

function showTestCases(){
    if (testPatients === null) { try {loadTestCases(); } catch (e) {} }
    highlight('open-test-cases', 'open-test-results');
    const currentState = document.getElementsByClassName("tests")[0].style.display;
    if (currentState !== "block"){ document.getElementsByClassName("tests")[0].style.display = "block"; } //make tabs + content visible
    else if (!document.getElementById("open-test-cases").classList.contains("highlight")) { //user clicks on the highlighted button (e.g., edit when tab test cases is open), leads to hide tabs + content
        document.getElementsByClassName("tests")[0].style.display = "none"; 
        document.getElementById("open-test-cases").classList.remove("highlight");
        document.getElementById("open-test-cases").classList.remove("active");
    }
    
    document.getElementById("test-cases").classList.add("active"); //switch to correct tab
    document.getElementById("test-results").classList.remove("active");
    document.getElementById("_target_testcases").style.display = "block";
    document.getElementById("_target_results").style.display = "none";
    return;
}

function getTestCasesTableCode(variables, medicalActions, testPatients = null){
    let table = document.getElementById("testcases");
    if (table === null && headings !== null) {
        var spinner = document.getElementById("load-testcases");
        spinner.style.display = "block";
        let target = document.getElementById("_target_testcases");
        
        let code = "<table class='table cell-hover table-border row-border cell-border compact' id='testcases'>";
        code += "<colgroup><col span='1' style='width: 5%'></colgroup><thead><tr>";
        variables.unshift("#");
        variables.forEach(element => {
            code += "<th>" + element + "</th>";
        });
        medicalActions.forEach(element => {
            code += "<th>" + element + "</th>";
        });
        code += "</tr></thead><tbody contenteditable>";
        if (testPatients !== null){
            testPatients.forEach(testcase => {
                code += "<tr>";
                variables.forEach(key => { 
                let variableValue = testcase[key];
                if (variableValue === undefined) { variableValue = ""; }
                    code += `<td>${variableValue}</td>`;
                });
                medicalActions.forEach(key => {
                    let value = testcase[key];
                    if (value === "true"){ code += `<td><input type="checkbox" name="${key}" checked></td>`; }
                    else { code += `<td><input type="checkbox" name="${key}"></td>`; }
                });
                code += "</tr>";
            });
        } else {
            code += "<tr><td>1</td>";
            for (let i = 0; i < variables.length - 1; i++) { code += "<td></td>"; }
            for (let j = 0; j < medicalActions.length; j++) { code += "<td><input type='checkbox'></td>"; }
            code += "</tr>";
        }
        code += "</tbody></table>";
        
        target.appendChild(parser.parseFromString(code, 'text/html').body.firstChild);
        spinner.style.display = "none";
//        showTestCases();
        document.getElementById("open-test-cases").classList.remove("disabled");
        document.getElementById("create-test-cases").classList.add("disabled");
    }
}

function loadTestCases(){
    newTestCasesFile = false;
    let result = null;
    if (testPatients === null) { 
        try {
            result = JSON.parse(servletRequest("./chartservlet?function=getTestCases")).testCases;
            testPatients = result.testCases;
            headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
        } catch (e) {}
    }
    getTestCasesTableCode(headings.retrievedata, headings.medicalActions, testPatients);
}

function refreshTable(){
    if (testPatients === null) { try {loadTestCases(); } catch (e) {} }
    document.getElementById("testcases").remove();
    const headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    getTestCasesTableCode(headings.retrievedata, headings.medicalActions, testPatients);
    showTestCases();
}

function showTestResults(){
    highlight('open-test-results', 'open-test-cases');
    const currentState = document.getElementsByClassName("tests")[0].style.display;
    if (currentState !== "block"){ document.getElementsByClassName("tests")[0].style.display = "block"; }
    else if (!document.getElementById("open-test-results").classList.contains("highlight")) { 
        document.getElementsByClassName("tests")[0].style.display = "none"; 
        document.getElementById("open-test-results").classList.remove("highlight");
        document.getElementById("open-test-results").classList.remove("active");
    }
    
    document.getElementById("test-cases").classList.remove("active");
    document.getElementById("test-results").classList.add("active");
    document.getElementById("_target_testcases").style.display = "none";
    document.getElementById("_target_results").style.display = "block";
    return;
}

function generateTestcases(){ //get variables and default values
    let table = document.getElementById("testcases");
    if (table === null){
        var spinner = $("#generate-testcases-load")[0];
        spinner.style.display = "block";
        let target = document.getElementById("_target_testcases");
        const tableCode = generateTestcasesTableCode(["CRP", "ANA"], [[5, 500], ["Pos", "Neg"]],["Log ANA positive"]); //get from chart
        target.appendChild(parser.parseFromString(tableCode, 'text/html').body.firstChild);
        spinner.style.display = "none";
        showTestCases();
    }
    document.getElementById("generate-test-cases-btn").classList.add("disabled");
    document.getElementById("open-test-cases").classList.remove("disabled");
    document.getElementById("create-test-cases").classList.add("disabled");
    updateTestCases();
}

function updateTestCases(){
    testPatients = [];
    const tableRows = document.getElementById("testcases").rows;
    headings = [];
    const headerCells = tableRows[0].cells;
    for (let i = 0; i < headerCells.length; i++){
        headings.push(headerCells[i].textContent);
    }
    for (let i = 1; i < tableRows.length; i++){
        let testCase = new Object();
        let row = tableRows[i].cells;
        for (let j = 0; j < row.length; j++){
            if (row[j].innerHTML.toString().includes("input")) { testCase[headings[j]] = row[j].firstChild.checked.toString(); }
            else { testCase[headings[j]] = row[j].textContent; }
        }
        testPatients.push(testCase);
    }
    servletRequestPost("./chartservlet?function=saveTestCases&newFile=" + newTestCasesFile, {"headings": headings, "testCases": testPatients});
    displayQueryInfoBox(infoBoxProperties.success, "Success", "Test cases saved");
}

Element.prototype.remove = function() {
    this.parentElement.removeChild(this);
};

function createTestCases(){
    newTestCasesFile = true;
    headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    getTestCasesTableCode(headings.retrievedata, headings.medicalActions, null);
}

function importTestCases(){
    event.preventDefault();
    closeAllForms();
    let fileName = document.getElementById("testcasefile-select").value;
    if (fileName.includes("\\")) { fileName = fileName.split("\\")[fileName.split("\\").length - 1]; }
    if (fileName.includes(".csv")) {
        const newFileName = processCSVData(fileName);
    } else {
        servletRequestPost("./chartservlet?function=setTestCasesFileLocation", fileName);
    }
    loadTestCases();    
}

function processCSVData(fileName){
    const http = new XMLHttpRequest(); // servletrequestpost doesnt work here, loading response somehow takes too long
    http.open("POST", "./chartservlet?function=readCSVFile", true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(fileName));
    http.onload = function(){ 
        const fileContent = JSON.parse(http.responseText).fileContent;
        if (fileContent === "Invalid file, no path") { 
            displayQueryInfoBox(infoBoxProperties.warningKeepOpen, "Warning: Nonexistent file", "Selected file does not exist");
            return;
        }
        testPatients = [];
        const records = fileContent.split(";ret;");
        records.shift();
        const headings = records[0].split(",");
        for (let i = 1; i < records.length; i++) {
            if (records[i] !== ""){
                let testCase = new Object();
                let cells = records[i].split(",");
                for (let j = 0; j < cells.length; j++) {
                    testCase[headings[j]] = cells[j];
                }
                console.log(testCase);
                testPatients.push(testCase);
            } 
        }
        servletRequestPost("./chartservlet?function=saveTestCases&newFile=true", {"headings": headings, "testCases": testPatients});
        displayQueryInfoBox(infoBoxProperties.success, "Success", "Test cases imported");
        loadTestCases();
    }
}

function exportToCSV(){
    let csvString = "";
    if (headings === null) { headings = headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings")); }
    console.log(headings);
    csvString += headings.retrievedata.join(",") + "," + headings.medicalActions.join(",") + ";ret;";
    testPatients.forEach(patient => {
        let patientString = "";
        headings.retrievedata.forEach(heading => {
            patientString += patient[heading] + ",";
        });
        headings.medicalActions.forEach(heading => {
            patientString += patient[heading] + ",";
        });
        patientString = patientString.substring(0, patientString.length - 1);
        csvString += patientString + ";ret;";
    });
    servletRequestPost("./chartservlet?function=exportCSV", csvString);
    displayQueryInfoBox(infoBoxProperties.success, "Success", "Test cases exported");
}

function startTests(){
    if (hasErrors) {
        displayQueryInfoBox(infoBoxProperties.warningKeepOpen, "Warning: Cannot run tests", "Cannot run tests while model contains errors");
        return;
    }
    if (testPatients === null) { try {loadTestCases(); } catch (e) {} }
    testsPassedIds = [];
    testsFailedIds = [];
    
    setUpResultsView(testPatients);
    const http = new XMLHttpRequest();
    http.open("GET", "./chartservlet?function=translateJS", false);
    http.send();
    if (http.readyState === 4 && http.status === 200) {
        if (http.status === 200) {
            const response = JSON.parse(http.responseText);
            const parameters = response.parameters;
            const code = response.code;
            let functionString = "new Function(";
            parameters.forEach(parameter => functionString += "\"" + parameter + "\",");
            functionString += "code)";
            chartJS = eval(functionString);
            const NR_OF_TESTS = testPatients.length;
            let nrOfTestsCompleted = 0;
            testPatients.forEach(patient => {
                let result = runTest(patient, parameters );
                if (result.passed === true) {
                    document.getElementById("statusTest" + result.testCaseNr).innerHTML = successIcon;
                    testsPassedIds.push("statusTest" + result.testCaseNr);
                } 
                else {
                    document.getElementById("statusTest" + result.testCaseNr).innerHTML = errorIcon;
                    document.getElementById("actualResultTest" + result.testCaseNr).innerText = result.expectedResult.join(", ");
                    testsFailedIds.push("statusTest" + result.testCaseNr);
                }
                nrOfTestsCompleted += 1;
                let newValue = Math.round((nrOfTestsCompleted/NR_OF_TESTS) * 100);
                document.getElementById("testingProgress").setAttribute("data-value", newValue);
                if (newValue < 100) { document.getElementById("testingProgressPercentage").innerText = newValue + "%"; }
                else { document.getElementById("testingProgressPercentage").innerText = "Done"; }
            });
            if (testsFailedIds.length > 0){
                document.getElementById("tests-failed-btn").setAttribute("class", "button active bg-gray js-active");
                document.getElementById("tests-passed-btn").setAttribute("class", "button");
                filterTestResults();
            } else {
                document.getElementById("tests-failed-btn").setAttribute("class", "button active bg-gray js-active");
                document.getElementById("tests-passed-btn").setAttribute("class", "button active bg-gray js-active");
            }
        } 
        else { displayQueryInfoBox(infoBoxProperties.error, "Error: Unknown error", "Cannot run tests due to unknown error"); }
    }
}

function filterTestResults(){
    setTimeout(function(){
        const showPassedTests = document.getElementById("tests-passed-btn").classList.contains("active");
        const showFailedTests = document.getElementById("tests-failed-btn").classList.contains("active");
        let table = document.getElementById("resultsTable");
        let tr = table.getElementsByTagName("tr");

        if (showPassedTests && showFailedTests){
            for (i = 0; i < tr.length; i++) { tr[i].style.display = ""; }
        } else if (showPassedTests && !showFailedTests) {
            for (i = 0; i < tr.length; i++) {
                td = tr[i].getElementsByTagName("td")[0];
                if (td) {
                    if (testsPassedIds.includes(td.id)) {
                        tr[i].style.display = "table-row";
                    } else {
                        tr[i].style.display = "none";
                    }
                }
            }
        }
        else if (!showPassedTests && showFailedTests){
            for (i = 0; i < tr.length; i++) {
                td = tr[i].getElementsByTagName("td")[0];
                if (td) {
                    if (testsFailedIds.includes(td.id)) {
                        tr[i].style.display = "table-row";
                    } else {
                        tr[i].style.display = "none";
                    }
                }
            }
        }
    }, 0);
}

function setUpResultsView(testPatients){
    if (headings === undefined || headings.medicalActions === undefined) {
        headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    }
    let target = document.getElementById("results_table");
    let tableCode = "<table style='width:100%' id='resultsTable'><thead><tr><th style='width:75px'>Status</th><th style='width:150px'>Test case</th><th>Expected result</th><th>Actual result</th></tr></thead><tbody>";
    testPatients.forEach(patient => {
        expectedResults = [];
        (headings.medicalActions).forEach(action => {
            let value = patient[action];
            if (value === "true") { expectedResults.push(action); }
        });
        tableCode += "<tr><td style='width:75px' id='statusTest" + patient["#"] + "'>" + waitingToRunIcon + "</td><td style='width:150px'>Test case " + patient["#"] + "</td><td>" + expectedResults.join(", ") + "</td><td id='actualResultTest" + patient["#"] + "'></td></tr>";
    });
    tableCode += "</tbody></table>";
    target.innerHTML = tableCode;
    showTestResults();
}

function runTest(patient, parameters) {
    let testString = "chartJS(";
    parameters.forEach(parameter => testString += patient[parameter] + ",");
    testString.substring(0, testString.length - 1);
    testString += ");";
    let results = eval(testString);
    
    expectedResults = [];
    if (headings === undefined || headings.medicalActions === undefined) {
        headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    }
    (headings.medicalActions).forEach(action => {
        let value = patient[action];
        if (value === "true") { expectedResults.push(action); }
    });
    
    let testPassed = true;
    const diff1 = expectedResults.filter(x => !results.includes(x));
    const diff2 = results.filter(y => !expectedResults.includes(y));
    if ((diff1.length > 0) || (diff2.length > 0)) {testPassed = false; }
    
    let result = new Object();
    result.testCaseNr = patient["#"];
    result.passed = testPassed;
    result.expectedResult = expectedResults;
    result.testResult = results;
    return result;
}

function stopTests(){}

function addNewTestCase(){
    const headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    let newRow = document.getElementById("testcases").insertRow();
    let newCell = newRow.insertCell(0);
    let nextId = document.getElementById("testcases").rows.length - 1;
    newCell.appendChild(document.createTextNode(nextId));
    (headings.retrievedata).forEach((e) => newRow.insertCell(-1));
    (headings.medicalActions).forEach((outcome) => {
        let content = `<input type="checkbox" id="${outcome}_${nextId}" name="${outcome}">`;
        newCell = newRow.insertCell(-1);
        newCell.appendChild(parser.parseFromString(content, 'text/html').body.firstChild);
    });
}
