include 'asynchttp_v1'

/**
 *  DoorSensorAPI
 *
 *  Copyright 2016 Jemuel Dalino
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "DoorSensorAPI",
    namespace: "DoorSensorAPI",
    author: "Jemuel Dalino",
    description: "Control smart doorSensorAPI",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section ("Allow external service to control these things...") {
    input "switches", "capability.contactSensor", multiple: true, required: true
  }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSensors"
    ]
  }
  
  path("/hublocalip"){
  	action: [
   	  GET: "hubLocalIp"
    ]
  }
  
  path("/open"){
  	action: [
   	  GET: "open"
    ]
  }
  
  path("/close"){
  	action: [
   	  GET: "close"
    ]
  }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// subscribe to attributes, devices, locations, etc.
    subscribe(switches, "contact.open", contactSensorOpenHandler)
    subscribe(switches, "contact.closed", contactSensorClosedHandler)
}

// implement event handlers
def contactSensorOpenHandler(evt) {
    log.debug "sensor open!"
    open()
}

def contactSensorClosedHandler(evt) {
    log.debug "sensor closed!"
    close()
}

// returns hub status
def hubLocalIp() {
    return [localip: switches[0].hub.localIP]
}

def open() {
	log.debug "sensor open!"
    def resp = []
    def appSettings = app.getAppSettings()
    log.debug appSettings
    
    def params = [
        uri: 'https://api.github.com',
        path: '/search/code',
        query: [q: "httpGet+repo:SmartThingsCommunity/SmartThingsPublic"]
    ]
    asynchttp_v1.get(processResponse, params)
}

def close() {
	log.debug "sensor closed!"
    
    def params = [
        uri: 'https://api.github.com',
        path: '/search/code',
        query: [q: "httpGet+repo:SmartThingsCommunity/SmartThingsPublic"]
    ]
    asynchttp_v1.get(processResponse, params)
}

// returns a list like
// [[name: "kitchen lamp", value: "open"], [name: "bathroom", value: "closed"]]
def listSensors() {
    def resp = []
    switches.each {
      resp << [name: it.displayName, value: it.currentValue("contact")]
    }
    return resp
}

def processResponse(response, data) {
    if (response.hasError()) {
        log.error "response has error: $response.errorMessage"
    } else {
        def results
        try {
            // json response already parsed into JSONElement object
            results = response.json
        } catch (e) {
            log.error "error parsing json from response: $e"
        }
        if (results) {
            def total = results?.total_count

            log.debug "there are $total occurences of httpGet in the SmartThingsPublic repo"

            // for each item found, log the name of the file
            results?.items.each { log.debug "httpGet usage found in file $it.name" }
        } else {
            log.debug "did not get json results from response body: $response.data"
        }
    }
}