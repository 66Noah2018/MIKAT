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
    addNotes: "<span class='fas fa-pencil-alt flow-icon'></span>",
    questionMark: "<span class='mif-question mif-3x flow-icon'></span>"
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
    retrievedata: "Retrieve Medical Data"
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
let retrievedData = null;
const clickToDefine = "Double-click to define";
let elementToDefine = null;

window.addEventListener('load', function () {
    maxX = document.getElementsByClassName("chartarea")[0].getBoundingClientRect().width;
    let state = JSON.parse(servletRequest("./chartservlet?function=state")).state;
    if (state.length > 0){
        drawChart(state);
    } else {
        addStart();
    }
});

document.addEventListener('keydown', function(event) {
    if (event.keyCode === 46){
        if (selectedItemId !== -1){ deleteItem(selectedItemId); }
    }
});

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

function updateState(newItemArray, isConditional = false){//conditional not added?
    if (newItemArray === null) { return; }
    let state = null;
    let result = null;
    let undoAvailable = null;
    if (!isConditional){
        result = JSON.parse(servletRequest(`./chartservlet?function=update&id=${newItemArray[0].id}&type=${newItemArray[0].type}&prevItemId=${newItemArray[0].prevItemId}&caption=${newItemArray[0].caption}`));
    } else {
        servletRequest(`./chartservlet?function=update&id=${newItemArray[0].id}&type=${newItemArray[0].type}&prevItemId=${newItemArray[0].prevItemId}&caption=${newItemArray[0].caption}&isMultipart=true`);
        servletRequest(`./chartservlet?function=update&id=${newItemArray[1].id}&type=${newItemArray[1].type}&prevItemId=${newItemArray[1].prevItemId}&caption=${newItemArray[1].caption}&condition=${newItemArray[1].condition}&isMultipart=true`);
        result = JSON.parse(servletRequest(`./chartservlet?function=update&id=${newItemArray[2].id}&type=${newItemArray[2].type}&prevItemId=${newItemArray[2].prevItemId}&caption=${newItemArray[2].caption}&condition=${newItemArray[2].condition}&isMultipart=true&finalMultipart=true`));
    }
    state = result.state;
    undoAvailable = (result.undo>0)?true:false;
    console.log(state)
    drawChart(state);
    if (undoAvailable){
        document.getElementById("undoBtn").classList.remove("disabled");
    }
}

function addNewStep(stepId, stepType, iconCaption, prevId, options, lowerY = false, condition=null){
    const iconCode = flowchartImageCodes[stepType];
    if (condition !== null && condition !== 'null') { options.middleLabel=condition; }
    if (stepType === "end" && endIconId !== -1) {
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
    if (stepType === "start" && startIcon !== null){
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
    if (stepType === "end") { coordinates = {x: Math.min(highestX + 120, maxX - 90), y: lastIconCoordinates.y + 90}; }
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
        if (stepType === "end"){ linesToEnd.push(line); }
    }
    
    if (stepType === "start"){ 
        startIcon = newStep; 
        document.getElementById("addStartBtn").classList.add("disabled");
    }
    if (stepType === "end"){ endIconId = stepId; } // do not disable, click again links to existing btn
    
    return {id: stepId, type: stepType, prevItemId: prevId, caption: iconCaption, condition: condition};
}

function addConditionalStep(stepId, stepType, iconCaption, posValue, negValue, stepTypePos, iconCaptionPos, stepTypeNeg, iconCaptionNeg, stepIdPos, stepIdNeg, prevId){
    const iconCode = flowchartImageCodes[stepType];
    
    if (stepType === "start" && startIcon !== null){
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
    
    let line = new LeaderLine(prevIcon.children[0], document.getElementById(stepId).children[0], getLineStyle());
    lines.push(line);
    
    // option 1: no end nodes
    if (stepTypePos !== "end" && stepTypeNeg !== "end"){
        addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
        addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
    } else if (stepTypePos === "end") {
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
            addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
        }
        // add step for neg
        stepIdNeg = addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
    } else if (stepTypeNeg === "end"){ // negative node is end node
        addNewStep(stepIdPos, stepTypePos, iconCaptionPos, stepId, getLineStyle(false, posValue));
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
            addNewStep(stepIdNeg, stepTypeNeg, iconCaptionNeg, stepId, getLineStyle(false, negValue), true);
        }
    }
    
    return [{id: stepId, type: stepType, prevItemId: prevId, caption: iconCaption, condition: null},
        {id:stepIdPos, type:stepTypePos, prevItemId: stepId, caption: iconCaptionPos, condition: posValue},
        {id:stepIdNeg, type:stepTypeNeg, prevItemId: stepId, caption: iconCaptionNeg, condition: negValue}];
}

