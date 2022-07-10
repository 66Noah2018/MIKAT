/* 
 * Copyright (C) 2022 RLvanBrummelen
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

let recentlyOpened = {};
let invalidForm = false;
let properties = null;

function valuesToJson(title, mlmname, arden, version, institution, author, specialist, date, validation, purpose, explanation, 
    keywords, citations, links, localMappingFile, standardizedMappingFile, triggers){
    const obj = {"maintenance": {
                    "title": title, 
                    "mlmname": mlmname, 
                    "arden": arden, 
                    "version": version, 
                    "institution": institution, 
                    "author": author, 
                    "specialist": specialist, 
                    "date": date, 
                    "validation": validation}, 
                "library": {
                    "purpose": purpose, 
                    "explanation": explanation, 
                    "keywords": keywords, 
                    "citations": citations, 
                    "links": links}, 
                "localMappingFile": localMappingFile, 
                "standardizedMappingFile": standardizedMappingFile,
                "triggers": triggers};
    return JSON.stringify(obj);
}

function checkDirValidity(){
    event.preventDefault();
    let targetBtn = document.getElementById("checkDirBtn");
    targetBtn.classList.remove("success");
    targetBtn.classList.remove("alert");
    let terminologyFormdata = new FormData(document.getElementById("edit-properties-dir-map"));
    let directory = terminologyFormdata.get("workingDirectory");
    
    const http = new XMLHttpRequest(); // servletrequestpost doesnt work here, loading response somehow takes too long
    http.open("POST", "../chartservlet?function=directoryExists", true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(directory));
    http.onload = function(){ 
        directoryExists = JSON.parse(http.responseText).directoryExists;
        if (directoryExists && directory !== "") { 
            targetBtn.classList.add("success"); 
            document.getElementById("selected-working-dir-edit").style.visibility = 'hidden';
        } 
        else { 
            targetBtn.classList.add("alert"); 
            document.getElementById("selected-working-dir-edit").style.visibility = 'visible';
        }
    };
}

function checkDefaultDirValidity(){
    event.preventDefault();
    let targetBtn = document.getElementById("checkDirBtn");
    targetBtn.classList.remove("success");
    targetBtn.classList.remove("alert");
    let directory = document.getElementById("defaultWorkingDirectory").value;
    
    const http = new XMLHttpRequest(); // servletrequestpost doesnt work here, loading response somehow takes too long
    http.open("POST", "../chartservlet?function=directoryExists", true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(directory));
    http.onload = function(){ 
        directoryExists = JSON.parse(http.responseText).directoryExists;
        if (directoryExists && directory !== "") { 
            targetBtn.classList.add("success"); 
            document.getElementById("selected-working-dir-edit").style.visibility = 'hidden';
        } 
        else { 
            targetBtn.classList.add("alert"); 
            document.getElementById("selected-working-dir-edit").style.visibility = 'visible';
        }
    };
}

function displayErrorMessage(formGroupId, errorMessage){
    document.getElementById(formGroupId).innerText = errorMessage;
    invalidForm = true;
}

function clearErrorMessages(){
    document.getElementById("selected-working-dir-edit").style.visibility = 'hidden';
    const groupsToClear = ["title-group", "mlmname-group", "version-group", "institution-group", "author-group", "date-group", "validation-group", "purpose-group", "explanation-group", "keywords-group",
        "local-group", "standardized-group"];
    groupsToClear.forEach((group) => { document.getElementById(group).innerText = ""; });
}

function editProject(isNew = false){
    if (isNew) { properties = null; }
    invalidForm = false;
    clearErrorMessages();
    if (!document.getElementById("checkDirBtn").classList.contains("success")){
        document.getElementById("selected-working-dir-edit").style.visibility = "visible";
    }
    let maintenanceFormdata = new FormData(document.getElementById("edit-properties-maintenance"));
    let libraryFormdata = new FormData(document.getElementById("edit-properties-library"));
    let terminologyFormdata = new FormData(document.getElementById("edit-properties-dir-map"));
    let localFile = document.getElementById("localMapping").value.split("C:\\fakepath\\")[1];
    let standardizedFile = document.getElementById("standardizedMapping").value.split("C:\\fakepath\\")[1];
    
    let title = maintenanceFormdata.get("title");
    let mlmname = maintenanceFormdata.get("mlmname");
    let arden = maintenanceFormdata.get("arden");
    let version = maintenanceFormdata.get("version");
    let institution = maintenanceFormdata.get("institution");
    let author = maintenanceFormdata.get("author");
    let specialist = maintenanceFormdata.get("specialist");
    let date = maintenanceFormdata.get("date");
    let validation = document.querySelector('input[name="validation"]:checked');
    let purpose = libraryFormdata.get("purpose");
    let explanation = libraryFormdata.get("explanation");
    let keywords = libraryFormdata.get("keywords");
    let citations = libraryFormdata.get("citations");
    let links = libraryFormdata.get("links");
    
    
    if (properties !== null) {
        if (localFile === undefined) { localFile = properties.localMappingFile; }
        if (standardizedFile === undefined) { standardizedFile = properties.standardizedMappingFile; } 
    }
    
    const specialChars = /[`!@#$%^&*()+\-=\[\]{};':"\\|,.<>\/?~]/;
    // form value checks
    
    if (title === "") { displayErrorMessage("title-group", "Title cannot be empty"); }
    if (mlmname === "" || mlmname.length > 80 || specialChars.test(mlmname) || mlmname.indexOf(" ") >= 0) {
        displayErrorMessage("mlmname-group", "MLMname cannot be empty, contain special characters or spaces (_ is allowed), or contain more than 80 characters");
    }
    if (version === "" || version.length > 80){ displayErrorMessage("version-group", "Version cannot be empty or smaller than 0, or contain more than 80 characters"); }
    if (institution === "" || institution.length > 80) { displayErrorMessage("institution-group", "Institution can not be empty or contain more than 80 characters"); }
    if (author === "") { displayErrorMessage("author-group", "Author cannot be empty"); }
    if (date === "") { displayErrorMessage("date-group", "Date cannot be empty"); }
    if (validation === null) { displayErrorMessage("validation-group", "Validation must be assigned a value"); }
    if (purpose === "") { displayErrorMessage("purpose-group", "Purpose cannot be empty"); }
    if (explanation === "") { displayErrorMessage("explanation-group", "Explanation cannot be empty"); }
    if (keywords === "") { displayErrorMessage("keywords-group", "Keywords cannot be empty"); }
    if (localFile === undefined) { displayErrorMessage("local-group", "Please select a local mappings file"); }
    if (standardizedFile === undefined) { displayErrorMessage("standardized-group", "Please select a standardized mappings file"); }
    let triggers = {};
    let tableRows = null;
    if (isNew) { tableRows = document.getElementById("triggers-table-new").rows; }
    else { tableRows = document.getElementById("triggers-table-edit").rows; }
    for (let singleRow of tableRows) {
        let key = singleRow.cells[0].innerText.trim();
        let value = singleRow.cells[1].innerText.trim();
        if (key !== "" && value !== "" && key !== "Trigger name") {
            if (!specialChars.test(key) && key.indexOf(" ") === -1) { triggers[key] = value; }
            else { displayErrorMessage("triggers-group-error", "Every trigger name must have a definition and trigger names cannot contain special characters (_ is allowed) or spaces"); }
        }
    }
    
    if (invalidForm) { return; }
    validation = document.querySelector('input[name="validation"]:checked').value;
    const projectJson = valuesToJson(title, mlmname, arden, version, institution, author, specialist, date, validation, purpose, explanation, keywords, citations, links, localFile, standardizedFile, triggers);
    servletRequestPost("../chartservlet?function=setWorkingDirectory", terminologyFormdata.get("workingDirectory"));
    servletRequestPost("../chartservlet?function=saveProject&isNew=" + isNew, projectJson);
    window.location.href = "../index.html";
}

function getLibraryFormCode(values){
    if (values === []) {
        return `
            <div class="page-content">
            <h2>Library</h2>
            <form id="edit-properties-library">
                <div class="form-group">
                    <label>Purpose</label>
                    <input type="text" class="metro-input" name="purpose" id="purpose">
                </div>
                <div class="form-group error" id="purpose-group"></div>
                <div class="form-group">
                    <label>Explanation</label>
                    <textarea data-role="textarea" name="explanation" id="explanation"></textarea>
                </div>
                <div class="form-group error" id="explanation-group"></div>
                <div class="form-group">To add a keyword, citation, or link, type a value and press enter</div>
                <div class="form-group">
                    <label>Keywords</label>
                    <input type="text" data-role="taginput" class="metro-input" name="keywords" id="keywords" data-tag-trigger="enter">
                </div>
                <div class="form-group error" id="keywords-group"></div>
                <div class="form-group" id="citations-group">
                    <label>Citations</label>
                    <input type="text" data-role="taginput" class="metro-input" name="citations" id="citations" data-tag-trigger="enter">
                </div>
                <div class="form-group" id="links-group">
                    <label>Links</label>
                    <input type="text" data-role="taginput" class="metro-input" name="links" id="links" data-tag-trigger="enter">
                </div>
            </form>
    </div>`;
    } else {
        return `
            <div class="page-content">
                <h2>Library</h2>
                <form id="edit-properties-library">
                    <div class="form-group">
                        <label>Purpose</label>
                        <input type="text" class="metro-input" name="purpose" id="purpose" value=${values[0]}>
                    </div>
                    <div class="form-group error" id="purpose-group"></div>
                    <div class="form-group">
                        <label>Explanation</label>
                        <textarea data-role="textarea" name="explanation" id="explanation">${values[1]}</textarea>
                    </div>
                    <div class="form-group error" id="explanation-group"></div>
                    <div class="form-group">To add a keyword, citation, or link, type a value and press enter</div>
                    <div class="form-group">
                        <label>Keywords</label>
                        <input type="text" data-role="taginput" class="metro-input" name="keywords" id="keywords" value=${values[2]}>
                    </div>
                    <div class="form-group error" id="keywords-group"></div>
                    <div class="form-group" id="citations-group">
                        <label>Citations</label>
                        <input type="text" data-role="taginput" class="metro-input" name="citations" id="citations" value=${values[3]}>
                    </div>
                    <div class="form-group" id="links-group">
                        <label>Links</label>
                        <input type="text" data-role="taginput" class="metro-input" name="links" id="links" value=${values[4]}>
                    </div>
                </form>
            </div>`;
    }
}

function editProjectProperties(){
    properties = JSON.parse(servletRequest("../chartservlet?function=getProjectProperties"));
    if (properties !== null){
        document.getElementById("checkDirBtn").classList.add("success"); 
        document.getElementById("selected-working-dir-edit").style.visibility = 'hidden';
        document.getElementById("title").value = properties.maintenance.title;
        document.getElementById("mlmname").value = properties.maintenance.mlmname;
        document.getElementById("arden").value = properties.maintenance.arden;
        document.getElementById("version").value = properties.maintenance.version;
        document.getElementById("institution").value = properties.maintenance.institution;
        document.getElementById("author").value = properties.maintenance.author;
        document.getElementById("specialist").value = properties.maintenance.specialist;
        document.getElementById("date").value = properties.maintenance.date;
        switch(properties.maintenance.validation){
            case "Production":
                document.getElementById("Production").checked = true;
                break;
            case "Research":
                document.getElementById("Research").checked = true;
                break;
            case "Testing":
                document.getElementById("Testing").checked = true;
                break;
            case "Expired":
                document.getElementById("Expired").checked = true;
                break;
            default:
                break;
        }
        const libFormCode = getLibraryFormCode([properties.library.purpose, properties.library.explanation, properties.library.keywords, properties.library.citations, properties.library.links]);
        let target = document.getElementById("edit-properties-section");
        target.innerHTML = "";
        target.appendChild(parser.parseFromString(libFormCode, 'text/html').body.firstChild);
        document.getElementById("workingDirectory").value = properties.workingDirectory;
        document.getElementById("selectedLocalFile").innerText = "Selected local mappings file: " + properties.localMappingFile;
        document.getElementById("selectedLocalFile").style.visibility = "visible";
        document.getElementById("selectedStandardizedFile").innerText = "Selected standardized mappings file: " + properties.standardizedMappingFile;
        document.getElementById("selectedStandardizedFile").style.visibility = "visible";
        let triggerTarget = document.getElementById("triggers-group");
        triggerTarget.innerHTML = "";
        let newRowsCode = '<table id="triggers-table-edit"><thead><tr><th>Trigger name</th><th>Trigger definition: event { ... }</th></tr></thead><tbody contenteditable>';
        for (const trigger in properties.triggers) {
            newRowsCode += "<tr><td>" + trigger + "</td><td>" + properties.triggers[trigger] + "</td></tr>";
        }
        newRowsCode += "</body></table>";
        triggerTarget.appendChild(parser.parseFromString(newRowsCode, 'text/html').body.firstChild);
    }   
}

/**
 * 
 * 
 * @returns {null}
 */
