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

let parser = new DOMParser();

const infoBoxProperties = {
    warning: {animation: 'easeOutBounce', cls: "edit-notify"},
    warningKeepOpen: {animation: 'easeOutBounce', cls: "edit-notify", keepOpen: true},
    success: {animation: 'easeOutBounce', cls: "save-success"}
};
const flowchartImageCodes = {
    start: "<span class='far fa-play-circle flow-icon'></span>",
    end: "<span class='far fa-stop-circle flow-icon'></span>",
    subroutine: "<span class='mif-link mif-3x flow-icon'></span>",
    conditional: "<span class='mif-flow-branch mif-3x flow-icon'></span>",
    loop: "<span class='mif-loop mif-3x flow-icon'></span>",
    retrievedata: "<span class='fas fa-file-medical flow-icon'></span>",
    newProcedure:"<span class='fas fa-procedures flow-icon'></span>",
    orderLabs: "<span class='fas fa-microscope flow-icon'></span>",
    newPrescription: "<span class='fas fa-prescription-bottle-alt flow-icon'></span>",
    addDiagnosis: "<span class='fas fa-diagnoses flow-icon'></span>",
    newVaccination: "<span class='fas fa-syringe flow-icon'></span>",
    addNotes: "<span class='fas fa-pencil-alt flow-icon'></span>",
    questionMark: "<span class='mif-question mif-3x flow-icon'></span>",
    returnValue: "<span class='mif-keyboard-return mif-3x flow-icon'></span>"
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
const chartItemTypesBasic = {
    end: "End",
    subroutine: "Add Subroutine",
    conditional: "If-Else",
    retrievedata: "Retrieve Medical Data",
    returnValue: "Return Value"
};
const chartItemMedicalActions = {
    newProcedure: "New Procedure",
    orderLabs: "Order Labs",
    newPrescription: "New Prescription",
    addDiagnosis: "Add Diagnosis",
    newVaccination: "New Vaccination",
    addNotes: "Add Medical Notes"
};
let conditionalPosValue = null;
let conditionalNegValue = null;
let conditionalVarValue = null;
const selectBoxCodePost = '</select>';
let retrievedData = {"singulars":[], "plurals":[], "subroutines":[]};
const clickToDefine = "Double-click to define";
let elementToDefine = null;
let conditionalNextElements = null;
let endLinesToAdd = [];
const elements = {
    start: "start",
    end: "end",
    subroutine: "subroutine",
    conditional: "conditional",
    retrievedata: "retrievedata",
    loop: "loop",
    newProcedure: "newProcedure",
    orderLabs: "orderLabs",
    newPrescription: "newPrescription",
    addDiagnosis: "addDiagnosis",
    newVaccination: "newVaccination",
    addNotes: "addNotes",
    questionMark: "questionMark",
    returnValue: "returnValue"
};
let hasErrors = false;

window.addEventListener('load', function () {
    maxX = document.getElementsByClassName("chartarea")[0].getBoundingClientRect().width;
    let hasProjectOpened = JSON.parse(servletRequest("./chartservlet?function=hasProjectOpened")).hasProjectOpened;
    let result = JSON.parse(servletRequest("./chartservlet?function=state"));
    console.log(result)
    if (hasProjectOpened){
        if (result.state.length < 1) {
            addStart();
        } 
        else {
            drawChart(result.state, result.endLines);
            showErrorsWarning(result.state);
        }
        document.getElementById("section-model").classList.remove("disabled");
        document.getElementById("section-test").classList.remove("disabled");
    }
    else {
        document.getElementById("section-model").classList.add("disabled");
        document.getElementById("section-test").classList.add("disabled");
        displayQueryInfoBox(infoBoxProperties.warningKeepOpen, "Warning: No project", "No project opened. Click File -> Open Project to open a project or File -> New Project to create a project");
    }
});

document.addEventListener('keydown', function(event) {
    if (event.keyCode === 46){
        if (selectedItemId !== -1){ deleteItem(selectedItemId); }
    }
    
//    if(event.keyCode === 17) isCtrl=true;
//    if(event.keyCode === 83 && isCtrl === true) {
//        event.preventDefault();
//        servletRequest("http://localhost:8080/katool/chartservlet?function=save");
//        displayQueryInfoBox(infoBoxProperties.success, "Success", "Project saved");
//    }
});

document.addEventListener('scroll', function(){ redrawLines(); });

function redrawLines(){
    lines.forEach((line) => { line.position(); });
}

function getLineStyle(stepType, label = null) {
    if (stepType === elements.end) return {"color": "dimGray", "size": 4, path: "fluid", startSocket: "right"};
    else if (label === null || label === "null") return {"color": "dimGray", "size": 4, startSocket: "right", endSocket: "left", startSocketGravity: 1, path:"fluid"};
    else return {"color": "dimGray", "size": 4, endLabel: LeaderLine.pathLabel(label), startSocket: "right", endSocket: "left", startSocketGravity: 1, path:"fluid"};
}

function getRandom(max){
    return (Math.floor(Math.random() * max));
}

function getRandomId() {
    return "a" + getRandom(9999);
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
    } 
    else {
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

function updateState(newItemArray, isConditional = false, redraw = true){
    if (newItemArray === null) { return; }
    let result = null;
    if (!isConditional){
        newItemArray.forEach((item) => {
            result = JSON.parse(servletRequest(`./chartservlet?function=update&id=${item.id}&type=${item.type}&prevItemId=${item.prevItemId}&caption=${item.caption}&condition=${item.condition}`));
        });
    } 
    else {
        servletRequest(`./chartservlet?function=update&id=${newItemArray[0].id}&type=${newItemArray[0].type}&prevItemId=${newItemArray[0].prevItemId}&caption=${newItemArray[0].caption}&condition=${newItemArray[0].condition}&isMultipart=true&firstMultipart=true`);
        if (newItemArray[1].type === elements.end && newItemArray[1].caption === null) { servletRequest(`./chartservlet?function=endline&id=${newItemArray[1].prevItemId}&isMultipart=true`); } 
        else { servletRequest(`./chartservlet?function=update&id=${newItemArray[1].id}&type=${newItemArray[1].type}&prevItemId=${newItemArray[1].prevItemId}&caption=${newItemArray[1].caption}&condition=${newItemArray[1].condition}&isMultipart=true`); }
        if (newItemArray[2].type === elements.end && newItemArray[2].caption === null) {
            servletRequest(`./chartservlet?function=endline&id=${newItemArray[2].prevItemId}&isMultipart=true`);
            result = JSON.parse(servletRequest("./chartservlet?function=state"));
        } 
        else { result = JSON.parse(servletRequest(`./chartservlet?function=update&id=${newItemArray[2].id}&type=${newItemArray[2].type}&prevItemId=${newItemArray[2].prevItemId}&caption=${newItemArray[2].caption}&condition=${newItemArray[2].condition}&isMultipart=true&finalMultipart=true`)); }
    }
    if (redraw === true) { drawChart(result.state, result.endLines); }
}

function updateEndLinesList(id, newEndline = false){
    let result = JSON.parse(servletRequest(`./chartservlet?function=endline&id=${id}`));
    let undoAvailable = (result.undo>0)?true:false;
    if (undoAvailable){
        document.getElementById("undoBtn").disabled = false;
    }
}

function addNewStep(stepId, stepType, iconCaption, prevId, options, lowerY = false, condition=null, isRedraw=false){
    if (stepType === elements.retrievedata) { 
        const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
        if (Object.keys(values.singulars).includes(iconCaption)) {
            retrievedData.singulars.includes(iconCaption)?null:retrievedData.singulars.push(iconCaption); 
        } else { // if not singular, then plural as there are no other options
            retrievedData.plurals.includes(iconCaption)?null:retrievedData.plurals.push(iconCaption);
        }
    }
    
    if (prevId !== -1 && prevId === endIconId) { 
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot add", "Cannot add element after end");
        return;
    }
    
    const iconCode = flowchartImageCodes[stepType];
    if (condition !== null && condition !== 'null') { options.endLabel=LeaderLine.pathLabel(condition); }
    if (stepType === elements.end && endIconId !== -1) { // end 
        endElement = document.getElementById(endIconId);
        let newX = Math.min(highestX + 120, maxX - 90);
        endElement.style.left = newX;
        const prevIcon = document.getElementById(prevId);
        const currIcon = document.getElementById(endIconId);
        let line = new LeaderLine(prevIcon.children[0], currIcon.children[0], options);
        lines.push(line);
        linesToEnd.push(line);
        updateEndLinesList(prevId);
        return;
    }
    if (stepType === elements.start && startIcon !== null){
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
    if (stepType === elements.end) { coordinates = {x: Math.min(highestX + 120, maxX - 90), y: lastIconCoordinates.y + 90}; }
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" ondblclick="defineElement(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
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
        if (stepType === elements.end){ 
            linesToEnd.push(line);
        }
    }
    
    if (stepType === elements.start){ 
        startIcon = newStep; 
        document.getElementById("addStartBtn").classList.add("disabled");
    }
    if (stepType === elements.end){ endIconId = stepId; } // do not disable, click again links to existing btn
    return {id: stepId, type: stepType, prevItemId: prevId, caption: iconCaption, condition: condition};
}

function addConditionalStep(stepId, stepType, iconCaption, posValue, stepTypePos, iconCaptionPos, stepTypeNeg, iconCaptionNeg, stepIdPos, stepIdNeg, prevId, isRedraw = false){
    const posIsEndLine = ((stepTypePos === null?true:false) && endIconId !== -1);
    const negIsEndLine = ((stepTypeNeg === null?true:false) && endIconId !== -1);
    const iconCode = flowchartImageCodes[stepType];
    let firstElement = null;
    try { firstElement = JSON.parse(servletRequest("./chartservlet?function=getElement&id=" + stepId)).chartItem; }
    catch (e) {}
    
    if (prevId !== -1 && prevId === endIconId) { 
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot add", "Cannot add element after end");
        return;
    }
    
    if (stepType === elements.start && startIcon !== null){
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
    let newIcon = `<div id="${stepId}" class="flow-icon-div" onclick="setSelectedItem(this.id)" ondblclick="defineElement(this.id)" selectable> ${iconCode} <p class="icon-font">${iconCaption}</p></div>`;
    let newStep = parser.parseFromString(newIcon, 'text/html').body.firstChild;  
    newStep.style.top = lastIconCoordinates.y;
    newStep.style.left = lastIconCoordinates.x + 120;
    
    target.appendChild(newStep);
    setSelectedItem(stepId);
    
    let line = new LeaderLine(prevIcon.children[0], document.getElementById(stepId).children[0], getLineStyle(stepType, firstElement?firstElement.condition:null));
    lines.push(line);
    
    // option 1: no end nodes
    if (stepTypePos !== elements.end && !posIsEndLine && stepTypeNeg !== elements.end && !negIsEndLine){
        addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(stepTypePos, posValue), false, null, isRedraw);
        addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(stepTypeNeg), true, null, isRedraw);
    }
    else if (stepTypePos === elements.end || posIsEndLine) {
        // option 2: positive node is end node
        if (endIconId !== -1 || posIsEndLine){
            // there is already an end node we can connect to 
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], getLineStyle(elements.end));
            lines.push(line);
            linesToEnd.push(line);
            endLinesToAdd.push(newStep.id);
        } 
        else {
            addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(stepTypePos, posValue), false, null, isRedraw);
        }
        // add step for neg
        stepIdNeg = addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(stepTypeNeg), true, null, isRedraw);
    } 
    else if (stepTypeNeg === elements.end || negIsEndLine){ // negative node is end node
        addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(stepTypePos, posValue), false, null, isRedraw);
        if (endIconId !== -1 || negIsEndLine){
            let endNode = document.getElementById(endIconId);
            endNode.style.left = highestX + 120; // move node
            linesToEnd.forEach((line) => {
                line.position();
            });
            let line = new LeaderLine(newStep.children[0], endNode.children[0], getLineStyle(elements.end), true);
            lines.push(line);
            linesToEnd.push(line);
            endLinesToAdd.push(newStep.id);
        } 
        else {
            addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(stepTypeNeg), true, null, isRedraw);
        }
    }
    
    return [{id: stepId, type: stepType, prevItemId: prevId, caption: iconCaption, condition: firstElement?firstElement.condition:null},
        {id:stepIdPos, type:stepTypePos, prevItemId: stepId, caption: iconCaptionPos, condition: posValue},
        {id:stepIdNeg, type:stepTypeNeg, prevItemId: stepId, caption: iconCaptionNeg, condition: null}];
}