function drawChart(state){
    lines = [];
    linesToEnd = [];
    startIcon = null;
    endIconId = -1;
    lowestY = 0;
    highestX = 0;
    selectedItemId = -1;
    prevId = -1;
    document.getElementsByClassName("chartarea")[0].innerHTML = '';
    
    let oldLines = document.getElementsByClassName("leader-line");
    for (let i = oldLines.length - 1; i >= 0; i--){ // live list, if not iterated in reverse, nothing gets deleted
        oldLines[i].remove();
    }
    
    if (state.length === 0) { document.getElementById("addStartBtn").classList.remove("disabled"); }
    
    let conditionalEncountered = false;

    let conditionalIds = [];
    for (let i = 0; i < state.length; i++){
        const item = state[[i]];
        if (!conditionalIds.includes(item.prevItemId)) {
            if (item.type === "start") { addNewStep(item.id, item.type, item.caption, -1, condition = item.condition); }
            else if (item.type === "conditional"){
                let conditionalData = [item];
                conditionalIds.push(item.id);
                state.forEach((nextItem) => {
                    if (nextItem.prevItemId === item.id) { conditionalData.push(nextItem); }
                });
                if (conditionalData.length < 3) { 
                    console.error("conditional incomplete!");
                    continue;
                } else {
                    addConditionalStep(conditionalData[0].id, "conditional", conditionalData[0].caption, conditionalData[1].condition, conditionalData[2].condition, conditionalData[1].type, conditionalData[1].caption, conditionalData[2].type, conditionalData[2].caption, conditionalData[1].id, conditionalData[2].id, conditionalData[0].prevItemId);   
                } 
            } else { addNewStep(item.id, item.type, item.caption, item.prevItemId, getLineStyle(), false); }
        }
    }

//    for (let i = 0; i < state.length; i++){
//        const item = state[i];
//        if (item.type === "start") { addNewStep(item.id, item.type, item.caption, -1, condition = item.condition); }
//        else if (item.type === "conditional"){
//            conditionalEncountered = true;
//            conditionalData.push(item);
//        } else {
//            if (conditionalEncountered){
//                if (conditionalData.length < 3){
//                    conditionalData.push(item);
//                    if (conditionalData.length === 3){
//                        conditionalEncountered = false;
//                        addConditionalStep(conditionalData[0].id, "conditional", conditionalData[0].caption, conditionalData[1].condition, conditionalData[2].condition, conditionalData[1].type, conditionalData[1].caption, conditionalData[2].type, conditionalData[2].caption, conditionalData[1].id, conditionalData[2].id, conditionalData[0].prevItemId);
//                    }
//                }
//            } else { addNewStep(item.id, item.type, item.caption, item.prevItemId, getLineStyle(), false); }
//        }
//    }
}

function addStart(){ 
    let newItem = addNewStep(getRandomId(), "start", "Start", -1, getLineStyle());
    updateState([newItem], false);
}

function addStop(){ 
    let newItem = addNewStep(getRandomId(), "end", "End", selectedItemId, getLineStyle()); 
    updateState([newItem], false);
}

function addLoop(){
    //popup: start new or end existing loop
     let newItem = addNewStep("loop", "");
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
    if (item.type === "start") {
        Metro.notify.create("Cannot delete start element", "Warning: cannot delete", {animation: 'easeOutBounce', cls: "edit-notify"});
        return;
    }
    selectedItemId = -1;
    if (item.condition !== null && item.condition !== 'null'){ // this is part of a conditional! we cannot delete it, so replace with questionmark + click to define
        let newItem = {id: item.id, type: "questionMark", prevId: item.prevItemId, caption: clickToDefine, condition: item.condition};
        updateState([newItem], false);
    } else {
       let state = JSON.parse(servletRequest(`./chartservlet?function=delete&id=${id}`)); 
       drawChart(state.state);
    }
}

// form-related functions

