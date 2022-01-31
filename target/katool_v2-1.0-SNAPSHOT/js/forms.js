/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//let parser = new DOMParser();

let formValues = null;

function retrieveData(){
    //popup: which data
    let selectBoxCode = '<select data-role="select" id="data-retrieve-select" data-add-empty-value="true" data-on-item-select="storeDataRetrieveValue(this.value)">';
    const selectBoxCodePost = '</select>';
    let popup = document.getElementById("retrieve-data-form-group");
    //fill select box
//    selectBox.innerHTML = ''; //clear old values
    //retrieve values for select from servlet
    const values = JSON.parse(servletRequest('./chartservlet?function=localmap'));
    Object.keys(values).forEach((key) => {
        selectBoxCode += `<option value=${key}>${key}</option>`;
    });
    popup.appendChild(parser.parseFromString(selectBoxCode, 'text/html').body.firstChild);
    document.getElementsByClassName("chart-retrieve-data-popup")[0].style.display = "initial";
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

function getFormValueRetrieve(){
    event.preventDefault();
    console.log(formValues.dataToRetrieve)
    addNewStep(flowchartImageCodes.retrievedata, formValues.dataToRetrieve);
    document.getElementsByClassName("chart-retrieve-data-popup")[0].style.display = "none";
}

function storeDataRetrieveValue(value){
    formValues = {"dataToRetrieve": value};
}