function drawChart(state, endlines){ //check for number of lines at conditional. seems possible to have three? (including endline that is NOT documented!!)
    lines = [];
    linesToEnd = [];
    startIcon = null;
    endIconId = -1;
    lowestY = 0;
    highestX = 0;
    selectedItemId = -1;
    prevId = -1;
    document.getElementsByClassName("chartarea")[0].innerHTML = '';
    
    let undoResult = JSON.parse(servletRequest("./chartservlet?function=undoSize")).size;
    let redoResult = JSON.parse(servletRequest("./chartservlet?function=redoSize")).size;
    const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
    const undoAvailable = (undoResult>0)?true:false;
    const redoAvailable = (redoResult>0)?true:false;
    if (undoAvailable){
        document.getElementById("undoBtn").disabled = false;
    }
    if (redoAvailable){
        document.getElementById("redoBtn").disabled = false;
    }
    
    let oldLines = document.getElementsByClassName("leader-line");
    for (let i = oldLines.length - 1; i >= 0; i--){ // live list, if not iterated in reverse, nothing gets deleted
        oldLines[i].remove();
    }
    
    if (state.length === 0) { document.getElementById("addStartBtn").disabled = false; }

    let conditionalIds = [];
    let endElement = null;
    console.log(state);
    for (let i = 0; i < state.length; i++){
        let item = state[[i]];
        if (!conditionalIds.includes(item.prevItemId)) {
            if (item.type === elements.start) { addNewStep(item.id, item.type, item.caption, -1, condition = item.condition, false, null, isRedraw=true); }
            else if (item.type === elements.end) { endElement = item; }
            else if (item.type === elements.conditional){
                let conditionalData = [item];
                conditionalIds.push(item.id);
                state.forEach((nextItem) => {
                    if (nextItem.prevItemId === item.id) { conditionalData.push(nextItem); }
                });
                if (conditionalData.length < 3) { 
                    if (conditionalData.length === 2 && (endlines.includes(item.id) || getEndIconPrevItemId(state) === item.id)) {
                        if (conditionalData[1].condition === null || conditionalData[1] === "null") {
                            // if branch is endline
                            addConditionalStep(conditionalData[0].id, elements.conditional, conditionalData[0].caption, null, null, null, conditionalData[1].type, conditionalData[1].caption, null, conditionalData[1].id, conditionalData[0].prevItemId, true);
                        } else {
                            // else branch is endline
                            addConditionalStep(conditionalData[0].id, elements.conditional, conditionalData[0].caption, conditionalData[1].condition, conditionalData[1].type, conditionalData[1].caption, null, null, conditionalData[1].id, null, conditionalData[0].prevItemId, true);
                        }
                    } else {
                        console.error("conditional incomplete!");
                        continue;
                    }
                } 
                else {
                    addConditionalStep(conditionalData[0].id, elements.conditional, conditionalData[0].caption, conditionalData[1].condition, conditionalData[1].type, conditionalData[1].caption, conditionalData[2].type, conditionalData[2].caption, conditionalData[1].id, conditionalData[2].id, conditionalData[0].prevItemId, true);   
                } 
                conditionalData.shift();
                conditionalData = conditionalData.filter((item) => { return item.type === elements.conditional; });
            }
            else {
                if (item.type === elements.retrievedata){
                    if (values.singulars[item.caption]) { if (!retrievedData.singulars.includes(item.caption)) { retrievedData.singulars.push(item.caption); }}
                    else if (values.plurals[item.caption]) { if (!retrievedData.plurals.includes(item.caption)) { retrievedData.plurals.push(item.caption); }}
                } else if (item.type === elements.subroutine) { if (!retrievedData.subroutines.includes(item.caption)) { retrievedData.subroutines.push(item.caption); }}
                addNewStep(item.id, item.type, item.caption, item.prevItemId, getLineStyle(item.type, item.condition), false, null, isRedraw=true); }
        }
    }
    if (endElement) { addNewStep(endElement.id, endElement.type, endElement.caption, endElement.prevItemId, getLineStyle(endElement.type), false, null, isRedraw=true); }
    if (endlines.length > 0) {
        if (endIconId === -1){
            addNewStep(getRandomId(), elements.end, "End", endlines[0], getLineStyle(elements.end), false, null, isRedraw=true);
        }
        let endIcon = document.getElementById(endIconId);
        endlines.forEach((id) => {
            if (id !== endIconId) {
                let prevIcon = document.getElementById(id);
                let line = new LeaderLine(prevIcon.children[0], endIcon.children[0], getLineStyle(elements.end));
                lines.push(line);
                linesToEnd.push(line);
            }
        });
        let newX = Math.min(highestX + 120, maxX - 90);
        endIcon.style.left = newX;
        lines.forEach((line) => {
            line.position();
        });
    }
    console.log(endlines);
    showErrorsWarning(state);
}