function defineElement(id){
    let chartItem = JSON.parse(servletRequest(`./chartservlet?function=getElement&id=${id}`)).chartItem;
    if (chartItem.type === "start" || chartItem.type === "end"){
        Metro.notify.create("Cannot edit properties of start and end elements", "Warning: cannot edit", {animation: 'easeOutBounce', cls: "edit-notify"});
    } else {
        elementToDefine = chartItem;
        switch(chartItem.type){
            case "subroutine":
                break;
            case "conditional":
                break;
            case "loop":
                break;
            case "retrievedata":
                break;
            case "newProcedure":
                //fallthrough
            case "orderLabs":
                //fallthrough
            case "newPrescription":
                //fallthrough
            case "addDiagnosis":
                //fallthrough
            case "newVaccination":
                //fallthrough
            case "addNotes":
                openFormPopup("chart-item-popup", chartItem.type, {caption: chartItem.caption});
                break; //action
            case "questionMark":
                break;
        }
    }
}

function conditionalPosForm(value){
    conditionalPosValue = value;
    document.getElementById("conditional-form-group-pos").innerHTML = "";
    document.getElementById("specify").style.display = "none";
    const captionCode = '<div style="display: -webkit-inline-box"><input type="text" name="statement1-caption" id="statement1-caption" required></div>';
    const caption = parser.parseFromString(captionCode, 'text/html').body.firstChild;
    const selectSubroutineCode = `<div style="display: -webkit-inline-box"><label>Select file</label><input type="file" data-role="file" data-button-title="<span class='mif-folder'></span>" onSelect="processEmbedFile(files, 'pos')" required></div>`;
    const ifElseCode = "<div id='if-else-specify-later'><i>Specify later</i></div>";
    switch (value){
        case "end": 
            break;
        case "subroutine":
            document.getElementById("conditional-form-group-pos").appendChild(parser.parseFromString(selectSubroutineCode, 'text/html').body.firstChild);
            break;
        case "retrievedata":
            const code = getRetrieveDataSelectBox("statement1-caption");
            const newChild = `<div style="display: -webkit-inline-box">${code}</div>`;
            document.getElementById("conditional-form-group-pos").appendChild(parser.parseFromString(newChild, 'text/html').body.firstChild);
            break;
        case "conditional":
            // place branch icon + double click to specify?
            document.getElementById("conditional-form-group-pos").appendChild(parser.parseFromString(ifElseCode, 'text/html').body.firstChild);
            break;
        case "newProcedure":
            // fallthrough
        case "orderLabs":
            // fallthrough
        case "newPrescription":
            // fallthrough
        case "addDiagnosis":
            // fallthrough
        case "newVaccination":
            // fallthrough
        case "addNotes":
            document.getElementById("conditional-form-group-pos").appendChild(caption);
            document.getElementById("specify").style.display = "inline";
            break;
        default: 
            break;
    }
}

function conditionalNegForm(value){
    conditionalNegValue = value;
    document.getElementById("conditional-form-group-neg").innerHTML = "";
    document.getElementById("specify").style.display = "none";
    const captionCode = '<div style="display: -webkit-inline-box"><input type="text" name="statement2-caption" id="statement2-caption" required></div>';
    const caption = parser.parseFromString(captionCode, 'text/html').body.firstChild;
    const selectSubroutineCode = `<div style="display: -webkit-inline-box"><label>Select file</label><input type="file" data-role="file" data-button-title="<span class='mif-folder'></span>" onSelect="processEmbedFile(files, 'neg')" required></div>`;
    const ifElseCode = "<div id='if-else-specify-later'><i>Specify later</i></div>";
    switch (value){
        case "end": 
            break;
        case "subroutine":
            document.getElementById("conditional-form-group-neg").appendChild(parser.parseFromString(selectSubroutineCode, 'text/html').body.firstChild);
            break;
        case "retrievedata":
            const code = getRetrieveDataSelectBox("statement2-caption");
            const newChild = `<div style="display: -webkit-inline-box">${code}</div>`;
            document.getElementById("conditional-form-group-neg").appendChild(parser.parseFromString(newChild, 'text/html').body.firstChild);
            break;
        case "conditional":
            document.getElementById("conditional-form-group-neg").appendChild(parser.parseFromString(ifElseCode, 'text/html').body.firstChild);
            break;
        case "newProcedure":
            // fallthrough
        case "orderLabs":
            // fallthrough
        case "newPrescription":
            // fallthrough
        case "addDiagnosis":
            // fallthrough
        case "newVaccination":
            // fallthrough
        case "addNotes":
            document.getElementById("conditional-form-group-neg").appendChild(caption);
            document.getElementById("specify").style.display = "inline";
            break;
        default:
            break;
    }
}

