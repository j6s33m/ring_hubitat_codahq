 /**
 *  Copyright 2020 Dominick Meglio
 *  Based on code and an idea from Markus Liljergren
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
metadata {
    definition(name: "JavaScript Injector", namespace: "markus-li", author: "Markus Liljergren") {
		capability "Actuator"
        command "refresh"
        command "clear"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }
    preferences {   
    }
}


void updated() {
    log.info "Updated..."
    refresh()
}

void refresh() {
  
    // The below is too many characters, need to use the minified version below
   String myScript = '''
<img style="display: none;" src='' onerror='
function loadJSON(path, callback, data) {
    var urlParams = new URLSearchParams(window.location.search);
    var xobj = new XMLHttpRequest();
        xobj.overrideMimeType("application/json");
    xobj.open("GET", window.location.href.split("?")[0] + path, true);
    xobj.withCredentials = true;
    xobj.setRequestHeader("Authorization","Bearer " + urlParams.get("access_token"));
    xobj.onreadystatechange = function () {
          if (xobj.readyState == 4 && xobj.status == "200") {
            callback(xobj.responseText, data);
          }
    };
    xobj.send(null);  
 }
loadJSON("/layout",function(response) {
      var data = JSON.parse(response);
	  for (i = 0; i < data.tiles.length; i++) {
		  var t = data.tiles[i]
		  if (t.template == "attribute" && t.templateExtra == "snapshot") {
			  loadJSON("/device/" + t.device, function(resp, id) { 
				var json = JSON.parse(resp)
				for (j =0; j <json[0].attr.length; j++) {
					var cameradata = json[0].attr[j]
					if (cameradata.snapshot != null) {
						document.querySelector("#tile-"+id + " .tile-primary").innerHTML = cameradata.snapshot;
					}
				}
			  },t.id);
		  }
		  if (t.template == "attribute" && t.templateExtra == "javascript") {
			  document.querySelector("#tile-"+t.id).style.display = "none";
		  }
	  }

    });
'>'''

    // Minified version of the above
    // https://javascript-minifier.com/
    myScript = '''
<img style="display: none;" src='' onerror='
function loadJSON(e,t,a){var n=new URLSearchParams(window.location.search),r=new XMLHttpRequest;r.overrideMimeType("application/json"),r.open("GET",window.location.href.split("?")[0]+e,!0),r.withCredentials=!0,r.setRequestHeader("Authorization","Bearer "+n.get("access_token")),r.onreadystatechange=function(){4==r.readyState&&"200"==r.status&&t(r.responseText,a)},r.send(null)}loadJSON("/layout",function(e){var t=JSON.parse(e);for(i=0;i<t.tiles.length;i++){var a=t.tiles[i];"attribute"==a.template&&"snapshot"==a.templateExtra&&loadJSON("/device/"+a.device,function(e,t){var a=JSON.parse(e);for(j=0;j<a[0].attr.length;j++){var n=a[0].attr[j];null!=n.snapshot&&(document.querySelector("#tile-"+t+" .tile-primary").innerHTML=n.snapshot)}},a.id),"attribute"==a.template&&"javascript"==a.templateExtra&&(document.querySelector("#tile-"+a.id).style.display="none")}});
'>'''

    // now() is to make the string unique each time, but is not needed in production...
    String myJSMsg = "${myScript}"
    
    sendEvent(name: "javascript", value: "${myJSMsg}", isStateChange: true)
    sendEvent(name: "javascriptLength", value: "${myJSMsg.length()}", isStateChange: true)
    
    log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, Maximum is 1024"
}

void clear() {
    sendEvent(name: "javascript", value: "", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}