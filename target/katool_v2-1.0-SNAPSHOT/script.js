/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let parser = new DOMParser();
let recentlyOpened = {};
let statusbarExpanded = false;
let testPatients = {};
const flowchartImageCodes = {
    start: "<span class='far fa-play-circle flow-icon'></span>",
    end: "<span class='far fa-stop-circle flow-icon'></span>",
    subroutine: "<span class='mif-link mif-3x'></span>",
    conditional: "<span class='mif-flow-branch mif-3x'></span>",
    loop: "<span class='mif-loop mif-3x'></span>",
    retrievedata: "<span class='fas fa-file-medical flow-icon'></span>",
    newProcedure:"<span class='fas fa-procedure flow-icon></span>",
    orderLabs: "<span class='fas fa-microscope flow-icon'></span>",
    newPrescription: "<span class='fas fa-prescription-bottle-alt flow-icon'></span>",
    addDiagnosis: "<span class='fas fa-diagnoses flow-icon'></span>",
    newVaccination: "<span class='fas fa-synringe flow-icon'></span>",
    addNotes: "<span class='fas fa-pencil-alt flow-icon'></span>"
};
let selectedItemId = -1;
let lowestY = 0;
let startIcon = null;
let endIconId = -1;
let maxX = 0;
let highestX = 0;
let lines = [];
let linesToEnd = [];
let nrVarsTable = 0;
let outcomesTable = 0;

function getRandom(max){
    return (Math.floor(Math.random() * max));
}

function getRandomId() {
    return "a" + getRandom(1000);
}

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
/**
 * 
 * 
 * @returns {null}
 */
function openProject(){
    // via Java!!
}

/**
 * 
 * @param {type} node
 * @returns {undefined}
 */
function openRecentProject(node){
    // getAttribute('data-filename') always returns the filename of the selected project. project can be opened by searching for full path in array. full path will not be included in listview
    alert(node.getAttribute('data-filename'));
    filename = node.getAttribute('data-filename');
    fullPath = recentlyOpened[filename];
    // some connection to Java code to a) check existence of file (update list if file doesn't exist) and b) load file
}

/**
 * 
 * 
 * @returns {null}
 */
function saveProject(){
    
}

/**
 * 
 * @returns {undefined}
 */
function saveProjectAs(){
    
}

/**
 * 
 * @returns {null}
 */
function editProperties(){}

/**
 * 
 * @returns {null}
 */
function exportAsArden(){}

/**
 * 
 * @returns {null}
 */
function preferences(){}

function showTestCases(){
    highlight('openTestCases', 'openTestResults');
    const currentState = document.getElementsByClassName("tests")[0].style.display;
    if (currentState !== "block"){ document.getElementsByClassName("tests")[0].style.display = "block"; } //make tabs + content visible
    else if (!document.getElementById("openTestCases").classList.contains("highlight")) { //user clicks on the highlighted button (e.g., edit when tab test cases is open), leads to hide tabs + content
        document.getElementsByClassName("tests")[0].style.display = "none"; 
        document.getElementById("openTestCases").classList.remove("highlight");
        document.getElementById("openTestCases").classList.remove("active");
    }
    
    document.getElementById("testCases").classList.add("active"); //switch to correct tab
    document.getElementById("testResults").classList.remove("active");
    document.getElementById("_target_testcases").style.display = "block";
    document.getElementById("_target_results").style.display = "none";
    return;
}

function showTestResults(){
    highlight('openTestResults', 'openTestCases');
    const currentState = document.getElementsByClassName("tests")[0].style.display;
    if (currentState !== "block"){ document.getElementsByClassName("tests")[0].style.display = "block"; }
    else if (!document.getElementById("openTestResults").classList.contains("highlight")) { 
        document.getElementsByClassName("tests")[0].style.display = "none"; 
        document.getElementById("openTestResults").classList.remove("highlight");
        document.getElementById("openTestResults").classList.remove("active");
    }
    
    document.getElementById("testCases").classList.remove("active");
    document.getElementById("testResults").classList.add("active");
    document.getElementById("_target_testcases").style.display = "none";
    document.getElementById("_target_results").style.display = "block";
    return;
}

function highlight(elToHighlight, elToNormalize){
    if (document.getElementById(elToHighlight).classList.contains("highlight")){
        document.getElementById(elToHighlight).classList.remove("highlight");
    } else {
        document.getElementById(elToHighlight).classList.add("highlight");
    }
    document.getElementById(elToNormalize).classList.remove("highlight");
}

/**
 * 
 * @returns {undefined}
 */