function getRetrieveDataSelectBox(name){
    let selectBoxCodeRetrieve = null;
    if (name){
        selectBoxCodeRetrieve = `<select data-role="select" id="data-retrieve-select" name="${name}" data-add-empty-value="true" required>`;
    } else {
        selectBoxCodeRetrieve = `<select data-role="select" id="data-retrieve-select" data-add-empty-value="true" required>`;
    }
    const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
    Object.keys(values).forEach((key) => {
        selectBoxCodeRetrieve += `<option value=${key}>${key}</option>`;
    });
    selectBoxCodeRetrieve += selectBoxCodePost;
    return selectBoxCodeRetrieve;
}

function openFormPopup(popupClass, subclass=null, values=null){
    document.getElementById("condition").value = "";
    document.getElementById("conditional-form-group-pos").innerHTML = "";
    document.getElementById("conditional-form-group-neg").innerHTML = "";
    document.getElementById("conditional-variable").innerHTML = "";
    document.getElementById("specify").style.display = "none";
    
    let selectBoxCodeConditionalPos = '<select data-role="select" name="data-conditional-pos" id="data-conditional-pos" data-add-empty-value="true" data-on-change="conditionalPosForm(this.value)" required>';
    let selectBoxCodeConditionalNeg = '<select data-role="select" name="data-conditional-neg" id="data-conditional-neg" data-add-empty-value="true" data-on-change="conditionalNegForm(this.value)" required>';
    
    let popup = document.getElementsByClassName(popupClass)[0];
    switch (popupClass){
        case "chart-item-popup":
            if (values!==null){
                document.getElementById("medical-action-input").value = values.caption;
            }
            medicalActionToAdd = subclass;
            popup.style.display = "initial";
            break;
        case "chart-conditional-popup": //let last field for every row depend on choice in 2nd field --> maybe new popup on submit?
            let selectBoxCode = getRetrieveDataSelectBox("data-conditional-var");
            document.getElementById("conditional-variable").appendChild(parser.parseFromString(selectBoxCode, 'text/html').body.firstChild);
            
            selectBoxCodeConditionalPos += "<optgroup label='Components'>";
            selectBoxCodeConditionalNeg += "<optgroup label='Components'>";
            Object.keys(chartItemTypesBasic).forEach((key) => {
                selectBoxCodeConditionalPos += `<option value=${key}>${chartItemTypesBasic[key]}</option>`;
                selectBoxCodeConditionalNeg += `<option value=${key}>${chartItemTypesBasic[key]}</option>`;
            });
            selectBoxCodeConditionalPos += "</optgroup><optgroup label='Medical Actions'>";
            selectBoxCodeConditionalNeg += "</optgroup><optgroup label='Medical Actions'>";
            Object.keys(chartItemMedicalActions).forEach((key) => {
                selectBoxCodeConditionalPos += `<option value=${key}>${chartItemMedicalActions[key]}</option>`;
                selectBoxCodeConditionalNeg += `<option value=${key}>${chartItemMedicalActions[key]}</option>`;
            });
            selectBoxCodeConditionalPos += "</optgroup>" + selectBoxCodePost;
            selectBoxCodeConditionalNeg += "</optgroup>" + selectBoxCodePost;
            
            document.getElementById("conditional-statement1").innerHTML = "";
            document.getElementById("conditional-statement2").innerHTML = "";
            document.getElementById("conditional-statement1").appendChild(parser.parseFromString(selectBoxCodeConditionalPos, 'text/html').body.firstChild);
            document.getElementById("conditional-statement2").appendChild(parser.parseFromString(selectBoxCodeConditionalNeg, 'text/html').body.firstChild);
            
            if (values !== null){
                document.getElementById("data-retrieve-select").value = values.variable;
                document.getElementById("condition").value = values.condition;
                document.getElementById("data-conditional-pos").value = values.conditionalPos;
                document.getElementById("data-conditional-neg").value = values.conditionalNeg;
            }
            
            popup.style.display = "initial";
            break;
        case "chart-retrieve-data-popup":
            const code = getRetrieveDataSelectBox(null);
            document.getElementById("retrieve-data-form-group").appendChild(parser.parseFromString(code, 'text/html').body.firstChild);
            document.getElementsByClassName("chart-retrieve-data-popup")[0].style.display = "block";
            break;
        case "chart-subroutine-popup":
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
    } else if (location === "neg"){
        conditionalNegEmbed = files[0];
    } else {}
}

