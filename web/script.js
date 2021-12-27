/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let parser = new DOMParser();
let recentlyOpened = {};
const testcaseWindowPrologue = "<div data-role='window' data-title='Testcases' data-resizeable='false' class='p-2' id='testcases' data-on-min-click='minimizeWindow()' data-on-max-click='maximizeWindow()' data-draggable='false'>";
const testcaseWindowEpilogue = "</div>";

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
        code += "<tr><td>" + caseNr + "</td>";
        combination.forEach(value => {
            code += "<td>" + value + "</td>";
        });
        code += "</tr>";
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

/**
 * 
 * @param {type} target
 * @returns {undefined}
 */
function generateTestcases(clicked_id){ //get variables and default values
//    let btn = document.getElementById(clicked_id);
//    $("#" + clicked_id).removeAttr("class");
////    $("#" + clicked_id).attr("class", "ribbon-button");
////    console.log(btn.getAttribute("class"));
////    btn.classList.remove("active");
////    btn.classList.remove("js-active");
//    console.log(btn.classList)
    table = document.getElementById("testcases");
    if (table === null){
        var spinner = $("#generate_testcases_load")[0];
        spinner.style.display = "block";
        let target = document.getElementsByClassName("index-body")[0];
        const tableCode = generateTestcasesTableCode(["CRP", "ANA"], [[5, 500], ["Pos", "Neg"]]);
        const windowCode = testcaseWindowPrologue + tableCode + testcaseWindowEpilogue;
        target.appendChild(parser.parseFromString(windowCode, 'text/html').body.firstChild);
        spinner.style.display = "none";
        document.getElementById("testcases").addEventListener("input", function() {
            console.log("input event fired");
        }, false);
    }
}

/**
 * 
 * 
 * @returns {undefined}
 */
function minimizeWindow(){
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
    var window = document.getElementById(event.path[3].id);
    var cellid = window.parentElement.getAttribute('id');
    if (cellid === null){
        return;
    }
    var target = document.getElementsByClassName("index-body")[0];
    target.appendChild(window);
    document.getElementById(cellid).remove();
}