function getEndIconPrevItemId(state){
    let prevItemId = -1;
    state.forEach(item => { if (item.type === elements.end) { prevItemId = item.prevItemId; } });
    return prevItemId;
}

function addStart(){ 
    let newItem = addNewStep(getRandomId(), elements.start, "Start", -1, getLineStyle(elements.start));
    updateState([newItem], false);
}

function addStop(){ 
    const itemHasNext = JSON.parse(servletRequest(`./chartservlet?function=hasNext&id=${selectedItemId}`)).hasNext;
    if (itemHasNext)
    {
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot insert end", "Cannot add insert between elements");
    } else {
        let newItem = addNewStep(getRandomId(), elements.end, "End", selectedItemId, getLineStyle(elements.end)); 
        if (newItem !== undefined) { updateState([newItem], false); }
    }
}

function addLoop(){
     let newItem = addNewStep(elements.loop, "");
     updateState([newItem], false);
}

function setSelectedItem(id){
    if (selectedItemId !== -1){
        document.getElementById(selectedItemId).classList.remove("highlight");
    }
    if (selectedItemId !== id){
        selectedItemId = id;
        document.getElementById(selectedItemId).classList.add("highlight");
    }
}

function deleteItem(id){
    let item = JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${id}`)).chartItem;
    let state = JSON.parse(servletRequest(`./chartservlet?function=state`)).state;
    if (item.type === elements.start) {
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot delete", "Cannot delete start element");
        return;
    }
    if (item.type === elements.retrievedata) {
        retrievedData.singulars = retrievedData.singulars.filter(el => el !== item.caption);
        retrievedData.plurals = retrievedData.plurals.filter(el => el !== item.caption);
    }
    if (item.type === elements.end) { // we have to deal with all involved conditionals
        let element = JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${item.prevItemId}`)).chartItem;
        const endLines = JSON.parse(servletRequest("./chartservlet?function=state")).endLines;
        servletRequest(`./chartservlet?function=delete&id=${item.id}`);
        if (element.type === elements.conditional) { 
            let actions = JSON.parse(servletRequest(`./chartservlet?function=getConditionalActions&id=${element.id}`)).items;
            actions.unshift(element);
            if (actions[1].condition === null || actions[1].condition === "null") {
                actions.splice(1, 0, {"id": getRandomId(), "type": elements.questionMark, "prevItemId": element.id, "caption": clickToDefine, "conditional": item.condition});
            } else {
                actions.push({"id": getRandomId(), "type": elements.questionMark, "prevItemId": element.id, "caption": clickToDefine, "conditional": item.condition});
            }
            updateState(actions, true, false); 
        }
        
        endLines.forEach(lineId => {
            element = JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${lineId}`)).chartItem;
            if (element.type === elements.conditional) { 
                let actions = JSON.parse(servletRequest(`./chartservlet?function=getConditionalActions&id=${element.id}`)).items;
                actions.unshift(element);
                if (actions[1].condition === null || actions[1].condition === "null") {
                    actions.splice(1, 0, {"id": getRandomId(), "type": elements.questionMark, "prevItemId": element.id, "caption": clickToDefine, "conditional": item.condition});
                } else {
                    actions.push({"id": getRandomId(), "type": elements.questionMark, "prevItemId": element.id, "caption": clickToDefine, "conditional": item.condition});
                }
                updateState(actions, true, false); 
            }
        });
        
        const result = JSON.parse(servletRequest("./chartservlet?function=state"));
        drawChart(result.state, result.endLines);
        return;
    }
    // add option: if item is end, find all conditionals with line to end and give them questionmark
    // option 1: the item is a loop
    if (item.type === elements.loop && !item.caption.startsWith("End for") && JSON.parse(servletRequest(`./chartservlet?function=loopHasEnd&caption=${item.caption}`)).hasEnd === true){
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot delete", "Cannot delete for loop with defined end");
        return;
    }
    // option 2: the item to be deleted is not a conditional, nor part of it
    else if (item.type !== elements.conditional && JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${item.prevItemId}`)).chartItem.type !== elements.conditional) {
        let result = JSON.parse(servletRequest(`./chartservlet?function=delete&id=${id}`));
        drawChart(result.state, result.endLines);
        return;
    } // option 3: the item is the first child of an if or an else branch
    else if (item.type !== elements.conditional) {
        if (item.type === elements.retrievedata) {
            retrievedData.singulars = retrievedData.singulars.filter(el => el !== item.caption);
            retrievedData.plurals = retrievedData.plurals.filter(el => el !== item.caption);
        }
        let newItem = {id: item.id, type: "questionMark", prevItemId: item.prevItemId, caption: clickToDefine, condition: item.condition}; 
        updateState([newItem], false);
        return;
    // option 4: the item is a conditional
    } 
    else {
        // option 4a: at least one of the conditional's branches is not undefined
        if (state.some((element) => 
        {
            if (element.prevItemId === id) {
                return element.type !== "questionMark";
            }
        })) {
            displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot delete", "Cannot delete if-else with defined child node(s)");
            return;
        // option 4b: both branches are undefined
        } 
        else {
            let result = JSON.parse(servletRequest(`./chartservlet?function=delete&id=${id}`));
            drawChart(result.state, result.endLines);
        }
    }
    
    selectedItemId = -1;
}

function undo(){
    let result = JSON.parse(servletRequest("./chartservlet?function=undo"));
    drawChart(result.state, result.endLines);
    let undoAvailable = (result.size>0)?true:false;
    if (undoAvailable){
        document.getElementById("undoBtn").disabled = false;
    } else {
        document.getElementById("undoBtn").disabled = true;
    }
}

function redo(){
    let result = JSON.parse(servletRequest("./chartservlet?function=redo"));
    drawChart(result.state, result.endLines);
    let redoAvailable = (result.size>0)?true:false;
    if (redoAvailable){
        document.getElementById("redoBtn").disabled = false;
    } else {
        document.getElementById("redoBtn").disabled = true;
    }
}
// form-related functions

