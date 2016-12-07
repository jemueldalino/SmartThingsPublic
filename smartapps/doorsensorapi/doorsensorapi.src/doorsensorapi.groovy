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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers
// returns hub status
def hubLocalIp() {
    return [localip: switches[0].hub.localIP]
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

