/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let parser = new DOMParser();

const flowchartImageCodes = {
    start: "<span class='far fa-play-circle flow-icon'></span>",
    end: "<span class='far fa-stop-circle flow-icon'></span>",
    subroutine: "<span class='mif-link mif-3x'></span>",
    conditional: "<span class='mif-flow-branch mif-3x'></span>",
    loop: "<span class='mif-loop mif-3x'></span>",
    retrievedata: "<span class='fas fa-file-medical flow-icon'></span>",
    newProcedure:"<span class='fas fa-procedures flow-icon'></span>",
    orderLabs: "<span class='fas fa-microscope flow-icon'></span>",
    newPrescription: "<span class='fas fa-prescription-bottle-alt flow-icon'></span>",
    addDiagnosis: "<span class='fas fa-diagnoses flow-icon'></span>",
    newVaccination: "<span class='fas fa-syringe flow-icon'></span>",
    addNotes: "<span class='fas fa-pencil-alt flow-icon'></span>"
};

let lines = [];
let linesToEnd = [];
let startIcon = null;
let endIconId = -1;
let statusbarExpanded = false;
let selectedItemId = -1;
let lowestY = 0;
let maxX = 0;
let highestX = 0;
let formValues = null;
let medicalActionToAdd = null;

function getLineStyle(standard=true, label=null){
    if (standard) return {"color": "black", "size": 4};
    else return {"color": "black", "size": 4, middleLabel: label, startSocket: "right", endSocket: "left", startSocketGravity: 1, path:"arc"};
}

function getRandom(max){
    return (Math.floor(Math.random() * max));
}

function getRandomId() {
    return "a" + getRandom(1000);
}

function highlight(elToHighlight, elToNormalize){
    if (document.getElementById(elToHighlight).classList.contains("highlight")){
        document.getElementById(elToHighlight).classList.remove("highlight");
    } else {
        document.getElementById(elToHighlight).classList.add("highlight");
    }
    document.getElementById(elToNormalize).classList.remove("highlight");
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
        const prevIcon = document.getElementById(prevId);
        const currIcon = document.getElementById(endIconId);
        let line = new LeaderLine(prevIcon.children[0], currIcon.children[0], options);
        lines.push(line);
        linesToEnd.push(line);
        return;
    }
    if (iconCode === flowchartImageCodes.start && startIcon !== null){
        return;
    }
    let lastIconCoordinates = {x: -90, y: 10};
    if (prevId !== -1){
        console.log(prevId)
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
    setSelectedItem(stepId);
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
    setSelectedItem(stepId);
    
    let line = new LeaderLine(prevIcon.children[0], document.getElementById(stepId).children[0], getLineStyle());
    lines.push(line);
    
    let stepIdPos = -1;
    let stepIdNeg = -1;
    // option 1: no end nodes
    if (iconCodePos !== flowchartImageCodes.end && iconCodeNeg !== flowchartImageCodes.end){
        stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
        stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
    } else if (iconCodePos === flowchartImageCodes.end) {
        // option 2: positive node is end node
        if (endIconId !== -1){
            // there is already an end node we can connect to          
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], getLineStyle(false, posValue));
            lines.push(line);
            linesToEnd.push(line);
        } else {
            stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
        }
        // add step for neg
        stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
    } else if (iconCodeNeg === flowchartImageCodes.end){
        stepIdPos = addNewStep(iconCodePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
        if (endIconId !== -1){
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], getLineStyle(false, negValue), true);
            lines.push(line);
            linesToEnd.push(line);
        } else {
            stepIdNeg = addNewStep(iconCodeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
        }
    }
    return {idPos: stepIdPos, idNeg: stepIdNeg};
}

function buildPrototypeChart(){
    let id = addNewStep(flowchartImageCodes.start, "Start", -1, );
    id = addNewStep(flowchartImageCodes.retrievedata, "CRP", id, getLineStyle());
    let idPosNeg = addConditionalStep(flowchartImageCodes.conditional, "CRP", ">=10", "<10", flowchartImageCodes.retrievedata, "ANA", flowchartImageCodes.end, "End", id);
    addConditionalStep(flowchartImageCodes.conditional, "ANA", "Pos", "Neg", flowchartImageCodes.addNotes, "ANA positive", flowchartImageCodes.end, "End", idPosNeg.idPos); 
}

function addStart(){ 
    maxX = document.getElementsByClassName("chartarea")[0].getBoundingClientRect().width;
//    buildPrototypeChart();
    addNewStep(flowchartImageCodes.start, "Start", -1, getLineStyle()); 
}

function addStop(){ addNewStep(flowchartImageCodes.end, "End", selectedItemId, getLineStyle()); }

function addSubroutine() { 
    // select project using java, read in project. use projectname as caption for subroutine
    addNewStep(flowchartImageCodes.subroutine, ""); 
}

function addIfElse(){
    // create popup to select variable and set values. select next action for both flows
    addNewStep(flowchartImageCodes.conditional, "");
}

//function addLoop(){
//    //popup: start new or end existing loop
//     addNewStep(flowchartImageCodes.loop, "");
//}

function setSelectedItem(id){
    if (selectedItemId !== -1){
        document.getElementById(selectedItemId).classList.remove("highlight");
    }
    if (selectedItemId !== id){
        selectedItemId = id;
        document.getElementById(selectedItemId).classList.add("highlight");
    }
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

// form-related functions

function openFormPopup(popupClass, subclass=null){
    let selectBoxCode = '<select data-role="select" id="data-retrieve-select" data-add-empty-value="true" data-on-item-select="storeDataRetrieveValue(this.value)">';
    const selectBoxCodePost = '</select>';
    let popup = document.getElementsByClassName(popupClass)[0];
    switch (popupClass){
        case "chart-item-popup":
            medicalActionToAdd = flowchartImageCodes[subclass];
            document.getElementsByClassName("chart-item-popup")[0].style.display = "initial";
            break;
        case "chart-conditional-popup":
            break;
        case "chart-retrieve-data-popup":
            const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
            Object.keys(values).forEach((key) => {
                selectBoxCode += `<option value=${key}>${key}</option>`;
            });
            selectBoxCode += selectBoxCodePost;
            popup.appendChild(parser.parseFromString(selectBoxCode, 'text/html').body.firstChild);
            document.getElementsByClassName("chart-retrieve-data-popup")[0].style.display = "initial";
            break;
        case "chart-subroutine-popup":
            break;
        default:
            break;
    }
}

function servletRequest(url){
    const Http = new XMLHttpRequest();
    Http.open("GET", url, false);
    Http.send();
    return Http.responseText;
}

function getFormValueStandard(){
    event.preventDefault();
    let formdata = new FormData(document.getElementById("basic-chartitem-form"));
    formValues = {"caption": formdata.get("caption")};
}

function processEmbedFile(files){
    console.log(files[0]);
}

function addMedicalAction(){
    event.preventDefault();
    const message = document.getElementById("medical-action-input").value;
    document.getElementById("medical-action-input").value = '';
    addNewStep(medicalActionToAdd, message, selectedItemId, getLineStyle());
    document.getElementsByClassName("chart-item-popup")[0].style.display = "none";
}

function storeDataRetrieveValue(value){
    formValues = {"dataToRetrieve": value};
}