function openProject(){
    const projectName = document.getElementById("openProjectFileUpload").value.split("path\\")[1];
    processOpen(projectName);
}

function processOpen(projectName){
    let target = document.getElementById("open-project-load");
    target.style.display = "block";
    const http = new XMLHttpRequest(); // servletrequestpost doesnt work here, loading response somehow takes too long
    http.open("POST", "../chartservlet?function=open", true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(projectName));
    http.onload = function(){ 
        let returnValue = JSON.parse(http.responseText).response;
        target.style.display = "none";
        
        if (returnValue === "Unsupported OS"){ Metro.notify.create("The detected OS is not supported", "Warning: unsupported OS", {animation: 'easeOutBounce', cls: "edit-notify"}); }
        else if (returnValue === "Invalid file, not MIKAT" || returnValue === "File is not MIKAT file"){Metro.notify.create("The selected file is not a MIKAT project", "Warning: Not MIKAT", {animation: 'easeOutBounce', cls: "edit-notify"}); }
        else if (returnValue === "Invalid file, no path"){ Metro.notify.create("The selected file could not be found", "Warning: File not found", {animation: 'easeOutBounce', cls: "edit-notify"}); }
        else if (returnValue === "File opened successfully"){
            const hasTestCases = JSON.parse(servletRequest("../chartservlet?function=hasTestCases")).hasTestCases;
            if (!hasTestCases) { document.getElementById("open-test-cases").classList.add("disabled"); }
            window.location.href = "../index.html";
        }
    };
}