function addMedicalAction(){
    event.preventDefault();
    const message = document.getElementById("medical-action-input").value;
    document.getElementById("medical-action-input").value = '';
    let newItem = null;
    if (elementToDefine === null){
        newItem = addNewStep(getRandomId(), medicalActionToAdd, message, selectedItemId, getLineStyle());
    } else {
        newItem = elementToDefine;
        newItem.caption = message;
    }
    document.getElementsByClassName("chart-item-popup")[0].style.display = "none";
    updateState([newItem], false);
}

function closeAllForms(){
    // "chart-forloop-popup"
    const popupClasses = ["chart-item-popup", "chart-conditional-popup", "chart-retrieve-data-popup", "chart-subroutine-popup"];
    popupClasses.forEach((popupClass) => {
        document.getElementsByClassName(popupClass)[0].style.display = "none";
    });
}

function processFormConditional(){
    event.preventDefault();
    
    //get data
    let formdata = new FormData(document.getElementById("conditional-form"));
    let variable = formdata.get("data-conditional-var");
    let statement1Caption = null;
    let statement2Caption = null;
    if (conditionalPosValue === "subroutine" && conditionalNegValue === "subroutine"){
        statement1Caption = conditionalPosEmbed;
        statement2Caption = conditionalNegEmbed;
    } else if (conditionalPosValue === "subroutine"){
        statement1Caption = conditionalPosEmbed;
        statement2Caption = formdata.get("statement2-caption");
    } else if (conditionalNegValue === "subroutine"){
        statement2Caption = conditionalNegEmbed;
        statement1Caption = formdata.get("statement1-caption");
    } else {
        statement1Caption = formdata.get("statement1-caption");
        statement2Caption = formdata.get("statement2-caption");
    }
    let condition1 = formdata.get("condition1");
    let conditionalStatement1 = formdata.get("data-conditional-pos");
    let condition2 = formdata.get("condition2");
    let conditionalStatement2 = formdata.get("data-conditional-neg");
    
    // process
    if (conditionalPosValue === "subroutine"){
        statement1Caption = JSON.parse(servletRequest(`./chartservlet?function=file&name=${conditionalPosEmbed.name}`)).mlmname; //let backend know of new file to be included
    }
    if (conditionalNegValue === "subroutine"){
        statement2Caption = JSON.parse(servletRequest(`./chartservlet?function=file&name=${conditionalNegEmbed.name}`)).mlmname;
    }
    
    if (conditionalPosValue === "end") {statement1Caption = "Stop"; }
    if (conditionalNegValue === "end") {statement2Caption = "Stop"; }
    
    let newSteps = null;
    if (conditionalPosValue === "conditional" && conditionalNegValue === "conditional"){
        newSteps = addConditionalStep(getRandomId(), "conditional", variable, condition1, condition2, conditionalPosValue, clickToDefine, conditionalNegValue, clickToDefine, getRandomId(), getRandomId(), selectedItemId);
    } else if (conditionalPosValue === "conditional"){
        newSteps = addConditionalStep(getRandomId(), "conditional", variable, condition1, condition2, conditionalPosValue, clickToDefine, conditionalNegValue, statement2Caption, getRandomId(), getRandomId(), selectedItemId);
    } else if (conditionalNegValue === "conditional"){
        newSteps = addConditionalStep(getRandomId(), "conditional", variable, condition1, condition2, conditionalPosValue, statement1Caption, conditionalNegValue, clickToDefine, getRandomId(), getRandomId(), selectedItemId);
    } else {
        newSteps = addConditionalStep(getRandomId(), "conditional", variable, condition1, condition2, conditionalPosValue, statement1Caption, conditionalNegValue, statement2Caption, getRandomId(), getRandomId(), selectedItemId);
    }
    document.getElementsByClassName("chart-conditional-popup")[0].style.display = "none";
    updateState(newSteps, true);
}
