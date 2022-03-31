/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let testPatients = null;
let nrVarsTable = 0;
let outcomesTable = 0;
let headings = null;

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
                variables.forEach(key => { code += `<td>${testcase[key]}</td>`;
                });
                medicalActions.forEach(key => {
                    let value = testcase[key];
                    if (value === true){ code += `<td><input type="checkbox" name="${key}" checked></td>`; }
                    else { code += `<td><input type="checkbox" name="${key}"></td>`; }
                });
                code += "</tr>";
            });
        } else {
            code += "<tr>";
            for (let i = 0; i < variables.length; i++) { code += "<td></td>"; }
            for (let j = 0; j < medicalActions.length; j++) { code += "<td><input type='checkbox'></td>"; }
            code += "</tr>";
        }
        code += "</tbody></table>";
        
        target.appendChild(parser.parseFromString(code, 'text/html').body.firstChild);
        spinner.style.display = "none";
        showTestCases();
        document.getElementById("open-test-cases").classList.remove("disabled");
        document.getElementById("create-test-cases").classList.add("disabled");
        document.getElementById("load-testcases-from-file").classList.add("disabled");
    }
}

function loadTestCases(){
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
    document.getElementById("testcases").remove();
    const headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    getTestCasesTableCode(headings.retrievedata, headings.medicalActions, testPatients);
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
    console.log(testPatients);
    servletRequestPost("./chartservlet?function=saveTestCases", {"headings": headings, "testCases": testPatients});
}

Element.prototype.remove = function() {
    this.parentElement.removeChild(this);
};

function createTestCases(){
    headings = JSON.parse(servletRequest("./chartservlet?function=getTestTableHeadings"));
    getTestCasesTableCode(headings.retrievedata, headings.medicalActions, null);
}

function importTestCases(){}

function startTests(){
    servletRequest("./chartservlet?function=translateJS");
}

function stopTests(){}

function addNewTestCase(){
    let newRow = document.getElementById("testcases").insertRow();
    let newCell = newRow.insertCell(0);
    let nextId = document.getElementById("testcases").rows.length - 1;
    newCell.appendChild(document.createTextNode(nextId));
    [...Array(nrVarsTable)].forEach((e) => newRow.insertCell(-1));
    [...Array(outcomesTable)].forEach((outcome) => {
        let content = `<input type="checkbox" id="${outcome}_${nextId}" name="${outcome}">`;
        newCell = newRow.insertCell(-1);
        newCell.appendChild(parser.parseFromString(content, 'text/html').body.firstChild);
    });
}

//function createTestCases(){
//    
//    
//    generateTestcasesTableCode(variables, possibleValues, possibleOutcomes
//}