function generateTestcases(){ //get variables and default values
    table = document.getElementById("testcases");
    if (table === null){
        var spinner = $("#generate_testcases_load")[0];
        spinner.style.display = "block";
        let target = document.getElementById("_target_testcases");
        const tableCode = generateTestcasesTableCode(["CRP", "ANA"], [[5, 500], ["Pos", "Neg"]],["Log ANA positive"]);
        target.appendChild(parser.parseFromString(tableCode, 'text/html').body.firstChild);
        spinner.style.display = "none";
        showTestCases();
        document.getElementById("_target_testcases").addEventListener("input", function(event) {
            console.log(event.target.value);
        }, false);
    }
    document.getElementById("generateTestcasesBtn").classList.add("disabled");
    document.getElementById("openTestCases").classList.remove("disabled");
    document.getElementById("createTestCases").classList.add("disabled");
}

function openStatusbarDbl(){
    if (statusbarExpanded){
        document.getElementById("statusbar").style.bottom = "0px";
        document.getElementById("error-warning-list").style.display = "none";
    } else {
        document.getElementById("statusbar").style.bottom = "225px";
        document.getElementById("error-warning-list").style.display = "block";
    }
    statusbarExpanded = !statusbarExpanded;    
}

function openErrorStatusbar(context){
    openStatusbarDbl();
    if (statusbarExpanded){
        context.firstChild.style.transform = "rotate(180deg)";
    } else {
        context.firstChild.style.transform = "rotate(0deg)";
    }
}

function addNewStep(iconCode, iconCaption, prevId, options, lowerY = false){
    if (iconCode === flowchartImageCodes.end && endIconId !== -1) {
        endElement = document.getElementById(endIconId);
        let newX = Math.min(highestX + 120, maxX - 90);
        endElement.style.left = newX;
        return;
    }
    if (iconCode === flowchartImageCodes.start && startIcon !== null){
        return;
    }
    let lastIconCoordinates = {x: -90, y: 10};
    if (prevId !== -1){
        const prevIcon = document.getElementById(prevId);
        prevX = prevIcon.style.left;
        prevY = prevIcon.style.top;
        lastIconCoordinates = {x: parseInt(prevX.substring(0, prevX.length - 2)), y: parseInt(prevY.substring(0, prevY.length - 2))};
        if (lowerY) { lastIconCoordinates.y += 90; }       
    }
    
    let coordinates = lastIconCoordinates;
    coordinates.x += 120;
    if (coordinates.x > (maxX - 120)){
        coordinates.x = 30;
        coordinates.y = lowestY + 90;
    }
    if (coordinates.y > lowestY) { lowestY = coordinates.y; }
    if (coordinates.x > highestX) { highestX = coordinates.x; }
    if (iconCode === flowchartImageCodes.end) { coordinates = {x: Math.min(highestX + 120, maxX - 90), y: lastIconCoordinates.y + 90}; }
    let stepId = getRandomId();
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
    let newStep = parser.parseFromString(newIcon, 'text/html').body.firstChild;
    let target = document.getElementsByClassName("chartarea")[0];
    newStep.style.top = coordinates.y;
    newStep.style.left = coordinates.x;
    target.appendChild(newStep);
    if (target.children.length >= 2){
        const prevIcon = document.getElementById(prevId);
        const currIcon = document.getElementById(stepId);
        let line = new LeaderLine(prevIcon.children[0], currIcon.children[0], options);
        lines.push(line);
        if (iconCode === flowchartImageCodes.end){ linesToEnd.push(line); }
    }
    
    if (iconCode === flowchartImageCodes.start){ 
        startIcon = newStep; 
        document.getElementById("addStartBtn").classList.add("disabled");
    }
    if (iconCode === flowchartImageCodes.end){ endIconId = stepId; } // do not disable, click again links to existing btn
    return stepId;
}

