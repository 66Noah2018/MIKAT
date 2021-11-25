/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @param {boolean} open
 * @returns {null}
 */
function openCreateProject(open){
    Metro.session.setItem("openCreateProject", open);
//    document.getElementById('file-content').style.display = 'none';
    $("#file-content").hide();
    alert("test")
}

/**
 * 
 * @param {boolean} saveAs
 * @returns {null}
 */
function saveProject(saveAs){
    Metro.session.setItem("saveProject", saveAs)
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
        