function defineElement(id){
    let chartItem = JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${id}`)).chartItem;
    if (chartItem.type === elements.start || chartItem.type === elements.end){
        displayQueryInfoBox(infoBoxProperties.warning, "Warning: cannot edit", "Cannot edit properties of start and end elements");
    } 
    else {
        elementToDefine = chartItem;
        switch(chartItem.type){
            case elements.subroutine:
                openFormPopup("chart-subroutine-popup", null, chartItem);
                break;
            case elements.conditional:
                openFormPopup("chart-conditional-popup", null, chartItem);
            case elements.loop:
                openFormPopup("chart-forloop-popup", null, chartItem);
                break;
            case elements.retrievedata:
                openFormPopup("chart-retrieve-data-popup", null, chartItem);
                break;
            case elements.newProcedure:
                //fallthrough
            case elements.orderLabs:
                //fallthrough
            case elements.newPrescription:
                //fallthrough
            case elements.addDiagnosis:
                //fallthrough
            case elements.newVaccination:
                //fallthrough
            case elements.addNotes:
                openFormPopup("chart-item-popup", chartItem.type, chartItem);
                break; //action
            case "questionMark":
                redefineQuestionmark(chartItem);
                break;
            case elements.returnValue:
                openFormPopup("chart-return-value-popup", null, chartItem);
        }
    }
}

function redefineQuestionmark(chartItem){
    document.getElementById("questionmark-popup-select").innerHTML = "";
    elementToDefine = chartItem;
    let selectboxCode = "<select data-role='select' id='redefine-questionmark-select' name='redefine-questionmark-select' data-add-empty-value='true' required><optgroup label='Components'>";
    Object.keys(chartItemTypesBasic).forEach((key) => {
        selectboxCode += `<option value="${key}">${chartItemTypesBasic[key]}</option>`;
    });
    selectboxCode += "</optgroup><optgroup label='Medical Actions'>";
    Object.keys(chartItemMedicalActions).forEach((key) => {
        selectboxCode += `<option value="${key}">${chartItemMedicalActions[key]}</option>`;
    });
    selectboxCode += "</optgroup></select>";
    const selectCode = parser.parseFromString(selectboxCode, 'text/html').body.firstChild;
    document.getElementById("questionmark-popup-select").appendChild(selectCode);
    document.getElementsByClassName("questionmark-popup")[0].style.display = "block";
}

function conditionalPosForm(value){
    let target = document.getElementById("conditional-form-group-pos");
    conditionalPosValue = value;
    const val = conditionalNextElements?conditionalNextElements[0].caption:null;
    target.innerHTML = "";
    const captionCode = `<div style="display: -webkit-inline-box"><input type="text" name="statement1-caption" id="statement1-caption" ${val?"value=\""+val+"\"":""} required></div>`;
    const caption = parser.parseFromString(captionCode, 'text/html').body.firstChild;
    const selectSubroutineCode = `<div style="display: -webkit-inline-box"><input type="file" accept="application/json" ${val?"value=\""+val+"\"":""} data-role="file" id="subroutine-conditional-pos-input" data-button-title="<span class='mif-folder'></span>" onSelect="processEmbedFile(files, 'pos')" required></div>`;
    const ifElseCode = "<div id='if-else-specify-later'><i>Specify later</i></div>";
    switch (value){
        case elements.end:
            break;
        case elements.subroutine:
            target.appendChild(parser.parseFromString(selectSubroutineCode, 'text/html').body.firstChild);
            document.getElementById("subroutine-conditional-pos-input").value = conditionalNextElements?conditionalNextElemens[0].caption:"";
            break;
        case elements.retrievedata:
            const code = getRetrieveDataSelectBox("statement1-caption", val);
            const newChild = `<div style="display: -webkit-inline-box">${code}</div>`;
            target.appendChild(parser.parseFromString(newChild, 'text/html').body.firstChild);
            $("#conditional-form-group-pos #data-retrieve-select").value = conditionalNextElements?conditionalNextElements[0].caption:"";
            break;
        case elements.conditional:
            // place branch icon + double click to specify?
            target.appendChild(parser.parseFromString(ifElseCode, 'text/html').body.firstChild);
            break;
        case elements.newProcedure:
            // fallthrough
        case elements.orderLabs:
            // fallthrough
        case elements.newPrescription:
            // fallthrough
        case elements.addDiagnosis:
            // fallthrough
        case elements.newVaccination:
            // fallthrough
        case elements.addNotes:
            //fallthrough
        case elements.returnValue:
            target.appendChild(caption);
            document.getElementById("statement1-caption").innerHTML = conditionalNextElements?conditionalNextElements[0].caption:"";
            break;
        default: 
            break;
    }
}

function conditionalNegForm(value){
    conditionalNegValue = value;
    const val = conditionalNextElements?conditionalNextElements[1].caption:null;
    let target = document.getElementById("conditional-form-group-neg");
    target.innerHTML = "";
    const captionCode = `<div style="display: -webkit-inline-box"><input type="text" name="statement2-caption" id="statement2-caption" ${val?"value=\""+val+"\"":""} required></div>`;
    const caption = parser.parseFromString(captionCode, 'text/html').body.firstChild;
    const selectSubroutineCode = `<div style="display: -webkit-inline-box"><input type="file" accept="application/json" ${val?"value=\""+val+"\"":""} data-role="file" data-button-title="<span class='mif-folder'></span>" onSelect="processEmbedFile(files, 'neg')" required></div>`;
    const ifElseCode = "<div id='if-else-specify-later'><i>Specify later</i></div>";
    switch (value){
        case elements.end: 
            break;
        case elements.subroutine:
            target.appendChild(parser.parseFromString(selectSubroutineCode, 'text/html').body.firstChild);
            document.getElementById("subroutine-conditional-neg-input").value = conditionalNextElements?conditionalNextElements[1].caption:"";
            break;
        case elements.retrievedata:
            const code = getRetrieveDataSelectBox("statement2-caption", val);
            const newChild = `<div style="display: -webkit-inline-box">${code}</div>`;
            target.appendChild(parser.parseFromString(newChild, 'text/html').body.firstChild);
            target.getElementById("data-retrieve-select").value = conditionalNextElements?conditionalNextElements[1].caption:"";
            break;
        case elements.conditional:
            target.appendChild(parser.parseFromString(ifElseCode, 'text/html').body.firstChild);
            break;
        case elements.newProcedure:
            // fallthrough
        case elements.orderLabs:
            // fallthrough
        case elements.newPrescription:
            // fallthrough
        case elements.addDiagnosis:
            // fallthrough
        case elements.newVaccination:
            // fallthrough
        case elements.addNotes:
            // fallthrough
        case elements.returnValue:
            target.appendChild(caption);
            document.getElementById("statement2-caption").innerHTML = conditionalNextElements?conditionalNextElements[1].caption:"";
            break;
        default:
            break;
    }
}

function getRetrieveDataSelectBox(name, value=null, setsOnly = false){
    let selectBoxCodeRetrieve = null;
    if (name){
        selectBoxCodeRetrieve = `<select data-role="select" id="data-retrieve-select" name="${name}" data-add-empty-value="true" required>`;
    } 
    else {
        selectBoxCodeRetrieve = `<select data-role="select" id="data-retrieve-select" data-add-empty-value="true" required>`;
    }
    const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
    let singulars = [];
    let plurals = [];
    if (values.singulars) {
        singulars = Object.keys(values.singulars).filter((el) => !retrievedData.singulars.includes(el));
    }
    if (values.plurals) { 
       plurals = Object.keys(values.plurals).filter((el) => !retrievedData.plurals.includes(el)); 
    }
    if (value) {
        if (retrievedData.singulars.includes(value)) { singulars.push(value); }
        else { plurals.push(value); }
    }
    singulars.sort();
    plurals.sort();
    if (!setsOnly) {
        selectBoxCodeRetrieve += '<optgroup label="Singular values">';
        singulars.forEach((key) => {
            selectBoxCodeRetrieve += `<option value="${key}" ${value===key?"selected":""}>${key}</option>`;
        });
        selectBoxCodeRetrieve += '</optgroup>';
    }
    selectBoxCodeRetrieve += '<optgroup label="Value sets">';
    plurals.forEach((key) => {
        selectBoxCodeRetrieve += `<option value="${key}" ${value===key?"selected":""}>${key}</option>`;
    });
    selectBoxCodeRetrieve += '</optgroup>' + selectBoxCodePost;
    return selectBoxCodeRetrieve;
}

function initConditionalPopup(conditionalElement=null){
    let conditionPrefix = null;
    let conditionText = null;
    let actionPos = null;
    let actionNeg = null;
    let actionPosVal = null;
    let actionNegVal = null;
    let variable = null;
    if (conditionalElement!==null && conditionalElement.caption !== clickToDefine) { 
        variable = conditionalElement.caption;
        conditionalNextElements = JSON.parse(servletRequest(`./chartservlet?function=getConditionalActions&id=${conditionalElement.id}`)).items;
        try {
            let matches = (conditionalNextElements[0].condition).match(/(===|<=|>=|<|>|!==|is-in|is-not-in)\s(.+)/);
            conditionPrefix = matches[1];
            conditionText = matches[2];
        } catch (e) {}
        actionPos = conditionalNextElements[0]?conditionalNextElements[0].type:elements.end;
        actionNeg = conditionalNextElements[1]?conditionalNextElements[1].type:elements.end;
        actionPosVal = conditionalNextElements[0]?conditionalNextElements[0].caption:null;
        actionNegVal = conditionalNextElements[1]?conditionalNextElements[1].caption:null; 
        if (actionPos === elements.end && actionPosVal === null) { conditionalNextElements.splice(1, 0, {"id": -1, "type": elements.end, "prevItemId": conditionalElement.id, "caption": null, "condition": null}); }
        if (actionNeg === elements.end && actionNegVal === null) { conditionalNextElements.push({"id": -1, "type": elements.end, "prevItemId": conditionalElement.id, "caption": null, "condition": null}); }
    }
    let target = document.getElementById("conditional-form");
    let selectVarCode = '<select data-role="select" name="data-conditional-var" id="data-retrieve-select" data-add-empty-value="true" required><optgroup label="Singular values">';
    retrievedData.singulars.forEach((el) => { selectVarCode += `<option value="${el}" ${variable===el?"selected":""}>${el}</option>`; });
    selectVarCode += '</optgroup><optgroup label="Value sets">';
    retrievedData.plurals.forEach((el) => { selectVarCode += `<option value="${el}" ${variable===el?"selected":""}>${el}</option>`; });
    selectVarCode += '</optgroup><optgroup label="Subroutine returns">';
    retrievedData.subroutines.forEach((el) => { selectVarCode += `<option value="${el}" ${variable===el?"selected":""}>${el}</option>`; });
    selectVarCode += '</optgroup></select>';
            
    // selectboxes for actions
    let selectBoxCodeConditionalPos = '<select data-role="select" name="data-conditional-pos" id="data-conditional-pos" data-add-empty-value="true" data-on-change="conditionalPosForm(this.value)" required>';
    let selectBoxCodeConditionalNeg = '<select data-role="select" name="data-conditional-neg" id="data-conditional-neg" data-add-empty-value="true" data-on-change="conditionalNegForm(this.value)" required>';
    selectBoxCodeConditionalPos += "<optgroup label='Components'>";
    selectBoxCodeConditionalNeg += "<optgroup label='Components'>";
    Object.keys(chartItemTypesBasic).forEach((key) => {
        selectBoxCodeConditionalPos += `<option value=${key} ${key===actionPos?"selected":""}>${chartItemTypesBasic[key]}</option>`;
        selectBoxCodeConditionalNeg += `<option value=${key} ${key===actionNeg?"selected":""}>${chartItemTypesBasic[key]}</option>`;
    });
    selectBoxCodeConditionalPos += "</optgroup><optgroup label='Medical Actions'>";
    selectBoxCodeConditionalNeg += "</optgroup><optgroup label='Medical Actions'>";
    Object.keys(chartItemMedicalActions).forEach((key) => {
        selectBoxCodeConditionalPos += `<option value=${key} ${key===actionPos?"selected":""}>${chartItemMedicalActions[key]}</option>`;
        selectBoxCodeConditionalNeg += `<option value=${key} ${key===actionNeg?"selected":""}>${chartItemMedicalActions[key]}</option>`;
    });
    selectBoxCodeConditionalPos += "</optgroup>" + selectBoxCodePost;
    selectBoxCodeConditionalNeg += "</optgroup>" + selectBoxCodePost;
    
    let formCode = `<div class="form-group" id="input-labels">
                        <label id="var">Variable</label>
                        <label id="condition-label">Condition</label>
                        <label id="specify">Specify</label>
                    </div>
                    <div class="form-group">
                        <label>If</label>
                        <div id="conditional-variable">
                            ${selectVarCode}
                        </div>
                        <div id="condition-prefix-div">
                            <select data-role="select" id="condition-prefix" name="condition-prefix" data-add-empty-value="true">
                                <optgroup label="Singular values">
                                    <option ${conditionPrefix==="==="?"selected":""} value="===">===</option>
                                    <option ${conditionPrefix==="<"?"selected":""} value="<"><</option>
                                    <option ${conditionPrefix===">"?"selected":""} value=">">></option>
                                    <option ${conditionPrefix==="<="?"selected":""} value="<="><=</option>
                                    <option ${conditionPrefix===">="?"selected":""} value=">=">>=</option>
                                    <option ${conditionPrefix==="!=="?"selected":""} value="!==">!==</option>
                                </optgroup><optgroup label="Value sets">
                                    <option ${conditionPrefix==="is-in"?"selected":""} value="is-in">Is in</option>
                                    <option ${conditionPrefix==="is-not-in"?"selected":""} value="is-not-in">Is not in</option>
                                </optgroup>
                            </select>
                        </div>
                        <input type="text" name="condition" id="condition" ${conditionText?"value=\""+conditionText+"\"":""}required>

                        <label>Then</label>
                        <div id="conditional-statement1">
                            ${selectBoxCodeConditionalPos}
                        </div>

                        <div id="conditional-form-group-pos"></div>
                    </div>
                    <div class="form-group" id="condition2-form-group">  
                        <div class="condition-error">Condition is not compatible with variable. 'Is in' and 'is not in' can only be used with value sets</div>
                        <label class="condition2-else">Else</label>
                        <div id="conditional-statement2">
                            ${selectBoxCodeConditionalNeg}
                        </div>

                        <div id="conditional-form-group-neg"></div>
                    </div>
                    <div class="form-group">
                        <button class="button" id="conditional-submit-btn">OK</button>
                    </div>`;
    target.innerHTML = formCode;
    actionPos?conditionalPosForm(actionPos):null;
    actionNeg?conditionalNegForm(actionNeg):null;
}

function openFormPopup(popupClass, subclass=null, values=null){
    let popup = document.getElementsByClassName(popupClass)[0];
    switch (popupClass){
        case "chart-item-popup":
            if (values!==null){
                document.getElementById("medical-action-input").value = values.caption;
            }
            medicalActionToAdd = subclass;
            popup.style.display = "initial";
            break;
        case "chart-conditional-popup":
            initConditionalPopup(values);
            popup.style.display = "initial";
            break;
        case "chart-retrieve-data-popup": // this is a file select, for security reasons it is impossible to add a file programmatically
            const code = getRetrieveDataSelectBox("retrieve-data-select-box", values?values.caption:null);
            document.getElementById("retrieve-data-form-group").innerHTML = "<label>Data to retrieve</label>";
            document.getElementById("retrieve-data-form-group").appendChild(parser.parseFromString(code, 'text/html').body.firstChild);
            document.getElementsByClassName("chart-retrieve-data-popup")[0].style.display = "block";
            break;
        case "chart-subroutine-popup":
            document.getElementsByClassName("chart-subroutine-popup")[0].style.display = "block";
            break;
        case "chart-forloop-popup":
            document.getElementsByClassName("for-loop-value-set-div")[0].innerHTML = "";
            document.getElementsByClassName("for-loop-first-action-div")[0].innerHTML = "";
            document.getElementsByClassName("for-loop-first-action-details-div")[0].innerHTML = "";
            let actionType = null;
            if (values) {
                let nextItem = JSON.parse(servletRequest(`./chartservlet?function=getNext&id=${values.id}`)).nextItem;
                actionType = nextItem?nextItem.type:null;
            }
            let valueSetCode = '<select data-role="select" name="for-loop-variable-select-box" id="for-loop-variable-select-box" data-add-empty-value="true" required><optgroup label="Value sets">';
            retrievedData.plurals.forEach((el) => { valueSetCode += `<option value="${el}" ${values?(values.caption===el?"selected":""):""}>${el}</option>`; });
            valueSetCode += '</optgroup><optgroup label="Subroutine returns">';
            retrievedData.subroutines.forEach((el) => { valueSetCode += `<option value="${el}" ${values?(values.caption===el?"selected":""):""}>${el}</option>`; });
            valueSetCode += '</optgroup></select>';
            
            let selectBoxCodeActions = '<select data-role="select" name="data-loop-action" id="data-loop-action" data-add-empty-value="true" data-on-change="setLoopAction(this.value)" required>';
            selectBoxCodeActions += "<optgroup label='Components'>";
            Object.keys(chartItemTypesBasic).slice(1).forEach((key) => {
                selectBoxCodeActions += `<option value=${key} ${key===actionType?"selected":""}>${chartItemTypesBasic[key]}</option>`;
            });
            selectBoxCodeActions += "</optgroup><optgroup label='Medical Actions'>";
            Object.keys(chartItemMedicalActions).forEach((key) => {
                selectBoxCodeActions += `<option value=${key} ${key===actionType?"selected":""}>${chartItemMedicalActions[key]}</option>`;
            });
            selectBoxCodeActions += "</optgroup>" + selectBoxCodePost;

            document.getElementsByClassName("for-loop-value-set-div")[0].appendChild(parser.parseFromString(valueSetCode, 'text/html').body.firstChild);
            document.getElementsByClassName("for-loop-first-action-div")[0].appendChild(parser.parseFromString(selectBoxCodeActions, 'text/html').body.firstChild);
            if (values) {setLoopAction(actionType); }
            document.getElementsByClassName("chart-forloop-popup")[0].style.display = "block";
            break;
        case "chart-return-value-popup":
            document.getElementById("return-value-input").value = values?values.caption:"";
            document.getElementsByClassName("chart-return-value-popup")[0].style.display = "block";
            break;
        case "select-testcasesfile-popup":
            document.getElementsByClassName("select-testcasesfile-popup")[0].style.display = "block";
            break;
        default:
            break;
    }
}

function servletRequest(url){
    const http = new XMLHttpRequest();
    http.open("GET", url, false);
    http.send();
    if (http.readyState === 4 && http.status === 200) {
        return http.responseText;
    }
}

function getFormValueStandard(){
    event.preventDefault();
    let formdata = new FormData(document.getElementById("basic-chartitem-form"));
    formValues = {"caption": formdata.get("caption")};
}

function processEmbedFile(files, location){
    if (location === "pos"){
        conditionalPosEmbed = files[0];
    } 
    else if (location === "neg"){
        conditionalNegEmbed = files[0];
    } 
    else {
        fileToEmbed = files[0];
    }
}

function addMedicalAction(){
    event.preventDefault();
    const message = document.getElementById("medical-action-input").value;
    document.getElementById("medical-action-input").value = '';
    let newItem = null;
    if (elementToDefine === null){
        newItem = addNewStep(getRandomId(), medicalActionToAdd, message, selectedItemId, getLineStyle(medicalActionToAdd));
    } else {
        newItem = elementToDefine;
        newItem.caption = message;
    }
    document.getElementsByClassName("chart-item-popup")[0].style.display = "none";
    updateState([newItem], false);
    elementToDefine = null;
}

function closeAllForms(){
    const popupClasses = ["chart-item-popup", "chart-conditional-popup", "chart-retrieve-data-popup", "chart-subroutine-popup", "questionmark-popup", "chart-forloop-popup", "chart-return-value-popup", "select-testcasesfile-popup"];
    popupClasses.forEach((popupClass) => {
        document.getElementsByClassName(popupClass)[0].style.display = "none";
    });
}

function validateConditionalForm(){
    let returnValue = true;
    try {
        let formdata = new FormData(document.getElementById("conditional-form"));
        const variable = formdata.get("data-conditional-var");
        const conditionPrefix = formdata.get("condition-prefix");
        const condition = formdata.get("condition");
        const dataConditionalPos = formdata.get("data-conditional-pos");
        const dataConditionalNeg = formdata.get("data-conditional-neg");
        let statementPos = null;
        let statementNeg = null;
        switch (dataConditionalPos){
            case elements.subroutine:
                statementPos = document.getElementById("subroutine-conditional-pos-input").files[0].name;
                break;
            case elements.retrievedata:
            case elements.newProcedure:
            case elements.orderLabs:
            case elements.newPrescription:
            case elements.addDiagnosis:
            case elements.newVaccination:
            case elements.addNotes:
            case elements.returnValue:
                statementPos = formdata.get("statement1-caption");
                break;
        }
        
        switch (dataConditionalNeg){
            case elements.subroutine:
                statementNeg = document.getElementById("subroutine-conditional-neg-input").files[0].name;
                break;
            case elements.retrievedata:
            case elements.newProcedure:
            case elements.orderLabs:
            case elements.newPrescription:
            case elements.addDiagnosis:
            case elements.newVaccination:
            case elements.addNotes:
            case elements.returnValue:
                statementNeg = formdata.get("statement2-caption");
                break;
        }
        
        if (variable === undefined || variable === "") {
            returnValue = false;
            document.getElementById("conditional-variable").children[0].classList.add("invalid");
        } else { document.getElementById("conditional-variable").children[0].classList.remove("invalid"); }
        
        if (conditionPrefix === undefined || conditionPrefix === "") {
            returnValue = false;
            document.getElementById("condition-prefix-div").children[0].classList.add("invalid");
        } else { document.getElementById("condition-prefix-div").children[0].classList.remove("invalid"); }
        
        if (condition === undefined || condition === "") {
            returnValue = false;
            document.getElementById("condition").classList.add("invalid");
        } else { document.getElementById("condition").classList.remove("invalid"); }
        
        if (dataConditionalPos === undefined || dataConditionalPos === ""){
            returnValue = false;
            document.getElementById("conditional-statement1").children[0].classList.add("invalid");
        } else { document.getElementById("conditional-statement1").children[0].classList.remove("invalid"); }
        
        if (dataConditionalNeg === undefined || dataConditionalNeg === "") {
            returnValue = false;
            document.getElementById("conditional-statement2").children[0].classList.add("invalid");
        } else { document.getElementById("conditional-statement2").children[0].classList.remove("invalid"); }

        if (dataConditionalPos !== elements.end && dataConditionalPos !== elements.conditional) {
            if (document.getElementById("conditional-form-group-pos").children.length > 0) {
                if (statementPos === "") { 
                    returnValue = false;
                    document.getElementById("conditional-form-group-pos").children[0].children[0].classList.add("invalid");
                } else { document.getElementById("conditional-form-group-pos").children[0].children[0].classList.remove("invalid"); }
            }
        }
        
        if (dataConditionalNeg !== elements.end && dataConditionalNeg !== elements.conditional) {
            if (document.getElementById("conditional-form-group-neg").children.length > 0) {
                if (statementNeg === "") { 
                    returnValue = false;
                    document.getElementById("conditional-form-group-neg").children[0].children[0].classList.add("invalid");
                } else { document.getElementById("conditional-form-group-neg").children[0].children[0].classList.remove("invalid"); }
            }
        }   
    }
    catch (e) { console.log(e) }
    return returnValue;
}
    
function processFormConditional(){
    event.preventDefault();
    if (validateConditionalForm() === false) { return; }
    
    //get data
    let formdata = new FormData(document.getElementById("conditional-form"));
    let variable = formdata.get("data-conditional-var");
    let statement1Caption = null;
    let statement2Caption = null;
    if (conditionalPosValue === elements.subroutine && conditionalNegValue === elements.subroutine){
        statement1Caption = conditionalPosEmbed;
        statement2Caption = conditionalNegEmbed;
    } 
    else if (conditionalPosValue === elements.subroutine){
        statement1Caption = conditionalPosEmbed;
        statement2Caption = formdata.get("statement2-caption");
    } 
    else if (conditionalNegValue === elements.subroutine){
        statement2Caption = conditionalNegEmbed;
        statement1Caption = formdata.get("statement1-caption");
    } 
    else {
        statement1Caption = formdata.get("statement1-caption");
        statement2Caption = formdata.get("statement2-caption");
    }
    
    let conditionPrefix = formdata.get("condition-prefix");
    let conditionValue = formdata.get("condition");
    if (conditionPrefix === "is-in" || conditionPrefix === "is-not-in") { 
        if (retrievedData.singulars.includes(variable)) { 
            document.getElementsByClassName("condition-error")[0].style.visibility = "visible"; 
            return;
        }
    }
    const condition = conditionPrefix + " " + conditionValue;
    
    // process
    if (conditionalPosValue === elements.subroutine){
        statement1Caption = JSON.parse(servletRequest(`./chartservlet?function=file&name=${conditionalPosEmbed.name}`)).mlmname; //let backend know of new file to be included
        if (!retrievedData.subroutines.includes(statement1Caption)) { retrievedData.subroutines.push(statement1Caption); }
    }
    if (conditionalNegValue === elements.subroutine){
        statement2Caption = JSON.parse(servletRequest(`./chartservlet?function=file&name=${conditionalNegEmbed.name}`)).mlmname;
        if (!retrievedData.subroutines.includes(statement2Caption)) { retrievedData.subroutines.push(statement2Caption); }
    }
    
    if (elementToDefine === null) {
        if (conditionalPosValue === elements.end) { statement1Caption = "End"; }
        if (conditionalNegValue === elements.end) { statement2Caption = "End"; }
    } 
    else {
        if (conditionalPosValue === elements.end && (conditionalNextElements || conditionalNextElements[0].caption !== null)) {statement1Caption = "End"; }
        if (conditionalNegValue === elements.end && (conditionalNextElements || conditionalNextElements[1].caption !== null)) {statement2Caption = "End"; } 
    }    
    
    let newSteps = null;
    
    const firstId = elementToDefine?elementToDefine.id:getRandomId();
    const posId = conditionalNextElements?conditionalNextElements[0].id:getRandomId();
    const negId = conditionalNextElements?conditionalNextElements[1].id:getRandomId();
    const prevId = elementToDefine?elementToDefine.prevItemId:selectedItemId;
    
    const redraw = (elementToDefine !== null)?true:false;
    if (conditionalPosValue === elements.conditional && conditionalNegValue === elements.conditional){
        newSteps = addConditionalStep(firstId, elements.conditional, variable, condition, conditionalPosValue, clickToDefine, conditionalNegValue, clickToDefine, posId, negId, prevId, redraw);
    }
    else if (conditionalPosValue === elements.conditional){
        newSteps = addConditionalStep(firstId, elements.conditional, variable, condition, conditionalPosValue, clickToDefine, conditionalNegValue, statement2Caption, posId, negId, prevId, redraw);
    } 
    else if (conditionalNegValue === elements.conditional){
        newSteps = addConditionalStep(firstId, elements.conditional, variable, condition, conditionalPosValue, statement1Caption, conditionalNegValue, clickToDefine, posId, negId, prevId, redraw);
    } 
    else {
        newSteps = addConditionalStep(firstId, elements.conditional, variable, condition, conditionalPosValue, statement1Caption, conditionalNegValue, statement2Caption, posId, negId, prevId, redraw);
    }
    closeAllForms();
    updateState(newSteps, true);
    endLinesToAdd.forEach((id) => {
        updateEndLinesList(id);
    });
    conditionalNextElements = null;
    elementToDefine = null;
}

function processSubroutine(){
    event.preventDefault();
    let caption;
    const http = new XMLHttpRequest();
    http.open("GET", `./chartservlet?function=file&name=${fileToEmbed.name}`, false);
    http.send();
    if (http.readyState === 4 && http.status === 200) {
        caption = JSON.parse(http.responseText).mlmname;
    }
    let newStep;
    if (elementToDefine !== null) {
        newStep = {id: elementToDefine.id, type: elementToDefine.type, prevItemId: elementToDefine.prevItemId, caption: caption, condition: elementToDefine.condition};
    } else {
        newStep = addNewStep(getRandomId(), elements.subroutine, caption, selectedItemId, getLineStyle(elements.subroutine));
    }
    updateState([newStep]);
    if (!retrievedData.subroutines.includes(caption)) { retrievedData.subroutines.push(caption); }
    document.getElementsByClassName("chart-subroutine-popup")[0].style.display = "none";
    elementToDefine = null;
}

function processQuestionmarkForm(){
    event.preventDefault();
    const value = document.getElementById("redefine-questionmark-select").value;
    elementToDefine.type = value;
    elementToDefine.caption = "";
    closeAllForms();
    switch(value){
        case elements.conditional:
            openFormPopup("chart-conditional-popup", null, elementToDefine);
            break;
        case elements.retrievedata:
            openFormPopup("chart-retrieve-data-popup", null, elementToDefine);
            break;
        case elements.subroutine:
            openFormPopup("chart-subroutine-popup", null, elementToDefine);
            break;
        case elements.end:
            elementToDefine.caption = "End";
            updateState([elementToDefine]);
            break;
        case elements.returnValue:
            openFormPopup("chart-return-value-popup", null, elementToDefine);
            break;
        default: //assume medical action
            openFormPopup("chart-item-popup", value, elementToDefine);
            break;
    }
}

function getFormValueRetrieve(){
    event.preventDefault();
    let formdata = new FormData(document.getElementById("retrieve-data-form"));
    const value = formdata.get("retrieve-data-select-box");
    if (elementToDefine) {
        retrievedData.singulars = retrievedData.singulars.filter(el => el !== elementToDefine.caption);
        retrievedData.plurals = retrievedData.plurals.filter(el => el !== elementToDefine.caption);
        elementToDefine.caption = value;
        updateState([elementToDefine]);
    } 
    else {
        let newStep = addNewStep(getRandomId(), elements.retrievedata, value, selectedItemId, getLineStyle(elements.retrievedata));
        updateState([newStep]);
    }
    elementToDefine = null;
    closeAllForms();
}

function processFormForloop(){
    event.preventDefault();
    const formdata = new FormData(document.getElementById("forloop-form"));
    const valueSet = formdata.get("for-loop-variable-select-box");
    const action = formdata.get("data-loop-action");
    let firstAction = null;
    let loopElement = null;   
    
    if (elementToDefine){
        elementToDefine.caption = valueSet;
        loopElement = elementToDefine;
        firstAction = JSON.parse(servletRequest(`./chartservlet?function=getNext&id=${elementToDefine.id}`)).nextItem;
        firstAction.type = action;
    } 
    else {
        loopElement = {id: getRandomId(), type: elements.loop, prevItemId: selectedItemId, caption: valueSet, condition: null};
        firstAction = {id: getRandomId(), type: action, prevItemId: loopElement.id, caption: "", condition: null};
    }
    
    if (firstAction.type === elements.subroutine) { 
        const subroutineCaption = JSON.parse(servletRequest(`./chartservlet?function=file&name=${fileToEmbed.name}`)).mlmname;
        firstAction.caption = subroutineCaption;
        updateState([
            addNewStep(loopElement.id, loopElement.type, loopElement.caption, loopElement.prevItemId, getLineStyle(loopElement.type), condition=loopElement.condition),
            addNewStep(firstAction.id, firstAction.type, firstAction.caption, firstAction.prevItemId, getLineStyle(firstAction.type), condition=firstAction.condition)
        ]);
    }
    else if (firstAction.type === elements.conditional) {
        firstAction.caption = valueSet;
        updateState([addNewStep(loopElement.id, loopElement.type, loopElement.caption, loopElement.prevItemId, getLineStyle(loopElement.type), condition=loopElement.condition)]);
        const conditionalItems = JSON.parse(servletRequest(`./chartservlet?function=getConditionalActions&id=${firstAction.id}`)).items;
        updateState(addConditionalStep(firstAction.id, elements.conditional, firstAction.caption, null, 
            conditionalItems[0]?conditionalItems[0].type:"questionMark", conditionalItems[0]?conditionalItems[0].caption:clickToDefine, 
            conditionalItems[0]?conditionalItems[1].type:"questionMark", conditionalItems[0]?conditionalItems[0].caption:clickToDefine, 
            conditionalItems[0]?conditionalItems[0].id:getRandomId(), conditionalItems[0]?conditionalItems[1].id:getRandomId(), loopElement.id), (elementToDefine!==null)?true:false);
    } 
    else if (firstAction.type === elements.retrievedata) {
        firstAction.caption = document.getElementById("first-action-caption").value;
        updateState([
            addNewStep(loopElement.id, loopElement.type, loopElement.caption, loopElement.prevItemId, getLineStyle(loopElement.type), false, loopElement.condition, (elementToDefine!==null)?true:false),
            addNewStep(firstAction.id, firstAction.type, firstAction.caption, firstAction.prevItemId, getLineStyle(firstAction.type), false, firstAction.condition, (elementToDefine!==null)?true:false)
        ]);
    } 
    else { //medicalAction
        firstAction.caption = document.getElementById("first-action-caption").value;
        updateState([
            addNewStep(loopElement.id, loopElement.type, loopElement.caption, loopElement.prevItemId, getLineStyle(loopElement.type), false, loopElement.condition, (elementToDefine!==null)?true:false),
            addNewStep(firstAction.id, firstAction.type, firstAction.caption, firstAction.prevItemId, getLineStyle(firstAction.type), false, firstAction.condition, (elementToDefine!==null)?true:false)
        ]);
    }
    elementToDefine = null;
    closeAllForms();
}

function setLoopAction(value){
    document.getElementsByClassName("for-loop-first-action-details-div")[0].innerHTML = "";
    let target = document.getElementsByClassName("for-loop-first-action-details-div")[0];
    let result = elementToDefine?JSON.parse(servletRequest(`./chartservlet?function=getNext&id=${elementToDefine.id}`)).nextItem:null;
    const val = result?result.caption:null;
    target.innerHTML = "";
    const captionCode = `<div style="display: -webkit-inline-box"><input type="text" name="first-action-caption" id="first-action-caption" ${val?"value=\""+val+"\"":""} required></div>`;
    const caption = parser.parseFromString(captionCode, 'text/html').body.firstChild;
    const selectSubroutineCode = `<div style="display: -webkit-inline-box"><input type="file" accept="application/json" ${val?"value=\""+val+"\"":""} data-role="file" id="subroutine-loop-input" data-button-title="<span class='mif-folder'></span>" onSelect="processEmbedFile(files, 'loop')" required></div>`;
    const ifElseCode = "<div id='if-else-specify-later'><i>Specify later</i></div>";
    switch (value){
        case elements.end:
            break;
        case elements.subroutine:
            target.appendChild(parser.parseFromString(selectSubroutineCode, 'text/html').body.firstChild);
            if (conditionalNextElements !== null) document.getElementById("subroutine-loop-input").value = val;
            break;
        case elements.retrievedata:
            const code = getRetrieveDataSelectBox("first-action-caption", val);
            const newChild = `<div style="display: -webkit-inline-box">${code}</div>`;
            target.appendChild(parser.parseFromString(newChild, 'text/html').body.firstChild);
            if (val !== null) $("#for-loop-first-action-details-div #data-retrieve-select").value = val;
            break;
        case elements.conditional:
            // place branch icon + double click to specify?
            target.appendChild(parser.parseFromString(ifElseCode, 'text/html').body.firstChild);
            break;
        case elements.newProcedure:
            // fallthrough
        case elements.orderLabs:
            // fallthrough
        case elements.newPrescription:
            // fallthrough
        case elements.addDiagnosis:
            // fallthrough
        case elements.newVaccination:
            // fallthrough
        case elements.addNotes:
            // fallthrough
        case elements.returnValue:
            target.appendChild(caption);
            if (val !== null) document.getElementById("first-action-caption").innerHTML = val;
            break;
        default: 
            break;
    }
}

function endForLoop(){
    let caption = JSON.parse(servletRequest(`./chartservlet?function=getClosestLoopStart&prevItemId=${selectedItemId}`)).caption;
    updateState([addNewStep(getRandomId(), elements.loop, "End for " + caption, selectedItemId, getLineStyle(elements.loop), null)]);
}

function showErrorsWarning(state){
    const warningIcon = "<span class='mif-warning'></span>";
    const errorIcon = "<span class='mif-cancel'></span>";
    let errorViewCode = '<ul data-role="listview" data-view="list" id="error-warning-list">';
    let errorViewPostCode = '</ul>';
    let warnings= [];
    let errors = [];
    state.forEach((item) => {
        switch(item.type){
            case "conditional":
                // retrievedData
                if (!retrievedData.singulars.includes(item.caption) && !retrievedData.plurals.includes(item.caption) && !retrievedData.subroutines.includes(item.caption) && item.caption !== clickToDefine) { errors.push(`Conditional uses data ${item.caption}, but ${item.caption} is not retrieved`); }
                else {
                    if (item.caption !== clickToDefine) {
                        let dataFound = false;
                        let i = 0;
                        while (i < state.length && state[i].id !== item.id && !dataFound) {
                            if (state[i].caption === item.caption) { dataFound = true; }
                            i++;
                        }
                        if (!dataFound) { errors.push(`Conditional uses data ${item.caption}, but at that point ${item.caption} has not yet been retrieved`); }
                    }
                }
                
                // branches
                if (item.caption !== clickToDefine) {
                    let branches = state.filter((el) => el.prevItemId === item.id);
                    try {
                        if (branches[0].type === "questionMark" && branches[1].type === "questionMark") { errors.push(`Conditional with caption ${item.caption} has no actions on either the if or else branch`); }
                        else if (branches[0].type === "questionMark") { warnings.push(`Conditional with caption ${item.caption} has no actions on the if branch`); }
                        else if (branches[1].type === "questionMark") { warnings.push(`Conditional with caption ${item.caption} has no actions on the else branch`); }
                    } catch (e) {}
                } 
                else {
                    errors.push(`Undefined conditional`);
                }
               break;
            case "loop":
                if (!item.caption.startsWith("End for")) {
                    // retrievedData
                    if (!retrievedData.singulars.includes(item.caption) && !retrievedData.plurals.includes(item.caption)) { errors.push(`For loop uses data ${item.caption}, but ${item.caption} is not retrieved`); }
                    else {
                        let dataFound = false;
                        let i = 0;
                        while (i < state.length && state[i].id !== item.id && !dataFound) {
                            if (state[i].caption === item.caption) { dataFound = true; }
                            i++;
                        }
                        if (!dataFound) { errors.push(`For loop uses data ${item.caption}, but at that point ${item.caption} has not yet been retrieved`); }
                    }

                    // action
                    let action = state.filter((el) => el.prevItemId === item.id);
                    if (action.length < 1) { errors.push(`For loop with caption ${item.caption} has no actions`); }

                    // no end
                    let hasEnd = JSON.parse(servletRequest(`./chartservlet?function=loopHasEnd&caption=${item.caption}`)).hasEnd;
                    if (!hasEnd) {
                        errors.push(`For loop with caption ${item.caption} has no end`); 
                    }
                }
                break;
            case "retrievedata":
                let elements = state.filter((el) => (el.caption === item.caption && el.id !== item.id));
                if (elements.length < 1) { warnings.push(`Retrieved data ${item.caption} is unused`); }
                break;
        }
    });
    if (warnings.length > 0 || errors.length > 0) { hasErrors = true; }
    else { hasErrors = false; }
    let target = document.getElementById("error-view");
    target.innerHTML = null;
    errors.forEach((error) => {
        errorViewCode += `<li data-icon="${errorIcon}" data-caption="${error}"></li>`;
    });
    warnings.forEach((warning) => {
        errorViewCode += `<li data-icon="${warningIcon}" data-caption="${warning}"></li>`;
    });
    errorViewCode += errorViewPostCode;
    
    target.appendChild(parser.parseFromString(errorViewCode, 'text/html').body.firstChild);
    document.getElementById("badge-warnings").innerText = warnings.length;
    document.getElementById("badge-errors").innerText = errors.length;
    openStatusbarDbl();
    openStatusbarDbl(); // reload statusbar;
}

function processReturnValueForm(){
    event.preventDefault();
    const returnValue = document.getElementById("return-value-input").value;
    if (elementToDefine) { updateState([addNewStep(elementToDefine.id, elementToDefine.type, returnValue, elementToDefine.prevItemId, getLineStyle(elementToDefine.type), condition = elementToDefine.condition)]); }
    else {
        updateState([addNewStep(getRandomId(), elements.returnValue, returnValue, selectedItemId, getLineStyle(elements.returnValue), condition = null)]);
    }
    closeAllForms();
}

function closeTestView(){
    document.getElementsByClassName("tests")[0].style.display = "none";
    document.getElementById("open-test-cases").classList.remove("highlight");
    document.getElementById("open-test-results").classList.remove("highlight");
}

function displayQueryInfoBox(properties, header, message){
    Metro.notify.create(message, header, properties);
}