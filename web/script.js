/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let parser = new DOMParser();
let recentlyOpened = {};
const testcaseWindowPrologue = "<div data-role='window' data-title='Testcases' data-resizeable='false' class='p-2' id='testcases' data-on-min-click='minimizeWindow()' data-on-max-click='maximizeWindow()' data-draggable='false'>";
const testcaseWindowEpilogue = "</div>";
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
    addNotes: "<span class='fas fa-notes-medical flow-icon'></span>"
};
let selectedItemId = -1;
let lowestY = 0;
let startIcon = null;
let endIcon = null;
let maxX = 0;
let lines = [];

function getRandom(max){
    return (Math.floor(Math.random() * max));
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

function generateTestcasesTableCode(variables, possibleValues){
    // possibleValues: array of arrays with every possible value for a variable (as in neg/pos or a value within a specific range)
    let code = "<table class='table cell-hover table-border row-border cell-border compact' id='testcases'><thead><tr><th>#</th>";
    variables.forEach(element => {
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
        code += "</tr>";
        testPatients[caseNr] = patient;
        caseNr += 1;
    });
    code += "</tbody></table>";
    console.log(testPatients);
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

/**
 * 
 * @param {type} clicked_id
 * @returns {undefined}
 */
function generateTestcases(clicked_id){ //get variables and default values
    table = document.getElementById("testcases");
    if (table === null){
        var spinner = $("#generate_testcases_load")[0];
        spinner.style.display = "block";
        let target = document.getElementsByClassName("index-body")[0];
        const tableCode = generateTestcasesTableCode(["CRP", "ANA"], [[5, 500], ["Pos", "Neg"]]);
        const windowCode = testcaseWindowPrologue + tableCode + testcaseWindowEpilogue;
        target.appendChild(parser.parseFromString(windowCode, 'text/html').body.firstChild);
        spinner.style.display = "none";
        document.getElementById("testcases").addEventListener("input", function(event) {
            console.log(event.target.value);
        }, false);
    }
}

/**
 * 
 * 
 * @returns {undefined}
 */
function minimizeWindow(){
    // if window is project window: hide all elements with class leader-line
    var window = document.getElementById(event.path[3].id);
    window.classList.add("minimized");
    if (window.parentElement.classList[0] === "cell-2"){ 
        return;
    }
    var doc = parser.parseFromString(`<div class='cell-2' id=cell${getRandom(10)}></div>`, 'text/html');
    var cell = doc.body.firstChild;
    var row = document.getElementsByClassName("row")[0];
    row.appendChild(cell);
    var targetList = document.getElementsByClassName("cell-2");
    var target = targetList[targetList.length -1];
    target.appendChild(window);
}

/**
 * 
 * @returns {undefined}
 */
function maximizeWindow(){
    // if window is project window: show all elements with class leader-line
    var window = document.getElementById(event.path[3].id);
    var cellid = window.parentElement.getAttribute('id');
    if (cellid === null){
        return;
    }
    var target = document.getElementsByClassName("index-body")[0];
    target.appendChild(window);
    document.getElementById(cellid).remove();
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

function addNewStep(iconCode, iconCaption, prevId){
    if (iconCode === flowchartImageCodes.end && endIcon !== null) {
        connectToEnd();
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
        console.log(lastIconCoordinates);
    }
    
    let coordinates = lastIconCoordinates;
    coordinates.x += 120;
    if (coordinates.x > (maxX - 120)){
        coordinates.x = 30;
        coordinates.y = lowestY + 90;
    }
    if (coordinates.y > lowestY) { lowestY = coordinates.y; }
    if (iconCode === flowchartImageCodes.end) { coordinates = {x: maxX - 60, y: 10}; }
    let stepId = "a" + getRandom(1000);
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
    let newStep = parser.parseFromString(newIcon, 'text/html').body.firstChild;
    let target = document.getElementsByClassName("window")[0].children[1].children[0];
    newStep.style.top = coordinates.y;
    newStep.style.left = coordinates.x;
    target.appendChild(newStep);
    if (target.children.length >= 2){
        const prevIcon = target.children[target.children.length-2];
        const currIcon = target.children[target.children.length-1];
        let line = new LeaderLine(prevIcon.children[0], currIcon.children[0], {'color':'black', 'size': 4});
        lines.push(line);
    }
    
    if (iconCode === flowchartImageCodes.start){ startIcon = newStep; }
    if (iconCode === flowchartImageCodes.end){ endIcon = newStep; }
    return stepId;
}

function addConditionalStep(iconCode, iconCaption, posValue, negValue, iconCodePos, iconCaptionPos, iconCodeNeg, iconCaptionNeg, prevId){
    const prevIcon = document.getElementById(prevId);
    let lastIconCoordinates = {x: -90, y: 10};
    if (prevId !== -1){
        const prevIcon = document.getElementById(prevId);
        prevX = prevIcon.style.left;
        prevY = prevIcon.style.top;
        lastIconCoordinates = {x: parseInt(prevX.substring(0, prevX.length - 2)), y: parseInt(prevY.substring(0, prevY.length - 2))}; 
        console.log(lastIconCoordinates)
    }
    if (lastIconCoordinates.x + 280 > (maxX - 120)){
        lastIconCoordinates.x = -90;
        lastIconCoordinates.y = lowestY + 90;
    }
    if (lastIconCoordinates.y + 90 > lowestY) { lowestY = lastIconCoordinates.y; }
    console.log(lastIconCoordinates)
    let stepId = "a" + getRandom(1000);
    let stepIdPos = "a" + getRandom(1000);
    let stepIdNeg = "a" + getRandom(1000);
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
    let posIcon = `<div id="${stepIdPos}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCodePos} <p class="icon-font">${iconCaptionPos}</p></div>`;
    let negIcon = `<div id="${stepIdNeg}" class="flow-icon-div" onclick="setSelectedItem(this.id)" selectable> ${iconCodeNeg} <p class="icon-font">${iconCaptionNeg}</p></div>`;
    let target = document.getElementsByClassName("window")[0].children[1].children[0];
    let newStep = parser.parseFromString(newIcon, 'text/html').body.firstChild;  
    newStep.style.top = lastIconCoordinates.y;
    newStep.style.left = lastIconCoordinates.x + 120;
    let posStep = parser.parseFromString(posIcon, 'text/html').body.firstChild;
    posStep.style.top = lastIconCoordinates.y;
    posStep.style.left = lastIconCoordinates.x + 280;
    let negStep = parser.parseFromString(negIcon, 'text/html').body.firstChild;
    negStep.style.top = lastIconCoordinates.y + 90;
    negStep.style.left = lastIconCoordinates.x + 280;
    
    if (iconCodePos === flowchartImageCodes.end) { 
        posStep.style.top = Math.max(Math.floor(lowestY/2), 10);
        posStep.style.left = maxX - 60;
    } else if (iconCodeNeg === flowchartImageCodes.end) {
        negStep.style.top = Math.max(Math.floor(lowestY/2), 10);
        negStep.style.left = maxX - 60;
    }
    
    target.appendChild(newStep);
    target.appendChild(posStep);
    target.appendChild(negStep);
    
    const conditionalIcon = target.children[target.children.length - 3];
    const posStepIcon = target.children[target.children.length - 2];
    const negStepIcon = target.children[target.children.length - 1];
    
    let line = new LeaderLine(prevIcon.children[0], conditionalIcon.children[0], {'color':'black', 'size': 4});
    lines.push(line);
    line = new LeaderLine(conditionalIcon.children[0], posStepIcon.children[0], {'color':'black', 'size': 4, middleLabel: posValue, startSocket: "right"});
    lines.push(line);
    line = new LeaderLine(conditionalIcon.children[0], negStepIcon.children[0], {'color':'black', 'size': 4, middleLabel: negValue, startSocket: "right", endSocket: "left", startSocketGravity: 1, path: "arc"});
    lines.push(line);
    
    return {idPos: stepIdPos, idNeg: stepIdNeg};
}

function buildPrototypeChart(){
    let id = addNewStep(flowchartImageCodes.start, "Start", -1);
    id = addNewStep(flowchartImageCodes.retrievedata, "CRP", id);
    let idPosNeg = addConditionalStep(flowchartImageCodes.conditional, "CRP", ">=10", "<10", flowchartImageCodes.retrievedata, "ANA", flowchartImageCodes.end, "End", id);
    addConditionalStep(flowchartImageCodes.conditional, "ANA", "Pos", "Neg", flowchartImageCodes.addNotes, "ANA positive", flowchartImageCodes.end, "End", idPosNeg.idPos); 
}

function addStart(){ 
    maxX = document.getElementsByClassName("window")[0].children[1].children[0].getBoundingClientRect().width;
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
    console.log(selectedItemId);
}