function addConditionalStep(iconCode, iconCaption, posValue, negValue, iconCodePos, iconCaptionPos, iconCodeNeg, iconCaptionNeg, prevId){
    if (iconCode === flowchartImageCodes.start && startIcon !== null){
        return;
    }
    const prevIcon = document.getElementById(prevId);
    let lastIconCoordinates = {x: -90, y: 10};
    if (prevId !== -1){
        const prevIcon = document.getElementById(prevId);
        prevX = prevIcon.style.left;
        prevY = prevIcon.style.top;
        lastIconCoordinates = {x: parseInt(prevX.substring(0, prevX.length - 2)), y: parseInt(prevY.substring(0, prevY.length - 2))}; 
        if (lastIconCoordinates.x > highestX) { highestX = lastIconCoordinates.x; }
    }
    let target = document.getElementsByClassName("chartarea")[0];
    
    // add first icon (conditional)
    let stepId = getRandomId();
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
    let newStep = parser.parseFromString(newIcon, 'text/html').body.firstChild;  
    newStep.style.top = lastIconCoordinates.y;
    newStep.style.left = lastIconCoordinates.x + 120;
    
    target.appendChild(newStep);
    let line = new LeaderLine(prevIcon.children[0], document.getElementById(stepId).children[0], {'color':'black', 'size': 4});
    lines.push(line);
    
    let stepIdPos = -1;
    let stepIdNeg = -1;
    // option 1: no end nodes
    if (iconCodePos !== flowchartImageCodes.end && iconCodeNeg !== flowchartImageCodes.end){
        stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, {'color':'black', 'size': 4, middleLabel: posValue, startSocket: "right", endSocket: "left", startSocketGravity: 1});
        stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, {'color':'black', 'size': 4, middleLabel: negValue, startSocket: "right", endSocket: "left", startSocketGravity: 1, path: "arc"}, true);
    } else if (iconCodePos === flowchartImageCodes.end) {
        // option 2: positive node is end node
        if (endIconId !== -1){
            // there is already an end node we can connect to          
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], {'color': 'black', 'size': 4, middleLabel: posValue, startSocket: "right", endSocket: "left", startSocketGravity: 1});
            lines.push(line);
            linesToEnd.push(line);
        } else {
            stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, {'color':'black', 'size': 4, middleLabel: posValue, startSocket: "right", endSocket: "left", startSocketGravity: 1});
        }
        // add step for neg
        stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, {'color':'black', 'size': 4, middleLabel: negValue, startSocket: "right", endSocket: "left", startSocketGravity: 1, path: "arc"}, true);
    } else if (iconCodeNeg === flowchartImageCodes.end){
        stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, {'color':'black', 'size': 4, middleLabel: posValue, startSocket: "right", endSocket: "left", startSocketGravity: 1});
        if (endIconId !== -1){
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], {'color': 'black', 'size': 4, middleLabel: negValue, startSocket: "right", endSocket: "left", startSocketGravity: 1, path:"arc"}, true);
            lines.push(line);
            linesToEnd.push(line);
        } else {
            stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, {'color':'black', 'size': 4, middleLabel: negValue, startSocket: "right", endSocket: "left", startSocketGravity: 1, path: "arc"}, true);
        }
    }
    return {idPos: stepIdPos, idNeg: stepIdNeg};
}

function buildPrototypeChart(){
    let id = addNewStep(flowchartImageCodes.start, "Start", -1, {'color':'black', 'size': 4});
    id = addNewStep(flowchartImageCodes.retrievedata, "CRP", id, {'color':'black', 'size': 4});
    let idPosNeg = addConditionalStep(flowchartImageCodes.conditional, "CRP", ">=10", "<10", flowchartImageCodes.retrievedata, "ANA", flowchartImageCodes.end, "End", id);
    addConditionalStep(flowchartImageCodes.conditional, "ANA", "Pos", "Neg", flowchartImageCodes.addNotes, "ANA positive", flowchartImageCodes.end, "End", idPosNeg.idPos); 
}

function addStart(){ 
    maxX = document.getElementsByClassName("chartarea")[0].getBoundingClientRect().width;
    buildPrototypeChart();
//    addNewStep(flowchartImageCodes.start, "Start"); 
}

function addStop(){ addNewStep(flowchartImageCodes.end, "End"); }

function addSubroutine() { 
    // select project using java, read in project. use projectname as caption for subroutine
    addNewStep(flowchartImageCodes.subroutine, ""); 
}

function addIfElse(){
    // create popup to select variable and set values. select next action for both flows
    addNewStep(flowchartImageCodes.conditional, "");
}

function addLoop(){
    //popup: start new or end existing loop
     addNewStep(flowchartImageCodes.loop, "");
}

function retrieveData(){
    //popup: which data
    addNewStep(flowchartImageCodes.retrievedata, "");
}

function addNewProcedure(){
    //popup: free text or imported template
    addNewStep(flowchartImageCodes.newProcedure, "");
}

function addOrderLabs(){
    // popup: free text or imported template
    addNewStep(flowchartImageCodes.orderLabs, "");
}

function addNewPrescription(){
    //popup: free text, imported template, select list?
    addNewStep(flowchartImageCodes.newPrescription,"");
}

function addDiagnosis(){
    // popup: free text, imported template, select list?
    addNewStep(flowchartImageCodes.addDiagnosis, "");
}

function addNewVaccination(){
    //popup: free text, imported template, select list?
    addNewStep(flowchartImageCodes.newVaccination, "");
}

function addMedicalNotes(){
    // popup: free text or imported template
    addNewStep(flowchartImageCodes.addNotes, "");
}

function setSelectedItem(id){
    selectedItemId = id;
    alert(selectedItemId);
}

function createTestCases(){}

function importTestCases(){}

function startTests(){}

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