/**
 * 
 * @param {type} node
 * @returns {undefined}
 */
function openRecentProject(file){
    let prevOpened = JSON.parse(servletRequest("../chartservlet?function=getPrevOpened"));
    let path = prevOpened.filter((item) => item.fileName === file)[0].path;
    processOpen(path);
}

/**
 * 
 * 
 * @returns {null}
 */
function saveProject(isFixedSidebar = false){
    servletRequest("http://localhost:8080/katool/chartservlet?function=save");
    if (!isFixedSidebar) {
       window.location.href = "http://localhost:8080/katool/index.html"; 
    }
    else {
        parent.window.location.href = "http://localhost:8080/katool/index.html"; 
    }
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

function loadMappings(){
    const buttonsToDisable = ["new-db-singular", "new-db-plural", "save-db-map-changes", "new-stand-singular", "new-stand-plural", "save-term-map-changes"];
    buttonsToDisable.forEach((btn) => document.getElementById(btn).classList.remove("disabled"));
    var spinner = document.getElementById("process-mappings-load");
    let dbMappings;
    let standardMappings;
    spinner.style.display = "block";
    try {
        dbMappings = JSON.parse(servletRequest("../chartservlet?function=localmap"));
        standardMappings = JSON.parse(servletRequest("../chartservlet?function=standardmap"));
    } catch (e) {
        Metro.notify.create("No open project", "Warning: No open project", {animation: 'easeOutBounce', cls: "edit-notify"});
        spinner.style.display = "none";
        buttonsToDisable.forEach((btn) => document.getElementById(btn).classList.add("disabled"));
        return;
    }
    
    const endTableCode = "</tbody></table>";
    
    let singularsTableDb = document.getElementById("database-mapping-table-singular");
    let pluralsTableDb = document.getElementById("database-mapping-table-plural");
    let singularsTableTerm = document.getElementById("standardized-mapping-table-singular");
    let pluralsTableTerm = document.getElementById("standardized-mapping-table-plural");
    
    let singularsTableDbCode = "<table id='database-mapping-singular'><thead><tr><th>Term</th><th>Database query</th></tr></thead><tbody contenteditable>";
    let pluralsTableDbCode = "<table id='database-mapping-plural'><thead><tr><th>Term</th><th>Database query</th></tr></thead><tbody contenteditable>";
    let singularsTableTermCode = "<table id='standardized-mapping-singular'><thead><tr><th>Term</th><th>Standardized term</th></tr></thead><tbody contenteditable>";
    let pluralsTableTermCode = "<table id='standardized-mapping-plural'><thead><tr><th>Term</th><th>Standardized term</th></tr></thead><tbody contenteditable>";
    try {
        Object.keys(dbMappings.singulars).forEach((key) => { singularsTableDbCode += `<tr><td>${key}</td><td>${dbMappings.singulars[key]}</td></tr>`;});
    } catch(e) {}
    try{
        Object.keys(dbMappings.plurals).forEach((key) => { pluralsTableDbCode += `<tr><td>${key}</td><td>${dbMappings.plurals[key]}</td></tr>`; });
    }catch(e) {}
    try{
        Object.keys(standardMappings.singulars).forEach((key) => { singularsTableTermCode += `<tr><td>${key}</td><td>${standardMappings.singulars[key]}</td></tr>`; });
    }catch(e) {}
    try{
        Object.keys(standardMappings.plurals).forEach((key) => { pluralsTableTermCode += `<tr><td>${key}</td><td>${standardMappings.plurals[key]}</td></tr>`; });
    } catch(e) {}
    
    singularsTableDbCode += `<tr><td></td><td></td></tr>`;
    pluralsTableDbCode += `<tr><td></td><td></td></tr>`;
    singularsTableTermCode += `<tr><td></td><td></td></tr>`;
    pluralsTableTermCode += `<tr><td></td><td></td></tr>`; //always have one empty row for user to fill

    singularsTableDbCode += endTableCode;
    pluralsTableDbCode += endTableCode;
    singularsTableTermCode += endTableCode;
    pluralsTableTermCode += endTableCode;
    
    singularsTableDb.appendChild(parser.parseFromString(singularsTableDbCode, 'text/html').body.firstChild);
    pluralsTableDb.appendChild(parser.parseFromString(pluralsTableDbCode, 'text/html').body.firstChild);
    singularsTableTerm.appendChild(parser.parseFromString(singularsTableTermCode, 'text/html').body.firstChild);
    pluralsTableTerm.appendChild(parser.parseFromString(pluralsTableTermCode, 'text/html').body.firstChild);
    spinner.style.display = "none";
}

function saveDbMapChanges(){
    let singulars = {};
    let plurals = {};
    const rowsSingular = document.getElementById("database-mapping-singular").rows;
    const rowsPlural = document.getElementById("database-mapping-plural").rows;
    for (let row of rowsSingular) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            if (key.indexOf(" ") >= 0) { 
                Metro.notify.create("Terms cannot contain spaces", "Warning: Space in term", {animation: 'easeOutBounce', cls: "edit-notify"}); 
                return;
            }
            singulars[key] = value;
        }
    }
    delete singulars.Term;
    
    for (let row of rowsPlural) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            if (key.indexOf(" ") >= 0) { 
                Metro.notify.create("Terms cannot contain spaces", "Warning: Space in term", {animation: 'easeOutBounce', cls: "edit-notify"}); 
                return;
            }
            plurals[key] = value;
        }
    }
    delete plurals.Term;
    
    const mapping = {"singulars": singulars, "plurals": plurals};
    
    servletRequestPost("../chartservlet?function=updateLocalMapping", mapping);
    Metro.notify.create("Database mapping saved", "Success", {animation: 'easeOutBounce', cls: "save-success"});
}

function saveTermMapChanges(){
    let singulars = {};
    let plurals = {};
    const rowsSingular = document.getElementById("standardized-mapping-singular").rows;
    const rowsPlural = document.getElementById("standardized-mapping-plural").rows;
    for (let row of rowsSingular) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            if (key.indexOf(" ") >= 0) { 
                Metro.notify.create("Terms cannot contain spaces", "Warning: Space in term", {animation: 'easeOutBounce', cls: "edit-notify"}); 
                return;
            }
            singulars[key] = value;
        }
    }
    delete singulars.Term;
    
    for (let row of rowsPlural) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            if (key.indexOf(" ") >= 0) { 
                Metro.notify.create("Terms cannot contain spaces", "Warning: Space in term", {animation: 'easeOutBounce', cls: "edit-notify"}); 
                return;
            }
            plurals[key] = value;
        }
    }
    delete plurals.Term;
    
    const mapping = {"singulars": singulars, "plurals": plurals};
    servletRequestPost("../chartservlet?function=updateStandardizedMapping", mapping);
    Metro.notify.create("Standardized terminology mapping saved", "Success", {animation: 'easeOutBounce', cls: "save-success"})
}

function addNewDBMapSingular() {
    let newRow = document.getElementById("database-mapping-singular").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewDbMapPlural() {
    let newRow = document.getElementById("database-mapping-plural").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewStandardMapSingular() {
    let newRow = document.getElementById("standardized-mapping-singular").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewStandardMapPlural() {
    let newRow = document.getElementById("standardized-mapping-plural").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewTrigger(id) {
    event.preventDefault();
    let newRow = document.getElementById(id).insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function servletRequestPost(url, body) {
    const http = new XMLHttpRequest();
    let response = null;
    http.open("POST", url, true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(body));
    http.onload = function(){ response = http.responseText; };
    return response;
}

function showSelectedDir(location){
    event.preventDefault();
    const firstFile = event.target.files[0].webkitRelativePath; //folder/file
    const regex = /(.+)\/.+/gm;
    const folder = regex.exec(firstFile)[1];
    document.getElementById(`selected-working-dir-${location}`).innerText = "Selected working directory: " + folder;
}

function removeClassesFromDirBtn(){
    let dirBtn = document.getElementById("checkDirBtn");
    dirBtn.classList.remove("error");
    dirBtn.classList.remove("success");
}

function showPrevOpened(){
    let prevOpened = JSON.parse(servletRequest("../chartservlet?function=getPrevOpened"));
    let target = document.getElementById("prevOpenedList");
    let listCode = `<ul data-role="listview" data-view="table" data-select-node="true" data-structure='{"fileName": true, "lastEdited": true}'>`;
    target.innerHTML = "";
    prevOpened.forEach((item) => {
        listCode += `<li data-icon="<span class='far fa-file-alt'>"
                    data-caption="${item.fileName}"
                    data-fileName="${item.path}"
                    data-lastEdited="${item.date}"
                    id="fileName" ondblclick='openRecentProject("${item.fileName}")'></li>`;
    });
    target.appendChild(parser.parseFromString(listCode, 'text/html').body.firstChild);
}

function showDefaultWorkingDir(){
    let defaultWorkingDirectory = JSON.parse(servletRequest("../chartservlet?function=getDefaultWorkingDirectory")).defaultWorkingDirectory;
    if (defaultWorkingDirectory === "null") { defaultWorkingDirectory = ""; }
    document.getElementById("workingDirectory").value = defaultWorkingDirectory;
    document.getElementById("checkDirBtn").classList.add("success");
}

function showPreferences(){
    let defaultWorkingDirectory = JSON.parse(servletRequest("../chartservlet?function=getDefaultWorkingDirectory")).defaultWorkingDirectory.replaceAll("\\\\", "\\");
    if (defaultWorkingDirectory !== "null" && defaultWorkingDirectory !== null){
        document.getElementById("defaultWorkingDirectory").value = defaultWorkingDirectory;
        document.getElementById("checkDirBtn").classList.add("success");
    }
}

function savePreferencesChanges(){
    const defaultWorkingDir = document.getElementById("defaultWorkingDirectory").value;
    servletRequestPost("../chartservlet?function=setDefaultWorkingDirectory", defaultWorkingDir);
    Metro.notify.create("Preferences saved", "Success", {animation: 'easeOutBounce', cls: "save-success"})
}

function removeClassesFromDefaultDirBtn() {
    let btn = document.getElementById("checkDirBtn");
    btn.classList.remove("alert");
    btn.classList.remove("success");
}

function setSidebarHeight(){
    let target = document.getElementById("sidebar-fixed");
    target.style.height=document.documentElement.scrollHeight+'px';
}